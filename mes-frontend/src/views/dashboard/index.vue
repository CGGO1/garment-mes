<template>
  <div class="page-container">
    <!-- 抬头 -->
    <div class="page-head">
      <div>
        <p class="page-head__eyebrow">WORKBENCH</p>
        <h1 class="page-head__title">生产工作台</h1>
        <p class="page-head__sub">实时生产执行概览 · {{ today }}</p>
      </div>
      <div class="head-actions">
        <el-button :icon="Plus" @click="$router.push('/trade/order')">新增订单</el-button>
        <span class="status-chip ok"><span class="status-chip__swatch"></span>数据已同步</span>
      </div>
    </div>

    <!-- 统计卡 -->
    <el-row :gutter="20" class="stat-row">
      <el-col v-for="s in stats" :key="s.label" :xs="12" :sm="12" :md="6">
        <StatCard v-bind="s" />
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="grid-2">
      <el-col :md="15">
        <SectionCard title="生产产量趋势（计划 vs 实际）">
          <div ref="trendEl" class="chart chart--trend"></div>
        </SectionCard>
      </el-col>
      <el-col :md="9">
        <SectionCard title="订单状态分布">
          <div ref="donutEl" class="chart chart--donut"></div>
        </SectionCard>
      </el-col>
    </el-row>

    <!-- 工单 + 质量 -->
    <el-row :gutter="20" class="grid-2">
      <el-col :md="15">
        <SectionCard title="近期生产工单">
          <template #extra><el-button link type="primary" @click="$router.push('/production/work-order')">全部</el-button></template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="no" label="工单号" min-width="130" />
            <el-table-column prop="product" label="产品" min-width="120" show-overflow-tooltip />
            <el-table-column label="进度" min-width="160">
              <template #default="{ row }">
                <div class="prog">
                  <div class="prog__bar"><span :style="{ width: row.progress + '%' }"></span></div>
                  <span class="prog__num mes-num">{{ row.progress }}%</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <StatusTag :text="row.statusText" :tone="row.cls" />
              </template>
            </el-table-column>
          </el-table>
        </SectionCard>
      </el-col>
      <el-col :md="9">
        <SectionCard title="质量快照">
          <div class="quality">
            <div class="quality__rate">
              <div class="quality__num mes-num">{{ quality.passRate }}<span>%</span></div>
              <div class="quality__label">一次通过率</div>
            </div>
            <div class="quality__list">
              <div class="quality__row" v-for="q in quality.items" :key="q.label">
                <StatusTag :text="q.label" :tone="q.cls" />
                <span class="mes-num quality__val">{{ q.value }}</span>
              </div>
            </div>
          </div>
        </SectionCard>
      </el-col>
    </el-row>

    <!-- 低库存 + 快捷入口 -->
    <el-row :gutter="20" class="grid-2">
      <el-col :md="12">
        <SectionCard title="低库存预警">
          <template #extra><el-button link type="primary" @click="$router.push('/inventory/stock')">去处理</el-button></template>
          <el-table :data="lowStocks" style="width: 100%">
            <el-table-column prop="materialName" label="物料" min-width="140" show-overflow-tooltip />
            <el-table-column prop="warehouseName" label="仓库" min-width="100" />
            <el-table-column label="现存量" width="90" align="right">
              <template #default="{ row }">
                <span class="low-qty mes-num">{{ fmtNum(row.qty) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </SectionCard>
      </el-col>
      <el-col :md="12">
        <SectionCard title="快捷入口">
          <div class="quick-grid">
            <div v-for="q in quickLinks" :key="q.path" class="quick-item" @click="$router.push(q.path)">
              <el-icon :size="22"><component :is="q.icon" /></el-icon>
              <span>{{ q.label }}</span>
            </div>
          </div>
        </SectionCard>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { Plus, Document, Loading, Box, CircleCheck, Tickets, Camera, Checked, ChatDotRound, Setting, Ship } from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'
import SectionCard from '@/components/SectionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fmtNum } from '@/utils/format'
import { useUserStore } from '@/stores/user'
import { getOrderPage } from '@/api/trade'
import { getWorkOrderPage } from '@/api/production'
import { getStockPage } from '@/api/inventory'
import { getInspectionPage } from '@/api/quality'

const userStore = useUserStore()
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })

const stats = ref([
  { label: '待处理订单', value: 0, icon: 'Document', delta: '本周 +12%', deltaType: 'up' as const },
  { label: '进行中工单', value: 0, icon: 'Loading', delta: '产能 86%', deltaType: '' as const },
  { label: '库存物料(SKU)', value: 0, icon: 'Box', delta: '低库存预警', deltaType: 'down' as const },
  { label: '质检批次', value: 0, icon: 'CircleCheck', delta: '一次通过率', deltaType: 'up' as const },
])

