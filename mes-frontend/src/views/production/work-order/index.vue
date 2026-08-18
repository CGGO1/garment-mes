<template>
  <div class="page-container">
    <PageHeader
      eyebrow="PRODUCTION EXECUTION"
      title="生产工单"
      subtitle="管理生产工单、工序进度、裁床与报工，实时掌握产能完成情况"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增工单</el-button>
      </template>
    </PageHeader>

    <el-row :gutter="16" class="kpi-row">
      <el-col v-for="k in kpis" :key="k.label" :xs="12" :sm="12" :md="6">
        <div class="kpi-card">
          <span class="kpi-card__label">{{ k.label }}</span>
          <span class="kpi-card__value mes-num">{{ k.value }}</span>
        </div>
      </el-col>
    </el-row>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="工单号" style="width: 200px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="load">
          <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="workOrderNo" label="工单号" min-width="150">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.workOrderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="产品" min-width="160" show-overflow-tooltip />
        <el-table-column label="计划 / 完成" width="140">
          <template #default="{ row }">
            <span class="cell-qty">{{ fmtNum(row.planQty) }} / {{ fmtNum(row.finishQty) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="完成进度" min-width="170">
          <template #default="{ row }">
            <div class="prog">
              <div class="prog__bar"><span :style="{ width: progressOf(row) + '%' }"></span></div>
              <span class="prog__num mes-num">{{ progressOf(row) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="105">
          <template #default="{ row }">
            <StatusTag :text="statusMap[row.status] || row.status" :tone="statusTone(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="开始日期" width="115">
          <template #default="{ row }">{{ fmtDate(row.startDate) }}</template>
        </el-table-column>
        <el-table-column label="完成日期" width="115">
          <template #default="{ row }">{{ fmtDate(row.endDate) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="success" @click="openReport(row)">报工</el-button>
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openQr(row)">二维码</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="load"
        @size-change="load"
      />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="工单详情" size="760px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="工单号">{{ detail.workOrderNo }}</el-descriptions-item>
            <el-descriptions-item label="产品">{{ detail.productName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="计划数量">
              <span class="cell-qty">{{ fmtNum(detail.planQty) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="完成数量">
              <span class="cell-qty">{{ fmtNum(detail.finishQty) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="完成进度">
              <div class="prog" style="max-width: 180px">
                <div class="prog__bar"><span :style="{ width: progressOf(detail) + '%' }"></span></div>
                <span class="prog__num mes-num">{{ progressOf(detail) }}%</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <StatusTag :text="statusMap[detail.status] || detail.status" :tone="statusTone(detail.status)" />
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ fmtDate(detail.startDate) }}</el-descriptions-item>
            <el-descriptions-item label="完成日期">{{ fmtDate(detail.endDate) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-block qr-block">
          <h4 class="detail-block__title">二维码</h4>
          <QrCode ref="detailQrRef" :content="detail.workOrderNo" :size="150" :label="detail.workOrderNo" :filename="`workorder-${detail.workOrderNo}.png`" />
          <el-button size="small" :icon="Download" @click="detailQrRef?.download()">下载二维码</el-button>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane :label="`工序明细 (${detail.items?.length || 0})`" name="items">
            <el-table :data="detail.items || []" border size="small">
              <el-table-column prop="processName" label="工序" min-width="130" />
              <el-table-column prop="seq" label="顺序" width="64" align="center" />
              <el-table-column label="计划数" width="100" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.planQty) }}</span></template>
              </el-table-column>
              <el-table-column label="完成数" width="100" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.finishQty) }}</span></template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <StatusTag :text="itemStatusMap[row.status] || row.status" :tone="itemStatusTone(row.status)" />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="`裁床记录 (${detail.cuttings?.length || 0})`" name="cuttings">
            <el-table :data="detail.cuttings || []" border size="small">
              <el-table-column prop="batchNo" label="批次" min-width="110" />
              <el-table-column prop="bundleNo" label="扎号" min-width="120" />
              <el-table-column label="用布量(m)" width="110" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.fabricUsed, 1) }}</span></template>
              </el-table-column>
              <el-table-column label="裁出数量" width="100" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.cutQty) }}</span></template>
              </el-table-column>
              <el-table-column prop="barcode" label="条码" min-width="140" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="`报工记录 (${detail.reports?.length || 0})`" name="reports">
            <el-table :data="detail.reports || []" border size="small">
              <el-table-column prop="workerName" label="工人" width="100" />
              <el-table-column prop="processId" label="工序ID" min-width="120" show-overflow-tooltip />
              <el-table-column label="报工数量" width="110" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.reportQty) }}</span></template>
              </el-table-column>
              <el-table-column label="报工时间" width="150">
                <template #default="{ row }">{{ fmtDateTime(row.reportTime) }}</template>
              </el-table-column>
              <el-table-column prop="barcode" label="条码" min-width="130" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <!-- 工单编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.workOrderId ? '编辑工单' : '新增工单'" width="820px" top="5vh">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="工单号"><el-input v-model="form.workOrderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="产品"><el-select v-model="form.productId" filterable style="width: 100%"><el-option v-for="p in products" :key="p.productId" :label="`${p.productCode} ${p.productName}`" :value="p.productId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.status" style="width: 100%"><el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划数量"><el-input-number v-model="form.planQty" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开始日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="完成日期"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">工序明细</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="工序" min-width="190">
            <template #default="{ row }">
              <el-select v-model="row.processId" filterable placeholder="选择工序" style="width: 100%">
                <el-option v-for="p in processes" :key="p.processId" :label="p.processName" :value="p.processId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="计划数" width="130"><template #default="{ row }"><el-input-number v-model="row.planQty" :min="0" style="width: 100%" /></template></el-table-column>
          <el-table-column label="顺序" width="100"><template #default="{ row }"><el-input-number v-model="row.seq" :min="0" style="width: 100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-btn" type="primary" plain :icon="Plus" @click="form.items.push({ processId: '', processName: '', planQty: 0, seq: form.items.length + 1 })">添加工序</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报工对话框 -->
    <el-dialog v-model="reportVisible" title="生产报工" width="500px">
      <el-form :model="reportForm" label-width="90px">
        <el-form-item label="工单号"><el-input :model-value="currentWorkOrder?.workOrderNo" disabled /></el-form-item>
        <el-form-item label="工序">
          <el-select v-model="reportForm.processId" filterable style="width: 100%">
            <el-option v-for="p in processes" :key="p.processId" :label="p.processName" :value="p.processId" />
          </el-select>
        </el-form-item>
        <el-form-item label="报工数量"><el-input-number v-model="reportForm.reportQty" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="工人"><el-input v-model="reportForm.workerName" /></el-form-item>
        <el-form-item label="条码"><ScanInput v-model="reportForm.barcode" placeholder="扫码或手动输入条码" @scan="onBarcodeScanned" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReport">提交报工</el-button>
      </template>
    </el-dialog>

    <QrCodeDialog
      v-model="qrVisible"
      title="工单二维码"
      :content="qrContent"
      :description="qrDescription"
      :filename="`workorder-${qrContent}.png`"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Download } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fmtDate, fmtDateTime, fmtNum } from '@/utils/format'
