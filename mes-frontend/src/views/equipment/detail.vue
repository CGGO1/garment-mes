<template>
  <div class="page-container">
    <div class="page-head with-back">
      <div>
        <div class="back-row">
          <el-button link @click="$router.push('/equipment')" :icon="ArrowLeft">返回列表</el-button>
        </div>
        <p class="page-head__eyebrow">EQUIPMENT MANAGEMENT</p>
        <h1 class="page-head__title">{{ eq.equipmentName || '设备详情' }}</h1>
        <p class="page-head__sub">{{ eq.equipmentCode }} · {{ eq.equipmentType }}</p>
      </div>
      <StatusTag v-if="eq.status" :text="statusMap[eq.status]" :tone="statusTone(eq.status)" />
    </div>

    <el-row v-if="!loading" :gutter="16">
      <el-col :span="8">
        <div class="mes-card profile">
          <h3 class="title-sm">基础信息</h3>
          <el-descriptions :column="1" size="default" border>
            <el-descriptions-item label="编码">{{ eq.equipmentCode }}</el-descriptions-item>
            <el-descriptions-item label="名称">{{ eq.equipmentName }}</el-descriptions-item>
            <el-descriptions-item label="类型"><el-tag effect="light">{{ eq.equipmentType }}</el-tag></el-descriptions-item>
            <el-descriptions-item label="型号">{{ eq.model }}</el-descriptions-item>
            <el-descriptions-item label="厂商">{{ eq.manufacturer }}</el-descriptions-item>
            <el-descriptions-item label="购置日期">{{ fmtDate(eq.purchaseDate) }}</el-descriptions-item>
            <el-descriptions-item label="购置金额">¥ {{ fmtMoney(eq.purchaseAmount) }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ eq.location }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ eq.managerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="库存状态">
              <el-tag :type="eq.stockStatus === 'OUT_STOCK' ? 'warning' : 'success'" effect="light">
                {{ stockStatusMap[eq.stockStatus] || '在库' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最近扫码">{{ fmtDateTime(eq.lastScanTime) }}</el-descriptions-item>
          </el-descriptions>
          <div class="profile-qr">
            <QrCode ref="profileQrRef" :content="eq.equipmentCode" :size="140" :label="eq.equipmentCode" :filename="`equipment-${eq.equipmentCode}.png`" />
            <el-button size="small" :icon="Download" @click="profileQrRef?.download()">下载二维码</el-button>
          </div>
          <div class="profile-actions">
            <el-button :icon="Camera" @click="scanDialog = true">扫码登记</el-button>
            <el-button type="primary" :icon="Edit" @click="openEdit">编辑信息</el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="16">
        <div class="mes-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="维护工单" name="maintenance">
              <div class="toolbar">
                <el-select v-model="mtFilter" placeholder="状态" clearable style="width: 130px" @change="filterMt">
                  <el-option v-for="(label, key) in mtStatusMap" :key="key" :label="label" :value="key" />
                </el-select>
                <div class="spacer"></div>
                <el-button type="primary" :icon="Plus" @click="openMtDialog()">新建维护</el-button>
              </div>
              <el-table :data="filteredMt" size="small" stripe>
                <el-table-column prop="maintenanceNo" label="维护单号" min-width="160" />
                <el-table-column label="类型" width="100">
                  <template #default="{ row }">
                    <el-tag effect="light">{{ mtTypeMap[row.maintenanceType] || row.maintenanceType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="180" />
                <el-table-column label="计划日期" width="110">
                  <template #default="{ row }">{{ fmtDate(row.planDate) }}</template>
                </el-table-column>
                <el-table-column label="完成日期" width="110">
                  <template #default="{ row }">{{ fmtDate(row.doneDate) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <StatusTag :text="mtStatusMap[row.status] || row.status" :tone="mtStatusTone(row.status)" />
                  </template>
                </el-table-column>
                <el-table-column prop="costAmount" label="费用" width="100">
                  <template #default="{ row }">¥ {{ fmtMoney(row.costAmount) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openMtDialog(row)">编辑</el-button>
                    <el-dropdown trigger="click" @command="(s: string) => changeMtStatus(row, s)">
                      <el-button link type="warning">流转<el-icon><ArrowDown /></el-icon></el-button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item v-for="s in nextMtStatus(row.status)" :key="s" :command="s">{{ mtStatusMap[s] }}</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                    <el-button link type="danger" @click="removeMt(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <el-tab-pane label="扫码记录" name="scan">
              <el-table :data="scans" size="small" stripe>
                <el-table-column prop="scanType" label="事件类型" width="140">
                  <template #default="{ row }">
                    <el-tag effect="light">{{ eqScanTypeMap[row.scanType] || row.scanType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="扫码时间" min-width="180">
                  <template #default="{ row }">{{ fmtDateTime(row.scanTime) }}</template>
                </el-table-column>
                <el-table-column prop="operatorId" label="操作人" min-width="120" />
                <el-table-column prop="qty" label="数量" width="100" />
                <el-table-column prop="remark" label="备注" min-width="200" />
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
    <el-empty v-else-if="!loading && !eq.equipmentId" description="设备不存在" />

    <!-- 维护工单编辑 -->
    <el-dialog v-model="mtDialogVisible" :title="mtForm.maintenanceId ? '编辑维护' : '新建维护'" width="640px">
      <el-form :model="mtForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="mtForm.maintenanceType" style="width: 100%"><el-option v-for="(label, key) in mtTypeMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="状态"><el-select v-model="mtForm.status" style="width: 100%"><el-option v-for="(label, key) in mtStatusMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标题"><el-input v-model="mtForm.title" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="内容"><el-input v-model="mtForm.content" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划日期"><el-date-picker v-model="mtForm.planDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="完成日期"><el-date-picker v-model="mtForm.doneDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="费用"><el-input-number v-model="mtForm.costAmount" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="mtForm.remark" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="mtDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMt">保存</el-button>
      </template>
    </el-dialog>

    <!-- 扫码登记 -->
    <el-dialog v-model="scanDialog" title="扫码登记" width="420px">
      <el-form :model="scanForm" label-width="100px">
        <el-form-item label="事件类型">
          <el-select v-model="scanForm.scanType" style="width: 100%">
            <el-option v-for="(label, key) in eqScanTypeMap" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="scanForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scanDialog = false">取消</el-button>
        <el-button type="primary" @click="submitScan">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Edit, Camera, ArrowDown, Download } from '@element-plus/icons-vue'
import StatusTag from '@/components/StatusTag.vue'
import QrCode from '@/components/QrCode.vue'
import { fmtDate, fmtDateTime, fmtMoney } from '@/utils/format'
import {
  getEquipmentDetail, updateEquipment,
  listMaintenance, createMaintenance, updateMaintenance, deleteMaintenance, updateMaintenanceStatus,
  listEquipmentScan, scanEquipment,
} from '@/api/equipment'

const route = useRoute()
const router = useRouter()
const id = String(route.params.id || '')

const statusMap: Record<string, string> = { NORMAL: '正常', REPAIR: '维修中', SCRAP: '已报废', RENT: '租赁' }
const stockStatusMap: Record<string, string> = { IN_STOCK: '在库', OUT_STOCK: '已出库' }
const statusTag = (k: string) => ({ NORMAL: 'success', REPAIR: 'warning', SCRAP: 'danger', RENT: 'info' }[k] || 'info') as any
const statusTone = (k: string) => ({ NORMAL: 'ok', REPAIR: 'warn', SCRAP: 'danger', RENT: 'info' }[k] || 'info') as any
const mtTypeMap: Record<string, string> = { DAILY: '日常点检', PERIODIC: '定期保养', REPAIR: '故障维修', UPGRADE: '升级改造' }
const mtStatusMap: Record<string, string> = { PLANNED: '已计划', DOING: '进行中', DONE: '已完成', CANCELLED: '已取消' }
const mtStatusTag = (k: string) => ({ PLANNED: 'info', DOING: 'warning', DONE: 'success', CANCELLED: 'info' }[k] || 'info') as any
const mtStatusTone = (k: string) => ({ PLANNED: 'info', DOING: 'warn', DONE: 'ok', CANCELLED: 'info' }[k] || 'info') as any
const mtFlowMap: Record<string, string[]> = { PLANNED: ['DOING', 'CANCELLED'], DOING: ['DONE'] }
const eqScanTypeMap: Record<string, string> = {
  CHECK_IN: '入库', CHECK_OUT: '出库', STOCKTAKE: '盘点', MAINTAIN: '维护', INSPECT: '点检', SCRAP: '报废',
}
const nextMtStatus = (s: string) => mtFlowMap[s] || []

const loading = ref(true)
const activeTab = ref('maintenance')
const eq = reactive<any>({})
const profileQrRef = ref<InstanceType<typeof QrCode> | null>(null)
const maintenances = ref<any[]>([])
const scans = ref<any[]>([])
const mtFilter = ref('')
const filteredMt = computed(() => mtFilter.value ? maintenances.value.filter((m) => m.status === mtFilter.value) : maintenances.value)

const mtDialogVisible = ref(false)
const mtForm = reactive<any>({ maintenanceId: '', maintenanceNo: '', maintenanceType: 'PERIODIC', title: '', content: '', planDate: '', doneDate: '', status: 'PLANNED', costAmount: 0, remark: '' })
const scanDialog = ref(false)
const scanForm = reactive({ scanType: 'CHECK_IN', remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getEquipmentDetail(id)
    Object.assign(eq, data)
    maintenances.value = data.maintenances || []
    scans.value = data.recentScans || []
  } finally { loading.value = false }
}

function openEdit() {
  router.push({ path: '/equipment', query: { edit: id } })
}

function openMtDialog(row?: any) {
  if (row) Object.assign(mtForm, { ...row })
  else Object.assign(mtForm, { maintenanceId: '', maintenanceNo: '', maintenanceType: 'PERIODIC', title: '', content: '', planDate: '', doneDate: '', status: 'PLANNED', costAmount: 0, remark: '' })
  mtDialogVisible.value = true
}

async function saveMt() {
  const payload = { ...mtForm, equipmentId: id }
  if (mtForm.maintenanceId) await updateMaintenance(payload)
  else await createMaintenance(payload)
  ElMessage.success('保存成功')
  mtDialogVisible.value = false
  await load()
}

async function changeMtStatus(row: any, status: string) {
  await updateMaintenanceStatus(row.maintenanceId, status)
  ElMessage.success('已更新状态')
  await load()
}

async function removeMt(row: any) {
  await ElMessageBox.confirm(`确认删除维护「${row.title}」？`, '提示', { type: 'warning' })
  await deleteMaintenance(row.maintenanceId)
  ElMessage.success('已删除')
  await load()
}

function filterMt() { /* 触发 computed */ }

async function submitScan() {
  await scanEquipment({ equipmentId: id, scanType: scanForm.scanType, remark: scanForm.remark })
  ElMessage.success('扫码登记成功')
  scanDialog.value = false
  scanForm.remark = ''
  await load()
}

onMounted(load)
</script>

<style scoped>
.with-back { display: flex; align-items: center; justify-content: space-between; }
.back-row { margin-bottom: 6px; }
.back-row .el-button { padding: 0; font-size: 13px; }
.title-sm { margin: 0 0 12px; font-size: 15px; font-weight: 600; color: var(--mes-ink); }
.profile .profile-actions { display: flex; gap: 8px; margin-top: 16px; }
.profile-qr { display: flex; flex-direction: column; align-items: center; gap: 10px; margin-top: 16px; padding-top: 16px; border-top: 1px dashed var(--mes-line, #e5e7eb); }
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.spacer { flex: 1; }
</style>
