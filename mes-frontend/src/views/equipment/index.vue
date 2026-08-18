<template>
  <div class="page-container">
    <PageHeader
      eyebrow="EQUIPMENT MANAGEMENT"
      title="设备台账"
      subtitle="管理设备档案、维护工单与扫码登记"
    >
      <template #actions>
        <el-button :icon="Camera" @click="scanDialog = true">扫码登记</el-button>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增设备</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <el-row :gutter="12" class="kpi-row">
        <el-col :span="5"><div class="kpi"><span class="kpi__label">设备总数</span><span class="kpi__value">{{ summary.total || 0 }}</span></div></el-col>
        <el-col :span="5"><div class="kpi"><span class="kpi__label">正常</span><span class="kpi__value ok">{{ summary.normal || 0 }}</span></div></el-col>
        <el-col :span="5"><div class="kpi"><span class="kpi__label">维修中</span><span class="kpi__value warn">{{ summary.repair || 0 }}</span></div></el-col>
        <el-col :span="5"><div class="kpi"><span class="kpi__label">已报废</span><span class="kpi__value danger">{{ summary.scrap || 0 }}</span></div></el-col>
        <el-col :span="4"><div class="kpi"><span class="kpi__label">待维护</span><span class="kpi__value">{{ summary.pendingMaintenance || 0 }}</span></div></el-col>
      </el-row>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="编码 / 名称 / 型号" style="width: 220px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-select v-model="query.equipmentType" placeholder="类型" clearable style="width: 130px" @change="load">
          <el-option v-for="t in eqTypeOptions" :key="t" :label="t" :value="t" />
        </el-select>
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px" @change="load">
          <el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe @row-click="(r) => $router.push(`/equipment/${r.equipmentId}`)">
        <el-table-column prop="equipmentCode" label="编码" width="110" />
        <el-table-column prop="equipmentName" label="名称" min-width="150" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag effect="light">{{ row.equipmentType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="model" label="型号" width="110" />
        <el-table-column prop="manufacturer" label="厂商" width="130" />
        <el-table-column prop="location" label="位置" width="110" />
        <el-table-column prop="managerName" label="负责人" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :text="statusMap[row.status] || row.status" :tone="statusTone(row.status)" />
          </template>
        </el-table-column>
        <el-table-column label="库存" width="90">
          <template #default="{ row }">
            <el-tag :type="row.stockStatus === 'OUT_STOCK' ? 'warning' : 'success'" effect="light">
              {{ stockStatusMap[row.stockStatus] || '在库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最近扫码" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.lastScanTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openQr(row)">二维码</el-button>
            <el-button link type="primary" @click.stop="openDialog(row)"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-dropdown trigger="click" @command="(s: string) => changeStatus(row, s)" @click.stop>
              <el-button link type="warning"><el-icon><Tools /></el-icon>状态<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="s in nextStatus(row.status)" :key="s" :command="s">{{ statusMap[s] }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="danger" @click.stop="remove(row)"><el-icon><Delete /></el-icon>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="load" />
    </div>

    <div class="mes-card recent-card">
      <div class="recent-head">
        <h3 class="title-sm">最近扫码记录（入库 / 出库 / 盘点）</h3>
        <el-button :icon="Refresh" link @click="loadRecentScans">刷新</el-button>
      </div>
      <el-table :data="recentScans" size="small" stripe v-loading="recentLoading">
        <el-table-column label="设备" min-width="200">
          <template #default="{ row }">
            <span class="link-like" @click="$router.push(`/equipment/${row.equipmentId}`)">{{ row.equipmentCode }} {{ row.equipmentName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="事件" width="120">
          <template #default="{ row }">
            <el-tag effect="light" :type="scanTypeTag(row.scanType)">{{ eqScanTypeMap[row.scanType] || row.scanType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="扫码时间" width="180">
          <template #default="{ row }">{{ fmtDateTime(row.scanTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      </el-table>
    </div>

    <!-- 设备编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.equipmentId ? '编辑设备' : '新增设备'" width="720px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="编码"><el-input v-model="form.equipmentCode" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称"><el-input v-model="form.equipmentName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.equipmentType" style="width: 100%"><el-option v-for="t in eqTypeOptions" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号"><el-input v-model="form.model" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="厂商"><el-input v-model="form.manufacturer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购置日期"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="购置金额"><el-input-number v-model="form.purchaseAmount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="位置"><el-input v-model="form.location" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="form.status" style="width: 100%"><el-option v-for="(label, key) in statusMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 扫码登记对话框 -->
    <el-dialog v-model="scanDialog" title="设备扫码登记" width="520px" destroy-on-close>
      <el-form :model="scanForm" label-width="100px">
        <el-form-item label="设备条码">
          <ScanInput v-model="scanForm.equipmentCode" placeholder="扫设备上的条码或输入编码" @scan="onCodeScanned" />
        </el-form-item>
        <el-form-item label="识别设备">
          <el-tag v-if="scannedEquipment" effect="light">{{ scannedEquipment.equipmentCode }} {{ scannedEquipment.equipmentName }}</el-tag>
          <span v-else class="hint">未识别，请先扫到设备编码</span>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="scanForm.scanType" style="width: 100%">
            <el-option v-for="(label, key) in eqScanTypeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="scanForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scanDialog = false">关闭</el-button>
        <el-button type="primary" :disabled="!scannedEquipment" @click="submitScan">提交</el-button>
      </template>
    </el-dialog>

    <QrCodeDialog
      v-model="qrVisible"
      title="设备二维码"
      :content="qrContent"
      :description="qrDescription"
      :filename="`equipment-${qrContent}.png`"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Camera, Edit, Delete, Tools, ArrowDown, Refresh } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import QrCodeDialog from '@/components/QrCodeDialog.vue'
import { fmtDateTime } from '@/utils/format'
import {
  getEquipmentPage, getEquipmentDetail, createEquipment, updateEquipment, deleteEquipment,
  updateEquipmentStatus, getEquipmentByCode, scanEquipment, getEquipmentSummary, getRecentEquipmentScan,
} from '@/api/equipment'
import ScanInput from '@/components/ScanInput.vue'

const router = useRouter()
const route = useRoute()
const eqTypeOptions = ['裁剪机', '缝纫机', '绣花机', '包装线', '整烫台', '检验台']
const statusMap: Record<string, string> = { NORMAL: '正常', REPAIR: '维修中', SCRAP: '已报废', RENT: '租赁' }
const stockStatusMap: Record<string, string> = { IN_STOCK: '在库', OUT_STOCK: '已出库' }
const eqScanTypeMap: Record<string, string> = {
  CHECK_IN: '入库', CHECK_OUT: '出库', STOCKTAKE: '盘点', MAINTAIN: '维护登记', INSPECT: '点检', SCRAP: '报废登记',
}
const scanTypeTag = (k: string) => ({
  CHECK_IN: 'success', CHECK_OUT: 'warning', STOCKTAKE: 'primary', MAINTAIN: 'info', INSPECT: 'info', SCRAP: 'danger',
}[k] || 'info') as any
const flowMap: Record<string, string[]> = { NORMAL: ['REPAIR', 'SCRAP'], REPAIR: ['NORMAL', 'SCRAP'] }
const statusTag = (k: string) => ({ NORMAL: 'success', REPAIR: 'warning', SCRAP: 'danger', RENT: 'info' }[k] || 'info') as any
const statusTone = (k: string) => ({ NORMAL: 'ok', REPAIR: 'warn', SCRAP: 'danger', RENT: 'info' }[k] || 'info') as any
const nextStatus = (s: string) => flowMap[s] || []

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const scanDialog = ref(false)
const qrVisible = ref(false)
const qrRow = ref<any>(null)
const summary = ref<any>({})
const scannedEquipment = ref<any>(null)
const recentScans = ref<any[]>([])
const recentLoading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', equipmentType: '', status: '' })
const form = reactive<any>({
  equipmentId: '', equipmentCode: '', equipmentName: '', equipmentType: '缝纫机', model: '',
  manufacturer: '', purchaseDate: '', purchaseAmount: 0, location: '', status: 'NORMAL', remark: '',
})
const scanForm = reactive({ equipmentCode: '', scanType: 'CHECK_IN', remark: '' })

const qrContent = computed(() => qrRow.value?.equipmentCode || '')
const qrDescription = computed(() => qrRow.value
  ? `设备编码：${qrRow.value.equipmentCode}（${qrRow.value.equipmentName}）`
  : '')

async function load() {
  loading.value = true
  try {
    const data: any = await getEquipmentPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally { loading.value = false }
}

async function loadSummary() {
  try { summary.value = await getEquipmentSummary() } catch { /* ignore */ }
}

async function loadRecentScans() {
  recentLoading.value = true
  try {
    recentScans.value = await getRecentEquipmentScan(20)
  } catch { recentScans.value = [] } finally { recentLoading.value = false }
}

function openDialog(row?: any) {
  if (row) Object.assign(form, { ...row })
  else Object.assign(form, { equipmentId: '', equipmentCode: '', equipmentName: '', equipmentType: '缝纫机', model: '', manufacturer: '', purchaseDate: '', purchaseAmount: 0, location: '', status: 'NORMAL', remark: '' })
  dialogVisible.value = true
}

function openQr(row: any) {
  qrRow.value = row
  qrVisible.value = true
}

async function save() {
  const isNew = !form.equipmentId
  if (isNew) {
    const saved: any = await createEquipment({ ...form })
    qrRow.value = saved?.equipmentId ? saved : { ...form }
    qrVisible.value = true
  } else {
    await updateEquipment({ ...form })
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
  await loadSummary()
  await loadRecentScans()
}

async function changeStatus(row: any, status: string) {
  await updateEquipmentStatus(row.equipmentId, status)
  ElMessage.success(`已流转为「${statusMap[status]}」`)
  await load()
  await loadSummary()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除设备「${row.equipmentName}」？`, '提示', { type: 'warning' })
  await deleteEquipment(row.equipmentId)
  ElMessage.success('删除成功')
  await load()
  await loadSummary()
}

async function onCodeScanned(payload: { barcode: string } | null) {
  if (!payload?.barcode) { scannedEquipment.value = null; return }
  try {
    const eq: any = await getEquipmentByCode(payload.barcode)
    if (eq?.equipmentId) {
      scannedEquipment.value = eq
      ElMessage.success(`已识别：${eq.equipmentName}`)
    } else {
      scannedEquipment.value = null
      ElMessage.warning('未找到该编码对应的设备')
    }
  } catch { scannedEquipment.value = null }
}

async function submitScan() {
  if (!scannedEquipment.value) return
  await scanEquipment({
    equipmentId: scannedEquipment.value.equipmentId,
    scanType: scanForm.scanType,
    remark: scanForm.remark,
  })
  ElMessage.success('登记成功')
  scanDialog.value = false
  scanForm.equipmentCode = ''
  scanForm.remark = ''
  scannedEquipment.value = null
  await load()
  await loadSummary()
  await loadRecentScans()
}

onMounted(async () => {
  await Promise.all([load(), loadSummary(), loadRecentScans()])
  // 从详情页"编辑"跳转过来时自动打开编辑框
  if (route.query.edit) {
    const detail: any = await getEquipmentDetail(String(route.query.edit))
    if (detail?.equipmentId) openDialog(detail)
  }
})
</script>

<style scoped>
.kpi-row { margin-bottom: 16px; }
.kpi { background: var(--mes-surface); border: 1px solid var(--mes-line); border-radius: 8px; padding: 12px 14px; display: flex; flex-direction: column; gap: 4px; }
.kpi__label { font-size: 12px; color: var(--mes-mist); }
.kpi__value { font-size: 22px; font-weight: 700; color: var(--mes-ink); font-family: var(--mes-font-data); }
.kpi__value.ok { color: var(--el-color-success); }
.kpi__value.warn { color: var(--el-color-warning); }
.kpi__value.danger { color: var(--el-color-danger); }
.pagination { margin-top: 16px; justify-content: flex-end; }
.hint { color: var(--mes-mist); font-size: 13px; }
.recent-card { margin-top: 16px; }
.recent-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.title-sm { margin: 0; font-size: 15px; font-weight: 600; color: var(--mes-ink); }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
</style>
