<template>
  <div class="page-container">
    <PageHeader
      eyebrow="EXPORT · IMPORT TRADE"
      title="进出口订单"
      subtitle="管理出口/进口订单、明细、单证与出货计划，全程跟踪状态流转"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增订单</el-button>
      </template>
    </PageHeader>

    <!-- 概览 -->
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
        <el-input v-model="query.keyword" placeholder="订单号" style="width: 200px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-select v-model="query.orderType" placeholder="订单类型" clearable style="width: 130px" @change="load">
          <el-option label="出口" value="EXPORT" />
          <el-option label="进口" value="IMPORT" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px" @change="load">
          <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" min-width="150">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.orderNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="78">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 'EXPORT' ? 'primary' : 'warning'" effect="light" round>
              {{ row.orderType === 'EXPORT' ? '出口' : '进口' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户" min-width="150" show-overflow-tooltip />
        <el-table-column label="贸易术语" width="86">
          <template #default="{ row }"><span class="mes-num">{{ row.incoterm || '—' }}</span></template>
        </el-table-column>
        <el-table-column label="金额" min-width="130" align="right">
          <template #default="{ row }">
            <span class="cell-qty">{{ fmtMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="交期" width="112">
          <template #default="{ row }">{{ fmtDate(row.deliveryDate) }}</template>
        </el-table-column>
        <el-table-column label="明细" width="74" align="center">
          <template #default="{ row }"><span class="mes-num">{{ row.itemsCount ?? '—' }} 行</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :text="statusMap[row.status] || row.status" :tone="statusTone(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="232" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-dropdown v-if="nextStatus(row.status).length" trigger="click" @command="(s: string) => changeStatus(row, s)">
                <el-button link type="warning">流转 <el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="s in nextStatus(row.status)" :key="s" :command="s">{{ statusMap[s] }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
    <el-drawer v-model="detailVisible" title="订单详情" size="720px">
      <template v-if="detail">
        <el-steps :active="stepIndex" align-center class="detail-steps" finish-status="success">
          <el-step v-for="label in flowSteps" :key="label" :title="label" />
        </el-steps>

        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单类型">{{ detail.orderType === 'EXPORT' ? '出口' : '进口' }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ detail.customerName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="贸易术语">{{ detail.incoterm || '—' }}</el-descriptions-item>
            <el-descriptions-item label="币种">{{ detail.currency || '—' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ statusMap[detail.status] || detail.status }}</el-descriptions-item>
            <el-descriptions-item label="下单日期">{{ fmtDate(detail.orderDate) }}</el-descriptions-item>
            <el-descriptions-item label="交期">{{ fmtDate(detail.deliveryDate) }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="cell-qty">{{ fmtMoney(detail.totalAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-block">
          <h4 class="detail-block__title">订单明细（{{ detail.items?.length || 0 }}）</h4>
          <el-table :data="detail.items || []" border size="small">
            <el-table-column label="产品" min-width="180">
              <template #default="{ row }">{{ row.productName || row.productId }}</template>
            </el-table-column>
            <el-table-column prop="size" label="尺码" width="70" />
            <el-table-column label="数量" width="100" align="right">
              <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.qty) }}</span></template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="right">
              <template #default="{ row }"><span class="mes-num">{{ fmtMoney(row.price) }}</span></template>
            </el-table-column>
            <el-table-column label="金额" width="130" align="right">
              <template #default="{ row }"><span class="mes-num">{{ fmtMoney(row.amount) }}</span></template>
            </el-table-column>
          </el-table>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane :label="`单证 (${detailDocs.length})`" name="docs">
            <div class="tab-toolbar">
              <el-button size="small" type="primary" plain :icon="Plus" @click="openDocDialog">新增单证</el-button>
            </div>
            <el-table :data="detailDocs" border size="small" v-loading="docLoading">
              <el-table-column prop="docNo" label="单证号" min-width="180" />
              <el-table-column label="类型" width="120">
                <template #default="{ row }">
                  <el-tag size="small" effect="light">{{ docTypeMap[row.docType] || row.docType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="removeDoc(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="`出货计划 (${detailShips.length})`" name="ships">
            <div class="tab-toolbar">
              <el-button size="small" type="primary" plain :icon="Plus" @click="openShipDialog">新增出货计划</el-button>
            </div>
            <el-table :data="detailShips" border size="small" v-loading="shipLoading">
              <el-table-column prop="portFrom" label="起运港" min-width="110" />
              <el-table-column prop="portTo" label="目的港" min-width="130" />
              <el-table-column label="预计开船 ETD" width="140">
                <template #default="{ row }">{{ fmtDateTime(row.etd) }}</template>
              </el-table-column>
              <el-table-column label="预计到港 ETA" width="140">
                <template #default="{ row }">{{ fmtDateTime(row.eta) }}</template>
              </el-table-column>
              <el-table-column prop="container" label="柜型" width="90" />
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="removeShip(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <!-- 订单编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.orderId ? '编辑订单' : '新增订单'" width="920px" top="5vh">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="订单号"><el-input v-model="form.orderNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="订单类型"><el-radio-group v-model="form.orderType"><el-radio-button value="EXPORT">出口</el-radio-button><el-radio-button value="IMPORT">进口</el-radio-button></el-radio-group></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-select v-model="form.status" style="width: 100%"><el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="客户"><el-select v-model="form.customerId" filterable style="width: 100%"><el-option v-for="c in customers" :key="c.customerId" :label="c.customerName" :value="c.customerId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="贸易术语"><el-select v-model="form.incoterm" style="width: 100%"><el-option v-for="i in incoterms" :key="i" :label="i" :value="i" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="币种"><el-select v-model="form.currency" style="width: 100%"><el-option label="USD" value="USD" /><el-option label="CNY" value="CNY" /><el-option label="EUR" value="EUR" /><el-option label="GBP" value="GBP" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="下单日期"><el-date-picker v-model="form.orderDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="交期"><el-date-picker v-model="form.deliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">订单明细</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="产品" min-width="210">
            <template #default="{ row }">
              <el-select v-model="row.productId" filterable placeholder="选择产品" style="width: 100%">
                <el-option v-for="p in products" :key="p.productId" :label="`${p.productCode} ${p.productName}`" :value="p.productId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="尺码" width="90"><template #default="{ row }"><el-input v-model="row.size" /></template></el-table-column>
          <el-table-column label="数量" width="130"><template #default="{ row }"><el-input-number v-model="row.qty" :min="0" style="width: 100%" @change="calcAmount(row)" /></template></el-table-column>
          <el-table-column label="单价" width="140"><template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" style="width: 100%" @change="calcAmount(row)" /></template></el-table-column>
          <el-table-column label="金额" width="120" align="right"><template #default="{ row }"><span class="mes-num">{{ fmtMoney(row.amount) }}</span></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-item-btn" type="primary" plain :icon="Plus" @click="addItem">添加明细</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 单证新增 -->
    <el-dialog v-model="docDialogVisible" title="新增单证" width="440px">
      <el-form :model="docForm" label-width="90px">
        <el-form-item label="单证类型">
          <el-select v-model="docForm.docType" style="width: 100%">
            <el-option v-for="(label, key) in docTypeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="单证号"><el-input v-model="docForm.docNo" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="docForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="docDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDoc">保存</el-button>
      </template>
    </el-dialog>

    <!-- 出货计划新增 -->
    <el-dialog v-model="shipDialogVisible" title="新增出货计划" width="560px">
      <el-form :model="shipForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="起运港"><el-input v-model="shipForm.portFrom" placeholder="如 Shanghai" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="目的港"><el-input v-model="shipForm.portTo" placeholder="如 Los Angeles" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预计开船 ETD"><el-date-picker v-model="shipForm.etd" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预计到港 ETA"><el-date-picker v-model="shipForm.eta" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="柜型"><el-select v-model="shipForm.container" style="width: 100%"><el-option label="整柜 20 尺" value="FCL20" /><el-option label="整柜 40 尺" value="FCL40" /><el-option label="拼箱" value="LCL" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="shipForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveShip">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fmtDate, fmtDateTime, fmtMoney, fmtNum } from '@/utils/format'
import {
  getOrderPage, getOrderDetail, createOrder, updateOrder, deleteOrder, updateOrderStatus,
  getDocuments, createDocument, deleteDocument, getShipments, createShipment, deleteShipment,
} from '@/api/trade'
import { getCustomerList, getProductList } from '@/api/master'

const statusMap: Record<string, string> = {
  DRAFT: '草稿', PENDING: '待生产', IN_PRODUCTION: '生产中', QC: '质检中', SHIPPED: '已出货', COMPLETED: '已完成',
}
const flowMap: Record<string, string[]> = {
  DRAFT: ['PENDING'], PENDING: ['IN_PRODUCTION'], IN_PRODUCTION: ['QC'], QC: ['SHIPPED'], SHIPPED: ['COMPLETED'],
}
const flowSteps = ['草稿', '待生产', '生产中', '质检中', '已出货', '已完成']
const incoterms = ['EXW', 'FCA', 'FOB', 'CFR', 'CIF', 'DAP', 'DDP']
const docTypeMap: Record<string, string> = {
  INVOICE: '商业发票', PACKING: '装箱单', BL: '提单', CO: '原产地证', LC: '信用证', CI: '形式发票', CC: '保险单',
}

function statusTone(s: string) {
  return ({ COMPLETED: 'ok', SHIPPED: 'ok', QC: 'warn', FAIL: 'danger', PENDING: 'info', DRAFT: 'info' } as any)[s] || 'info'
}
function nextStatus(s: string) {
  return flowMap[s] || []
}

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailTab = ref('docs')
const detail = ref<any>(null)
const detailDocs = ref<any[]>([])
const detailShips = ref<any[]>([])
const docLoading = ref(false)
const shipLoading = ref(false)
const docDialogVisible = ref(false)
const shipDialogVisible = ref(false)
const customers = ref<any[]>([])
const products = ref<any[]>([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: '', orderType: '' })
const form = reactive<any>({ orderId: '', orderNo: '', orderType: 'EXPORT', customerId: '', incoterm: 'FOB', currency: 'USD', orderDate: '', deliveryDate: '', status: 'PENDING', remark: '', items: [] })
const docForm = reactive<any>({ docType: 'INVOICE', docNo: '', remark: '' })
const shipForm = reactive<any>({ portFrom: 'Shanghai', portTo: '', etd: '', eta: '', container: 'FCL40', remark: '' })

const kpis = computed(() => {
  const pending = list.value.filter((r) => r.status === 'PENDING' || r.status === 'DRAFT').length
  const inProd = list.value.filter((r) => r.status === 'IN_PRODUCTION' || r.status === 'QC').length
  const done = list.value.filter((r) => r.status === 'SHIPPED' || r.status === 'COMPLETED').length
  return [
    { label: '订单总数', value: total.value },
    { label: '待处理', value: pending },
    { label: '执行中', value: inProd },
    { label: '已完成', value: done },
  ]
})
const stepIndex = computed(() => {
  if (!detail.value) return 0
  const idx = flowSteps.indexOf(statusMap[detail.value.status])
  return idx < 0 ? 0 : idx
})

async function load() {
  loading.value = true
  try {
    const data: any = await getOrderPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

function addItem() {
  form.items.push({ productId: '', size: 'M', qty: 1, price: 0, amount: 0 })
}
function calcAmount(row: any) {
  row.amount = (Number(row.qty) || 0) * (Number(row.price) || 0)
}

async function openDialog(row?: any) {
  if (row) {
    const source = row.items && row.items.length ? row : await getOrderDetail(row.orderId)
    Object.assign(form, { ...source, items: (source.items || []).map((i: any) => ({ ...i })) })
  } else {
    Object.assign(form, { orderId: '', orderNo: '', orderType: 'EXPORT', customerId: '', incoterm: 'FOB', currency: 'USD', orderDate: '', deliveryDate: '', status: 'PENDING', remark: '', items: [] })
    addItem()
  }
  dialogVisible.value = true
}

async function save() {
  form.totalAmount = form.items.reduce((sum: number, i: any) => sum + (Number(i.amount) || 0), 0)
  if (form.orderId) await updateOrder({ ...form })
  else await createOrder({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function changeStatus(row: any, status: string) {
  await updateOrderStatus(row.orderId, status)
  ElMessage.success(`已流转为「${statusMap[status]}」`)
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除订单「${row.orderNo}」？删除后将同时移除其明细。`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteOrder(row.orderId)
  ElMessage.success('删除成功')
  load()
}

async function openDetail(row: any) {
  const source = await getOrderDetail(row.orderId)
  detail.value = source
  detailVisible.value = true
  detailTab.value = 'docs'
  loadDocs(source.orderId)
  loadShips(source.orderId)
}

async function loadDocs(orderId: string) {
  docLoading.value = true
  try {
    detailDocs.value = await getDocuments(orderId)
    if (detail.value) detail.value.docCount = detailDocs.value.length
  } finally {
    docLoading.value = false
  }
}

async function loadShips(orderId: string) {
  shipLoading.value = true
  try {
    detailShips.value = await getShipments(orderId)
    if (detail.value) detail.value.shipCount = detailShips.value.length
  } finally {
    shipLoading.value = false
  }
}

function openDocDialog() {
  Object.assign(docForm, { docType: 'INVOICE', docNo: '', remark: '' })
  docDialogVisible.value = true
}

async function saveDoc() {
  await createDocument({ orderId: detail.value.orderId, ...docForm })
  ElMessage.success('单证已登记')
  docDialogVisible.value = false
  loadDocs(detail.value.orderId)
}

async function removeDoc(row: any) {
  await deleteDocument(row.docId)
  ElMessage.success('单证已删除')
  loadDocs(detail.value.orderId)
}

function openShipDialog() {
  Object.assign(shipForm, { portFrom: 'Shanghai', portTo: '', etd: '', eta: '', container: 'FCL40', remark: '' })
  shipDialogVisible.value = true
}

async function saveShip() {
  await createShipment({ orderId: detail.value.orderId, ...shipForm })
  ElMessage.success('出货计划已保存')
  shipDialogVisible.value = false
  loadShips(detail.value.orderId)
}

async function removeShip(row: any) {
  await deleteShipment(row.shipmentId)
  ElMessage.success('出货计划已删除')
  loadShips(detail.value.orderId)
}

onMounted(async () => {
  load()
  customers.value = await getCustomerList()
  products.value = await getProductList()
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
.add-item-btn { margin-top: 12px; }
.link-like {
  color: var(--mes-thread-deep);
  font-weight: 600;
  cursor: pointer;
}
.link-like:hover { text-decoration: underline; }
.detail-steps { margin: 4px 0 24px; }
.tab-toolbar { display: flex; justify-content: flex-end; margin-bottom: 12px; }
</style>
