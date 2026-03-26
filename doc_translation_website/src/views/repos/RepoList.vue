<template>
  <AppLayout>
    <!-- Full-height wrapper (prevents layout-main from scrolling) -->
    <div class="repo-page">
      <!-- Top: PageHero (sticky header) -->
      <PageHero title="仓库管理">
        <template #right>
          <el-button type="primary" @click="openAddDialog">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
                 style="margin-right:5px">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加仓库
          </el-button>
        </template>
      </PageHero>

      <!-- Body: toolbar + scrollable list + pagination -->
      <div class="repo-body">

        <!-- Search toolbar (fixed at top of body) -->
        <div class="search-toolbar">
          <div class="toolbar-left">
            <div class="search-wrap">
              <svg class="search-icon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input
                  v-model="searchQuery"
                  class="search-input"
                  placeholder="搜索仓库名称..."
                  @input="handleSearch"
              />
              <button v-if="searchQuery" class="search-clear" @click="searchQuery = ''; handleSearch()">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <div class="status-tabs">
              <button
                  v-for="opt in statusOptions"
                  :key="opt.value"
                  :class="['status-tab', { active: statusFilter === opt.value }]"
                  @click="statusFilter = opt.value; handleSearch()"
              >{{ opt.label }}
              </button>
            </div>
          </div>
          <span class="total-badge">共 {{ total }} 个仓库</span>
        </div>

        <!-- Scrollable list area -->
        <div class="repo-scroll" v-loading="loading">
          <div v-if="repos.length > 0" class="repo-list">
            <div v-for="repo in repos" :key="repo.id" class="repo-item card">
              <div class="repo-item-header">
                <div class="repo-title-group">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#94A3B8" stroke-width="2">
                    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                  </svg>
                  <span class="repo-full-name" @click="$router.push(`/repos/${repo.id}`)">{{ repo.fullName }}</span>
                  <StatusTag :status="repo.status"/>
                  <div class="webhook-indicator">
                    <span class="dot active"></span>
                    <span class="dot-label">Webhook 活跃</span>
                  </div>
                </div>
              </div>
              <div class="repo-item-meta">
                <div class="meta-item">
                  <span class="meta-key">仓库描述</span>
                  <span class="meta-val">{{ repo.description || '无' }}</span>
                </div>
              </div>
              <div class="repo-item-meta">
                <div class="meta-item">
                  <span class="meta-key">基准语言</span>
                  <span class="meta-val">{{ LANGUAGE_NAMES[repo.baseLanguage] || repo.baseLanguage }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-key">目标语言</span>
                  <span class="meta-val">{{
                      repo.targetLanguages.map(l => LANGUAGE_NAMES[l] || l).join('、') || '未设置'
                    }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-key">AI 模型</span>
                  <span class="meta-val mono">{{ repo.aiModel }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-key">翻译文件</span>
                  <span class="meta-val">{{ repo.translatedFileCount }} 个</span>
                </div>
                <div class="meta-item" v-if="repo.lastSyncAt">
                  <span class="meta-key">最后同步</span>
                  <span class="meta-val">{{ formatTime(repo.lastSyncAt) }}</span>
                </div>
              </div>

              <div class="repo-item-actions">
                <el-button size="small" @click="$router.push(`/repos/${repo.id}/config`)">翻译配置</el-button>
                <el-button size="small" type="primary" @click="triggerSync(repo)" :loading="syncingId === repo.id">
                  手动同步
                </el-button>
                <el-button size="small" @click="openPR(repo)" :disabled="!repo.translatedFileCount">查看PR</el-button>
                <el-dropdown @command="(cmd: string) => handleMoreCmd(cmd, repo)" trigger="click">
                  <el-button size="small">
                    更多
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                         style="margin-left:4px">
                      <polyline points="6 9 12 15 18 9"/>
                    </svg>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                      <el-dropdown-item v-if="repo.status === 'active'" command="pause">暂停同步</el-dropdown-item>
                      <el-dropdown-item v-else command="resume">恢复同步</el-dropdown-item>
                      <el-dropdown-item command="remove" style="color:#EF4444">移除仓库</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>

          <EmptyState
              v-else-if="!loading"
              title="暂无仓库"
              description="添加你的第一个仓库开始翻译"
          >
            <template #action>
              <el-button type="primary" @click="openAddDialog">添加仓库</el-button>
            </template>
          </EmptyState>
        </div>

        <!-- Pagination bar (fixed at bottom) -->
        <div class="pagination-bar">
          <div class="page-size-wrap">
            <span class="size-label">每页</span>
            <div class="size-tabs">
              <button
                  v-for="s in pageSizeOptions"
                  :key="s"
                  :class="['size-tab', { active: pageSize === s }]"
                  @click="changePageSize(s)"
              >{{ s }}
              </button>
            </div>
            <span class="size-label">条</span>
          </div>
          <el-pagination
              v-model:current-page="currentPage"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="loadRepos"
          />
        </div>

      </div>
    </div>

    <!-- Sync dialog -->
    <el-dialog v-model="showSyncDialog" :title="`手动同步 - ${syncTarget?.fullName}`" width="400px">
      <p style="margin:0 0 16px;color:var(--text-secondary)">选择翻译模式：</p>
      <el-radio-group v-model="syncType" style="display:flex;flex-direction:column;gap:10px">
        <el-radio value="full">
          <strong>全量翻译</strong>
          <span style="color:var(--text-muted);font-size:12px;margin-left:6px">重新翻译所有选中文件</span>
        </el-radio>
        <el-radio value="incremental">
          <strong>增量翻译</strong>
          <span style="color:var(--text-muted);font-size:12px;margin-left:6px">仅翻译自上次以来变更的文件</span>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="showSyncDialog = false">取消</el-button>
        <el-button type="primary" :loading="syncingId !== null" @click="confirmSync">开始翻译</el-button>
      </template>
    </el-dialog>

    <!-- Add Repo Dialog -->
    <el-dialog v-model="showAddDialog" title="添加仓库" width="560px">
      <el-input v-model="searchAvail" placeholder="搜索仓库..." :prefix-icon="Search" clearable
                style="margin-bottom:12px"/>
      <div class="available-repos">
        <div v-if="availLoading" class="repos-loading">
          <div class="repos-loading-inner">
            <div class="loading-spinner"></div>
            <span>正在加载仓库列表...</span>
          </div>
        </div>
        <template v-else>
          <div
              v-for="r in filteredAvail"
              :key="r.githubRepoId"
              :class="['avail-item', { disabled: r.alreadyAdded, selected: selectedIds.includes(r.githubRepoId) }]"
              @click="toggleSel(r)"
          >
            <div class="avail-check">
              <svg v-if="selectedIds.includes(r.githubRepoId)" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="#DE2910" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <div v-else class="check-box"></div>
            </div>
            <div class="avail-info">
              <span class="avail-name">{{ r.fullName }}</span>
              <span v-if="r.description" class="avail-desc">{{ r.description }}</span>
            </div>
            <el-tag v-if="r.alreadyAdded" size="small" type="success">已添加</el-tag>
          </div>
          <EmptyState v-if="filteredAvail.length === 0" title="暂无可用仓库"/>
        </template>
      </div>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="adding" :disabled="!selectedIds.length" @click="doAdd">
          添加 ({{ selectedIds.length }})
        </el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHero from '@/components/layout/PageHero.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { reposApi } from '@/api/repos'
import { tasksApi } from '@/api/tasks'
import type { AvailableRepository, Repository } from '@/types'
import { LANGUAGE_NAMES } from '@/types'

const router = useRouter()

const statusOptions = [
  { label: '全部', value: '' },
  { label: '活跃', value: 'active' },
  { label: '已暂停', value: 'paused' },
  { label: '错误', value: 'error' },
]
const pageSizeOptions = [10, 20, 50, 100]

const repos = ref<Repository[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')
const statusFilter = ref('')

const syncingId = ref<number | null>(null)
const showSyncDialog = ref(false)
const syncTarget = ref<Repository | null>(null)
const syncType = ref<'full' | 'incremental'>('incremental')

const showAddDialog = ref(false)
const availableRepos = ref<AvailableRepository[]>([])
const availLoading = ref(false)
const selectedIds = ref<number[]>([])
const searchAvail = ref('')
const adding = ref(false)

const filteredAvail = computed(() => {
  if (!searchAvail.value) return availableRepos.value
  const q = searchAvail.value.toLowerCase()
  return availableRepos.value.filter(r => r.fullName.toLowerCase().includes(q))
})

function formatTime(d: string) {
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function handleSearch() {
  currentPage.value = 1
  loadRepos()
}

function changePageSize(s: number) {
  pageSize.value = s
  currentPage.value = 1
  loadRepos()
}

async function loadRepos() {
  loading.value = true
  try {
    const res = await reposApi.list({
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value,
      status: statusFilter.value
    })
    repos.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function triggerSync(repo: Repository) {
  syncTarget.value = repo
  syncType.value = 'incremental'
  showSyncDialog.value = true
}

async function confirmSync() {
  if (!syncTarget.value) return
  syncingId.value = syncTarget.value.id
  try {
    const { taskIds } = await tasksApi.trigger(syncTarget.value.id, { type: syncType.value })
    ElMessage.success('翻译任务已创建')
    showSyncDialog.value = false
    router.push(`/repos/${syncTarget.value.id}?tab=tasks`)
  } finally {
    syncingId.value = null
  }
}

function openPR(repo: Repository) {
  window.open(`https://github.com/${repo.fullName}/pulls`, '_blank')
}

async function handleMoreCmd(cmd: string, repo: Repository) {
  if (cmd === 'detail') {
    router.push(`/repos/${repo.id}`)
  } else if (cmd === 'remove') {
    await ElMessageBox.confirm(`确认从平台移除 ${repo.fullName}？已生成的翻译文件不会被删除。`, '移除仓库', {
      confirmButtonText: '确认移除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await reposApi.remove(repo.id)
    ElMessage.success('仓库已移除')
    loadRepos()
  }
}

function openAddDialog() {
  selectedIds.value = []
  searchAvail.value = ''
  availableRepos.value = []
  showAddDialog.value = true
  loadAvail()
}

async function loadAvail() {
  availLoading.value = true
  try {
    const res = await reposApi.available()
    availableRepos.value = res.records
  } finally {
    availLoading.value = false
  }
}

function toggleSel(r: AvailableRepository) {
  if (r.alreadyAdded) return
  const idx = selectedIds.value.indexOf(r.githubRepoId)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
  else selectedIds.value.push(r.githubRepoId)
}

async function doAdd() {
  adding.value = true
  try {
    const toAdd = availableRepos.value.filter(r => selectedIds.value.includes(r.githubRepoId))
    for (const r of toAdd) {
      await reposApi.add({
        githubRepoId: r.githubRepoId,
        installationId: r.installationId,
        fullName: r.fullName,
        defaultBranch: r.defaultBranch,
        description: r.description
      })
    }
    ElMessage.success(`成功添加 ${toAdd.length} 个仓库`)
    showAddDialog.value = false
    loadRepos()
  } finally {
    adding.value = false
  }
}

onMounted(loadRepos)
</script>

<style scoped>
/* ── Full-height page wrapper ── */
.repo-page {
  height: calc(100vh - var(--header-height));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.repo-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}

/* ── Search Toolbar ── */
.search-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-color);
  padding: 10px 28px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.search-wrap {
  display: flex;
  align-items: center;
  gap: 7px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  padding: 0 10px;
  height: 34px;
  width: 240px;
  flex-shrink: 0;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.search-wrap:focus-within {
  border-color: var(--chinese-red);
  box-shadow: 0 0 0 3px rgba(222, 41, 16, 0.08);
}

.search-icon {
  color: var(--text-placeholder);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  min-width: 0;
  font-family: var(--font-primary);
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-clear {
  display: flex;
  align-items: center;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--text-muted);
  padding: 0;
  transition: color 0.12s;
}

.search-clear:hover {
  color: var(--text-secondary);
}

.status-tabs {
  display: flex;
  gap: 2px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  padding: 3px;
}

.status-tab {
  padding: 4px 12px;
  border: none;
  border-radius: var(--border-radius-sm);
  background: transparent;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
  white-space: nowrap;
}

.status-tab:hover {
  color: var(--text-primary);
  background: rgba(0, 0, 0, 0.03);
}

.status-tab.active {
  background: var(--bg-card);
  color: var(--chinese-red);
  box-shadow: var(--shadow-xs);
  font-weight: 600;
}

.total-badge {
  font-size: 12px;
  color: var(--text-muted);
  white-space: nowrap;
  font-family: var(--font-mono);
  letter-spacing: 0.01em;
}

/* ── Scrollable list area ── */
.repo-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 18px 28px;
}

/* ── Repo Cards ── */
.repo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.repo-item {
  padding: 16px 20px;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.repo-item:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color-hover);
}

.repo-item-header {
  margin-bottom: 10px;
}

.repo-title-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.repo-full-name {
  font-size: 14.5px;
  font-weight: 700;
  color: var(--text-primary);
  cursor: pointer;
  letter-spacing: -0.01em;
  transition: color 0.15s;
}

.repo-full-name:hover {
  color: var(--chinese-red);
}

.webhook-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot.active {
  background: #16A34A;
  box-shadow: 0 0 0 2px rgba(22, 163, 74, 0.2);
}

.dot.inactive {
  background: var(--text-muted);
}

.dot-label {
  font-size: 11.5px;
  color: var(--text-muted);
}

.repo-item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 14px;
}

.meta-item {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 13px;
}

.meta-key {
  color: var(--text-muted);
  font-weight: 400;
}

.meta-val {
  color: var(--text-primary);
  font-weight: 600;
}

.meta-val.mono {
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 400;
  background: var(--bg-subtle);
  padding: 1px 6px;
  border-radius: var(--border-radius-xs);
  border: 1px solid var(--border-color);
}

.repo-item-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* ── Pagination Bar ── */
.pagination-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 28px;
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
}

.page-size-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-label {
  font-size: 12.5px;
  color: var(--text-muted);
}

.size-tabs {
  display: flex;
  gap: 2px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-sm);
  padding: 2px;
}

.size-tab {
  padding: 3px 10px;
  border: none;
  border-radius: var(--border-radius-xs);
  background: transparent;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 0.12s, color 0.12s;
  font-family: var(--font-mono);
}

.size-tab:hover {
  color: var(--text-primary);
}

.size-tab.active {
  background: var(--bg-card);
  color: var(--chinese-red);
  box-shadow: var(--shadow-xs);
  font-weight: 600;
}

/* ── Add Repo Dialog ── */
.available-repos {
  height: 360px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
}

.repos-loading {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.repos-loading-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}

.loading-spinner {
  width: 26px;
  height: 26px;
  border: 2.5px solid var(--bg-subtle);
  border-top-color: var(--chinese-red);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.avail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.12s;
}

.avail-item:last-child {
  border-bottom: none;
}

.avail-item:hover:not(.disabled) {
  background: var(--bg-hover);
}

.avail-item.disabled {
  opacity: 0.55;
  cursor: default;
}

.avail-item.selected {
  background: var(--chinese-red-light);
}

.avail-check {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.check-box {
  width: 16px;
  height: 16px;
  border: 1.5px solid #C8D0DC;
  border-radius: var(--border-radius-xs);
  transition: border-color 0.15s;
}

.avail-item:hover:not(.disabled) .check-box {
  border-color: var(--border-color-hover);
}

.avail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.avail-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.avail-desc {
  font-size: 11.5px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
