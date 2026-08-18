<template>
  <div class="page-container">
    <PageHeader
      eyebrow="SYSTEM ADMIN"
      title="字典管理"
      subtitle="维护系统数据字典（贸易术语、AQL 等级等）"
    />

    <el-row :gutter="16">
      <!-- 字典类型 -->
      <el-col :span="10">
        <div class="mes-card">
          <div class="toolbar">
            <strong class="panel-title">字典类型</strong>
            <div class="spacer"></div>
            <el-button type="primary" size="small" :icon="Plus" @click="openTypeDialog()">新增</el-button>
          </div>
          <el-table :data="types" v-loading="typeLoading" highlight-current-row @current-change="selectType">
            <el-table-column prop="dictName" label="名称" min-width="100" />
            <el-table-column prop="dictType" label="编码" min-width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTypeDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeType(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 字典数据 -->
      <el-col :span="14">
        <div class="mes-card">
          <div class="toolbar">
            <strong class="panel-title">字典数据{{ currentType ? `（${currentType.dictName}）` : '' }}</strong>
            <div class="spacer"></div>
            <el-button type="primary" size="small" :icon="Plus" :disabled="!currentType" @click="openDataDialog()">新增</el-button>
          </div>
          <el-table :data="datas" v-loading="dataLoading">
            <el-table-column prop="dictLabel" label="标签" min-width="120" />
            <el-table-column prop="dictValue" label="值" min-width="120" />
            <el-table-column prop="dictSort" label="排序" width="80" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDataDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeData(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
    </el-row>

    <!-- 类型对话框 -->
    <el-dialog v-model="typeDialogVisible" :title="typeForm.dictId ? '编辑字典类型' : '新增字典类型'" width="420px">
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="typeForm.dictType" placeholder="如 incoterms" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 数据对话框 -->
    <el-dialog v-model="dataDialogVisible" :title="dataForm.dictCode ? '编辑字典数据' : '新增字典数据'" width="420px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="标签"><el-input v-model="dataForm.dictLabel" /></el-form-item>
        <el-form-item label="值"><el-input v-model="dataForm.dictValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.dictSort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import {
  getDictTypePage, createDictType, updateDictType, deleteDictType,
  getDictData, createDictData, updateDictData, deleteDictData,
} from '@/api/system'

const types = ref<any[]>([])
const datas = ref<any[]>([])
const typeLoading = ref(false)
const dataLoading = ref(false)
const currentType = ref<any>(null)
const typeDialogVisible = ref(false)
const dataDialogVisible = ref(false)

const typeForm = reactive({ dictId: '', dictName: '', dictType: '', remark: '' })
const dataForm = reactive({ dictCode: '', dictLabel: '', dictValue: '', dictSort: 0, dictType: '' })

async function loadTypes() {
  typeLoading.value = true
  try {
    const data: any = await getDictTypePage({ pageNum: 1, pageSize: 100 })
    types.value = data.records
  } finally {
    typeLoading.value = false
  }
}

async function selectType(row: any) {
  currentType.value = row
  await loadData(row.dictType)
}

async function loadData(dictType: string) {
  dataLoading.value = true
  try {
    datas.value = await getDictData(dictType)
  } finally {
    dataLoading.value = false
  }
}

function openTypeDialog(row?: any) {
  Object.assign(typeForm, {
    dictId: row?.dictId || '',
    dictName: row?.dictName || '',
    dictType: row?.dictType || '',
    remark: row?.remark || '',
  })
  typeDialogVisible.value = true
}

async function saveType() {
  if (typeForm.dictId) {
    await updateDictType({ ...typeForm })
  } else {
    await createDictType({ ...typeForm })
  }
  ElMessage.success('保存成功')
  typeDialogVisible.value = false
  loadTypes()
}

async function removeType(row: any) {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」？`, '提示', { type: 'warning' })
  await deleteDictType(row.dictId)
  ElMessage.success('删除成功')
  loadTypes()
}

function openDataDialog(row?: any) {
  Object.assign(dataForm, {
    dictCode: row?.dictCode || '',
    dictLabel: row?.dictLabel || '',
    dictValue: row?.dictValue || '',
    dictSort: row?.dictSort || 0,
    dictType: currentType.value.dictType,
  })
  dataDialogVisible.value = true
}

async function saveData() {
  if (dataForm.dictCode) {
    await updateDictData({ ...dataForm })
  } else {
    await createDictData({ ...dataForm })
  }
  ElMessage.success('保存成功')
  dataDialogVisible.value = false
  loadData(currentType.value.dictType)
}

async function removeData(row: any) {
  await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」？`, '提示', { type: 'warning' })
  await deleteDictData(row.dictCode)
  ElMessage.success('删除成功')
  loadData(currentType.value.dictType)
}

onMounted(loadTypes)
</script>

<style scoped>
.panel-title {
  color: var(--mes-ink);
  font-size: 14px;
}
</style>
