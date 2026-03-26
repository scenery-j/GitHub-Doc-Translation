<template>
  <AppLayout>
    <div class="config-wrapper" v-loading="loading">
      <!-- ===== Hero Header ===== -->
      <PageHero
          title="翻译配置"
          back-to="/repos"
          :trail="[
          { label: '仓库管理', to: '/repos' },
          { label: repoName || '...', to: `/repos/${repoId}` },
          { label: '翻译配置' }
        ]"
      >
        <template #right>
          <span v-if="repoName" class="hero-repo-badge">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            {{ repoName }}
          </span>
        </template>
      </PageHero>

      <!-- ===== Content ===== -->
      <div class="config-content">
        <div class="config-body">

          <!-- ── Global: Basic Settings ── -->
          <div class="config-section">
            <h2 class="config-section-title">基本设置</h2>
            <el-form :model="globalConfig" label-position="top" class="config-form">
              <el-form-item label="基准语言">
                <el-select v-model="globalConfig.baseLanguage" style="width:240px">
                  <el-option v-for="l in BASE_LANGUAGES" :key="l.code" :label="l.name" :value="l.code"/>
                </el-select>
              </el-form-item>

              <el-form-item label="目标语言">
                <div class="lang-checkbox-grid">
                  <div
                      v-for="lang in SUPPORTED_LANGUAGES"
                      :key="lang.code"
                      :class="['lang-option', { selected: globalConfig.targetLanguages.includes(lang.code) }]"
                      @click="toggleLang(lang.code)"
                  >
                    <svg v-if="globalConfig.targetLanguages.includes(lang.code)" width="14" height="14"
                         viewBox="0 0 24 24" fill="none" stroke="#DE2910" stroke-width="2.5">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    <span>{{ lang.nativeName }} ({{ lang.code }})</span>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <!-- ── Global: AI Model Settings ── -->
          <div class="config-section">
            <h2 class="config-section-title">AI 模型设置</h2>
            <el-form :model="globalConfig" label-position="top" class="config-form">
              <el-form-item label="翻译模型">
                <el-select
                    v-model="globalConfig.aiModel"
                    filterable
                    style="width:400px"
                    :loading="modelsLoading"
                    placeholder="选择 AI 模型"
                >
                  <el-option-group label="🆓 免费模型" v-if="freeModels.length">
                    <el-option v-for="m in freeModels" :key="m.id" :value="m.id" :label="m.name">
                      <div class="model-option">
                        <span class="model-name">{{ m.name }}</span>
                        <span class="model-price free-badge">免费</span>
                      </div>
                    </el-option>
                  </el-option-group>
                  <el-option-group label="💰 付费模型" v-if="paidModels.length">
                    <el-option v-for="m in paidModels" :key="m.id" :value="m.id" :label="m.name">
                      <div class="model-option">
                        <span class="model-name">{{ m.name }}</span>
                        <span class="model-price">${{ m.inputPrice }}/${{ m.outputPrice }}/1M</span>
                      </div>
                    </el-option>
                  </el-option-group>
                </el-select>
              </el-form-item>

              <div class="model-info" v-if="selectedModel">
                <div class="model-info-item">
                  <span class="info-key">费用</span>
                  <span class="info-val" v-if="selectedModel.isFree"><span class="free-tag">免费</span></span>
                  <span class="info-val" v-else>${{
                      selectedModel.inputPrice
                    }} / 1M input · ${{ selectedModel.outputPrice }} / 1M output</span>
                </div>
                <div class="model-info-item">
                  <span class="info-key">上下文</span>
                  <span class="info-val">{{
                      selectedModel.contextLength ? (selectedModel.contextLength / 1000).toFixed(0) + 'K tokens' : '-'
                    }}</span>
                </div>
              </div>

              <div class="model-recommend" v-if="recommended">
                <div class="recommend-title">推荐模型<span class="recommend-hint">平台优先推荐免费模型</span></div>
                <div class="recommend-columns">
                  <div class="rec-column">
                    <div class="rec-col-header free">🆓 免费</div>
                    <div v-for="m in recommended.free" :key="m.id"
                         :class="['rec-card', { active: globalConfig.aiModel === m.id }]"
                         @click="globalConfig.aiModel = m.id">
                      <div class="rec-card-top"><span class="rec-name">{{ m.name }}</span><span v-if="m.tag"
                                                                                                class="rec-tag free-tag-sm">{{
                          m.tag
                        }}</span></div>
                      <div class="rec-desc">{{ m.desc }}</div>
                    </div>
                  </div>
                  <div class="rec-column">
                    <div class="rec-col-header paid">💰 付费</div>
                    <div v-for="m in recommended.paid" :key="m.id"
                         :class="['rec-card', { active: globalConfig.aiModel === m.id }]"
                         @click="globalConfig.aiModel = m.id">
                      <div class="rec-card-top"><span class="rec-name">{{ m.name }}</span><span v-if="m.tag"
                                                                                                class="rec-tag paid-tag-sm">{{
                          m.tag
                        }}</span></div>
                      <div class="rec-desc">{{ m.desc }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-form>
          </div>

          <!-- ── Per-branch Config ── -->
          <div class="config-section">
            <h2 class="config-section-title">分支翻译配置</h2>

            <!-- Branch Tab Bar -->
            <div class="branch-tab-bar">
              <div
                  v-for="b in branches"
                  :key="b.branchName"
                  :class="['branch-tab', { active: activeBranch === b.branchName }]"
                  @click="switchBranch(b.branchName)"
              >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="6" y1="3" x2="6" y2="15"/>
                  <circle cx="18" cy="6" r="3"/>
                  <circle cx="6" cy="18" r="3"/>
                  <path d="M18 9a9 9 0 0 1-9 9"/>
                </svg>
                <span>{{ b.branchName }}</span>
                <span
                    v-if="branches.length > 1"
                    class="branch-tab-close"
                    @click.stop="confirmDeleteBranch(b.branchName)"
                >×</span>
              </div>
              <div class="branch-tab-add" @click="openAddBranchDialog">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                添加分支
              </div>
            </div>

            <!-- Branch Config Panel -->
            <div class="branch-panel" v-if="activeBranchConfig" v-loading="branchLoading">
              <!-- Auto trigger -->
              <div class="auto-trigger-content">
                <div class="auto-trigger-left">
                  <div class="auto-trigger-label">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                    </svg>
                    自动触发翻译
                  </div>
                  <div class="auto-trigger-desc">开启后，push 到此分支时自动触发增量翻译并提交 PR</div>
                </div>
                <el-switch v-model="activeBranchConfig.webhookActive" active-text="开启" inactive-text="关闭"
                           style="--el-switch-on-color: #DE2910"/>
              </div>

              <!-- Auto merge PR -->
              <div class="auto-trigger-content" style="margin-top:8px">
                <div class="auto-trigger-left">
                  <div class="auto-trigger-label">
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    翻译完成后自动合并 PR
                  </div>
                  <div class="auto-trigger-desc">
                    开启后翻译完成提交 PR 时会自动触发合并（squash merge）。若存在分支保护规则或合并检查，自动合并可能失败，届时任务状态将标记为失败，PR
                    保持开启供手动合并。
                  </div>
                </div>
                <el-switch v-model="activeBranchConfig.autoMergePr" active-text="开启" inactive-text="关闭"
                           style="--el-switch-on-color: #DE2910"/>
              </div>

              <!-- File Selection & Ignore -->
              <div class="file-config-split" style="margin-top:16px">
                <div class="file-tree-panel">
                  <div class="panel-header">
                    <span class="panel-title">文件选择</span>
                    <div class="tree-actions">
                      <span class="action-btn" @click="selectAll">全选</span>
                      <span class="sep">|</span>
                      <span class="action-btn" @click="deselectAll">全不选</span>
                    </div>
                  </div>
                  <div class="tree-wrap" v-loading="treeLoading">
                    <el-tree
                        ref="treeRef"
                        :data="treeData"
                        show-checkbox
                        node-key="path"
                        :props="treeProps"
                        :default-checked-keys="activeBranchConfig.selectedPaths"
                        @check="onTreeCheck"
                        class="file-tree"
                    >
                      <template #default="{ node, data }">
                        <span class="tree-node">
                          <svg v-if="data.type === 'directory'" width="14" height="14" viewBox="0 0 24 24"
                               fill="#F59E0B" stroke="#F59E0B" stroke-width="0">
                            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                          </svg>
                          <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B"
                               stroke-width="2">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                            <polyline points="14 2 14 8 20 8"/>
                          </svg>
                          <span class="node-label">{{ node.label }}</span>
                          <span v-if="data.size" class="node-size">{{ formatSize(data.size) }}</span>
                        </span>
                      </template>
                    </el-tree>
                    <EmptyState v-if="!treeData.length && !treeLoading" title="暂无 Markdown 文件"
                                description="仓库中未找到 .md 文件"/>
                  </div>
                </div>

                <div class="ignore-panel">
                  <div class="panel-header">
                    <span class="panel-title">.gitdoc-ignore</span>
                    <div class="sync-status" v-if="syncStatus">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#22C55E" stroke-width="2">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      {{ syncStatus }}
                    </div>
                  </div>
                  <textarea
                      v-model="ignoreContent"
                      class="ignore-editor"
                      placeholder="# 忽略规则 (类似 .gitignore 语法)&#10;# 示例:&#10;CHANGELOG.md&#10;docs/internal/&#10;**/draft-*.md"
                      @input="onIgnoreChange"
                      spellcheck="false"
                  ></textarea>
                </div>
              </div>

              <!-- Output path preview -->
              <div class="output-section">
                <div class="output-label">输出路径模板</div>
                <el-input
                    v-model="activeBranchConfig.outputPathPattern"
                    style="width:320px"
                    placeholder="translations/{lang}/"
                />
                <div class="output-preview-hint" v-if="exampleFile">
                  示例: {{ exampleFile }} → {{
                    activeBranchConfig.outputPathPattern.replace('{lang}', 'en')
                  }}{{ exampleFile }}
                </div>
              </div>
            </div>

            <EmptyState v-else-if="!branchLoading" title="暂无分支配置" description="请点击「添加分支」开始配置"/>
          </div>

        </div><!-- /.config-body -->
      </div><!-- /.config-content -->

      <!-- ===== Sticky Action Bar ===== -->
      <div class="config-sticky-bar">
        <div class="sticky-bar-inner">
          <div class="sticky-hint"
               v-if="globalConfig.targetLanguages.length === 0 || !activeBranchConfig?.selectedPaths.length">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ globalConfig.targetLanguages.length === 0 ? '请选择目标语言' : '请在当前分支下选择翻译文件' }}
          </div>
          <div class="sticky-branch-hint" v-if="activeBranch">
            当前分支：<strong>{{ activeBranch }}</strong>
          </div>
          <div class="sticky-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" plain :loading="saving" @click="saveConfig(false)">保存配置</el-button>
            <el-button type="primary" :loading="savingAndStart" @click="saveAndStart">保存并开始翻译</el-button>
          </div>
        </div>
      </div>
    </div><!-- /.config-wrapper -->

    <!-- Add Branch Dialog -->
    <el-dialog v-model="addBranchDialogVisible" title="添加分支" width="480px" :close-on-click-modal="false">
      <div class="add-branch-body">
        <el-form label-position="top">
          <el-form-item label="选择远端分支">
            <el-select
                v-model="newBranchName"
                filterable
                allow-create
                placeholder="选择或输入分支名称"
                style="width:100%"
                :loading="availableBranchesLoading"
            >
              <el-option
                  v-for="b in availableBranches"
                  :key="b"
                  :label="b"
                  :value="b"
                  :disabled="branches.some(existing => existing.branchName === b)"
              >
                <span>{{ b }}</span>
                <span v-if="branches.some(ex => ex.branchName === b)" class="already-configured">已配置</span>
              </el-option>
            </el-select>
            <div class="add-branch-hint">如未找到分支，可直接输入分支名称</div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="addBranchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addingBranch" @click="doAddBranch">添加</el-button>
      </template>
    </el-dialog>
  </AppLayout>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHero from '@/components/layout/PageHero.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { reposApi } from '@/api/repos'
