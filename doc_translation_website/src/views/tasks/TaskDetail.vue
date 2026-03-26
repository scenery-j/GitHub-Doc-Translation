<template>
  <AppLayout>
    <PageHero
        :title="`任务 #${task?.id ?? taskId}`"
        :back-to="task?.repositoryId ? `/repos/${task.repositoryId}` : '/repos'"
        :trail="[
        { label: '仓库管理', to: '/repos' },
        { label: task?.repositoryName || '仓库', to: task?.repositoryId ? `/repos/${task.repositoryId}` : undefined },
        { label: `任务 #${taskId}` }
      ]"
    >
      <template #right>
        <el-button
            v-if="task && (task.status === 'running' || task.status === 'queued')"
            type="danger"
            plain
            :loading="cancelling"
            @click="cancelTask"
        >取消任务
        </el-button>
      </template>
    </PageHero>

    <div class="page-container" v-loading="loading">
      <div v-if="task">

        <!-- Task info card -->
        <div class="task-info-card">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-key">仓库</span>
              <span class="info-val">{{ task.repositoryName }}</span>
            </div>
            <div class="info-item" v-if="task.branchName">
              <span class="info-key">分支</span>
              <span class="info-val mono" style="font-family:var(--font-mono)">{{ task.branchName }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">触发方式</span>
              <el-tag size="small" :type="task.triggerType === 'webhook' ? 'info' : ''">
                {{ task.triggerType === 'webhook' ? 'Webhook (push)' : '手动触发' }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="info-key">状态</span>
              <StatusTag :status="task.status"/>
            </div>
            <div class="info-item">
              <span class="info-key">开始时间</span>
              <span class="info-val mono">{{ task.startedAt ? formatTime(task.startedAt) : '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-key">耗时</span>
              <span class="info-val mono">{{ elapsedTime }}</span>
            </div>
          </div>

          <!-- Task error banner -->
          <div class="task-error-banner" v-if="task.status === 'failed' && task.errorMessage">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <div class="error-banner-body">
              <div class="error-banner-title">任务失败原因</div>
              <pre class="error-banner-msg">{{ task.errorMessage }}</pre>
            </div>
          </div>

          <!-- Overall progress -->
          <div class="overall-progress">
            <div class="progress-header">
              <span class="progress-label">总进度</span>
              <span class="progress-text">{{ task.completedFiles }}/{{ totalUnits }} 翻译单元 ({{
                  overallPercent
                }}%)</span>
            </div>
            <el-progress
                :percentage="overallPercent"
                :status="progressStatus"
                :stroke-width="10"
            />
          </div>
        </div>

        <!-- Language progress -->
        <div class="lang-progress-card">
          <h3 class="card-title">各语言翻译进度</h3>
          <el-collapse v-model="openCollapse">
            <el-collapse-item
                v-for="lang in task.targetLanguages"
                :key="lang"
                :name="lang"
            >
              <template #title>
                <div class="collapse-title">
                  <span class="lang-badge">{{ lang }}</span>
                  <span class="lang-label">{{ LANGUAGE_NAMES[lang] || lang }}</span>
                  <span class="lang-count">{{ getLangCompleted(lang) }}/{{ getLangTotal(lang) }} 完成</span>
                </div>
              </template>

              <el-table :data="getFilesByLang(lang)" size="small" :border="false">
                <el-table-column label="源文件" prop="sourcePath" min-width="200">
                  <template #default="{ row }">
                    <div class="file-path">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                      </svg>
                      {{ row.sourcePath }}
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <StatusTag :status="row.status"/>
                  </template>
                </el-table-column>
                <el-table-column label="Token消耗" width="120">
                  <template #default="{ row }">
                    <span class="mono-text" v-if="row.tokensUsed">{{ row.tokensUsed.toLocaleString() }}</span>
                    <span class="text-muted" v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="完成时间" width="140">
                  <template #default="{ row }">
                    <span class="mono-text" v-if="row.translatedAt">{{ formatShortTime(row.translatedAt) }}</span>
                    <span class="text-muted" v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column label="失败原因" min-width="160">
                  <template #default="{ row }">
                    <el-tooltip
                        v-if="row.status === 'failed' && row.errorMessage"
                        :content="row.errorMessage"
                        placement="top-start"
                        :max-width="480"
                        effect="dark"
                    >
                      <span class="file-error-msg">{{ truncateError(row.errorMessage) }}</span>
                    </el-tooltip>
                    <span class="text-muted" v-else>-</span>
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>

        <!-- Token usage -->
        <div class="token-card" v-if="task.tokensUsed > 0">
          <h3 class="card-title">Token 用量统计</h3>
          <div class="token-grid">
            <div class="token-item">
              <span class="token-label">已消耗</span>
              <span class="token-val">{{ task.tokensUsed.toLocaleString() }} tokens</span>
            </div>
            <div class="token-item" v-if="estimatedTotal > 0">
              <span class="token-label">预估总量</span>
              <span class="token-val">{{ estimatedTotal.toLocaleString() }} tokens</span>
            </div>
            <div class="token-item" v-if="task.estimatedCost > 0">
              <span class="token-label">预估费用</span>
              <span class="token-val">${{ task.estimatedCost.toFixed(4) }}</span>
            </div>
          </div>
        </div>

        <!-- PR info -->
        <div class="pr-card" v-if="task.prUrl">
          <div class="pr-header">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                 :stroke="task.prStatus === 'merged' ? '#8B5CF6' : task.prStatus === 'closed' ? '#6B7280' : '#22C55E'"
                 stroke-width="2">
              <circle cx="18" cy="18" r="3"/>
              <circle cx="6" cy="6" r="3"/>
              <path d="M13 6h3a2 2 0 0 1 2 2v7"/>
              <line x1="6" y1="9" x2="6" y2="21"/>
            </svg>
            <span class="pr-title">PR #{{ task.prNumber }}</span>
            <el-tag
                size="small"
                :type="task.prStatus === 'merged' ? 'warning' : task.prStatus === 'closed' ? 'info' : 'success'"
            >
              {{ task.prStatus === 'merged' ? '已合并' : task.prStatus === 'closed' ? '已关闭' : '开启中' }}
            </el-tag>
          </div>
          <p class="pr-desc">
            <span v-if="task.prStatus === 'merged'">PR 已合并，翻译内容已进入主分支</span>
            <span v-else-if="task.prStatus === 'closed'">PR 已关闭（未合并）</span>
            <span v-else>翻译完成，PR 已提交到原仓库，等待审核合并</span>
          </p>
          <a :href="task.prUrl" target="_blank" class="pr-link">
            在 GitHub 中查看 PR
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
              <polyline points="15 3 21 3 21 9"/>
              <line x1="10" y1="14" x2="21" y2="3"/>
            </svg>
          </a>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHero from '@/components/layout/PageHero.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { tasksApi } from '@/api/tasks'
import type { TranslationFileRecord, TranslationTaskDetail } from '@/types'
import { LANGUAGE_NAMES } from '@/types'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.taskId)

const task = ref<TranslationTaskDetail | null>(null)
const loading = ref(false)
const cancelling = ref(false)
const openCollapse = ref<string[]>([])

let pollingTimer: ReturnType<typeof setInterval> | null = null

const totalUnits = computed(() => {
  if (!task.value) return 0
  return task.value.totalFiles
})

const overallPercent = computed(() => {
  if (!task.value || totalUnits.value === 0) return 0
  const completed = task.value.files?.filter(f => f.status === 'completed').length || 0
  return Math.round((completed / totalUnits.value) * 100)
})

const progressStatus = computed(() => {
  if (!task.value) return undefined
  if (task.value.status === 'completed') return 'success'
  if (task.value.status === 'failed') return 'exception'
  return undefined
})

const elapsedTime = computed(() => {
  if (!task.value?.startedAt) return '-'
  const end = task.value.completedAt ? new Date(task.value.completedAt) : new Date()
  const diff = end.getTime() - new Date(task.value.startedAt).getTime()
  const secs = Math.floor(diff / 1000)
  if (secs < 60) return `${secs}秒`
  return `${Math.floor(secs / 60)}分${secs % 60}秒`
})

const estimatedTotal = computed(() => {
  if (!task.value || overallPercent.value === 0) return 0
  return Math.round(task.value.tokensUsed / overallPercent.value * 100)
})

function formatTime(d: string) {
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function formatShortTime(d: string) {
  return new Date(d).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

function truncateError(msg: string, max = 60): string {
  if (!msg) return ''
  const oneLine = msg.replace(/\n/g, ' ').trim()
  return oneLine.length > max ? oneLine.slice(0, max) + '…' : oneLine
}

function getFilesByLang(lang: string): TranslationFileRecord[] {
  return task.value?.files?.filter(f => f.targetLanguage === lang) || []
}

function getLangCompleted(lang: string) {
  return getFilesByLang(lang).filter(f => f.status === 'completed').length
}

function getLangTotal(lang: string) {
  return getFilesByLang(lang).length
}

async function fetchTask() {
  try {
    task.value = await tasksApi.get(taskId)
    if (!openCollapse.value.length && task.value.targetLanguages.length) {
      openCollapse.value = [task.value.targetLanguages[0]]
    }
  } catch { /* ignore */
  }
}

function startPolling() {
  pollingTimer = setInterval(async () => {
    await fetchTask()
    const s = task.value?.status
    if (s === 'completed' || s === 'failed' || s === 'cancelled') {
      stopPolling()
    }
  }, 3000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

async function cancelTask() {
  await ElMessageBox.confirm('确认取消当前翻译任务？', '取消任务', {
    confirmButtonText: '确认取消',
    cancelButtonText: '不取消',
    type: 'warning',
  })
  cancelling.value = true
  try {
    await tasksApi.cancel(taskId)
    ElMessage.success('任务已取消')
    await fetchTask()
    stopPolling()
  } finally {
    cancelling.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await fetchTask()
  loading.value = false

  const s = task.value?.status
  if (s === 'queued' || s === 'running') {
    startPolling()
  }
})

onUnmounted(stopPolling)
</script>

<style scoped>
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.bc-link {
  color: var(--chinese-red);
  text-decoration: none;
}

.bc-current {
  color: var(--text-secondary);
}

.task-info-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.info-key {
  color: var(--text-muted);
}

.info-val {
  color: var(--text-primary);
  font-weight: 500;
}

.info-val.mono {
  font-family: var(--font-mono);
}

.overall-progress {
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  font-weight: 600;
}

.progress-text {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: var(--font-mono);
}

.lang-progress-card, .token-card, .pr-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.lang-badge {
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  font-family: var(--font-mono);
  font-weight: 600;
  color: var(--text-secondary);
}

.lang-label {
  font-size: 14px;
  font-weight: 500;
}

.lang-count {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.file-path {
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: var(--font-mono);
  font-size: 12px;
}

.mono-text {
  font-family: var(--font-mono);
  font-size: 12px;
}

.text-muted {
  color: var(--text-muted);
}

/* Token */
.token-grid {
  display: flex;
  gap: 32px;
}

.token-item {
}

.token-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
  display: block;
}

.token-val {
  font-size: 18px;
  font-weight: 600;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

/* Error banner */
.task-error-banner {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #fff5f5;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 16px;
  color: #dc2626;
}

.error-banner-body {
  flex: 1;
  min-width: 0;
}

.error-banner-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 6px;
}

.error-banner-msg {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #7f1d1d;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
  line-height: 1.6;
}

/* File error */
.file-error-msg {
  color: #dc2626;
  font-size: 12px;
  font-family: var(--font-mono);
  cursor: help;
  border-bottom: 1px dashed #dc2626;
}

/* PR */
.pr-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.pr-title {
  font-size: 15px;
  font-weight: 600;
}

.pr-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px;
}

.pr-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--chinese-red);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
}

.pr-link:hover {
  opacity: 0.8;
}
</style>
