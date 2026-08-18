<template>
  <div class="scan-input">
    <el-input
      v-model="value"
      :placeholder="placeholder || '扫码或手动输入'"
      clearable
      @keyup.enter="submit"
      @clear="$emit('scan', null)"
    >
      <template #append>
        <el-button :icon="Camera" @click="openScanner">扫码</el-button>
      </template>
    </el-input>

    <el-dialog v-model="cameraVisible" title="摄像头扫码" width="480px" destroy-on-close>
      <div class="scan-stage">
        <div id="qr-reader" class="qr-reader" />
        <p v-if="cameraError" class="scan-hint danger">{{ cameraError }}</p>
        <p v-else class="scan-hint">将二维码 / 条码对准取景框，自动识别</p>
      </div>
      <template #footer>
        <el-button @click="closeCamera">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { Html5Qrcode } from 'html5-qrcode'
import { getWechatJsSdk } from '@/api/wechat'

declare global {
  interface Window {
    wx?: any
  }
}

const props = defineProps<{
  modelValue?: string
  placeholder?: string
  scanType?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'scan', payload: { barcode: string } | null): void
}>()

const value = ref(props.modelValue || '')
watch(() => props.modelValue, (v) => { value.value = v || '' })
watch(value, (v) => emit('update:modelValue', v))

const cameraVisible = ref(false)
const cameraError = ref('')
let scanner: Html5Qrcode | null = null
const SCANNER_ID = 'qr-reader'
const isWeChat = /MicroMessenger/i.test(navigator.userAgent)

function submit() {
  const v = (value.value || '').trim()
  if (!v) return
  emit('scan', { barcode: v })
}

async function openCamera() {
  cameraError.value = ''
  cameraVisible.value = true
  await nextTick()
  try {
    if (!navigator.mediaDevices?.getUserMedia) {
      cameraError.value = '当前浏览器不支持摄像头，请使用 USB 扫码枪或手动输入'
      return
    }
    scanner = new Html5Qrcode(SCANNER_ID)
    await scanner.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 240, height: 240 } },
      (decodedText) => {
        value.value = decodedText
        ElMessage.success(`识别成功：${decodedText}`)
        emit('scan', { barcode: decodedText })
        closeCamera()
      },
      () => { /* 持续扫描中，忽略单帧失败 */ }
    )
  } catch (e: any) {
    cameraError.value = `摄像头启动失败：${e?.message || e}。请确认浏览器已授权摄像头权限，或改用 USB 扫码枪 / 手动输入。`
    scanner = null
  }
}

async function openScanner() {
  if (isWeChat) {
    cameraVisible.value = true
    await scanWithWechat()
  } else {
    await openCamera()
  }
}

/** 微信内置浏览器：走 JS-SDK wx.scanQRCode，未配置或失败时降级为摄像头扫码 */
async function scanWithWechat() {
  cameraError.value = ''
  try {
    await loadJweixin()
    const wxObj = window.wx
    const url = location.href.split('#')[0]
    const cfg = await getWechatJsSdk(url)
    if (!cfg.enabled || !cfg.appId || !cfg.signature) {
      cameraError.value = '微信扫码未配置公众号 appId/Secret，已降级为摄像头扫码'
      await openCamera()
      return
    }

    wxObj.config({
      debug: false,
      appId: cfg.appId,
      timestamp: cfg.timestamp,
      nonceStr: cfg.nonceStr,
      signature: cfg.signature,
      jsApiList: ['scanQRCode'],
    })

    let configFailed = false
    await new Promise<void>((resolve) => {
      wxObj.error(() => { configFailed = true; resolve() })
      wxObj.ready(resolve)
    })
    if (configFailed) throw new Error('微信 JSSDK 签名校验失败（请确认公众号 JS 接口安全域名已配置）')

    wxObj.scanQRCode({
      needResult: 1,
      scanType: ['qrCode', 'barCode'],
      success: (res: any) => {
        const raw = res?.resultStr || ''
        const barcode = raw.includes(',') ? raw.split(',')[1] : raw
        value.value = barcode
        ElMessage.success(`识别成功：${barcode}`)
        emit('scan', { barcode })
        closeCamera()
      },
      fail: (err: any) => {
        cameraError.value = `微信扫码失败：${err?.errMsg || '用户取消'}`
      },
    })
  } catch (e: any) {
    cameraError.value = `${e?.message || e}，已降级为摄像头扫码`
    await openCamera()
  }
}

let jweixinReady: Promise<void> | null = null
function loadJweixin(): Promise<void> {
  if (window.wx) return Promise.resolve()
  if (!jweixinReady) {
    jweixinReady = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script')
      script.src = 'https://res.wx.qq.com/open/js/jweixin-1.6.0.js'
      script.onload = () => resolve()
      script.onerror = () => {
        jweixinReady = null
        reject(new Error('加载微信 JSSDK 失败'))
      }
      document.head.appendChild(script)
    })
  }
  return jweixinReady
}

function closeCamera() {
  if (scanner) {
    scanner.stop().then(() => scanner?.clear()).catch(() => { /* ignore */ })
    scanner = null
  }
  cameraVisible.value = false
}

onUnmounted(() => closeCamera())

// 微任务延迟，确保 Dialog 渲染后再初始化 scanner
function nextTick() {
  return new Promise<void>((r) => setTimeout(r, 50))
}
</script>

<style scoped>
.scan-input { width: 100%; }
.qr-reader { width: 100%; min-height: 280px; background: #0e1426; border-radius: 8px; overflow: hidden; }
.qr-reader :deep(video) { width: 100% !important; height: auto !important; }
.scan-hint { text-align: center; color: var(--el-text-color-secondary); margin: 12px 0 0; font-size: 13px; }
.scan-hint.danger { color: var(--el-color-danger); }
</style>