const recentOrders = ref<any[]>([])
const lowStocks = ref<any[]>([])
const quickLinks = [
  { label: '新增订单', path: '/trade/order', icon: Document },
  { label: '生产工单', path: '/production/work-order', icon: Tickets },
  { label: '扫码报工', path: '/production/scan', icon: Camera },
  { label: '验货质检', path: '/quality/inspection', icon: Checked },
  { label: '库存管理', path: '/inventory/stock', icon: Box },
  { label: '设备台账', path: '/equipment', icon: Setting },
  { label: '贸易订单', path: '/trade/order', icon: Ship },
  { label: 'AI 助手', path: '/ai/assistant', icon: ChatDotRound },
]

const quality = ref({
  passRate: 0,
  items: [
    { label: '已检批次', value: 0, cls: 'ok' as const },
    { label: '待检', value: 0, cls: 'warn' as const },
    { label: '不合格', value: 0, cls: 'danger' as const },
  ],
})

const trendEl = ref<HTMLElement>()
const donutEl = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let donutChart: echarts.ECharts | null = null

function textColor() {
  return document.documentElement.classList.contains('dark') ? '#9aa6ba' : '#69778f'
}

function renderCharts(trend: number[], plan: number[], months: string[], donutData: { value: number; name: string; color: string }[]) {
  const tc = textColor()
  if (trendEl.value) {
    trendChart = echarts.init(trendEl.value)
    trendChart.setOption({
      grid: { left: 44, right: 16, top: 28, bottom: 28 },
      tooltip: { trigger: 'axis' },
      legend: { top: 0, textStyle: { color: tc } },
      xAxis: {
        type: 'category',
        data: months,
        axisLine: { lineStyle: { color: tc } },
        axisLabel: { color: tc },
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: 'rgba(150,160,180,0.18)' } },
        axisLabel: { color: tc },
      },
      series: [
        {
          name: '计划产量',
          type: 'bar',
          barWidth: 16,
          data: plan,
          itemStyle: { color: 'rgba(77,156,255,0.28)', borderRadius: [6, 6, 0, 0] },
        },
        {
          name: '实际产量',
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 7,
          data: trend,
          lineStyle: { width: 3, color: '#4d9cff' },
          itemStyle: { color: '#4d9cff' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(77,156,255,0.32)' },
              { offset: 1, color: 'rgba(77,156,255,0.02)' },
            ]),
          },
        },
      ],
    })
  }
  if (donutEl.value) {
    donutChart = echarts.init(donutEl.value)
    donutChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, textStyle: { color: tc } },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['52%', '74%'],
          center: ['50%', '44%'],
          avoidLabelOverlap: true,
          itemStyle: { borderColor: 'var(--mes-surface)', borderWidth: 2, borderRadius: 4 },
          label: { show: false },
          data: donutData,
        },
      ],
    })
  }
}

function onResize() {
  trendChart?.resize()
  donutChart?.resize()
}

const observer = new MutationObserver(() => {
  trendChart?.dispose()
  donutChart?.dispose()
  trendChart = null
  donutChart = null
  renderCharts(trendCache.trend, trendCache.plan, trendCache.months, trendCache.donut)
})

const trendCache = { trend: [] as number[], plan: [] as number[], months: [] as string[], donut: [] as any[] }