import { tasksApi } from '@/api/tasks'
import type { AIModel, BranchConfig, FileTreeNode, RecommendedModel, TranslationConfig } from '@/types'
import { BASE_LANGUAGES, SUPPORTED_LANGUAGES } from '@/types'

const route = useRoute()
const router = useRouter()
const repoId = Number(route.params.id)
const repoName = ref('')
const defaultBranch = ref('')

const loading = ref(false)
const treeLoading = ref(false)
const modelsLoading = ref(false)
const branchLoading = ref(false)
const saving = ref(false)
const savingAndStart = ref(false)
const syncStatus = ref('')

// ── Global config ──────────────────────────────────────────────────────────
const globalConfig = ref<TranslationConfig>({
  baseLanguage: 'zh',
  targetLanguages: [],
  aiModel: '',
})

// ── Branch config ──────────────────────────────────────────────────────────
const branches = ref<BranchConfig[]>([])
const activeBranch = ref('')
const activeBranchConfig = computed(
    () => branches.value.find(b => b.branchName === activeBranch.value) ?? null
)

const treeData = ref<any[]>([])
const treeRef = ref()
const treeProps = { label: 'name', children: 'children' }
const ignoreContent = ref('')

// ── Models ─────────────────────────────────────────────────────────────────
const models = ref<AIModel[]>([])
const freeModels = computed(() => models.value.filter(m => m.isFree))
const paidModels = computed(() => models.value.filter(m => !m.isFree))
const recommended = ref<{ free: RecommendedModel[]; paid: RecommendedModel[] } | null>(null)
const selectedModel = computed(() => models.value.find(m => m.id === globalConfig.value.aiModel))

