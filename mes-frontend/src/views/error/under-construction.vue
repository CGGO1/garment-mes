<template>
  <div class="uc">
    <div class="uc-card">
      <div class="uc-glyph">🛠</div>
      <h1 class="uc-title">页面建设中</h1>
      <p class="uc-desc">该模块正在开发中，后续版本开放。</p>
      <el-button type="primary" plain @click="router.push('/dashboard')">返回工作台</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped lang="scss">
.uc {
  height: 100%;
  display: grid;
  place-items: center;
  padding: 24px;
}
.uc-card {
  text-align: center;
  max-width: 380px;
}
.uc-glyph {
  font-size: 56px;
  filter: grayscale(0.1);
}
.uc-title {
  font-size: 22px;
  font-weight: 600;
  margin: 16px 0 8px;
  color: var(--mes-text-primary);
}
.uc-desc {
  color: var(--mes-text-secondary);
  margin: 0 0 24px;
  font-size: 14px;
}
</style>
