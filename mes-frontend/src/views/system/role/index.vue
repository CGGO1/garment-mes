<template>
  <div class="page-container">
    <PageHeader
      eyebrow="SYSTEM ADMIN"
      title="角色管理"
      subtitle="维护角色并分配菜单权限"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增角色</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="角色名称 / 编码" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAssign(row)">分配菜单</el-button>
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="load"
      />
    </div>

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="form.roleName" placeholder="如：生产主管" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="form.roleCode" placeholder="如：production" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单 -->
    <el-dialog v-model="assignVisible" :title="`分配菜单 - ${currentRole?.roleName || ''}`" width="420px">
      <el-tree
        ref="treeRef"
        :data="menuTree"
        show-checkbox
        node-key="menuId"
        :props="{ label: 'menuName', children: 'children' }"
        default-expand-all
      />
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElTree } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { fmtDateTime } from '@/utils/format'
import { getRolePage, createRole, updateRole, deleteRole, getMenuTree, getRoleMenus, saveRoleMenus } from '@/api/system'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const assignVisible = ref(false)
const menuTree = ref<any[]>([])
const treeRef = ref<InstanceType<typeof ElTree>>()
const currentRole = ref<any>(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ roleId: '', roleName: '', roleCode: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getRolePage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

function openDialog(row?: any) {
  Object.assign(form, {
    roleId: row?.roleId || '',
    roleName: row?.roleName || '',
    roleCode: row?.roleCode || '',
    remark: row?.remark || '',
  })
  dialogVisible.value = true
}

async function save() {
  if (form.roleId) {
    await updateRole({ ...form })
  } else {
    await createRole({ ...form })
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', { type: 'warning' })
  await deleteRole(row.roleId)
  ElMessage.success('删除成功')
  load()
}

async function openAssign(row: any) {
  currentRole.value = row
  menuTree.value = await getMenuTree()
  assignVisible.value = true
  await nextTick()
  const checked = await getRoleMenus(row.roleId)
  treeRef.value?.setCheckedKeys(checked || [])
}

async function saveAssign() {
  const checked = treeRef.value?.getCheckedKeys() as string[]
  await saveRoleMenus(currentRole.value.roleId, checked)
  ElMessage.success('菜单权限已保存')
  assignVisible.value = false
}

onMounted(load)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
