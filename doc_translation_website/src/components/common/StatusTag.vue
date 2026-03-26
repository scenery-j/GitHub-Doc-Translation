<template>
  <el-tag :type="tagType" size="small" :effect="effect">{{ label }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  status: string
  effect?: 'light' | 'dark' | 'plain'
}>()

const statusMap: Record<string, { type: '' | 'success' | 'warning' | 'danger' | 'info', label: string }> = {
  queued: { type: 'info', label: '排队中' },
  running: { type: 'warning', label: '翻译中' },
  completed: { type: 'success', label: '已完成' },
  failed: { type: 'danger', label: '失败' },
  cancelled: { type: 'info', label: '已取消' },
  pending: { type: 'info', label: '排队中' },
  translating: { type: 'warning', label: '翻译中' },
  active: { type: 'success', label: '活跃' },
  paused: { type: 'warning', label: '已暂停' },
  error: { type: 'danger', label: '错误' },
  open: { type: 'warning', label: 'open' },
  closed: { type: 'danger', label: 'closed' },
  merged: { type: 'success', label: 'merged' },
}

const tagType = computed(() => statusMap[props.status]?.type || '')
const label = computed(() => statusMap[props.status]?.label || props.status)
</script>