const exampleFile = computed(() => {
  const files = activeBranchConfig.value?.selectedPaths.filter(p => p.endsWith('.md')) ?? []
  return files[0] || null
})

// ── Add Branch Dialog ──────────────────────────────────────────────────────
const addBranchDialogVisible = ref(false)
const newBranchName = ref('')
const availableBranches = ref<string[]>([])
const availableBranchesLoading = ref(false)
const addingBranch = ref(false)

// ── Language helpers ──────────────────────────────────────────────────────
function toggleLang(code: string) {
  const idx = globalConfig.value.targetLanguages.indexOf(code)
  if (idx >= 0) globalConfig.value.targetLanguages.splice(idx, 1)
  else globalConfig.value.targetLanguages.push(code)
}

function formatSize(bytes: number) {
  if (bytes < 1024) return `${bytes}B`
  return `${(bytes / 1024).toFixed(1)}KB`
}

// ── Tree helpers ──────────────────────────────────────────────────────────
function hasMd(nodes: FileTreeNode[]): boolean {
  for (const n of nodes) {
    if (!n.children && n.path.endsWith('.md')) return true
    if (n.children && hasMd(n.children)) return true
  }
  return false
}

function flattenTree(nodes: FileTreeNode[]): any[] {
  const result: any[] = []
  for (const n of nodes) {
    const name = n.path.split('/').pop() || n.path
    if (!n.children) {
      if (n.path.endsWith('.md')) result.push({ ...n, name })
    } else {
      if (hasMd(n.children)) {
        result.push({ ...n, name, children: flattenTree(n.children) })
      }
    }
  }
  return result
}

