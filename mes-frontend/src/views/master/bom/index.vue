<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="产品 BOM"
      subtitle="维护产品物料清单（面料/辅料用量），支持多版本管理"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增 BOM</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="产品款号 / 名称" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="productName" label="产品" min-width="200">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.productName || row.productId }}</span>
          </template>
        </el-table-column>
        <el-table-column label="版本" width="90">
          <template #default="{ row }"><el-tag effect="light" size="small">{{ row.version }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">物料明细</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" background @current-change="load" @size-change="load" />
    </div>

    <!-- 物料明细抽屉 -->
    <el-drawer v-model="detailVisible" title="BOM 物料明细" size="620px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">BOM 信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="产品">{{ detail.productName || detail.productId }}</el-descriptions-item>
            <el-descriptions-item label="版本">{{ detail.version }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-block">
          <h4 class="detail-block__title">物料清单（{{ detail.items?.length || 0 }}）</h4>
          <el-table :data="detail.items || []" border size="small">
            <el-table-column label="物料" min-width="180">
              <template #default="{ row }">{{ row.materialName || row.materialId }}</template>
            </el-table-column>
            <el-table-column label="单耗" width="110" align="right">
              <template #default="{ row }"><span class="cell-qty">{{ fmtNum(row.qty, 2) }}</span></template>
            </el-table-column>
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="remark" label="备注" min-width="120" />
          </el-table>
        </div>
      </template>
    </el-drawer>

    <!-- BOM 编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.bomId ? '编辑 BOM' : '新增 BOM'" width="760px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="产品"><el-select v-model="form.productId" filterable style="width: 100%"><el-option v-for="p in products" :key="p.productId" :label="`${p.productCode} ${p.productName}`" :value="p.productId" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="版本"><el-input v-model="form.version" placeholder="如 V1.0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">物料清单</el-divider>
        <el-table :data="form.items" border size="small">
          <el-table-column label="物料" min-width="220">
            <template #default="{ row }">
              <el-select v-model="row.materialId" filterable style="width: 100%">
                <el-option v-for="m in materials" :key="m.materialId" :label="`${m.materialCode} ${m.materialName}`" :value="m.materialId" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="单耗" width="140"><template #default="{ row }"><el-input-number v-model="row.qty" :min="0" :precision="2" style="width: 100%" /></template></el-table-column>
          <el-table-column label="单位" width="100"><template #default="{ row }"><el-input v-model="row.unit" /></template></el-table-column>
          <el-table-column label="操作" width="70"><template #default="{ $index }"><el-button link type="danger" @click="form.items.splice($index, 1)">删除</el-button></template></el-table-column>
        </el-table>
        <el-button class="add-btn" type="primary" plain :icon="Plus" @click="form.items.push({ materialId: '', qty: 1, unit: '米', remark: '' })">添加物料</el-button>
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
import { fmtNum } from '@/utils/format'
import { getBomPage, getBomDetail, createBom, updateBom, deleteBom, createBomItem, deleteBomItem, getProductList, getMaterialList } from '@/api/master'

const loading = ref(false)
const list = ref<any[]>([])
const products = ref<any[]>([])
const materials = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive<any>({ bomId: '', productId: '', version: 'V1.0', remark: '', items: [] })

async function load() {
  loading.value = true
  try {
    const data: any = await getBomPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

async function openDetail(row: any) {
  detail.value = await getBomDetail(row.bomId)
  detailVisible.value = true
}

async function openDialog(row?: any) {
  if (row) {
    const source = await getBomDetail(row.bomId)
    Object.assign(form, { ...source, items: (source.items || []).map((i: any) => ({ ...i })) })
  } else {
    Object.assign(form, { bomId: '', productId: '', version: 'V1.0', remark: '', items: [] })
  }
  dialogVisible.value = true
}

async function save() {
  const payload = { ...form }
  delete payload.items
  const isNew = !form.bomId
  const bom = isNew ? await createBom(payload) : await updateBom(payload)
  if (isNew) {
    // 新增后逐条写入物料明细（演示简化：服务端分批保存）
    const bomId = (bom as any)?.bomId
    for (const item of form.items) {
      if (item.materialId) await createBomItem({ bomId, materialId: item.materialId, qty: item.qty, unit: item.unit, remark: item.remark || '' })
    }
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除 BOM「${row.productName || row.productId} ${row.version}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteBom(row.bomId)
  ElMessage.success('删除成功')
  load()
}

onMounted(async () => {
  load()
  products.value = await getProductList()
  materials.value = await getMaterialList()
})
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.add-btn { margin-top: 12px; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
</style>
