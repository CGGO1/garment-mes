import { defineStore } from 'pinia'
import { get, post } from '@/utils/request'

export interface UserInfo {
  userId: string
  username: string
  nickname: string
  roleCode?: string
}

export interface SysMenu {
  menuId: string
  parentId: string
  menuName: string
  menuType: string
  path: string
  component?: string
  icon?: string
  sort?: number
  children?: SysMenu[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('mes-token') || '',
    userInfo: null as UserInfo | null,
    menus: [] as SysMenu[],
  }),
  getters: {
    isLogin: (state) => !!state.token,
  },
  actions: {
    async login(username: string, password: string) {
      const data = await post<any>('/auth/login', { username, password })
      this.token = data.token
      localStorage.setItem('mes-token', data.token)
      return data
    },
    async fetchInfo() {
      this.userInfo = await get<UserInfo>('/auth/info')
      return this.userInfo
    },
    async fetchMenus() {
      const flat = await get<SysMenu[]>('/auth/menus')
      this.menus = buildMenuTree(flat)
      return this.menus
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.menus = []
      localStorage.removeItem('mes-token')
    },
  },
})

// 后端返回扁平菜单，这里构建成嵌套树（按 parentId 分组 + 排序）
function buildMenuTree(flat: SysMenu[]): SysMenu[] {
  const nodeMap = new Map<string, SysMenu>()
  flat.forEach((m) => nodeMap.set(m.menuId, { ...m, children: [] }))
  const roots: SysMenu[] = []
  flat.forEach((m) => {
    const node = nodeMap.get(m.menuId)!
    const parent = m.parentId && m.parentId !== '0' ? nodeMap.get(m.parentId) : undefined
    if (parent) {
      parent.children!.push(node)
    } else {
      roots.push(node)
    }
  })
  const sortRec = (list: SysMenu[]) => {
    list.sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
    list.forEach((n) => n.children?.length && sortRec(n.children))
  }
  sortRec(roots)
  return roots
}