function selectAll() {
  treeRef.value?.setCheckedNodes(flatAllLeaves(treeData.value))
  updateSelectedPaths()
}

function deselectAll() {
  treeRef.value?.setCheckedKeys([])
  if (activeBranchConfig.value) activeBranchConfig.value.selectedPaths = []
}

function flatAllLeaves(nodes: any[]): any[] {
  const result: any[] = []
  for (const n of nodes) {
    if (!n.children) result.push(n)
    else result.push(...flatAllLeaves(n.children))
  }
  return result
}

function onTreeCheck() {
  updateSelectedPaths()
}

function updateSelectedPaths() {
  if (!activeBranchConfig.value) return
  const checked = treeRef.value?.getCheckedKeys() || []
  activeBranchConfig.value.selectedPaths = checked.filter((k: string) => k.endsWith('.md'))
}

function onIgnoreChange() {
  if (!activeBranchConfig.value) return
  const patterns = ignoreContent.value.split('\n').filter(l => l.trim() && !l.trim().startsWith('#'))
  activeBranchConfig.value.ignorePatterns = patterns

  const allKeys = treeRef.value?.getCheckedKeys() || []
  const newChecked = allKeys.filter((key: string) => !patterns.some(p => {
    const pattern = p.trim()
    if (!pattern) return false
    if (key === pattern) return true
    if (pattern.endsWith('/') && key.startsWith(pattern)) return true
    if (pattern.startsWith('**/')) {
      const suffix = pattern.slice(3)
      return key.endsWith(suffix) || key.includes('/' + suffix)
    }
    return false
  }))
  if (treeRef.value) {
    treeRef.value.setCheckedKeys(newChecked)
    updateSelectedPaths()
    syncStatus.value = '已同步'
    setTimeout(() => syncStatus.value = '', 2000)
  }
}

