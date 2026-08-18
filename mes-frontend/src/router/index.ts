import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore, SysMenu } from '@/stores/user'
import UnderConstruction from '@/views/error/under-construction.vue'

const layout = () => import('@/layouts/index.vue')

const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    name: 'Root',
    component: layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'equipment/:id',
        name: 'EquipmentDetail',
        component: () => import('@/views/equipment/detail.vue'),
        meta: { title: '设备详情', icon: 'Box', hideInMenu: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
})

// 视图组件动态加载
const modules = import.meta.glob('/src/views/**/*.vue')

function loadView(component?: string) {
  if (!component) return UnderConstruction
  const candidates = [
    `/src/views/${component}.vue`,
    `src/views/${component}.vue`,
    `../src/views/${component}.vue`,
  ]
  for (const key of candidates) {
    const loader = (modules as Record<string, unknown>)[key]
    if (loader) return loader as RouteRecordRaw['component']
  }
  console.warn('[router] 未匹配到视图组件:', component, '→ 使用兜底页')
  return UnderConstruction
}

// 从菜单树收集叶子菜单路由（DIR 仅作菜单分组，不生成路由）
function collectMenuRoutes(menus: SysMenu[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []
  const walk = (list: SysMenu[]) => {
    for (const menu of list) {
      if (menu.menuType === 'MENU' && menu.component) {
        routes.push({
          path: menu.path,
          name: menu.menuId,
          component: loadView(menu.component),
          meta: { title: menu.menuName, icon: menu.icon },
        })
      }
      if (menu.children?.length) {
        walk(menu.children)
      }
    }
  }
  walk(menus)
  return routes
}

// 全局守卫
router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    next()
    return
  }
  if (!userStore.token) {
    next('/login')
    return
  }
  // 首次进入，加载菜单并注册动态路由
  if (!userStore.menus.length) {
    try {
      const menus = await userStore.fetchMenus()
      const menuRoutes = collectMenuRoutes(menus)
      menuRoutes.forEach((r) => router.addRoute('Root', r))
      // 动态路由注册后按完整路径重新导航；
      // 不能直接 next({...to})，否则会把兜底 404 路由的 name/matched 一起带过去，导致永远命中 404。
      next({ path: to.path, query: to.query, hash: to.hash, replace: true })
      return
    } catch (e) {
      userStore.logout()
      next('/login')
      return
    }
  }
  next()
})

export { collectMenuRoutes }
export default router
