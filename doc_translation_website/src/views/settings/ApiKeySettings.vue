<template>
  <div class="apikey-settings">
    <!-- Header -->
    <div class="section-header">
      <h2 class="section-title">OpenRouter API Key</h2>
      <p class="section-desc">配置自带 Key 后翻译消耗由您的账号承担，不消耗平台免费额度</p>
    </div>

    <div class="apikey-body">
      <!-- Status banner -->
      <div :class="['status-banner', hasKey ? 'status-ok' : 'status-empty']">
        <div class="status-banner-left">
          <div class="status-icon-wrap">
            <svg v-if="hasKey" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2">
              <path
                  d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
            <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path
                  d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
            </svg>
          </div>
          <div>
            <div class="status-banner-title">{{ hasKey ? 'API Key 已配置' : '未配置 API Key' }}</div>
            <div class="status-banner-desc" v-if="hasKey">
              当前密钥：<code class="key-mask">sk-or-••••••••••••7f2a</code>
              &nbsp;·&nbsp;使用自带 Key 翻译
            </div>
            <div class="status-banner-desc" v-else
            ">
            当前使用平台免费额度（{{ remainingQuota.toLocaleString() }} tokens 剩余）
          </div>
        </div>
      </div>
      <el-button
          v-if="hasKey"
          size="small"
          type="danger"
          plain
          :loading="deleting"
          @click="deleteKey"
          style="flex-shrink:0"
      >
        清除 Key
      </el-button>
    </div>

    <!-- Input form -->
    <div class="form-card">
      <h3 class="form-title">{{ hasKey ? '更新 API Key' : '添加 API Key' }}</h3>

      <div class="input-group">
        <label class="input-label">API Key</label>
        <div class="input-row">
          <div class="input-wrap">
            <input
                v-model="form.apiKey"
                :type="showKey ? 'text' : 'password'"
                class="key-input"
                placeholder="sk-or-v1-xxxxxxxxxxxxxxxxxxxxxxxx"
                autocomplete="off"
                spellcheck="false"
            />
            <button class="eye-btn" type="button" @click="showKey = !showKey">
              <svg v-if="showKey" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
                <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
              <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
          <el-button :loading="verifying" :disabled="!form.apiKey" @click="verifyKey">
            验证
          </el-button>
          <el-button type="primary" :loading="saving" :disabled="!form.apiKey" @click="saveKey">
            保存
          </el-button>
        </div>
      </div>

      <!-- Verify result -->
      <Transition name="fade">
        <div v-if="verifyResult !== null" :class="['verify-result', verifyResult.valid ? 'result-ok' : 'result-fail']">
          <svg v-if="verifyResult.valid" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
          {{ verifyResult.message }}
        </div>
      </Transition>
    </div>

    <!-- How to get key -->
    <div class="guide-card">
      <div class="guide-header">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        <span>如何获取 OpenRouter API Key</span>
      </div>
      <ol class="guide-steps">
        <li>
          访问
          <a href="https://openrouter.ai" target="_blank" rel="noopener" class="guide-link">
            openrouter.ai
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
              <polyline points="15 3 21 3 21 9"/>
              <line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
          </a>
          注册并登录账号
        </li>
        <li>进入顶部导航 <strong>Keys</strong> 页面</li>
        <li>点击 <strong>Create Key</strong> 创建新密钥</li>
        <li>复制密钥并粘贴到上方输入框，点击保存</li>
      </ol>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { quotaApi } from '@/api/quota'

const authStore = useAuthStore()
const hasKey = computed(() => authStore.user?.hasOpenrouterKey || false)
const remainingQuota = computed(() => {
  const u = authStore.user
  return u ? (u.freeQuota ?? 0) - (u.usedQuota ?? 0) : 0
})

const form = ref({ apiKey: '' })
const showKey = ref(false)
const verifying = ref(false)
const saving = ref(false)
const deleting = ref(false)
const verifyResult = ref<{ valid: boolean; message: string } | null>(null)

async function verifyKey() {
  if (!form.value.apiKey) return
  verifying.value = true
  verifyResult.value = null
  try {
    verifyResult.value = await quotaApi.verifyApiKey(form.value.apiKey)
  } finally {
    verifying.value = false
  }
}

async function saveKey() {
  if (!form.value.apiKey) return
  saving.value = true
  try {
    await quotaApi.saveApiKey(form.value.apiKey)
    ElMessage.success('API Key 已保存')
    await authStore.fetchUser()
    form.value.apiKey = ''
    verifyResult.value = null
  } finally {
    saving.value = false
  }
}

async function deleteKey() {
  deleting.value = true
  try {
    await quotaApi.deleteApiKey()
    ElMessage.success('API Key 已清除')
    await authStore.fetchUser()
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.apikey-settings {
  height: 100%;
  overflow-y: auto;
}

.section-header {
  padding: 24px 28px 20px;
  border-bottom: 1px solid var(--border-color);
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.section-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.apikey-body {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Status banner */
.status-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 10px;
  border: 1px solid;
}

.status-ok {
  background: #F0FDF4;
  border-color: #BBF7D0;
}

.status-empty {
  background: var(--bg-page);
  border-color: var(--border-color);
}

.status-banner-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.status-icon-wrap {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #15803D;
}

.status-empty .status-icon-wrap {
  color: var(--text-muted);
}

.status-banner-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 3px;
}

.status-banner-desc {
  font-size: 12.5px;
  color: var(--text-secondary);
}

.key-mask {
  font-family: var(--font-mono);
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 7px;
  border-radius: 4px;
  font-size: 12px;
}

/* Form card */
.form-card {
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px;
}

.form-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
}

.input-label {
  display: block;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-wrap {
  position: relative;
  flex: 1;
}

.key-input {
  width: 100%;
  height: 36px;
  padding: 0 36px 0 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  font-size: 13px;
  font-family: var(--font-mono);
  color: var(--text-primary);
  background: #fff;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
  box-sizing: border-box;
}

.key-input:focus {
  border-color: var(--chinese-red);
  box-shadow: 0 0 0 3px #fdefee;
}

.key-input::placeholder {
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.eye-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted);
  padding: 2px;
  display: flex;
  align-items: center;
  transition: color 0.15s;
}

.eye-btn:hover {
  color: var(--text-secondary);
}

.verify-result {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.result-ok {
  background: #F0FDF4;
  color: #15803D;
}

.result-fail {
  background: #FEF2F2;
  color: #DC2626;
}

/* Guide card */
.guide-card {
  background: #EFF6FF;
  border: 1px solid #BFDBFE;
  border-radius: 10px;
  padding: 16px 20px;
}

.guide-header {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13.5px;
  font-weight: 600;
  color: #1D4ED8;
  margin-bottom: 12px;
}

.guide-steps {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.guide-steps li {
  font-size: 13px;
  color: #1E40AF;
  line-height: 1.5;
}

.guide-steps strong {
  color: #1D4ED8;
}

.guide-link {
  color: #1D4ED8;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  text-decoration: none;
}

.guide-link:hover {
  text-decoration: underline;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