// ── Branch switching ──────────────────────────────────────────────────────
async function switchBranch(branchName: string) {
  if (activeBranch.value === branchName) return
  activeBranch.value = branchName
  await loadBranchTree(branchName)
  await loadBranchIgnore(branchName)
  // Reset tree checked state after DOM updates
  await nextTick()
  setTimeout(() => {
    const cfg = activeBranchConfig.value
    if (treeRef.value && cfg) {
      treeRef.value.setCheckedKeys(cfg.selectedPaths)
    }
  }, 100)
}

async function loadBranchTree(branchName: string) {
  treeLoading.value = true
  try {
    const data = await reposApi.getTree(repoId, branchName)
    treeData.value = flattenTree(data)
  } catch {
    treeData.value = []
  } finally {
    treeLoading.value = false
  }
}

async function loadBranchIgnore(branchName: string) {
  try {
    const content = await reposApi.getIgnore(repoId, branchName)
    ignoreContent.value = typeof content === 'string' ? content : ''
  } catch {
    ignoreContent.value = ''
  }
}

// ── Add Branch ────────────────────────────────────────────────────────────
async function openAddBranchDialog() {
  newBranchName.value = ''
  addBranchDialogVisible.value = true
  availableBranchesLoading.value = true
  try {
    availableBranches.value = await reposApi.getAvailableBranches(repoId)
  } catch {
    availableBranches.value = []
  } finally {
    availableBranchesLoading.value = false
  }
}

async function doAddBranch() {
  if (!newBranchName.value.trim()) {
    ElMessage.warning('请输入分支名称')
    return
  }
  addingBranch.value = true
  try {
    const newCfg = await reposApi.addBranch(repoId, newBranchName.value.trim())
    branches.value.push(newCfg)
    addBranchDialogVisible.value = false
    await switchBranch(newCfg.branchName)
    ElMessage.success(`分支 ${newCfg.branchName} 已添加`)
  } finally {
    addingBranch.value = false
  }
}

