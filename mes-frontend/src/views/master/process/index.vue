<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="工序管理"
      subtitle="维护裁剪、车缝、后整、包装等工序与工价"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增工序</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="工序名称 / 编码" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="seq" label="顺序" width="70" align="center" />
        <el-table-column prop="processCode" label="工序编码" width="120" />
        <el-table-column prop="processName" label="工序名称" min-width="170">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.processName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工价(元)" width="120" align="right">
          <template #default="{ row }"><span class="cell-qty">{{ fmtMoney(row.price) }}</span></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" background @current-change="load" @size-change="load" />
    </div>

    <el-drawer v-model="detailVisible" title="工序详情" size="460px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">工序信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="工序编码">{{ detail.processCode }}</el-descriptions-item>
            <el-descriptions-item label="工序名称">{{ detail.processName }}</el-descriptions-item>
            <el-descriptions-item label="顺序">{{ detail.seq }}</el-descriptions-item>
            <el-descriptions-item label="工价(元)">{{ fmtMoney(detail.price) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.processId ? '编辑工序' : '新增工序'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="工序编码"><el-input v-model="form.processCode" /></el-form-item>
        <el-form-item label="工序名称"><el-input v-model="form.processName" /></el-form-item>
        <el-form-item label="顺序"><el-input-number v-model="form.seq" :min="0" /></el-form-item>
        <el-form-item label="工价(元)"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { fmtMoney } from '@/utils/format'
import { getProcessPage, createProcess, updateProcess, deleteProcess } from '@/api/master'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive<any>({ processId: '', processCode: '', processName: '', seq: 0, price: 0, remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getProcessPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

function openDetail(row: any) {
  detail.value = row
  detailVisible.value = true
}

function openDialog(row?: any) {
  Object.assign(form, row ? { ...row } : { processId: '', processCode: '', processName: '', seq: 0, price: 0, remark: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.processId) await updateProcess({ ...form })
  else await createProcess({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除工序「${row.processName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteProcess(row.processId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
</style>
