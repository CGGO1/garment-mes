<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="产品管理"
      subtitle="维护款式、HS 编码、成分克重等基础信息"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增产品</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="款号 / 款式名称" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="productCode" label="款号" width="120" />
        <el-table-column prop="productName" label="款式名称" min-width="170">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.productName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="品类" width="90">
          <template #default="{ row }"><el-tag effect="light" size="small">{{ row.category }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="hsCode" label="HS 编码" width="120" />
        <el-table-column prop="composition" label="成分" min-width="130" show-overflow-tooltip />
        <el-table-column label="克重 GSM" width="100" align="right">
          <template #default="{ row }"><span class="mes-num">{{ fmtNum(row.gsm, 1) }}</span></template>
        </el-table-column>
        <el-table-column prop="width" label="门幅" width="80" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openQr(row)">二维码</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" background @current-change="load" @size-change="load" />
    </div>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="产品详情" size="520px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">款式信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="款号">{{ detail.productCode }}</el-descriptions-item>
            <el-descriptions-item label="款式名称">{{ detail.productName }}</el-descriptions-item>
            <el-descriptions-item label="品类">{{ detail.category || '—' }}</el-descriptions-item>
            <el-descriptions-item label="HS 编码">{{ detail.hsCode || '—' }}</el-descriptions-item>
            <el-descriptions-item label="成分">{{ detail.composition || '—' }}</el-descriptions-item>
            <el-descriptions-item label="克重 GSM">{{ fmtNum(detail.gsm, 1) }}</el-descriptions-item>
            <el-descriptions-item label="门幅">{{ detail.width || '—' }}</el-descriptions-item>
            <el-descriptions-item label="单位">{{ detail.unit || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-block qr-block">
          <h4 class="detail-block__title">二维码</h4>
          <QrCode ref="detailQrRef" :content="detail.productCode" :size="150" :label="detail.productCode" :filename="`product-${detail.productCode}.png`" />
          <el-button size="small" :icon="Download" @click="detailQrRef?.download()">下载二维码</el-button>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.productId ? '编辑产品' : '新增产品'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="款号"><el-input v-model="form.productCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="款式名称"><el-input v-model="form.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品类"><el-select v-model="form.category" style="width: 100%"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="HS 编码"><el-input v-model="form.hsCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="成分"><el-input v-model="form.composition" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="克重 GSM"><el-input-number v-model="form.gsm" :min="0" style="width: 100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="门幅"><el-input v-model="form.width" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <QrCodeDialog
      v-model="qrVisible"
      title="产品二维码"
      :content="qrContent"
      :description="qrDescription"
      :filename="`product-${qrContent}.png`"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Download } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import QrCodeDialog from '@/components/QrCodeDialog.vue'
import QrCode from '@/components/QrCode.vue'
import { fmtNum } from '@/utils/format'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '@/api/master'

const categories = ['上装', '下装', '外套', '内衣', '童装', '制服', '针织', '羽绒']

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const detailQrRef = ref<InstanceType<typeof QrCode> | null>(null)
const qrVisible = ref(false)
const qrRow = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive<any>({ productId: '', productCode: '', productName: '', category: '', hsCode: '', composition: '', gsm: 0, width: '', unit: '件', remark: '' })

const qrContent = computed(() => qrRow.value?.productCode || '')
const qrDescription = computed(() => qrRow.value ? `款号：${qrRow.value.productCode}（${qrRow.value.productName}）` : '')

async function load() {
  loading.value = true
  try {
    const data: any = await getProductPage(query)
    list.value = data.records
    total.value = Number(data.total)
  } finally {
    loading.value = false
  }
}

function openDetail(row: any) {
  detail.value = row
  detailVisible.value = true
}

function openDialog(row?: any) {
  Object.assign(form, row ? { ...row } : { productId: '', productCode: '', productName: '', category: '', hsCode: '', composition: '', gsm: 0, width: '', unit: '件', remark: '' })
  dialogVisible.value = true
}

function openQr(row: any) {
  qrRow.value = row
  qrVisible.value = true
}

async function save() {
  const isNew = !form.productId
  if (isNew) await createProduct({ ...form })
  else await updateProduct({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  if (isNew) {
    qrRow.value = { ...form }
    qrVisible.value = true
  }
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除产品「${row.productName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteProduct(row.productId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
.qr-block { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; }
</style>
