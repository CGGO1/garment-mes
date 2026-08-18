<template>
  <div class="page-container assistant-page">
    <PageHeader
      eyebrow="AI ASSISTANT"
      title="AI 助手"
      subtitle="工艺问答、单证理解、合规检索（由 Lili 知识库驱动）"
    />

    <div class="mes-card chat-card">
      <!-- 消息区 -->
      <div ref="msgBox" class="msg-box">
        <el-empty v-if="messages.length === 0" description="向我提问服装工艺、贸易术语、HS 编码、单证合规等问题" :image-size="90" />
        <div v-for="(msg, i) in messages" :key="i" class="msg-item" :class="msg.role">
          <div class="msg-avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="input-box">
        <el-input
          v-model="input"
          type="textarea"
          :rows="2"
          placeholder="输入问题，如：FOB 和 CIF 的区别是什么？"
          @keydown.enter.exact.prevent="send"
        />
        <div class="input-actions">
          <el-button type="primary" :loading="sending" :icon="Promotion" @click="send">发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { aiChat } from '@/api/ai'

const input = ref('')
const sending = ref(false)
const messages = ref<{ role: 'user' | 'ai'; content: string }[]>([])
const msgBox = ref<HTMLElement>()

async function send() {
  const text = input.value.trim()
  if (!text) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  sending.value = true
  await scrollBottom()
  try {
    const resp: any = await aiChat(text)
    let content = resp
    // 若返回 JSON 字符串，尝试提取 answer 字段
    try {
      const parsed = JSON.parse(resp)
      if (parsed.answer) content = parsed.answer
      else if (parsed.data?.answer) content = parsed.data.answer
      else if (parsed.msg) content = parsed.msg
    } catch {
      /* 非 JSON，保持原文 */
    }
    messages.value.push({ role: 'ai', content })
  } catch (e: any) {
    messages.value.push({ role: 'ai', content: e.message || 'AI 服务暂不可用' })
  } finally {
    sending.value = false
    await scrollBottom()
  }
}

async function scrollBottom() {
  await nextTick()
  if (msgBox.value) {
    msgBox.value.scrollTop = msgBox.value.scrollHeight
  }
}
</script>

<style scoped>
.assistant-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.msg-box {
  flex: 1;
  overflow-y: auto;
  padding: 8px 4px;
}

.msg-item {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.msg-item.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.msg-item.user .msg-avatar {
  background: var(--el-color-primary);
  color: #fff;
}

.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 10px;
  background: var(--mes-surface-2);
  color: var(--mes-ink);
  line-height: 1.7;
  white-space: pre-wrap;
  border: 1px solid var(--mes-line);
}

.msg-item.user .msg-bubble {
  background: var(--el-color-primary);
  color: #fff;
}

.input-box {
  border-top: 1px solid var(--mes-line);
  padding-top: 12px;
}

.input-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>
