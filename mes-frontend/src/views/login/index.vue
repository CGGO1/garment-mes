<template>
  <div class="login">
    <!-- 品牌区：深墨蓝 + 织纹 + 生产流线描 -->
    <aside class="login__brand mes-weave">
      <div class="brand-top">
        <img class="brand-mark" src="/favicon.png" alt="Garment MES" />
        <span class="brand-name">Garment MES · 服装智造</span>
      </div>

      <div class="brand-body">
        <p class="brand-eyebrow">GARMENT · MANUFACTURING · EXECUTION</p>
        <h1 class="brand-title">把每一道工序<br />织进数据里</h1>
        <p class="brand-desc">
          覆盖外贸订单、生产执行、库存与质检全链路，<br />
          融合 AI 即时报表与知识库问答，驱动服装制造数字化。
        </p>

        <!-- 签名插画：裁床叠层 / 生产流 -->
        <svg class="brand-art" viewBox="0 0 320 180" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <line x1="20" y1="20" x2="20" y2="160" stroke="#4d9cff" stroke-width="2" stroke-linecap="round" />
          <text x="32" y="26" fill="#cdd6e6" font-size="11" font-family="Space Grotesk, sans-serif">LAYERS</text>
          <rect x="40" y="40" width="240" height="20" rx="6" fill="#ffffff" fill-opacity="0.10" />
          <rect x="40" y="70" width="200" height="20" rx="6" fill="#ffffff" fill-opacity="0.14" />
          <rect x="40" y="100" width="260" height="20" rx="6" fill="#ffffff" fill-opacity="0.10" />
          <rect x="40" y="130" width="170" height="20" rx="6" fill="#4d9cff" fill-opacity="0.30" />
          <circle cx="280" cy="50" r="5" fill="#4d9cff" />
          <circle cx="240" cy="80" r="5" fill="#4d9cff" />
          <circle cx="300" cy="110" r="5" fill="#4d9cff" />
          <circle cx="210" cy="140" r="5" fill="#4d9cff" />
          <path d="M280 50 L240 80 L300 110 L210 140" stroke="#4d9cff" stroke-width="1.5" stroke-opacity="0.6" />
        </svg>

        <div class="brand-tags">
          <span>生产执行</span>
          <span>进出口贸易</span>
          <span>智能质检</span>
          <span>AI 报表</span>
        </div>
      </div>
    </aside>

    <!-- 登录区 -->
    <main class="login__form">
      <div class="form-card">
        <p class="form-eyebrow">欢迎回来</p>
        <h2 class="form-title">登录工作台</h2>
        <p class="form-sub">使用企业账号进入 Garment MES · 服装智造</p>

        <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="form-foot">
          <span class="status-chip info"><span class="status-chip__swatch"></span>默认账号</span>
          <span class="mes-num">admin / admin123</span>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    await userStore.fetchInfo()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login {
  height: 100vh;
  display: flex;
  background: var(--mes-porcelain);
}

/* ===== 品牌区 ===== */
.login__brand {
  flex: 1.05;
  color: #fff;
  display: flex;
  flex-direction: column;
  padding: 44px 56px;
  position: relative;
  overflow: hidden;
}
.login__brand::after {
  /* 柔光晕 */
  content: '';
  position: absolute;
  width: 420px; height: 420px; border-radius: 50%;
  background: radial-gradient(circle, rgba(77, 156, 255, 0.22), transparent 70%);
  top: -120px; right: -120px;
}

.brand-top { display: flex; align-items: center; gap: 12px; position: relative; z-index: 1; }
.brand-mark {
  width: 42px; height: 42px;
  object-fit: contain;
  display: inline-block;
  position: relative;
}
.brand-name { font-family: var(--mes-font-display); font-size: 19px; font-weight: 600; }

.brand-body { position: relative; z-index: 1; margin: auto 0; max-width: 460px; }
.brand-eyebrow {
  font-family: var(--mes-font-data);
  font-size: 11px; letter-spacing: 0.22em; color: #8ea2c4; margin: 0 0 18px;
}
.brand-title {
  font-family: var(--mes-font-display);
  font-size: 40px; line-height: 1.25; font-weight: 700; margin: 0 0 18px;
}
.brand-desc { font-size: 15px; line-height: 1.85; color: #c2cde0; margin: 0 0 30px; }
.brand-art { width: 100%; max-width: 340px; margin-bottom: 30px; }
.brand-tags { display: flex; flex-wrap: wrap; gap: 10px; }
.brand-tags span {
  padding: 7px 16px; border-radius: 20px; font-size: 13px;
  background: rgba(255, 255, 255, 0.10); border: 1px solid rgba(255, 255, 255, 0.14);
  color: #dbe3f1;
}

/* ===== 登录区 ===== */
.login__form {
  flex: 1;
  display: flex; align-items: center; justify-content: center;
}
.form-card { width: 380px; }
.form-eyebrow {
  font-family: var(--mes-font-data);
  font-size: 11px; letter-spacing: 0.18em; color: var(--mes-thread-deep);
  font-weight: 600; margin: 0 0 8px;
}
.form-title {
  font-family: var(--mes-font-display);
  font-size: 30px; font-weight: 700; color: var(--mes-ink); margin: 0 0 6px;
}
.form-sub { color: var(--mes-slate); font-size: 14px; margin: 0 0 34px; }

.login-btn { width: 100%; height: 46px; font-size: 16px; letter-spacing: 4px; border-radius: 11px; }

.form-foot {
  display: flex; align-items: center; gap: 10px;
  margin-top: 20px; padding-top: 18px; border-top: 1px solid var(--mes-line);
  font-size: 13px; color: var(--mes-slate);
}

@media (max-width: 860px) {
  .login__brand { display: none; }
}
</style>