import { getWorkOrderPage, getWorkOrderDetail, createWorkOrder, updateWorkOrder, deleteWorkOrder, createReport } from '@/api/production'
import { getProductList, getProcessList } from '@/api/master'
import { getScanByBarcode } from '@/api/scan'
import ScanInput from '@/components/ScanInput.vue'
import QrCodeDialog from '@/components/QrCodeDialog.vue'
import QrCode from '@/components/QrCode.vue'

const statusMap: Record<string, string> = { PLANNED: '已计划', IN_PRODUCTION: '生产中', COMPLETED: '已完成' }
const itemStatusMap: Record<string, string> = { PENDING: '待开始', DOING: '进行中', DONE: '已完成' }
const itemStatusTone = (s: string) => ({ DONE: 'ok', DOING: 'thread', PENDING: 'info' } as any)[s] || 'info'
const statusTone = (s: string) => ({ COMPLETED: 'ok', IN_PRODUCTION: 'thread', PLANNED: 'info' } as any)[s] || 'info'

function progressOf(row: any) {
  const plan = Number(row.planQty) || 0
  const fin = Number(row.finishQty) || 0
  return plan ? Math.min(100, Math.round((fin / plan) * 100)) : 0
}

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const reportVisible = ref(false)
const detailVisible = ref(false)
const detailTab = ref('items')
const detail = ref<any>(null)
const detailQrRef = ref<InstanceType<typeof QrCode> | null>(null)
const qrVisible = ref(false)
const qrRow = ref<any>(null)
const products = ref<any[]>([])
const processes = ref<any[]>([])
const currentWorkOrder = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '' })
const form = reactive<any>({ workOrderId: '', workOrderNo: '', productId: '', planQty: 0, status: 'PLANNED', startDate: '', endDate: '', remark: '', items: [] })
const reportForm = reactive({ processId: '', reportQty: 1, workerName: '', barcode: '' })

