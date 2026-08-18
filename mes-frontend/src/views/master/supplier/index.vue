<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="供应商管理"
      subtitle="维护面料、辅料、包装等供应商档案"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增供应商</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="供应商名称 / 编码" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="supplierCode" label="供应商编码" width="120" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="180">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.supplierName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag effect="light" size="small">{{ row.supplierType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
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

    <el-drawer v-model="detailVisible" title="供应商详情" size="520px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="供应商编码">{{ detail.supplierCode }}</el-descriptions-item>
            <el-descriptions-item label="供应商名称">{{ detail.supplierName }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ detail.supplierType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactPerson || '—' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ detail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detail.email || '—' }}</el-descriptions-item>
            <el-descriptions-item label="国家/地区">{{ detail.country || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.supplierId ? '编辑供应商' : '新增供应商'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="供应商编码"><el-input v-model="form.supplierCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="供应商名称"><el-input v-model="form.supplierName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.supplierType" style="width: 100%"><el-option v-for="t in supplierTypes" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="联系人"><el-input v-model="form.contactPerson" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="国家/地区"><el-input v-model="form.country" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="地址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
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
import { getSupplierPage, createSupplier, updateSupplier, deleteSupplier } from '@/api/master'

const supplierTypes = ['面料', '辅料', '包装', '染料', '服务']

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ supplierId: '', supplierCode: '', supplierName: '', supplierType: '面料', contactPerson: '', phone: '', email: '', country: '中国', address: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getSupplierPage(query)
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
  Object.assign(form, row ? { ...row } : { supplierId: '', supplierCode: '', supplierName: '', supplierType: '面料', contactPerson: '', phone: '', email: '', country: '中国', address: '', remark: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.supplierId) await updateSupplier({ ...form })
  else await createSupplier({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除供应商「${row.supplierName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteSupplier(row.supplierId)
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
