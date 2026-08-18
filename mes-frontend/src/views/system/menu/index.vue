<template>
  <div class="page-container">
    <PageHeader
      eyebrow="SYSTEM ADMIN"
      title="菜单管理"
      subtitle="维护系统菜单、路由与按钮级权限标识"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增菜单</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="tree" v-loading="loading" row-key="menuId" :tree-props="{ children: 'children' }">
        <el-table-column prop="menuName" label="菜单名称" min-width="160" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.menuType)" effect="light">{{ typeLabel(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="140" />
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="perms" label="权限标识" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.menuId ? '编辑菜单' : '新增菜单'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            check-strictly
            :render-after-expand="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio value="DIR">目录</el-radio>
            <el-radio value="MENU">菜单</el-radio>
            <el-radio value="BUTTON">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称">
          <el-input v-model="form.menuName" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model="form.path" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item v-if="form.menuType === 'MENU'" label="组件路径">
          <el-input v-model="form.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 User" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/system'

const loading = ref(false)
const tree = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive({
  menuId: '',
  parentId: '0',
  menuName: '',
  menuType: 'MENU',
  path: '',
  component: '',
  icon: '',
  sort: 0,
})

const parentOptions = computed(() => [{ menuId: '0', menuName: '根目录', children: tree.value }])

function typeLabel(type: string) {
  return { DIR: '目录', MENU: '菜单', BUTTON: '按钮' }[type] || type
}

function typeTag(type: string) {
  return { DIR: 'warning', MENU: 'primary', BUTTON: 'info' }[type] || 'info'
}

async function load() {
  loading.value = true
  try {
    tree.value = await getMenuTree()
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, {
    menuId: row?.menuId || '',
    parentId: row?.parentId || '0',
    menuName: row?.menuName || '',
    menuType: row?.menuType || 'MENU',
    path: row?.path || '',
    component: row?.component || '',
    icon: row?.icon || '',
    sort: row?.sort || 0,
  })
  dialogVisible.value = true
}

async function save() {
  if (form.menuId) {
    await updateMenu({ ...form })
  } else {
    await createMenu({ ...form })
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' })
  await deleteMenu(row.menuId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
