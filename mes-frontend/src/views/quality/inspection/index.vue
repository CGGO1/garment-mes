<template>
  <div class="page-container">
    <PageHeader
      eyebrow="QUALITY ASSURANCE"
      title="质量管理"
      subtitle="验货单、AQL 检验标准与疵点记录，覆盖 IQC / IPQC / FQC / OQC"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增验货单</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="验货单" name="inspection">
          <div class="toolbar">
            <el-input v-model="query.keyword" placeholder="验货单号" style="width: 200px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
            <el-select v-model="query.inspectionType" placeholder="检验类型" clearable style="width: 140px" @change="load">
              <el-option v-for="(label, key) in typeMap" :key="key" :label="label" :value="key" />
            </el-select>
            <el-select v-model="query.result" placeholder="结果" clearable style="width: 120px" @change="load">
              <el-option v-for="(label, key) in resultMap" :key="key" :label="label" :value="key" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="load">查询</el-button>
          </div>
          <el-table :data="list" v-loading="loading">
            <el-table-column prop="inspectionNo" label="验货单号" min-width="155">
              <template #default="{ row }">
                <span class="link-like" @click="openDetail(row)">{{ row.inspectionNo }}</span>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="105">
              <template #default="{ row }">
                <el-tag size="small" effect="light">{{ typeMap[row.inspectionType] || row.inspectionType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="抽样 / 合格 / 不合格" min-width="160">
              <template #default="{ row }">
                <span class="mes-num">{{ fmtNum(row.sampleQty) }} / {{ fmtNum(row.passQty) }} / <span class="fail-num">{{ fmtNum(row.failQty) }}</span></span>
              </template>
            </el-table-column>
            <el-table-column label="结果" width="100">
              <template #default="{ row }">
                <StatusTag :text="resultMap[row.result] || row.result" :tone="resultTone(row.result)" />
              </template>
            </el-table-column>
            <el-table-column prop="inspector" label="检验员" width="95" />
            <el-table-column label="检验日期" width="115">
              <template #default="{ row }">{{ fmtDate(row.inspectDate) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
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
        </el-tab-pane>

        <el-tab-pane label="质检标准" name="standard">
          <div class="toolbar">
            <div class="spacer"></div>
            <el-button type="primary" :icon="Plus" @click="openStandard()">新增标准</el-button>
          </div>
          <el-table :data="standards" stripe>
            <el-table-column prop="standardName" label="标准名称" min-width="190" />
            <el-table-column label="AQL 等级" width="110">
              <template #default="{ row }">
                <el-tag effect="light" size="small">AQL {{ row.aqlLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <div class="row-actions">
                  <el-button link type="primary" @click="openStandard(row)">编辑</el-button>
                  <el-button link type="danger" @click="removeStandard(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 验货单详情抽屉 -->
    <el-drawer v-model="detailVisible" title="验货单详情" size="720px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="验货单号">{{ detail.inspectionNo }}</el-descriptions-item>
            <el-descriptions-item label="检验类型">{{ typeMap[detail.inspectionType] || detail.inspectionType }}</el-descriptions-item>
            <el-descriptions-item label="检验员">{{ detail.inspector || '—' }}</el-descriptions-item>
            <el-descriptions-item label="结果">
              <StatusTag :text="resultMap[detail.result] || detail.result" :tone="resultTone(detail.result)" />
            </el-descriptions-item>
            <el-descriptions-item label="抽样数">
              <span class="cell-qty">{{ fmtNum(detail.sampleQty) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="合格 / 不合格">
              <span class="cell-qty">{{ fmtNum(detail.passQty) }} / {{ fmtNum(detail.failQty) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="检验日期">{{ fmtDate(detail.inspectDate) }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-tabs v-model="detailTab">
          <el-tab-pane :label="`检验项目 (${detail.items?.length || 0})`" name="items">
            <el-table :data="detail.items || []" border size="small">
              <el-table-column prop="checkItem" label="检验项目" min-width="150" />
              <el-table-column label="结果" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.checkResult === 'PASS' ? 'success' : 'danger'" size="small" effect="light">
                    {{ row.checkResult === 'PASS' ? '合格' : '不合格' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="疵点数" width="100" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.defectQty) }}</span></template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="100" />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="`疵点记录 (${detail.defects?.length || 0})`" name="defects">
            <el-table :data="detail.defects || []" border size="small">
              <el-table-column label="疵点类型" min-width="110">
                <template #default="{ row }"><el-tag effect="light" size="small">{{ row.defectType }}</el-tag></template>
              </el-table-column>
              <el-table-column prop="defectDesc" label="疵点描述" min-width="200" show-overflow-tooltip />
              <el-table-column label="数量" width="90" align="right">
                <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.defectQty) }}</span></template>
              </el-table-column>
              <el-table-column label="严重程度" width="100">
                <template #default="{ row }">
                  <StatusTag :text="severityMap[row.severity] || row.severity" :tone="severityTone(row.severity)" />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <!-- 验货单编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.inspectionId ? '编辑验货单' : '新增验货单'" width="760px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="验货单号"><el-input v-model="form.inspectionNo" placeholder="留空自动生成" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="检验类型"><el-select v-model="form.inspectionType" style="width: 100%"><el-option v-for="(label, key) in typeMap" :key="key" :label="label" :value="key" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="标准"><el-select v-model="form.standardId" style="width: 100%"><el-option v-for="s in standards" :key="s.standardId" :label="`${s.standardName} (AQL ${s.aqlLevel})`" :value="s.standardId" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="抽样数"><el-input-number v-model="form.sampleQty" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合格数"><el-input-number v-model="form.passQty" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="不合格数"><el-input-number v-model="form.failQty" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检验日期"><el-date-picker v-model="form.inspectDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="检验员"><el-input v-model="form.inspector" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">检验项目</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="检验项目" min-width="200"><template #default="{ row }"><el-input v-model="row.checkItem" /></template></el-table-column>
          <el-table-column label="结果" width="120"><template #default="{ row }"><el-select v-model="row.checkResult" style="width: 100%"><el-option label="合格" value="PASS" /><el-option label="不合格" value="FAIL" /></el-select></template></el-table-column>
          <el-table-column label="疵点数" width="120"><template #default="{ row }"><el-input-number v-model="row.defectQty" :min="0" style="width: 100%" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-btn" type="primary" plain :icon="Plus" @click="form.items.push({ checkItem: '', checkResult: 'PASS', defectQty: 0 })">添加项目</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标准编辑 -->
    <el-dialog v-model="standardVisible" :title="standardForm.standardId ? '编辑标准' : '新增标准'" width="480px">
      <el-form :model="standardForm" label-width="90px">
        <el-form-item label="标准名称"><el-input v-model="standardForm.standardName" /></el-form-item>
        <el-form-item label="AQL 等级"><el-select v-model="standardForm.aqlLevel" style="width: 100%"><el-option v-for="a in ['1.0', '1.5', '2.5', '4.0']" :key="a" :label="`AQL ${a}`" :value="a" /></el-select></el-form-item>
        <el-form-item label="说明"><el-input v-model="standardForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="standardVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStandard">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fmtDate, fmtNum } from '@/utils/format'
import { getInspectionPage, getInspectionDetail, createInspection, updateInspection, deleteInspection, getStandardList, createStandard, updateStandard, deleteStandard } from '@/api/quality'

const typeMap: Record<string, string> = { IQC: '来料检验', IPQC: '过程检验', FQC: '成品检验', OQC: '出货检验', QA: 'QA检验' }
const resultMap: Record<string, string> = { PENDING: '待检验', PASS: '合格', FAIL: '不合格' }
const severityMap: Record<string, string> = { MINOR: '次要', MAJOR: '主要', CRITICAL: '严重' }
const severityTone = (s: string) => ({ MINOR: 'info', MAJOR: 'warn', CRITICAL: 'danger' } as any)[s] || 'info'
const resultTone = (s: string) => ({ PASS: 'ok', FAIL: 'danger', PENDING: 'info' } as any)[s] || 'info'

const activeTab = ref('inspection')
const loading = ref(false)
const list = ref<any[]>([])
const standards = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const standardVisible = ref(false)
const detailVisible = ref(false)
const detailTab = ref('items')
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', inspectionType: '', result: '' })
const form = reactive<any>({ inspectionId: '', inspectionNo: '', inspectionType: 'IQC', standardId: '', sampleQty: 0, passQty: 0, failQty: 0, inspectDate: '', inspector: '', remark: '', items: [] })
const standardForm = reactive({ standardId: '', standardName: '', aqlLevel: '2.5', description: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getInspectionPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function loadStandards() {
  standards.value = await getStandardList()
}

async function openDialog(row?: any) {
  if (row) {
    const source = row.items && row.items.length ? row : await getInspectionDetail(row.inspectionId)
    Object.assign(form, { ...source, items: (source.items || []).map((i: any) => ({ ...i })) })
  } else {
    Object.assign(form, { inspectionId: '', inspectionNo: '', inspectionType: 'IQC', standardId: '', sampleQty: 0, passQty: 0, failQty: 0, inspectDate: '', inspector: '', remark: '', items: [] })
  }
  dialogVisible.value = true
}

async function openDetail(row: any) {
  detail.value = await getInspectionDetail(row.inspectionId)
  detailVisible.value = true
  detailTab.value = 'items'
}

async function save() {
  if (form.inspectionId) await updateInspection({ ...form })
  else await createInspection({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除验货单「${row.inspectionNo}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteInspection(row.inspectionId)
  ElMessage.success('删除成功')
  load()
}

function openStandard(row?: any) {
  Object.assign(standardForm, row ? { ...row } : { standardId: '', standardName: '', aqlLevel: '2.5', description: '' })
  standardVisible.value = true
}

async function saveStandard() {
  if (standardForm.standardId) await updateStandard({ ...standardForm })
  else await createStandard({ ...standardForm })
  ElMessage.success('保存成功')
  standardVisible.value = false
  loadStandards()
}

async function removeStandard(row: any) {
  await ElMessageBox.confirm(`确认删除标准「${row.standardName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteStandard(row.standardId)
  ElMessage.success('删除成功')
  loadStandards()
}

onMounted(() => {
  load()
  loadStandards()
})
</script>

<style scoped lang="scss">
.pagination { margin-top: 16px; justify-content: flex-end; }
.add-btn { margin-top: 12px; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
.fail-num { color: var(--mes-danger); font-weight: 700; }
</style>
