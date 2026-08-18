<template>
  <div class="layout">
    <!-- 侧栏 -->
    <aside class="sidebar" :class="{ collapsed }">
      <div class="brand">
        <img class="brand__mark" src="/favicon.png" alt="Garment MES" />
        <span v-if="!collapsed" class="brand__name">Garment MES · 服装智造</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <sidebar-item v-for="menu in userStore.menus" :key="menu.menuId" :menu="menu" />
        </el-menu>
      </el-scrollbar>
      <div v-if="!collapsed" class="sidebar-foot">
        <span class="status-chip thread"><span class="status-chip__swatch"></span>生产环境 · 在线</span>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main">
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Expand v-if="collapsed" />
            <Fold v-else />
          </el-icon>
          <div class="crumb">
            <span class="crumb__eyebrow">GARMENT MES</span>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/dashboard' }">工作台</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
        <div class="header-right">
          <el-button class="ai-btn" :icon="MagicStick" @click="openAiReport">AI 即时报表</el-button>
          <el-tooltip :content="isDark ? '切换亮色模式' : '切换暗色模式'" placement="bottom">
            <el-icon class="action-icon" @click="toggleDark">
              <Sunny v-if="isDark" />
              <Moon v-else />
            </el-icon>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user">
              <el-avatar :size="32" class="user__avatar">{{ avatarText }}</el-avatar>
              <span class="user__name">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon class="user__arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <ai-report ref="aiReportRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import SidebarItem from './components/SidebarItem.vue'
import AiReport from '@/components/AiReport.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const collapsed = ref(false)
const isDark = ref(localStorage.getItem('mes-dark') === '1')
const aiReportRef = ref<InstanceType<typeof AiReport>>()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title as string)
const avatarText = computed(() => (userStore.userInfo?.nickname || 'A').charAt(0))

function openAiReport() {
  aiReportRef.value?.open()
}

function toggleDark() {
  isDark.value = !isDark.value
  localStorage.setItem('mes-dark', isDark.value ? '1' : '0')
  document.documentElement.classList.toggle('dark', isDark.value)
}

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

onMounted(() => {
  document.documentElement.classList.toggle('dark', isDark.value)
  if (!userStore.userInfo) {
    userStore.fetchInfo()
  }
})
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧栏 ===== */
.sidebar {
  width: var(--mes-sidebar-w);
  background: var(--mes-surface);
  border-right: 1px solid var(--mes-line);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width var(--mes-dur) var(--mes-ease);
}
.sidebar.collapsed { width: 68px; }

.brand {
  height: var(--mes-header-h);
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 0 18px;
  border-bottom: 1px solid var(--mes-line);
  overflow: hidden;
  white-space: nowrap;
}
.brand__mark {
  width: 36px; height: 36px;
  object-fit: contain;
  flex-shrink: 0;
  position: relative;
}
.brand__name {
  font-family: var(--mes-font-display);
  font-size: 16px; font-weight: 600; color: var(--mes-ink);
}

.menu-scroll { flex: 1; }
.sidebar-menu { border-right: none; background: transparent; padding: 10px 6px; }

.sidebar-foot {
  padding: 14px 20px;
  border-top: 1px solid var(--mes-line);
}

/* ===== 主区域 ===== */
.main { flex: 1; display: flex; flex-direction: column; min-width: 0; }

.header {
  height: var(--mes-header-h);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 22px;
  background: var(--mes-surface);
  border-bottom: 1px solid var(--mes-line);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  font-size: 19px; cursor: pointer; color: var(--mes-slate);
  transition: color var(--mes-dur) var(--mes-ease);
}
.collapse-btn:hover { color: var(--mes-thread-deep); }

.crumb { display: flex; flex-direction: column; gap: 1px; }
.crumb__eyebrow {
  font-size: 10px; letter-spacing: 0.18em; font-weight: 600;
  color: var(--mes-mist); font-family: var(--mes-font-data);
}

.header-right { display: flex; align-items: center; gap: 18px; }

.ai-btn {
  background: var(--mes-thread); border-color: var(--mes-thread);
  color: #fff; font-weight: 600; border-radius: 10px;
  transition: background var(--mes-dur) var(--mes-ease), box-shadow var(--mes-dur) var(--mes-ease);
}
.ai-btn:hover {
  background: var(--mes-thread-deep); border-color: var(--mes-thread-deep);
  box-shadow: 0 6px 16px rgba(77, 156, 255, 0.35);
}

.action-icon {
  font-size: 19px; cursor: pointer; color: var(--mes-slate);
  transition: color var(--mes-dur) var(--mes-ease);
}
.action-icon:hover { color: var(--mes-thread-deep); }

.user { display: flex; align-items: center; gap: 9px; cursor: pointer; }
.user__avatar { background: var(--mes-ink); color: #fff; font-weight: 600; }
.user__name { font-size: 14px; color: var(--mes-ink); }
.user__arrow { font-size: 12px; color: var(--mes-mist); }

.content { flex: 1; overflow-y: auto; background: var(--mes-porcelain); }
</style>