async function confirmDeleteBranch(branchName: string) {
  await ElMessageBox.confirm(`确认删除分支「${branchName}」的翻译配置？`, '删除分支配置', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
  try {
    await reposApi.deleteBranch(repoId, branchName)
    const idx = branches.value.findIndex(b => b.branchName === branchName)
    if (idx >= 0) branches.value.splice(idx, 1)
    if (activeBranch.value === branchName && branches.value.length > 0) {
      await switchBranch(branches.value[0].branchName)
    }
    ElMessage.success('已删除')
  } catch { /* interceptor handles it */
  }
}

// ── Save ──────────────────────────────────────────────────────────────────
async function saveConfig(startTranslation = false) {
  if (globalConfig.value.targetLanguages.length === 0) {
    ElMessage.warning('请至少选择一种目标语言')
    return
  }

  // When starting translation, check all branches have files; otherwise only check active branch
  if (startTranslation) {
    const branchesWithFiles = branches.value.filter(b => b.selectedPaths.length > 0)
    if (branchesWithFiles.length === 0) {
      ElMessage.warning('请至少在一个分支下选择翻译文件')
      return
    }
  } else {
    if (!activeBranchConfig.value?.selectedPaths.length) {
      ElMessage.warning('请在当前分支下至少选择一个翻译文件')
      return
    }
  }

  saving.value = !startTranslation
  savingAndStart.value = startTranslation

  try {
    // Save global config
    await reposApi.updateConfig(repoId, {
      baseLanguage: globalConfig.value.baseLanguage,
      targetLanguages: globalConfig.value.targetLanguages,
      aiModel: globalConfig.value.aiModel,
    })

    // Save current branch config
    if (activeBranch.value && activeBranchConfig.value) {
      await reposApi.updateBranchConfig(repoId, activeBranch.value, {
        selectedPaths: activeBranchConfig.value.selectedPaths,
        ignorePatterns: activeBranchConfig.value.ignorePatterns,
        outputPathPattern: activeBranchConfig.value.outputPathPattern,
        webhookActive: activeBranchConfig.value.webhookActive,
        autoMergePr: activeBranchConfig.value.autoMergePr,
      })
      await reposApi.updateIgnore(repoId, activeBranch.value, ignoreContent.value)
    }

    if (startTranslation) {
      const branchesWithFiles = branches.value.filter(b => b.selectedPaths.length > 0)
      const { taskIds } = await tasksApi.trigger(repoId, {
        type: 'full',
        branches: branchesWithFiles.map(b => b.branchName),
      })
      ElMessage.success(`配置已保存，已为 ${taskIds.length} 个分支启动翻译任务`)
      router.push(`/repos/${repoId}?tab=tasks`)
    } else {
      ElMessage.success('配置保存成功')
    }
  } finally {
    saving.value = false
    savingAndStart.value = false
  }
}

async function saveAndStart() {
  await saveConfig(true)
}

// ── Mount ─────────────────────────────────────────────────────────────────
onMounted(async () => {
  loading.value = true
  modelsLoading.value = true

  try {
    const [cfgRes, branchesRes, modelsRes, repoRes] = await Promise.allSettled([
      reposApi.getConfig(repoId),
      reposApi.getBranches(repoId),
      reposApi.getModels(),
      reposApi.get(repoId),
    ])

    if (cfgRes.status === 'fulfilled') {
      globalConfig.value = cfgRes.value
    }
    if (branchesRes.status === 'fulfilled') {
      branches.value = branchesRes.value
    }
    if (modelsRes.status === 'fulfilled') {
      models.value = modelsRes.value.models
      recommended.value = modelsRes.value.recommended
    }
    if (repoRes.status === 'fulfilled') {
      repoName.value = repoRes.value.fullName
      defaultBranch.value = repoRes.value.defaultBranch
    }

    // Default AI model
    const savedModel = globalConfig.value.aiModel
    if (!savedModel || !models.value.some(m => m.id === savedModel)) {
      const firstFree = freeModels.value[0]?.id ?? recommended.value?.free?.[0]?.id
      globalConfig.value.aiModel = firstFree || models.value[0]?.id || ''
    }

    // If no branches configured yet, auto-add the default branch
    if (branches.value.length === 0 && defaultBranch.value) {
      try {
        const newCfg = await reposApi.addBranch(repoId, defaultBranch.value)
        branches.value.push(newCfg)
      } catch {
        // ignore, fall through to empty state
      }
    }

    // Activate first branch
    if (branches.value.length > 0) {
      const firstBranch = branches.value[0].branchName
      activeBranch.value = firstBranch
      await Promise.allSettled([
        loadBranchTree(firstBranch),
        loadBranchIgnore(firstBranch),
      ])
    }
  } finally {
    loading.value = false
    modelsLoading.value = false
  }

  // Restore tree checked state
  setTimeout(() => {
    const cfg = activeBranchConfig.value
    if (treeRef.value && cfg?.selectedPaths.length) {
      treeRef.value.setCheckedKeys(cfg.selectedPaths)
    }
  }, 150)
})
</script>

<style scoped>
/* ===== Layout Shell ===== */
.config-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.config-content {
  flex: 1;
  overflow-y: auto;
}

.config-body {
  padding: 24px 32px 8px;
}

.hero-repo-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 12px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  font-family: var(--font-mono);
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== Sticky Bottom Bar ===== */
.config-sticky-bar {
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  border-top: 1px solid var(--border-color);
  z-index: 20;
}

.sticky-bar-inner {
  padding: 12px 32px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sticky-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #D97706;
}

.sticky-branch-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.sticky-branch-hint strong {
  color: var(--text-primary);
  font-family: var(--font-mono);
}

.sticky-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* ===== Sections ===== */
.config-section {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 16px;
}

.config-section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.config-form {
  max-width: 600px;
}

/* ===== Language ===== */
.lang-checkbox-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.lang-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: border-color 0.15s, background 0.15s;
}

.lang-option:hover {
  border-color: var(--chinese-red);
  background: #fdefee;
}

.lang-option.selected {
  border-color: var(--chinese-red);
  background: #fdefee;
  color: var(--chinese-red);
  font-weight: 500;
}

/* ===== Model ===== */
.model-option {
  display: flex;
  justify-content: space-between;
  width: 100%;
}

.model-name {
  font-size: 13px;
}

