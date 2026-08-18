<template>
  <div class="qr-code" :style="{ width: size + 'px' }">
    <canvas ref="canvasRef" :style="{ width: size + 'px', height: size + 'px' }" />
    <p v-if="label" class="qr-code__label">{{ label }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import QRCode from 'qrcode'

const props = withDefaults(defineProps<{
  content: string
  size?: number
  label?: string
  filename?: string
}>(), {
  size: 200,
  filename: 'mes-qrcode.png',
})

const canvasRef = ref<HTMLCanvasElement | null>(null)

async function render() {
  if (!canvasRef.value || !props.content) return
  await QRCode.toCanvas(canvasRef.value, props.content, {
    width: props.size,
    margin: 1,
    errorCorrectionLevel: 'M',
    color: { dark: '#0e1426', light: '#ffffff' },
  })
}

function download() {
  const canvas = canvasRef.value
  if (!canvas) return
  const link = document.createElement('a')
  link.download = props.filename
  link.href = canvas.toDataURL('image/png')
  link.click()
}

onMounted(render)
watch(() => props.content, render)

defineExpose({ download })
</script>

<style scoped>
.qr-code {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.qr-code canvas {
  display: block;
  border: 1px solid var(--mes-line, #e5e7eb);
  border-radius: 8px;
  background: #fff;
}
.qr-code__label {
  margin: 0;
  font-size: 12px;
  color: var(--mes-slate, #64748b);
  text-align: center;
  word-break: break-all;
}
</style>
