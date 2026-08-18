<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="客户管理"
      subtitle="维护客户档案与进出口贸易往来信息"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增客户</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="客户名称 / 编码" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="customerCode" label="客户编码" width="110" />
        <el-table-column prop="customerName" label="客户名称" min-width="180">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.customerName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="170" show-overflow-tooltip />
        <el-table-column label="国家/地区" width="110">
          <template #default="{ row }"><el-tag effect="light" size="small">{{ row.country }}</el-tag></template>
        </el-table-column>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="客户详情" size="520px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="客户编码">{{ detail.customerCode }}</el-descriptions-item>
            <el-descriptions-item label="客户名称">{{ detail.customerName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactPerson || '—' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ detail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detail.email || '—' }}</el-descriptions-item>
            <el-descriptions-item label="国家/地区">{{ detail.country || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地址">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ fmtDateTime(detail.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.customerId ? '编辑客户' : '新增客户'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="客户编码"><el-input v-model="form.customerCode" placeholder="如 C0001" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="客户名称"><el-input v-model="form.customerName" /></el-form-item></el-col>
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
import { fmtDateTime } from '@/utils/format'
import { getCustomerPage, createCustomer, updateCustomer, deleteCustomer } from '@/api/master'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ customerId: '', customerCode: '', customerName: '', contactPerson: '', phone: '', email: '', country: '', address: '', remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getCustomerPage(query)
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
  Object.assign(form, row ? { ...row } : { customerId: '', customerCode: '', customerName: '', contactPerson: '', phone: '', email: '', country: '', address: '', remark: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.customerId) await updateCustomer({ ...form })
  else await createCustomer({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除客户「${row.customerName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteCustomer(row.customerId)
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
