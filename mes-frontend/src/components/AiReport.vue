<template>
  <el-dialog v-model="visible" title="AI 智能报表" width="900px" top="5vh" class="ai-report-dialog">
    <!-- 生成配置 -->
    <div class="report-config">
      <div class="config-row">
        <span class="label">数据模块</span>
        <el-radio-group v-model="moduleType">
          <el-radio-button v-for="m in modules" :key="m.value" :value="m.value">{{ m.label }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="config-row">
        <span class="label">报表需求</span>
        <el-input
          v-model="prompt"
          type="textarea"
          :rows="2"
          placeholder="描述你要的报表，如：按状态统计订单数量与金额分布，并给出交期风险提示"
        />
      </div>
      <div class="config-row">
        <el-button type="primary" :loading="loading" :icon="MagicStick" @click="generate">生成报表</el-button>
      </div>
    </div>

    <!-- 预览 -->
    <div v-if="report" class="report-preview">
      <div class="report-head">
        <h3 class="report-title">{{ report.title }}</h3>
        <div class="report-actions">
          <el-button type="primary" :icon="Download" @click="downloadPdf">下载 PDF</el-button>
          <el-button type="success" :icon="Download" @click="downloadExcel">下载 Excel</el-button>
        </div>
      </div>
      <div v-if="report.summary" class="report-summary">{{ report.summary }}</div>
      <div v-if="report.analysis" class="report-analysis">{{ report.analysis }}</div>

      <!-- 图表 -->
      <div v-if="report.charts && report.charts.length" class="chart-area">
        <div v-for="(chart, i) in report.charts" :key="i" class="chart-box">
          <h4>{{ chart.title }}</h4>
          <div :ref="(el) => setChartRef(el, i)" class="chart-canvas"></div>
        </div>
      </div>

      <!-- 表格 -->
      <div v-for="(table, i) in report.tables" :key="'t' + i" class="table-box">
        <h4>{{ table.title }}</h4>
        <el-table :data="table.rows" border size="small" max-height="300">
          <el-table-column v-for="(h, hi) in table.headers" :key="hi" :prop="`${hi}`" :label="h" min-width="120">
            <template #default="{ row }">{{ row[hi] }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-empty v-else-if="!loading" description="选择模块并描述需求，点击「生成报表」" :image-size="80" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { generateReport, exportReportPdf, exportReportExcel, downloadBlob } from '@/api/report'

const modules = [
  { label: '订单', value: 'order' },
  { label: '生产', value: 'production' },
  { label: '库存', value: 'inventory' },
  { label: '质检', value: 'quality' },
]

const visible = ref(false)
const moduleType = ref('order')
const prompt = ref('')
const loading = ref(false)
const report = ref<any>(null)
const chartInstances: echarts.ECharts[] = []
const chartEls: any[] = []

function setChartRef(el: any, index: number) {
  chartEls[index] = el
}

function open() {
  visible.value = true
  report.value = null
}

async function generate() {
  loading.value = true
  report.value = null
  try {
    report.value = await generateReport(moduleType.value, prompt.value)
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  chartInstances.forEach((c) => c.dispose())
  chartInstances.length = 0
  if (!report.value?.charts) return
  report.value.charts.forEach((chart: any, i: number) => {
    const el = chartEls[i]
    if (!el) return
    const instance = echarts.init(el)
    const option: any = {
      tooltip: { trigger: chart.type === 'pie' ? 'item' : 'axis' },
      legend: {},
    }
    if (chart.type === 'pie') {
      option.series = [{
        type: 'pie',
        radius: '60%',
        data: (chart.labels || []).map((l: string, idx: number) => ({
          name: l,
          value: chart.series?.[0]?.data?.[idx] ?? 0,
        })),
      }]
    } else {
      option.xAxis = { type: 'category', data: chart.labels || [] }
      option.yAxis = { type: 'value' }
      option.series = (chart.series || []).map((s: any) => ({ name: s.name, type: chart.type, data: s.data }))
    }
    instance.setOption(option)
    chartInstances.push(instance)
  })
}

async function downloadPdf() {
  const blob: any = await exportReportPdf(report.value)
  downloadBlob(blob, `${report.value.title || '报表'}.pdf`)
  ElMessage.success('PDF 已生成')
}

async function downloadExcel() {
  const blob: any = await exportReportExcel(report.value)
  downloadBlob(blob, `${report.value.title || '报表'}.xlsx`)
  ElMessage.success('Excel 已生成')
}

defineExpose({ open })
</script>

<style scoped>
.report-config {
  padding: 4px 0 16px;
  border-bottom: 1px dashed var(--mes-border-color);
  margin-bottom: 16px;
}

.config-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.config-row .label {
  width: 70px;
  color: var(--mes-text-regular);
  flex-shrink: 0;
}

.config-row .el-input {
  flex: 1;
}

.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.report-title {
  margin: 0;
  font-size: 18px;
  color: var(--mes-text-primary);
}

.report-summary {
  background: var(--el-color-primary-light-9);
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 12px;
  color: var(--mes-text-regular);
  font-size: 13px;
}

.report-analysis {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--mes-text-regular);
  margin-bottom: 16px;
}

.chart-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-box h4,
.table-box h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--mes-text-primary);
}

.chart-canvas {
  height: 240px;
}

.table-box {
  margin-bottom: 16px;
}
</style>
