<template>
  <!-- 目录：仅作分组，不可点击导航；无子项时折叠隐藏 -->
  <el-sub-menu v-if="isDir && hasChildren" :index="menu.menuId">
    <template #title>
      <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
      <span>{{ menu.menuName }}</span>
    </template>
    <sidebar-item v-for="child in menu.children!" :key="child.menuId" :menu="child" />
  </el-sub-menu>

  <!-- 叶子菜单：可点击导航（仅 MENU 类型，BUTTON 权限不渲染） -->
  <el-menu-item v-else-if="menu.menuType === 'MENU'" :index="menu.path">
    <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
    <template #title>{{ menu.menuName }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import type { SysMenu } from '@/stores/user'

const props = defineProps<{ menu: SysMenu }>()

const isDir = props.menu.menuType === 'DIR'
const hasChildren = !!props.menu.children && props.menu.children.length > 0
</script>

<style scoped lang="scss">
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px;
  line-height: 46px;
  border-radius: 10px;
  margin: 3px 8px;
  position: relative;
  color: var(--mes-slate);
  transition: background var(--mes-dur) var(--mes-ease), color var(--mes-dur) var(--mes-ease);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background: var(--mes-surface-2);
  color: var(--mes-ink);
}

:deep(.el-menu-item.is-active) {
  background: var(--mes-thread-soft);
  color: var(--mes-thread-deep);
  font-weight: 600;
}

/* 织线签名：激活项左侧细竖线 */
:deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 2px;
  background: var(--mes-thread);
}

:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--mes-ink);
  font-weight: 600;
}
</style>