onMounted(async () => {
  let allOrders: any[] = []
  let allWorks: any[] = []
  let allStocks: any[] = []
  let allQc: any[] = []
  try {
    const [orders, works, stocks, woPage, qcPage] = await Promise.all([
      getOrderPage({ pageNum: 1, pageSize: 100 }),
      getWorkOrderPage({ pageNum: 1, pageSize: 100, status: 'IN_PRODUCTION' }),
      getStockPage({ pageNum: 1, pageSize: 100 }),
      getWorkOrderPage({ pageNum: 1, pageSize: 6 }),
      getInspectionPage({ pageNum: 1, pageSize: 100 }),
    ])
    allOrders = (orders as any).records || []
    allWorks = (works as any).records || []
    allStocks = (stocks as any).records || []
    allQc = (qcPage as any).records || []

    stats.value[0].value = allOrders.filter((o) => o.status === 'PENDING' || o.status === 'DRAFT').length
    stats.value[1].value = (works as any).total ?? 0
    stats.value[2].value = (stocks as any).total ?? 0
    stats.value[3].value = allQc.length

    // 近期生产工单（真实数据）
    const woRecords = (woPage as any).records || []
    const statusMap: Record<string, { text: string; cls: any }> = {
      PLANNED: { text: '待执行', cls: 'info' },
      IN_PRODUCTION: { text: '生产中', cls: 'thread' },
      COMPLETED: { text: '已完成', cls: 'ok' },
    }
    recentOrders.value = woRecords.map((w: any) => {
      const plan = Number(w.planQty) || 0
      const fin = Number(w.finishQty) || 0
      const progress = plan ? Math.min(100, Math.round((fin / plan) * 100)) : 0
      const s = statusMap[w.status] || { text: w.status, cls: 'info' }
      return { no: w.workOrderNo, product: w.productName || '—', progress, statusText: s.text, cls: s.cls }
    })

    // 低库存预警
    lowStocks.value = allStocks
      .filter((s: any) => Number(s.qty) < 100)
      .sort((a: any, b: any) => Number(a.qty) - Number(b.qty))
      .slice(0, 6)

    // 质量快照（真实统计）
    let pass = 0, fail = 0, pending = 0
    allQc.forEach((q: any) => {
      if (q.result === 'PASS') pass++
      else if (q.result === 'FAIL') fail++
      else pending++
    })
    const totalQc = allQc.length || 1
    quality.value.passRate = Math.round((pass / totalQc) * 100)
    quality.value.items[0].value = pass + fail + pending
    quality.value.items[1].value = pending
    quality.value.items[2].value = fail

    // 产量趋势：按工单开始月份聚合计划/实际
    const allWorksFull: any[] = await (await getWorkOrderPage({ pageNum: 1, pageSize: 100 })).records || []
    const monthMap = new Map<string, { plan: number; finish: number }>()
    allWorksFull.forEach((w: any) => {
      if (!w.startDate) return
      const d = new Date(w.startDate)
      if (isNaN(d.getTime())) return
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
      const cur = monthMap.get(key) || { plan: 0, finish: 0 }
      cur.plan += Number(w.planQty) || 0
      cur.finish += Number(w.finishQty) || 0
      monthMap.set(key, cur)
    })
    const months = [...monthMap.keys()].sort()
    trendCache.months = months.map((m) => `${Number(m.split('-')[1])}月`)
    trendCache.plan = months.map((m) => Math.round(monthMap.get(m)!.plan))
    trendCache.trend = months.map((m) => Math.round(monthMap.get(m)!.finish))

    // 订单状态分布
    const statusColor: Record<string, string> = {
      DRAFT: '#9aa6b8', PENDING: '#c8a24a', IN_PRODUCTION: '#4d9cff', QC: '#7a5cd6', SHIPPED: '#1fa971', COMPLETED: '#2c8a5a',
    }
    const statusName: Record<string, string> = {
      DRAFT: '草稿', PENDING: '待生产', IN_PRODUCTION: '生产中', QC: '质检中', SHIPPED: '已出货', COMPLETED: '已完成',
    }
    const counts = new Map<string, number>()
    allOrders.forEach((o: any) => counts.set(o.status, (counts.get(o.status) || 0) + 1))
    trendCache.donut = [...counts.entries()].map(([k, v]) => ({
      value: v, name: statusName[k] || k, color: statusColor[k] || '#9aa6b8',
      itemStyle: { color: statusColor[k] || '#9aa6b8' },
    }))
  } catch (e) {
    // 后端未连接时保留空数据
  }

  renderCharts(trendCache.trend, trendCache.plan, trendCache.months, trendCache.donut)
  window.addEventListener('resize', onResize)
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  observer.disconnect()
  trendChart?.dispose()
  donutChart?.dispose()
})
</script>

<style scoped lang="scss">
.stat-row { margin-bottom: 20px; }
.grid-2 { margin-bottom: 20px; }
.head-actions { display: flex; align-items: center; gap: 12px; }

.chart { width: 100%; }
.chart--trend { height: 280px; }
.chart--donut { height: 280px; }

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

.quality { display: flex; gap: 24px; align-items: center; }
.quality__rate { text-align: center; flex-shrink: 0; }
.quality__num {
  font-family: var(--mes-font-data);
  font-size: 44px; font-weight: 700; color: var(--mes-ink); line-height: 1;
}
.quality__num span { font-size: 20px; color: var(--mes-slate); margin-left: 2px; }
.quality__label { font-size: 12px; color: var(--mes-slate); margin-top: 8px; }
.quality__list { flex: 1; display: flex; flex-direction: column; gap: 14px; }
.quality__row { display: flex; align-items: center; justify-content: space-between; }
.quality__val { font-size: 16px; font-weight: 600; color: var(--mes-ink); }

.low-qty { color: var(--mes-danger); font-weight: 700; }

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 8px;
  border-radius: var(--mes-r-sm);
  border: 1px solid var(--mes-line);
  background: var(--mes-surface-2);
  color: var(--mes-slate);
  cursor: pointer;
  transition: all var(--mes-dur) var(--mes-ease);
  font-size: 13px;
}
.quick-item:hover {
  border-color: var(--mes-thread);
  color: var(--mes-thread-deep);
  background: var(--mes-thread-soft);
  transform: translateY(-2px);
  box-shadow: var(--mes-shadow-sm);
}
</style>