.model-price {
  font-size: 11px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.model-info {
  margin-top: 12px;
  background: var(--bg-page);
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.model-info-item {
  display: flex;
  gap: 10px;
  font-size: 13px;
}

.info-key {
  color: var(--text-muted);
  min-width: 80px;
}

.info-val {
  color: var(--text-primary);
  font-family: var(--font-mono);
}

.model-price.free-badge {
  background: #F0FDF4;
  color: #16A34A;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.free-tag {
  background: #F0FDF4;
  color: #16A34A;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.model-recommend {
  margin-top: 12px;
}

.recommend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.recommend-hint {
  font-size: 11px;
  font-weight: 400;
  background: #F0FDF4;
  color: #16A34A;
  padding: 1px 8px;
  border-radius: 10px;
}

.recommend-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.rec-column {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rec-col-header {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  margin-bottom: 2px;
}

.rec-col-header.free {
  background: #F0FDF4;
  color: #16A34A;
}

.rec-col-header.paid {
  background: #FEF3C7;
  color: #B45309;
}

.rec-card {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.rec-card:hover {
  border-color: var(--chinese-red);
  background: #fdfafa;
}

.rec-card.active {
  border-color: var(--chinese-red);
  background: #fdefee;
}

.rec-card-top {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 3px;
}

.rec-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.free-tag-sm {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: #F0FDF4;
  color: #16A34A;
  font-weight: 600;
}

.paid-tag-sm {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  background: #FEF3C7;
  color: #B45309;
  font-weight: 600;
}

.rec-desc {
  font-size: 11px;
  color: var(--text-muted);
  line-height: 1.4;
}

.rec-tag {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  font-weight: 600;
}

/* ===== Branch Tab Bar ===== */
.branch-tab-bar {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.branch-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  font-family: var(--font-mono);
  color: var(--text-secondary);
  background: var(--bg-page);
  transition: all 0.15s;
}

.branch-tab:hover {
  border-color: var(--chinese-red);
  color: var(--chinese-red);
}

.branch-tab.active {
  border-color: var(--chinese-red);
  background: #fdefee;
  color: var(--chinese-red);
  font-weight: 500;
}

.branch-tab-close {
  margin-left: 2px;
  font-size: 15px;
  line-height: 1;
  color: var(--text-muted);
  padding: 0 2px;
  border-radius: 3px;
  transition: background 0.1s, color 0.1s;
}

.branch-tab-close:hover {
  background: #fecaca;
  color: #dc2626;
}

.branch-tab-add {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border: 1px dashed var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-muted);
  transition: all 0.15s;
}

.branch-tab-add:hover {
  border-color: var(--chinese-red);
  color: var(--chinese-red);
  background: #fdefee;
}

/* ===== Branch Panel ===== */
.branch-panel {
  padding-top: 4px;
}

/* Auto-trigger */
.auto-trigger-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  background: var(--bg-page);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.auto-trigger-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.auto-trigger-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.auto-trigger-desc {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

/* File config split */
.file-config-split {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.file-tree-panel, .ignore-panel {
  display: flex;
  flex-direction: column;
}

.file-tree-panel {
  border-right: 1px solid var(--border-color);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border-color);
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.tree-actions {
  display: flex;
  gap: 4px;
  font-size: 12px;
}

.action-btn {
  color: var(--chinese-red);
  cursor: pointer;
}

.action-btn:hover {
  opacity: 0.8;
}

.sep {
  color: var(--border-color);
}

.sync-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #22C55E;
}

.tree-wrap {
  flex: 1;
  padding: 8px;
  min-height: 300px;
  overflow-y: auto;
  max-height: 400px;
}

.file-tree {
  background: transparent;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
}

.node-label {
  color: var(--text-primary);
}

.node-size {
  color: var(--text-muted);
  font-size: 11px;
  font-family: var(--font-mono);
  margin-left: 4px;
}

.ignore-editor {
  flex: 1;
  padding: 12px;
  border: none;
  resize: none;
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-primary);
  background: #FAFAFA;
  min-height: 300px;
  outline: none;
}

.ignore-editor::placeholder {
  color: var(--text-muted);
}

/* Output path */
.output-section {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.output-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.output-preview-hint {
  font-size: 12px;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* Add branch dialog */
.add-branch-body {
  padding: 0 4px;
}

.add-branch-hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 6px;
}

.already-configured {
  font-size: 11px;
  color: var(--text-muted);
  margin-left: 8px;
}
</style>
