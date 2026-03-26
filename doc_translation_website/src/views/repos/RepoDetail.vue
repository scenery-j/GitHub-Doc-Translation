<template>
  <AppLayout>
    <PageHero
        :title="repo?.fullName || '加载中...'"
        back-to="/repos"
        :trail="[{ label: '仓库管理', to: '/repos' }, { label: repo?.fullName || '...' }]"
    >
      <template #right v-if="repo">
        <StatusTag :status="repo.status"/>
        <el-button @click="goConfig">翻译配置</el-button>
        <el-button type="primary" @click="showSyncDialog = true">手动同步翻译</el-button>
        <a :href="`https://github.com/${repo.fullName}`" target="_blank">
          <el-button>在 GitHub 中查看</el-button>
        </a>
      </template>
    </PageHero>

    <div class="detail-body" v-loading="loading">
      <div v-if="repo" class="detail-inner">

        <!-- Tabs -->
        <el-tabs v-model="activeTab" class="detail-tabs">

          <!-- ===== Overview ===== -->
          <el-tab-pane label="概览" name="overview">
            <div class="overview-content">
              <!-- Stats -->
              <div class="mini-stats">
                <div class="mini-stat">
                  <div class="mini-val">{{ repo.stats.totalFiles }}</div>
                  <div class="mini-label">翻译文件数</div>
                </div>
                <div class="mini-stat">
                  <div class="mini-val">{{ repo.targetLanguages.length }}</div>
                  <div class="mini-label">目标语言</div>
                </div>
                <div class="mini-stat">
                  <div class="mini-val">{{ repo.stats.pendingChanges }}</div>
                  <div class="mini-label">待处理变更</div>
                </div>
                <div class="mini-stat">
                  <div class="mini-val">{{ repo.stats.totalPRs }}</div>
                  <div class="mini-label">总 PR 数</div>
                </div>
              </div>

              <!-- Language progress -->
              <div class="section-card">
                <h3 class="card-title">各语言翻译进度</h3>
                <div class="lang-progress-list">
                  <div v-for="lp in repo.stats.languageProgress" :key="lp.language" class="lang-progress-item">
                    <div class="lang-progress-header">
                      <span class="lang-name">{{ LANGUAGE_NAMES[lp.language] || lp.language }} ({{
                          lp.language
                        }})</span>
                      <span class="lang-stats">{{ lp.completed }}/{{ lp.total }} 文件</span>
                      <span class="lang-pct">{{ lp.percentage }}%</span>
                    </div>
                    <el-progress :percentage="lp.percentage" :show-text="false" :stroke-width="8"/>
                  </div>
                </div>
              </div>

              <!-- Recent tasks (overview only, no pagination) -->
              <div class="section-card">
                <div class="section-header-row">
                  <h3 class="card-title">最近翻译任务</h3>
                  <span class="link-text" @click="activeTab = 'tasks'">查看全部</span>
                </div>
                <el-table
                    :data="overviewTasks"
                    v-loading="overviewLoading"
                    size="small"
                    :row-style="{ height: '48px' }"
                >
                  <el-table-column label="任务ID" width="80">
                    <template #default="{ row }"><span class="mono-text">#{{ row.id }}</span></template>
                  </el-table-column>
                  <el-table-column label="分支" min-width="130">
                    <template #default="{ row }">
                      <span v-if="row.branchName" class="branch-tag">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2"><line x1="6" y1="3" x2="6" y2="15"/><circle cx="18" cy="6" r="3"/><circle
                            cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/></svg>
                        {{ row.branchName }}
                      </span>
                      <span v-else class="text-muted">-</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="触发方式" width="100">
                    <template #default="{ row }">
                      <el-tag size="small" :type="row.triggerType === 'webhook' ? 'info' : ''">
                        {{ row.triggerType === 'webhook' ? 'Webhook' : '手动' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="翻译状态" width="100">
                    <template #default="{ row }">
                      <StatusTag :status="row.status"/>
                    </template>
                  </el-table-column>
                  <el-table-column label="PR状态" width="100">
                    <template #default="{ row }">
                      <StatusTag :status="row.prStatus"/>
                    </template>
                  </el-table-column>
                  <el-table-column label="文件数" width="90">
                    <template #default="{ row }">{{ row.completedFiles }}/{{ row.totalFiles }}</template>
                  </el-table-column>
                  <el-table-column label="时间">
                    <template #default="{ row }"><span class="time-text">{{ formatRelTime(row.createdAt) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="" width="80">
                    <template #default="{ row }">
                      <span class="link-text" @click="viewTask(row.id)">详情</span>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>

          <!-- ===== 任务历史 ===== -->
          <el-tab-pane label="任务历史" name="tasks">
            <div class="tab-pane-list">
              <el-table
                  :data="tasks"
                  v-loading="tasksLoading"
                  :row-style="{ height: '50px' }"
                  class="fill-table"
              >
                <el-table-column label="ID" width="80">
                  <template #default="{ row }"><span class="mono-text">#{{ row.id }}</span></template>
                </el-table-column>
                <el-table-column label="分支" min-width="140">
                  <template #default="{ row }">
                    <span v-if="row.branchName" class="branch-tag">
                      <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2"><line x1="6" y1="3" x2="6" y2="15"/><circle cx="18" cy="6" r="3"/><circle
                          cx="6" cy="18" r="3"/><path d="M18 9a9 9 0 0 1-9 9"/></svg>
                      {{ row.branchName }}
                    </span>
                    <span v-else class="text-muted">-</span>
                  </template>
                </el-table-column>
                <el-table-column label="触发方式" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.triggerType === 'webhook' ? 'info' : ''">
                      {{ row.triggerType === 'webhook' ? 'Webhook' : '手动' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <StatusTag :status="row.status"/>
                  </template>
                </el-table-column>
                <el-table-column label="PR状态" width="100">
                  <template #default="{ row }">
                    <StatusTag :status="row.prStatus"/>
                  </template>
                </el-table-column>
                <el-table-column label="语言" min-width="120">
                  <template #default="{ row }">
                    <div style="display:flex;gap:4px;flex-wrap:wrap">
                      <el-tag v-for="l in row.targetLanguages" :key="l" size="small" type="info">{{ l }}</el-tag>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="文件" width="80">
                  <template #default="{ row }">{{ row.completedFiles }}/{{ row.totalFiles }}</template>
                </el-table-column>
                <el-table-column label="Token消耗" width="110">
                  <template #default="{ row }"><span class="mono-text">{{ row.tokensUsed.toLocaleString() }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="时间" width="160">
                  <template #default="{ row }"><span class="time-text">{{ formatTime(row.createdAt) }}</span></template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ row }">
                    <span class="link-text" @click="viewTask(row.id)">详情</span>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pager-row">
                <span class="pager-info">第 {{ tasksPage }} 页 / 共 {{ tasksTotal }} 条</span>
                <el-pagination
                    v-model:current-page="tasksPage"
                    :page-size="PAGE_SIZE"
                    :total="tasksTotal"
                    layout="prev, pager, next"
                    @current-change="loadTasks"
                    small
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- ===== PR记录 ===== -->
          <el-tab-pane label="PR记录" name="prs">
            <div class="tab-pane-list">
              <el-table
                  :data="prs"
                  v-loading="prsLoading"
                  :row-style="{ height: '50px' }"
                  class="fill-table"
              >
                <el-table-column label="PR #" width="80">
                  <template #default="{ row }"><span class="mono-text">#{{ row.prNumber }}</span></template>
                </el-table-column>
                <el-table-column label="翻译状态" width="100">
                  <template #default="{ row }">
                    <StatusTag :status="row.status"/>
                  </template>
                </el-table-column>
                <el-table-column label="PR状态" width="100">
                  <template #default="{ row }">
                    <StatusTag :status="row.prStatus"/>
                  </template>
                </el-table-column>
                <el-table-column label="时间" width="160">
                  <template #default="{ row }"><span class="time-text">{{ formatTime(row.createdAt) }}</span></template>
                </el-table-column>
                <el-table-column label="链接">
                  <template #default="{ row }">
                    <a v-if="row.prUrl" :href="row.prUrl" target="_blank" class="link-text">在 GitHub 查看</a>
                  </template>
                </el-table-column>
              </el-table>
              <div class="pager-row">
                <span class="pager-info">第 {{ prsPage }} 页 / 共 {{ prsTotal }} 条</span>
                <el-pagination
                    v-model:current-page="prsPage"
                    :page-size="PAGE_SIZE"
                    :total="prsTotal"
                    layout="prev, pager, next"
                    @current-change="loadPrs"
                    small
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- ===== 操作日志 ===== -->
          <el-tab-pane label="操作日志" name="logs">
            <div class="tab-pane-list" v-loading="logsLoading">
              <div class="log-list">
                <div v-for="log in logs" :key="log.id" class="log-item">
                  <div class="log-action">{{ actionLabel(log.action) }}</div>
                  <div class="log-detail">{{ log.detail }}</div>
                  <div class="log-time">{{ formatTime(log.createdAt) }}</div>
                </div>
                <EmptyState v-if="!logs.length && !logsLoading" title="暂无操作日志"/>
              </div>
              <div class="pager-row">
                <span class="pager-info">第 {{ logsPage }} 页 / 共 {{ logsTotal }} 条</span>
                <el-pagination
                    v-model:current-page="logsPage"
                    :page-size="PAGE_SIZE"
                    :total="logsTotal"
                    layout="prev, pager, next"
                    @current-change="loadLogs"
                    small
                />
              </div>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>

    <!-- Sync dialog -->
    <el-dialog v-model="showSyncDialog" title="手动同步翻译" width="440px" @open="onSyncDialogOpen">
      <el-form label-position="top" style="margin-bottom:4px">
        <el-form-item label="翻译分支">
          <el-select
              v-model="syncBranch"
              placeholder="选择要翻译的分支"
              style="width:100%"
              :loading="syncBranchesLoading"
          >
            <el-option
                v-for="b in syncBranches"
                :key="b.branchName"
                :label="b.branchName"
                :value="b.branchName"
            >
              <span style="display:flex;align-items:center;gap:6px">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line
                    x1="6" y1="3" x2="6" y2="15"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path
                    d="M18 9a9 9 0 0 1-9 9"/></svg>
                {{ b.branchName }}
                <el-tag v-if="!b.selectedPaths.length" size="small" type="warning">未配置文件</el-tag>
              </span>
            </el-option>
          </el-select>
          <div style="font-size:12px;color:var(--text-muted);margin-top:4px">仅翻译已在「翻译配置」中配置了文件的分支
          </div>
        </el-form-item>
        <el-form-item label="翻译模式">
          <el-radio-group v-model="syncType" style="display:flex;flex-direction:column;gap:10px">
            <el-radio value="full"><strong>全量翻译</strong> — 重新翻译所选分支的所有配置文件</el-radio>
            <el-radio value="incremental"><strong>增量翻译</strong> — 仅翻译有变化的文件</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSyncDialog = false">取消</el-button>
        <el-button type="primary" :loading="syncing" :disabled="!syncBranch" @click="doSync">开始翻译</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHero from '@/components/layout/PageHero.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { reposApi } from '@/api/repos'
import { tasksApi } from '@/api/tasks'
import type { BranchConfig, OperationLog, RepositoryDetail, TranslationTask } from '@/types'
import { LANGUAGE_NAMES } from '@/types'

const route = useRoute()
const router = useRouter()
const repoId = Number(route.params.id)
const PAGE_SIZE = 10

const repo = ref<RepositoryDetail | null>(null)
const loading = ref(false)
const activeTab = ref((route.query.tab as string) || 'overview')
const showSyncDialog = ref(false)
const syncType = ref<'full' | 'incremental'>('incremental')
const syncing = ref(false)
const syncBranch = ref('')
const syncBranches = ref<BranchConfig[]>([])
const syncBranchesLoading = ref(false)

// Overview recent tasks (first 5, no pagination)
const overviewTasks = ref<TranslationTask[]>([])
const overviewLoading = ref(false)

// 任务历史 tab
const tasks = ref<TranslationTask[]>([])
const tasksLoading = ref(false)
const tasksPage = ref(1)
const tasksTotal = ref(0)

// PR记录 tab
const prs = ref<TranslationTask[]>([])
const prsLoading = ref(false)
const prsPage = ref(1)
const prsTotal = ref(0)

// 操作日志 tab
const logs = ref<OperationLog[]>([])
const logsLoading = ref(false)
const logsPage = ref(1)
const logsTotal = ref(0)

function formatTime(d: string) {
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatRelTime(d: string) {
  const diff = Date.now() - new Date(d).getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return `${m}分钟前`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}小时前`
  return `${Math.floor(h / 24)}天前`
}

function viewTask(taskId: number) {
  router.push(`/repos/${repoId}/tasks/${taskId}`)
}

function goConfig() {
  router.push(`/repos/${repoId}/config`)
}

function actionLabel(action: string) {
  const m: Record<string, string> = {
    'translation.started': '翻译开始',
    'translation.completed': '翻译完成',
    'translation.failed': '翻译失败',
    'pr.created': 'PR 已创建',
    'pr.merged': 'PR 已合并',
    'webhook.received': '收到 Webhook',
    'config.updated': '配置已更新',
  }
  return m[action] || action
}

async function loadTasks(page = tasksPage.value) {
  tasksPage.value = page
  tasksLoading.value = true
  try {
    const res = await tasksApi.list(repoId, { page, size: PAGE_SIZE })
    tasks.value = res.records
    tasksTotal.value = res.total
  } finally {
    tasksLoading.value = false
  }
}

async function loadPrs(page = prsPage.value) {
  prsPage.value = page
  prsLoading.value = true
  try {
    const res = await tasksApi.list(repoId, { page, size: PAGE_SIZE })
    prs.value = res.records.filter(t => t.prNumber)
    prsTotal.value = res.total
  } finally {
    prsLoading.value = false
  }
}

async function loadLogs(page = logsPage.value) {
  logsPage.value = page
  logsLoading.value = true
  try {
    const res = await reposApi.getLogs(repoId, { page, size: PAGE_SIZE })
    logs.value = res.records
    logsTotal.value = res.total
  } finally {
    logsLoading.value = false
  }
}

async function onSyncDialogOpen() {
  syncBranchesLoading.value = true
  syncBranch.value = ''
  try {
    const list = await reposApi.getBranches(repoId)
    syncBranches.value = list
    const firstWithFiles = list.find(b => b.selectedPaths.length > 0)
    syncBranch.value = firstWithFiles?.branchName || list[0]?.branchName || ''
  } catch {
    syncBranches.value = []
  } finally {
    syncBranchesLoading.value = false
  }
}

async function doSync() {
  if (!repo.value || !syncBranch.value) return
  syncing.value = true
  try {
    const { taskIds } = await tasksApi.trigger(repoId, { type: syncType.value, branches: [syncBranch.value] })
    ElMessage.success('翻译任务已创建')
    showSyncDialog.value = false
    await loadTasks(1)
    activeTab.value = 'tasks'
  } finally {
    syncing.value = false
  }
}

watch(activeTab, async (tab) => {
  if (tab === 'tasks') await loadTasks(1)
  if (tab === 'prs' && !prs.value.length) await loadPrs(1)
  if (tab === 'logs' && !logs.value.length) await loadLogs(1)
})

onMounted(async () => {
  loading.value = true
  overviewLoading.value = true
  try {
    const [r, t] = await Promise.all([
      reposApi.get(repoId),
      tasksApi.list(repoId, { page: 1, size: 5 }),
    ])
    repo.value = r
    overviewTasks.value = t.records
  } finally {
    loading.value = false
    overviewLoading.value = false
  }
})
</script>

<style scoped>
.detail-body {
  height: calc(100vh - var(--header-height) - 68px);
  overflow-y: auto;
  padding: 20px var(--spacing-xl, 32px);
  box-sizing: border-box;
}

.detail-inner {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.detail-tabs :deep(.el-tabs__content) {
  flex: 1;
}

/* ===== Overview ===== */
.overview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mini-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.mini-stat {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 16px;
  text-align: center;
}

.mini-val {
  font-size: 28px;
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

.mini-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.section-card {
  background: #fff;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  padding: 16px 20px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 14px;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.lang-progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lang-progress-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}

.lang-name {
  flex: 1;
  color: var(--text-primary);
  font-weight: 500;
}

.lang-stats {
  color: var(--text-muted);
  margin-right: 8px;
  font-family: var(--font-mono);
}

.lang-pct {
  color: var(--chinese-red);
  font-weight: 600;
  font-family: var(--font-mono);
  min-width: 38px;
  text-align: right;
}

/* ===== List tab panes ===== */
.tab-pane-list {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.fill-table {
  flex: 1;
}

/* Row height for all tables */
.tab-pane-list :deep(.el-table__row) td,
.section-card :deep(.el-table__row) td {
  height: 50px;
}

.pager-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0 4px;
  border-top: 1px solid var(--border-color);
  margin-top: 4px;
}

.pager-info {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* ===== Log list ===== */
.log-list {
  flex: 1;
}

.log-item {
  display: grid;
  grid-template-columns: 120px 1fr 160px;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border-color);
  align-items: center;
  font-size: 13px;
}

.log-action {
  font-weight: 500;
  color: var(--text-primary);
}

.log-detail {
  color: var(--text-secondary);
}

.log-time {
  color: var(--text-muted);
  text-align: right;
  font-family: var(--font-mono);
  font-size: 12px;
}

/* ===== Common ===== */
.mono-text {
  font-family: var(--font-mono);
  font-size: 12px;
}

.time-text {
  font-size: 12px;
  color: var(--text-secondary);
}

.link-text {
  color: var(--chinese-red);
  cursor: pointer;
  font-size: 13px;
}

.link-text:hover {
  opacity: 0.8;
}

.branch-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-family: var(--font-mono);
  color: #475569;
  background: #f1f5f9;
  border-radius: 4px;
  padding: 2px 7px;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  font-size: 12px;
  color: var(--text-muted);
}
</style>
