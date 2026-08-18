<template>
  <el-dialog
    :model-value="modelValue"
    :title="title || '二维码'"
    width="420px"
    :close-on-click-modal="false"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="qr-dialog">
      <p v-if="description" class="qr-dialog__desc">{{ description }}</p>
      <QrCode ref="qrRef" :content="content" :size="220" :label="content" :filename="filename" />
      <p class="qr-dialog__hint">使用微信「扫一扫」或本系统扫码登记，即可识别该条码</p>
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" :icon="Download" @click="download">下载 PNG</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Download } from '@element-plus/icons-vue'
import QrCode from '@/components/QrCode.vue'

defineProps<{
  modelValue: boolean
  title?: string
  description?: string
  content: string
  filename?: string
}>()

defineEmits<{ (e: 'update:modelValue', v: boolean): void }>()

const qrRef = ref<InstanceType<typeof QrCode> | null>(null)

function download() {
  qrRef.value?.download()
}
</script>

<style scoped>
.qr-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 8px 0 4px;
}
.qr-dialog__desc {
  margin: 0;
  font-size: 14px;
  color: var(--mes-ink, #1f2937);
}
.qr-dialog__hint {
  margin: 0;
  font-size: 12px;
  color: var(--mes-mist, #9ca3af);
  text-align: center;
}
</style>
