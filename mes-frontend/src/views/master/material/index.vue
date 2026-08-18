<template>
  <div class="page-container">
    <PageHeader
      eyebrow="MASTER DATA"
      title="物料管理"
      subtitle="维护面料、辅料（拉链/纽扣/标签）等物料信息"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增物料</el-button>
      </template>
    </PageHeader>

    <div class="mes-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="物料名称 / 编码" style="width: 240px" clearable :prefix-icon="Search" @keyup.enter="load" @clear="load" />
        <el-button type="primary" :icon="Search" @click="load">查询</el-button>
        <div class="spacer"></div>
        <el-button :icon="Refresh" @click="load">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" min-width="160">
          <template #default="{ row }">
            <span class="link-like" @click="openDetail(row)">{{ row.materialName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.materialType === '面料' ? 'primary' : 'success'" effect="light" size="small">{{ row.materialType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="100" />
        <el-table-column prop="spec" label="规格" min-width="130" show-overflow-tooltip />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="操作" width="160" fixed="right">
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
    </div>

    <el-drawer v-model="detailVisible" title="物料详情" size="500px">
      <template v-if="detail">
        <div class="detail-block">
          <h4 class="detail-block__title">物料信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="物料编码">{{ detail.materialCode }}</el-descriptions-item>
            <el-descriptions-item label="物料名称">{{ detail.materialName }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ detail.materialType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="颜色">{{ detail.color || '—' }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ detail.spec || '—' }}</el-descriptions-item>
            <el-descriptions-item label="单位">{{ detail.unit || '—' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </template>
    </el-drawer>

    <el-dialog v-model="dialogVisible" :title="form.materialId ? '编辑物料' : '新增物料'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="物料编码"><el-input v-model="form.materialCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="物料名称"><el-input v-model="form.materialName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.materialType" style="width: 100%"><el-option v-for="t in materialTypes" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="颜色"><el-input v-model="form.color" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><el-input v-model="form.unit" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
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
import { getMaterialPage, createMaterial, updateMaterial, deleteMaterial } from '@/api/master'

const materialTypes = ['面料', '辅料', '里料', '填充物', '印花料']

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const detail = ref<any>(null)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ materialId: '', materialCode: '', materialName: '', materialType: '面料', color: '', spec: '', unit: '米', remark: '' })

async function load() {
  loading.value = true
  try {
    const data: any = await getMaterialPage(query)
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
  Object.assign(form, row ? { ...row } : { materialId: '', materialCode: '', materialName: '', materialType: '面料', color: '', spec: '', unit: '米', remark: '' })
  dialogVisible.value = true
}

async function save() {
  if (form.materialId) await updateMaterial({ ...form })
  else await createMaterial({ ...form })
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row: any) {
  await ElMessageBox.confirm(`确认删除物料「${row.materialName}」？`, '删除确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
  await deleteMaterial(row.materialId)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.link-like { color: var(--mes-thread-deep); font-weight: 600; cursor: pointer; }
.link-like:hover { text-decoration: underline; }
</style>
