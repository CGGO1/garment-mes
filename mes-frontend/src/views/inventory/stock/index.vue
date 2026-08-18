<template>
  <div class="page-container">
    <PageHeader
      eyebrow="INVENTORY CONTROL"
      title="库存管理"
      subtitle="查询现存量、入库、出库与库存流水，掌握面辅料实时动态"
    >
      <template #actions>
        <el-button :icon="Plus" @click="inboundVisible = true">新增入库</el-button>
        <el-button type="primary" :icon="Plus" @click="outboundVisible = true">新增出库</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <el-tabs v-model="activeTab">
        <!-- 现存量 -->
        <el-tab-pane label="现存量" name="stock">
          <div class="toolbar">
            <el-input v-model="stockQuery.keyword" placeholder="物料名称" style="width: 220px" clearable :prefix-icon="Search" @keyup.enter="loadStock" @clear="loadStock" />
            <el-button type="primary" :icon="Search" @click="loadStock">查询</el-button>
          </div>
          <el-table :data="stocks" v-loading="stockLoading">
            <el-table-column prop="materialName" label="物料" min-width="170" />
            <el-table-column prop="warehouseName" label="仓库" min-width="130" />
            <el-table-column label="现存量" width="140" align="right">
              <template #default="{ row }">
                <span class="cell-qty" :class="{ 'low-stock': Number(row.qty) < 50 }">{{ fmtNum(row.qty) }}</span>
                <el-tag v-if="Number(row.qty) < 50" size="small" type="danger" effect="light" class="low-tag">低库存</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="更新时间" width="165">
              <template #default="{ row }">{{ fmtDateTime(row.updateTime) }}</template>
            </el-table-column>
          </el-table>
          <el-pagination class="pagination" v-model:current-page="stockQuery.pageNum" v-model:page-size="stockQuery.pageSize" :total="stockTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" background @current-change="loadStock" @size-change="loadStock" />
        </el-tab-pane>

        <!-- 入库 -->
        <el-tab-pane label="入库单" name="inbound">
          <el-table :data="inbounds" v-loading="inboundLoading">
            <el-table-column prop="inboundNo" label="入库单号" min-width="160">
              <template #default="{ row }">
                <span class="link-like" @click="openInboundDetail(row)">{{ row.inboundNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" min-width="120" />
            <el-table-column prop="inboundType" label="类型" width="110">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ row.inboundType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceNo" label="来源单号" min-width="140" />
            <el-table-column label="合计数量" width="100" align="right">
              <template #default="{ row }"><span class="cell-qty">{{ fmtNum(row.totalQty) }}</span></template>
            </el-table-column>
            <el-table-column label="入库日期" width="165">
              <template #default="{ row }">{{ fmtDateTime(row.inboundDate) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openInboundDetail(row)">详情</el-button>
                  <el-button link type="danger" @click="removeInbound(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="pagination" v-model:current-page="inboundQuery.pageNum" v-model:page-size="inboundQuery.pageSize" :total="inboundTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" background @current-change="loadInbound" @size-change="loadInbound" />
        </el-tab-pane>

        <!-- 出库 -->
        <el-tab-pane label="出库单" name="outbound">
          <el-table :data="outbounds" v-loading="outboundLoading">
            <el-table-column prop="outboundNo" label="出库单号" min-width="160">
              <template #default="{ row }">
                <span class="link-like" @click="openOutboundDetail(row)">{{ row.outboundNo }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="warehouseName" label="仓库" min-width="120" />
            <el-table-column prop="outboundType" label="类型" width="110">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ row.outboundType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceNo" label="来源单号" min-width="140" />
            <el-table-column label="合计数量" width="100" align="right">
              <template #default="{ row }"><span class="cell-qty">{{ fmtNum(row.totalQty) }}</span></template>
            </el-table-column>
            <el-table-column label="出库日期" width="165">
              <template #default="{ row }">{{ fmtDateTime(row.outboundDate) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openOutboundDetail(row)">详情</el-button>
                  <el-button link type="danger" @click="removeOutbound(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination class="pagination" v-model:current-page="outboundQuery.pageNum" v-model:page-size="outboundQuery.pageSize" :total="outboundTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" background @current-change="loadOutbound" @size-change="loadOutbound" />
        </el-tab-pane>

        <!-- 库存流水 -->
        <el-tab-pane :label="`库存流水 (${logTotal})`" name="log">
          <el-table :data="logs" v-loading="logLoading">
            <el-table-column label="时间" width="165">
              <template #default="{ row }">{{ fmtDateTime(row.logTime) }}</template>
            </el-table-column>
            <el-table-column prop="materialName" label="物料" min-width="160" />
            <el-table-column prop="warehouseName" label="仓库" min-width="110" />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.changeType === 'IN' ? 'success' : 'danger'" size="small" effect="light">
                  {{ row.changeType === 'IN' ? '入库' : '出库' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="变动数量" width="110" align="right">
              <template #default="{ row }">
                <span class="cell-qty" :class="Number(row.changeQty) > 0 ? 'qty-in' : 'qty-out'">
                  {{ Number(row.changeQty) > 0 ? '+' : '' }}{{ fmtNum(row.changeQty) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="结存" width="100" align="right">
              <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.balanceQty) }}</span></template>
            </el-table-column>
            <el-table-column prop="bizNo" label="业务单号" min-width="150" />
          </el-table>
          <el-pagination class="pagination" v-model:current-page="logQuery.pageNum" v-model:page-size="logQuery.pageSize" :total="logTotal" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next" background @current-change="loadLog" @size-change="loadLog" />
        </el-tab-pane>

        <!-- 仓库 -->
        <el-tab-pane label="仓库" name="warehouse">
          <div class="toolbar">
            <div class="spacer"></div>
            <el-button type="primary" :icon="Plus" @click="openWarehouse()">新增仓库</el-button>
          </div>
          <el-table :data="warehouses" stripe>
            <el-table-column prop="warehouseCode" label="仓库编码" width="140" />
            <el-table-column prop="warehouseName" label="仓库名称" min-width="160" />
            <el-table-column prop="location" label="位置" min-width="160" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openWarehouse(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeWarehouse(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 入库详情抽屉 -->
    <el-drawer v-model="inboundDetailVisible" title="入库单详情" size="620px">
      <template v-if="inboundDetail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="入库单号">{{ inboundDetail.inboundNo }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ inboundDetail.warehouseName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ inboundDetail.inboundType }}</el-descriptions-item>
            <el-descriptions-item label="来源单号">{{ inboundDetail.sourceNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="入库日期">{{ fmtDateTime(inboundDetail.inboundDate) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ inboundDetail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-block">
          <h4 class="detail-block__title">入库明细（{{ inboundDetail.items?.length || 0 }}）</h4>
          <el-table :data="inboundDetail.items || []" border size="small">
            <el-table-column label="物料" min-width="180">
              <template #default="{ row }">{{ row.materialName || row.materialId }}</template>
            </el-table-column>
            <el-table-column label="数量" width="110" align="right">
              <template #default="{ row }"><span class="cell-qty">{{ fmtNum(row.qty) }}</span></template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" />
          </el-table>
        </div>
      </template>
    </el-drawer>

    <!-- 出库详情抽屉 -->
    <el-drawer v-model="outboundDetailVisible" title="出库单详情" size="620px">
      <template v-if="outboundDetail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="出库单号">{{ outboundDetail.outboundNo }}</el-descriptions-item>
            <el-descriptions-item label="仓库">{{ outboundDetail.warehouseName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ outboundDetail.outboundType }}</el-descriptions-item>
            <el-descriptions-item label="来源单号">{{ outboundDetail.sourceNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="出库日期">{{ fmtDateTime(outboundDetail.outboundDate) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ outboundDetail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-block">
          <h4 class="detail-block__title">出库明细（{{ outboundDetail.items?.length || 0 }}）</h4>
          <el-table :data="outboundDetail.items || []" border size="small">
            <el-table-column label="物料" min-width="180">
              <template #default="{ row }">{{ row.materialName || row.materialId }}</template>
            </el-table-column>
            <el-table-column label="数量" width="110" align="right">
              <template #default="{ row }"><span class="cell-qty">{{ fmtNum(row.qty) }}</span></template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="100" />
          </el-table>
        </div>
      </template>
    </el-drawer>

    <!-- 入库对话框 -->
    <el-dialog v-model="inboundVisible" title="新增入库单" width="660px">
      <el-form :model="inboundForm" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="仓库"><el-select v-model="inboundForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="inboundForm.inboundType" style="width: 100%"><el-option v-for="t in INBOUND_TYPES" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源单号"><el-input v-model="inboundForm.sourceNo" placeholder="采购单/工单号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入库日期"><el-date-picker v-model="inboundForm.inboundDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">入库明细</el-divider>
        <el-table :data="inboundForm.items" border size="small">
          <el-table-column label="物料" min-width="210"><template #default="{ row }"><el-select v-model="row.materialId" filterable style="width: 100%"><el-option v-for="m in materials" :key="m.materialId" :label="`${m.materialCode} ${m.materialName}`" :value="m.materialId" /></el-select></template></el-table-column>
          <el-table-column label="数量" width="140"><template #default="{ row }"><el-input-number v-model="row.qty" :min="0" style="width: 100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="inboundForm.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-btn" type="primary" plain :icon="Plus" @click="inboundForm.items.push({ materialId: '', qty: 1 })">添加明细</el-button>
      </el-form>
      <template #footer>
        <el-button @click="inboundVisible = false">取消</el-button>
        <el-button type="primary" @click="saveInbound">保存</el-button>
      </template>
    </el-dialog>

    <!-- 出库对话框 -->
    <el-dialog v-model="outboundVisible" title="新增出库单" width="660px">
      <el-form :model="outboundForm" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="仓库"><el-select v-model="outboundForm.warehouseId" style="width: 100%"><el-option v-for="w in warehouses" :key="w.warehouseId" :label="w.warehouseName" :value="w.warehouseId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="outboundForm.outboundType" style="width: 100%"><el-option v-for="t in OUTBOUND_TYPES" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="来源单号"><el-input v-model="outboundForm.sourceNo" placeholder="销售单/工单号" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出库日期"><el-date-picker v-model="outboundForm.outboundDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">出库明细</el-divider>
        <el-table :data="outboundForm.items" border size="small">
          <el-table-column label="物料" min-width="210"><template #default="{ row }"><el-select v-model="row.materialId" filterable style="width: 100%"><el-option v-for="m in materials" :key="m.materialId" :label="`${m.materialCode} ${m.materialName}`" :value="m.materialId" /></el-select></template></el-table-column>
          <el-table-column label="数量" width="140"><template #default="{ row }"><el-input-number v-model="row.qty" :min="0" style="width: 100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="outboundForm.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-btn" type="primary" plain :icon="Plus" @click="outboundForm.items.push({ materialId: '', qty: 1 })">添加明细</el-button>
      </el-form>
      <template #footer>
        <el-button @click="outboundVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOutbound">保存</el-button>
      </template>
    </el-dialog>

    <!-- 仓库对话框 -->
    <el-dialog v-model="warehouseVisible" :title="warehouseForm.warehouseId ? '编辑仓库' : '新增仓库'" width="460px">
      <el-form :model="warehouseForm" label-width="90px">
        <el-form-item label="仓库编码"><el-input v-model="warehouseForm.warehouseCode" /></el-form-item>
        <el-form-item label="仓库名称"><el-input v-model="warehouseForm.warehouseName" /></el-form-item>
        <el-form-item label="位置"><el-input v-model="warehouseForm.location" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="warehouseVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWarehouse">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { fmtDateTime, fmtNum } from '@/utils/format'
import {
  getStockPage, getInboundPage, createInbound, getInboundDetail, deleteInbound,
  getOutboundPage, createOutbound, getOutboundDetail, deleteOutbound,
  getWarehouseList, createWarehouse, updateWarehouse, deleteWarehouse, getStockLog,
} from '@/api/inventory'
import { getMaterialList } from '@/api/master'

const INBOUND_TYPES = ['采购入库', '生产入库', '退料入库', '调拨入库', '退货入库']
const OUTBOUND_TYPES = ['生产领料', '销售出库', '调拨出库', '报废出库', '样品出库']

const activeTab = ref('stock')
const stocks = ref<any[]>([])
const inbounds = ref<any[]>([])
const outbounds = ref<any[]>([])
const logs = ref<any[]>([])
const warehouses = ref<any[]>([])
const materials = ref<any[]>([])
const stockLoading = ref(false)
const inboundLoading = ref(false)
const outboundLoading = ref(false)
const logLoading = ref(false)
const inboundVisible = ref(false)
const outboundVisible = ref(false)
const warehouseVisible = ref(false)
const inboundDetailVisible = ref(false)
const outboundDetailVisible = ref(false)
const inboundDetail = ref<any>(null)
const outboundDetail = ref<any>(null)

const stockQuery = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const inboundQuery = reactive({ pageNum: 1, pageSize: 10 })
const outboundQuery = reactive({ pageNum: 1, pageSize: 10 })
const logQuery = reactive({ pageNum: 1, pageSize: 10 })
const stockTotal = ref(0)
const inboundTotal = ref(0)
const outboundTotal = ref(0)
const logTotal = ref(0)

const inboundForm = reactive<any>({ warehouseId: '', inboundType: '采购入库', sourceNo: '', inboundDate: '', items: [] })
const outboundForm = reactive<any>({ warehouseId: '', outboundType: '生产领料', sourceNo: '', outboundDate: '', items: [] })
const warehouseForm = reactive({ warehouseId: '', warehouseCode: '', warehouseName: '', location: '' })

async function loadStock() {
  stockLoading.value = true
  try {
    const data: any = await getStockPage(stockQuery)
    stocks.value = data.records
    stockTotal.value = Number(data.total)
  } finally {
    stockLoading.value = false
  }
}

async function loadInbound() {
  inboundLoading.value = true
  try {
    const data: any = await getInboundPage(inboundQuery)
    inbounds.value = data.records
    inboundTotal.value = Number(data.total)
  } finally {
    inboundLoading.value = false
  }
}

async function loadOutbound() {
  outboundLoading.value = true
  try {
    const data: any = await getOutboundPage(outboundQuery)
    outbounds.value = data.records
    outboundTotal.value = Number(data.total)
  } finally {
    outboundLoading.value = false
  }
}

async function loadLog() {
  logLoading.value = true
  try {
    const data: any = await getStockLog(logQuery)
    logs.value = data.records
    logTotal.value = Number(data.total)
  } finally {
    logLoading.value = false
  }
}

async function loadWarehouses() {
  warehouses.value = await getWarehouseList()
}

async function openInboundDetail(row: any) {
  inboundDetail.value = await getInboundDetail(row.inboundId)
  inboundDetailVisible.value = true
}

async function openOutboundDetail(row: any) {
  outboundDetail.value = await getOutboundDetail(row.outboundId)
  outboundDetailVisible.value = true
}

async function saveInbound() {
  await createInbound({ ...inboundForm })
  ElMessage.success('入库成功，库存已更新')
  inboundVisible.value = false
  Object.assign(inboundForm, { warehouseId: '', inboundType: '采购入库', sourceNo: '', inboundDate: '', items: [] })
  loadInbound()
  loadStock()
}

async function saveOutbound() {
  await createOutbound({ ...outboundForm })
  ElMessage.success('出库成功，库存已更新')
  outboundVisible.value = false
  Object.assign(outboundForm, { warehouseId: '', outboundType: '生产领料', sourceNo: '', outboundDate: '', items: [] })
  loadOutbound()
  loadStock()
}

async function removeInbound(row: any) {
  await ElMessageBox.confirm(`确认删除入库单「${row.inboundNo}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteInbound(row.inboundId)
  ElMessage.success('删除成功')
  loadInbound()
}

async function removeOutbound(row: any) {
  await ElMessageBox.confirm(`确认删除出库单「${row.outboundNo}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteOutbound(row.outboundId)
  ElMessage.success('删除成功')
  loadOutbound()
}

function openWarehouse(row?: any) {
  Object.assign(warehouseForm, row ? { ...row } : { warehouseId: '', warehouseCode: '', warehouseName: '', location: '' })
  warehouseVisible.value = true
}

async function saveWarehouse() {
  if (warehouseForm.warehouseId) await updateWarehouse({ ...warehouseForm })
  else await createWarehouse({ ...warehouseForm })
  ElMessage.success('保存成功')
  warehouseVisible.value = false
  loadWarehouses()
}

async function removeWarehouse(row: any) {
  await ElMessageBox.confirm(`确认删除仓库「${row.warehouseName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteWarehouse(row.warehouseId)
  ElMessage.success('删除成功')
  loadWarehouses()
}

onMounted(async () => {
  loadStock()
  loadInbound()
  loadOutbound()
  loadLog()
  loadWarehouses()
  materials.value = await getMaterialList()
})
</script>

<style scoped lang="scss">
.pagination { margin-top: 16px; justify-content: flex-end; }
.add-btn { margin-top: 12px; }
.low-stock { color: var(--mes-danger); font-weight: 700; }
.low-tag { margin-left: 6px; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
.qty-in { color: var(--mes-success); }
.qty-out { color: var(--mes-danger); }
</style>
