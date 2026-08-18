<template>
  <div class="page-container">
    <PageHeader
      eyebrow="PRODUCTION EXECUTION"
      title="扫码登记"
      subtitle="生产事件扫码上报（裁床 / 报工 / 收货 / 转移 / 出货）"
    />

    <div class="mes-card scan-card">
      <el-form :model="form" label-width="90px" size="large">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="扫码类型">
              <el-select v-model="form.scanType" style="width: 100%">
                <el-option v-for="(label, key) in typeMap" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="关联工单">
              <el-select v-model="form.workOrderId" filterable clearable style="width: 100%">
                <el-option v-for="w in workOrders" :key="w.workOrderId" :label="`${w.workOrderNo} (${w.productName || ''})`" :value="w.workOrderId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扫码数量">
              <el-input-number v-model="form.scanQty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="条码">
          <ScanInput v-model="form.barcode" placeholder="将条码对准摄像头 / 直接输入后回车" @scan="onScanned" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Promotion" :loading="submitting" @click="submit">提交登记</el-button>
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="mes-card">
      <div class="recent-head">
        <h3 class="title-sm">最近扫码记录</h3>
        <el-button :icon="Refresh" link @click="loadRecent">刷新</el-button>
      </div>
      <el-table :data="recent" v-loading="loading" stripe size="small">
        <el-table-column prop="barcode" label="条码" min-width="180" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.scanType)" effect="light">{{ typeMap[row.scanType] || row.scanType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workOrderId" label="工单ID" width="160" />
        <el-table-column prop="scanQty" label="数量" width="90" />
        <el-table-column label="扫码时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.scanTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion, RefreshLeft, Refresh } from '@element-plus/icons-vue'
import ScanInput from '@/components/ScanInput.vue'
import PageHeader from '@/components/PageHeader.vue'
import { fmtDateTime } from '@/utils/format'
import { getScanRecent, receiveScan } from '@/api/scan'
import { getWorkOrderPage } from '@/api/production'

const typeMap: Record<string, string> = {
  CUTTING: '裁床扫码', REPORT: '工序报工', RECEIVE: '收货', TRANSFER: '工序转移', OQC: '出货检验',
}
const typeTag = (k: string) => ({
  CUTTING: 'primary', REPORT: 'success', RECEIVE: 'info', TRANSFER: 'warning', OQC: 'success',
}[k] || 'info') as any

const submitting = ref(false)
const loading = ref(false)
const workOrders = ref<any[]>([])
const recent = ref<any[]>([])

const form = reactive({
  scanType: 'REPORT',
  barcode: '',
  workOrderId: '',
  scanQty: 1,
  remark: '',
})

function onScanned(payload: { barcode: string } | null) {
  if (payload?.barcode) form.barcode = payload.barcode
  if (form.barcode) submit()
}

async function submit() {
  if (!form.barcode) { ElMessage.warning('请先扫码或输入条码'); return }
  submitting.value = true
  try {
    await receiveScan({
      barcode: form.barcode,
      scanType: form.scanType,
      workOrderId: form.workOrderId || undefined,
      scanQty: form.scanQty,
      remark: form.remark || '生产扫码登记',
    })
    ElMessage.success(`登记成功：${form.barcode}`)
    if (navigator.vibrate) navigator.vibrate(80)
    form.barcode = ''
    await loadRecent()
  } finally {
    submitting.value = false
  }
}

function reset() {
  form.barcode = ''
  form.remark = ''
  form.scanQty = 1
  form.workOrderId = ''
}

async function loadRecent() {
  loading.value = true
  try {
    recent.value = await getScanRecent()
  } finally {
    loading.value = false
  }
}

async function loadWorkOrders() {
  const data: any = await getWorkOrderPage({ pageNum: 1, pageSize: 100 })
  workOrders.value = data.records || []
}

onMounted(async () => {
  await Promise.all([loadRecent(), loadWorkOrders()])
})
</script>

<style scoped>
.scan-card { margin-bottom: 16px; }
.recent-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.title-sm { margin: 0; font-size: 15px; font-weight: 600; color: var(--mes-ink); }
</style>