const qrContent = computed(() => qrRow.value?.workOrderNo || '')
const qrDescription = computed(() => qrRow.value
  ? `工单号：${qrRow.value.workOrderNo}（${qrRow.value.productName || ''}）`
  : '')

const kpis = computed(() => {
  const planned = list.value.filter((r) => r.status === 'PLANNED').length
  const running = list.value.filter((r) => r.status === 'IN_PRODUCTION').length
  const done = list.value.filter((r) => r.status === 'COMPLETED').length
  return [
    { label: '工单总数', value: total.value },
    { label: '待执行', value: planned },
    { label: '生产中', value: running },
    { label: '已完成', value: done },
  ]
})

async function load() {
  loading.value = true
  try {
    const data: any = await getWorkOrderPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function openDialog(row?: any) {
  if (row) {
    const source = row.items && row.items.length ? row : await getWorkOrderDetail(row.workOrderId)
    Object.assign(form, { ...source, items: (source.items || []).map((i: any) => ({ ...i })) })
  } else {
    Object.assign(form, { workOrderId: '', workOrderNo: '', productId: '', planQty: 0, status: 'PLANNED', startDate: '', endDate: '', remark: '', items: [] })
  }
  dialogVisible.value = true
}

async function save() {
  form.items.forEach((i: any) => {
    const p = processes.value.find((x: any) => x.processId === i.processId)
    if (p) i.processName = p.processName
  })
  const isNew = !form.workOrderId
  if (isNew) await createWorkOrder({ ...form })
  else await updateWorkOrder({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  if (isNew) {
    qrRow.value = { ...form }
    qrVisible.value = true
  }
  load()
}

function openQr(row: any) {
  qrRow.value = row
  qrVisible.value = true
}

async function openDetail(row: any) {
  detail.value = await getWorkOrderDetail(row.workOrderId)
  detailVisible.value = true
  detailTab.value = 'items'
}

function openReport(row: any) {
  currentWorkOrder.value = row
  Object.assign(reportForm, { processId: '', reportQty: 1, workerName: '', barcode: '' })
  reportVisible.value = true
}

async function onBarcodeScanned(payload: { barcode: string } | null) {
  if (!payload?.barcode) return
  const list: any[] = await getScanByBarcode(payload.barcode)
  const hit = list.find((s) => s.workOrderId === currentWorkOrder.value?.workOrderId) || list[0]
  if (hit) {
    if (hit.processId) reportForm.processId = hit.processId
    if (hit.scanQty) reportForm.reportQty = Number(hit.scanQty)
  }
}

async function saveReport() {
  await createReport({ workOrderId: currentWorkOrder.value.workOrderId, ...reportForm })
  ElMessage.success('报工成功')
  reportVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除工单「${row.workOrderNo}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteWorkOrder(row.workOrderId)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  load()
  products.value = await getProductList()
  processes.value = await getProcessList()
})
</script>

<style scoped lang="scss">
.kpi-row { margin-bottom: 16px; }
.kpi-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: var(--mes-surface);
  border: 1px solid var(--mes-line);
  border-radius: var(--mes-r);
  padding: 16px 18px;
  box-shadow: var(--mes-shadow-xs);
}
.kpi-card__label { font-size: 12px; color: var(--mes-mist); }
.kpi-card__value { font-size: 26px; font-weight: 700; color: var(--mes-ink); }

.pagination { margin-top: 16px; justify-content: flex-end; }
.add-btn { margin-top: 12px; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }

.prog { display: flex; align-items: center; gap: 10px; }
.prog__bar {
  flex: 1; height: 7px; border-radius: 4px; background: var(--mes-line); overflow: hidden;
}
.prog__bar span {
  display: block; height: 100%; border-radius: 4px;
  background: linear-gradient(90deg, var(--mes-thread), var(--mes-thread-deep));
  transition: width var(--mes-dur) var(--mes-ease);
}
.prog__num { font-size: 12px; color: var(--mes-slate); min-width: 38px; text-align: right; }
.qr-block { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; }
</style>
