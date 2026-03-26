<template>
  <AppLayout>
    <div class="dashboard-container">
      <!-- Stats -->
      <div class="stats-grid" v-loading="statsLoading">
        <div class="stat-card clickable" @click="$router.push('/repos')">
          <div class="stat-icon" style="background:#fdefee">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#DE2910" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.totalRepos ?? '-' }}</div>
            <div class="stat-label">管理仓库</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background:#f0fdf4">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#22C55E" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.totalTranslatedFiles ?? '-' }}</div>
            <div class="stat-label">已翻译文件</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background:#eff6ff">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path
                  d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.totalLanguages ?? '-' }}</div>
            <div class="stat-label">目标语言数</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background:#fefce8">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats?.monthlyTasks ?? '-' }}</div>
            <div class="stat-label">本月任务</div>
          </div>
        </div>
      </div>

      <!-- Recent Tasks -->
      <div class="section-card tasks-card" style="margin-top:16px">
        <div class="section-header">
          <h2 class="section-title">最近翻译任务</h2>
        </div>
        <el-table
            :data="recentTasks"
            v-loading="tasksLoading"
            :border="false"
            style="width:100%"
            row-class-name="task-row-clickable"
            @row-click="(row: RecentTask) => $router.push(`/repos/${row.repositoryId}/tasks/${row.id}`)"
        >
          <el-table-column label="仓库名称" prop="repositoryName" min-width="160">
            <template #default="{ row }">
              <span class="repo-name">{{ row.repositoryName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="分支" width="160">
            <template #default="{ row }">
              <span v-if="row.branchName" class="branch-tag">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                     style="flex-shrink:0">
                  <line x1="6" y1="3" x2="6" y2="15"/><circle cx="18" cy="6" r="3"/><circle cx="6" cy="18" r="3"/><path
                    d="M18 9a9 9 0 0 1-9 9"/>
                </svg>
                {{ row.branchName }}
              </span>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="触发方式" prop="triggerType" width="100">
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
          <el-table-column label="文件数" width="100">
            <template #default="{ row }">
              <span class="file-count">{{ row.completedFiles }}/{{ row.totalFiles }}</span>
            </template>
          </el-table-column>
          <el-table-column label="PR状态" width="100">
            <template #default="{ row }">
              <StatusTag :status="row.prStatus"/>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="160">
            <template #default="{ row }">
              <span class="time-text">{{ formatRelativeTime(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState title="暂无翻译任务" description="添加仓库并配置翻译后，任务记录将显示在这里"/>
          </template>
        </el-table>

        <!-- Pagination -->
        <div class="tasks-pagination" v-if="tasksTotal > tasksPageSize">
          <el-pagination
              v-model:current-page="tasksPage"
              :page-size="tasksPageSize"
              :total="tasksTotal"
              layout="prev, pager, next, total"
              :pager-count="5"
              @current-change="loadRecentTasks"
          />
        </div>
        <div class="tasks-footer-hint" v-else-if="tasksTotal > 0">
          <span>共 {{ tasksTotal }} 条记录</span>
        </div>
      </div>

    </div>

    <!-- Add Repo Dialog -->
    <el-dialog v-model="showAddRepoDialog" title="添加仓库" width="560px" :close-on-click-modal="false">
      <div class="dialog-search">
        <el-input
            v-model="searchRepo"
            placeholder="搜索仓库名称..."
            clearable
            :prefix-icon="Search"
        />
      </div>
      <div class="available-repos">
        <!-- Loading skeleton -->
        <div v-if="availableLoading" class="repos-loading">
          <div class="repos-loading-inner">
            <div class="loading-spinner"></div>
            <span>正在加载仓库列表...</span>
          </div>
        </div>

        <template v-else>
          <div
              v-for="repo in filteredAvailable"
              :key="repo.githubRepoId"
              :class="['avail-repo-item', { disabled: repo.alreadyAdded, selected: selectedRepos.includes(repo.githubRepoId) }]"
              @click="toggleRepoSelect(repo)"
          >
            <div class="avail-check">
              <svg v-if="selectedRepos.includes(repo.githubRepoId)" width="16" height="16" viewBox="0 0 24 24"
                   fill="none" stroke="#DE2910" stroke-width="2.5">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <div v-else class="check-placeholder"></div>
            </div>
            <div class="avail-info">
              <div class="avail-name">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                </svg>
                {{ repo.fullName }}
                <el-tag v-if="repo.isPrivate" size="small" type="info">私有</el-tag>
                <el-tag v-if="repo.alreadyAdded" size="small" type="success">已添加</el-tag>
              </div>
              <div class="avail-desc" v-if="repo.description">{{ repo.description }}</div>
            </div>
            <div class="avail-stars">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="#F59E0B" stroke="#F59E0B" stroke-width="1">
                <polygon
                    points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              {{ repo.starCount }}
            </div>
          </div>
          <EmptyState v-if="filteredAvailable.length === 0" title="暂无可添加的仓库"
                      description="请先安装 GitHub App 并授权仓库"/>
        </template>
      </div>
      <template #footer>
        <el-button @click="showAddRepoDialog = false">取消</el-button>
        <el-button type="primary" :loading="addingRepos" :disabled="selectedRepos.length === 0"
                   @click="addSelectedRepos">
          添加选中的仓库 ({{ selectedRepos.length }})
        </el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppLayout from '@/components/layout/AppLayout.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { dashboardApi } from '@/api/dashboard'
import { reposApi } from '@/api/repos'
import type { AvailableRepository, DashboardStats, RecentTask } from '@/types'

const stats = ref<DashboardStats | null>(null)
const recentTasks = ref<RecentTask[]>([])
const statsLoading = ref(false)
const tasksLoading = ref(false)
const tasksPage = ref(1)
const tasksPageSize = 10
const tasksTotal = ref(0)

const showAddRepoDialog = ref(false)
const searchRepo = ref('')
const availableRepos = ref<AvailableRepository[]>([])
const availableLoading = ref(false)
const selectedRepos = ref<number[]>([])
const addingRepos = ref(false)

const filteredAvailable = computed(() => {
  if (!searchRepo.value) return availableRepos.value
  const q = searchRepo.value.toLowerCase()
  return availableRepos.value.filter(r => r.fullName.toLowerCase().includes(q))
})

function formatRelativeTime(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  return `${days}天前`
}

function toggleRepoSelect(repo: AvailableRepository) {
  if (repo.alreadyAdded) return
  const idx = selectedRepos.value.indexOf(repo.githubRepoId)
  if (idx >= 0) selectedRepos.value.splice(idx, 1)
  else selectedRepos.value.push(repo.githubRepoId)
}

async function loadAvailableRepos() {
  availableLoading.value = true
  try {
    const res = await reposApi.available()
    availableRepos.value = res.records
  } catch {
    availableRepos.value = []
  } finally {
    availableLoading.value = false
  }
}

async function addSelectedRepos() {
  if (selectedRepos.value.length === 0) return
  addingRepos.value = true
  try {
    const toAdd = availableRepos.value.filter(r => selectedRepos.value.includes(r.githubRepoId))
    for (const repo of toAdd) {
      await reposApi.add({
        githubRepoId: repo.githubRepoId,
        installationId: repo.installationId,
        fullName: repo.fullName,
        defaultBranch: repo.defaultBranch
      })
    }
    ElMessage.success(`成功添加 ${toAdd.length} 个仓库`)
    showAddRepoDialog.value = false
    selectedRepos.value = []
  } catch {
    // handled by interceptor
  } finally {
    addingRepos.value = false
  }
}

watch(showAddRepoDialog, (v) => {
  if (v) {
    // Clear stale data before loading fresh list
    availableRepos.value = []
    selectedRepos.value = []
    searchRepo.value = ''
    loadAvailableRepos()
  }
})

async function loadRecentTasks() {
  tasksLoading.value = true
  try {
    const res = await dashboardApi.getRecentTasks(tasksPage.value, tasksPageSize)
    recentTasks.value = res.records
    tasksTotal.value = res.total
  } finally {
    tasksLoading.value = false
  }
}

onMounted(async () => {
  statsLoading.value = true
  try {
    stats.value = await dashboardApi.getStats()
  } finally {
    statsLoading.value = false
  }
  loadRecentTasks()
})
</script>

<style scoped>
.dashboard-container {
  height: calc(100vh - var(--header-height));
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: var(--spacing-xl);
  gap: 0;
  box-sizing: border-box;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  flex-shrink: 0;
}

.tasks-card {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.tasks-card :deep(.el-table) {
  flex: 1;
  overflow-y: auto;
}

.tasks-card :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}

.tasks-pagination {
  padding: 12px 0 4px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
  margin-top: 4px;
}

.tasks-footer-hint {
  padding: 10px 0 2px;
  text-align: right;
  font-size: 12px;
  color: var(--text-muted);
  border-top: 1px solid var(--border-color);
  margin-top: 4px;
  flex-shrink: 0;
}

:deep(.task-row-clickable) {
  cursor: pointer;
  transition: background 0.15s;
}

:deep(.task-row-clickable:hover td) {
  background: var(--bg-page) !important;
}

/* ── Stat Cards ── */
.stat-card {
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  padding: 20px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-sm);
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.18s;
}

.stat-card.clickable:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--border-color-hover);
  transform: translateY(-1px);
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  font-family: var(--font-mono);
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 12.5px;
  color: var(--text-secondary);
  margin-top: 5px;
  font-weight: 500;
}

/* ── Section Card ── */
.section-card {
  background: var(--bg-card);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  padding: 20px 22px;
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--text-primary);
}

.view-all {
  font-size: 13px;
  color: var(--chinese-red);
  text-decoration: none;
  font-weight: 500;
  transition: opacity 0.15s;
}

.view-all:hover {
  opacity: 0.75;
}

.repo-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.file-count, .time-text {
  font-size: 12.5px;
  color: var(--text-secondary);
  font-family: var(--font-mono);
}

.branch-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  font-family: var(--font-mono);
  color: var(--text-secondary);
  background: var(--bg-subtle);
  border-radius: var(--border-radius-xs);
  padding: 2px 8px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  border: 1px solid var(--border-color);
}

.text-muted {
  font-size: 12px;
  color: var(--text-muted);
}

/* ── Dialog ── */
.dialog-search {
  margin-bottom: 12px;
}

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

.avail-repo-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.12s;
}

.avail-repo-item:last-child {
  border-bottom: none;
}

.avail-repo-item:hover:not(.disabled) {
  background: var(--bg-hover);
}

.avail-repo-item.disabled {
  cursor: default;
  opacity: 0.55;
}

.avail-repo-item.selected {
  background: var(--chinese-red-light);
}

.avail-check {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-placeholder {
  width: 16px;
  height: 16px;
  border: 1.5px solid #C8D0DC;
  border-radius: var(--border-radius-xs);
}

.avail-info {
  flex: 1;
  min-width: 0;
}

.avail-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.avail-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.avail-stars {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  flex-shrink: 0;
}
</style>
