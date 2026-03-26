<template>
  <div class="quota-settings">

    <!-- Fixed header -->
    <div class="section-header">
      <h2 class="section-title">翻译额度</h2>
      <p class="section-desc">查看平台免费额度使用情况及翻译用量明细</p>
    </div>

    <!-- Top summary (stats + progress + banners), shrinks to natural height -->
    <div class="quota-top">
      <div v-if="quota" class="quota-content">
        <!-- Stats row -->
        <div class="stats-row">
          <div class="stat-block">
            <div class="stat-label">免费总额度</div>
            <div class="stat-value">{{ formatNum(quota.freeQuota) }}</div>
            <div class="stat-unit">tokens</div>
          </div>
          <div class="stat-divider"/>
          <div class="stat-block">
            <div class="stat-label">已使用</div>
            <div class="stat-value used">{{ formatNum(quota.usedQuota) }}</div>
            <div class="stat-unit">tokens</div>
          </div>
          <div class="stat-divider"/>
          <div class="stat-block">
            <div class="stat-label">剩余额度</div>
            <div :class="['stat-value', usagePercent >= 90 ? 'danger' : 'remain']">
              {{ formatNum(quota.remaining) }}
            </div>
            <div class="stat-unit">tokens</div>
          </div>
        </div>

        <!-- Progress bar -->
        <div class="progress-section">
          <div class="progress-header">
            <span class="progress-label">额度使用率</span>
            <span :class="['progress-pct', usagePercent >= 90 ? 'danger-text' : '']">{{ usagePercent }}%</span>
          </div>
          <div class="progress-track">
            <div
                class="progress-fill"
                :class="{ 'fill-danger': usagePercent >= 90, 'fill-warn': usagePercent >= 70 && usagePercent < 90 }"
                :style="{ width: usagePercent + '%' }"
            />
          </div>
          <div class="progress-foot">
            <span>0</span>
            <span>{{ formatNum(quota.freeQuota) }} tokens</span>
          </div>
        </div>

        <!-- Banners -->
        <div v-if="quota.hasCustomApiKey" class="custom-key-banner">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path
                d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4"/>
          </svg>
          <span>已配置自带 API Key，翻译消耗由您的 OpenRouter 账户承担，不占用平台额度</span>
          <router-link to="/settings/api-key" class="banner-link">管理 Key →</router-link>
        </div>
        <div v-else-if="usagePercent >= 80" class="warn-banner">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>免费额度已使用超过 80%，建议配置自带 API Key 以避免翻译中断</span>
          <router-link to="/settings/api-key" class="banner-link warn-link">配置 Key →</router-link>
        </div>
      </div>
      <div v-else class="skeleton-wrap">
        <el-skeleton :rows="4" animated/>
      </div>
    </div>

    <!-- Usage section: fills remaining height, table scrolls internally -->
    <div class="usage-section">

      <!-- Usage header bar: title + page size selector -->
      <div class="usage-header">
        <h3 class="usage-title">用量明细</h3>
        <div class="page-size-wrap">
          <span class="size-label">每页</span>
          <div class="size-tabs">
            <button
                v-for="s in pageSizeOptions"
                :key="s"
                :class="['size-tab', { active: currentPageSize === s }]"
                @click="changePageSize(s)"
            >{{ s }}
            </button>
          </div>
          <span class="size-label">条</span>
        </div>
      </div>

      <!-- Table fills remaining height, scrolls internally -->
      <div class="table-wrap">
        <el-table
            :data="usageRecords"
            v-loading="usageLoading"
            height="100%"
            size="small"
            :border="false"
            style="width:100%"
        >
          <el-table-column label="时间" width="150">
            <template #default="{ row }">
              <span class="mono-text">{{ formatDate(row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="仓库" prop="repositoryName" min-width="160">
            <template #default="{ row }">
              <span class="repo-label">{{ row.repositoryName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="任务" width="80">
            <template #default="{ row }">
              <span class="mono-text task-id">#{{ row.taskId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="消耗" width="120">
            <template #default="{ row }">
              <span class="mono-text token-val">{{ row.tokensUsed.toLocaleString() }}</span>
              <span class="token-unit">tokens</span>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="110">
            <template #default="{ row }">
              <span :class="['source-badge', row.source === 'platform' ? 'badge-platform' : 'badge-custom']">
                {{ row.source === 'platform' ? '平台额度' : '自带 Key' }}
              </span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="table-empty">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#CBD5E1" stroke-width="1.5">
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
              </svg>
              <div>暂无用量记录</div>
            </div>
          </template>
        </el-table>
      </div>

      <!-- Pagination bar at bottom of usage section -->
      <div class="pagination-wrap">
        <span class="pager-info">第 {{ currentPage }} 页 / 共 {{ total }} 条</span>
        <el-pagination
            v-model:current-page="currentPage"
            :page-size="currentPageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="loadUsage"
            small
        />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { quotaApi } from '@/api/quota'
import type { QuotaInfo, QuotaUsageRecord } from '@/types'

const pageSizeOptions = [10, 20, 50, 100]

const quota = ref<QuotaInfo | null>(null)
const usageRecords = ref<QuotaUsageRecord[]>([])
const usageLoading = ref(false)
const currentPage = ref(1)
const currentPageSize = ref(10)
const total = ref(0)

const usagePercent = computed(() => {
  if (!quota.value || !quota.value.freeQuota) return 0
  return Math.min(100, Math.round((quota.value.usedQuota / quota.value.freeQuota) * 100))
})

function formatNum(n: number) {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return n.toLocaleString()
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

function changePageSize(s: number) {
  currentPageSize.value = s
  currentPage.value = 1
  loadUsage()
}

async function loadUsage() {
  usageLoading.value = true
  try {
    const res = await quotaApi.getUsage({ page: currentPage.value, size: currentPageSize.value })
    usageRecords.value = res.records
    total.value = res.total
  } finally {
    usageLoading.value = false
  }
}

onMounted(async () => {
  const [q] = await Promise.allSettled([quotaApi.get()])
  if (q.status === 'fulfilled') quota.value = q.value
  loadUsage()
})
</script>

<style scoped>
/* Full-height flex column, no overflow */
.quota-settings {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* Fixed header */
.section-header {
  flex-shrink: 0;
  padding: 20px 28px 16px;
  border-bottom: 1px solid var(--border-color);
  background: #fff;
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

/* Top summary (stats + progress) — shrinks to content height */
.quota-top {
  flex-shrink: 0;
  padding: 20px 28px 16px;
  border-bottom: 1px solid var(--border-color);
}

.quota-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Stats row */
.stats-row {
  display: flex;
  align-items: stretch;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
}

.stat-block {
  flex: 1;
  padding: 16px 20px;
  text-align: center;
}

.stat-divider {
  width: 1px;
  background: var(--border-color);
  align-self: stretch;
}

.stat-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  font-family: var(--font-mono);
  color: var(--text-primary);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-value.used {
  color: #F59E0B;
}

.stat-value.remain {
  color: #22C55E;
}

.stat-value.danger {
  color: #EF4444;
}

.stat-unit {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* Progress */
.progress-section {
  padding: 14px 16px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: 10px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}

.progress-pct {
  font-size: 13px;
  font-weight: 600;
  font-family: var(--font-mono);
  color: var(--text-primary);
}

.progress-pct.danger-text {
  color: #EF4444;
}

.progress-track {
  height: 8px;
  background: #E2E8F0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: #22C55E;
  border-radius: 4px;
  transition: width 0.6s ease;
}

.progress-fill.fill-warn {
  background: #F59E0B;
}

.progress-fill.fill-danger {
  background: #EF4444;
}

.progress-foot {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* Banners */
.custom-key-banner, .warn-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
}

.custom-key-banner {
  background: #F0FDF4;
  border: 1px solid #BBF7D0;
  color: #15803D;
}

.warn-banner {
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  color: #92400E;
}

.banner-link {
  margin-left: auto;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--chinese-red);
  text-decoration: none;
  flex-shrink: 0;
}

.warn-link {
  color: #D97706;
}

.skeleton-wrap {
  padding: 4px 0;
}

/* Usage section: fills remaining height */
.usage-section {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border-top: 1px solid var(--border-color);
}

.usage-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 28px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-page);
}

.usage-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0;
}

/* Page size selector */
.page-size-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.size-label {
  font-size: 12px;
  color: var(--text-muted);
}

.size-tabs {
  display: flex;
  gap: 2px;
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 7px;
  padding: 2px;
}

.size-tab {
  padding: 3px 9px;
  border: none;
  border-radius: 5px;
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
  background: #fdefee;
  color: var(--chinese-red);
}

/* Table wrapper fills remaining space */
.table-wrap {
  flex: 1;
  overflow: hidden;
}

/* Row height */
:deep(.el-table__row) td {
  height: 46px;
}

/* Table content */
.mono-text {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-secondary);
}

.task-id {
  color: var(--text-muted);
}

.token-val {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.token-unit {
  font-size: 11px;
  color: var(--text-muted);
  margin-left: 2px;
}

.repo-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.source-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11.5px;
  font-weight: 500;
}

.badge-platform {
  background: #FFFBEB;
  color: #92400E;
  border: 1px solid #FDE68A;
}

.badge-custom {
  background: #F0FDF4;
  color: #15803D;
  border: 1px solid #BBF7D0;
}

.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px;
  color: var(--text-muted);
  font-size: 13px;
}

/* Pagination fixed at bottom */
.pagination-wrap {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 28px;
  border-top: 1px solid var(--border-color);
  background: #fff;
}

.pager-info {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}
</style>
