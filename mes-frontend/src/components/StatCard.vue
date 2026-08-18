<template>
  <div class="stat-card">
    <div class="stat-card__top">
      <span v-if="icon" class="stat-card__icon">
        <el-icon :size="17"><component :is="icon" /></el-icon>
      </span>
      <span class="stat-card__label">{{ label }}</span>
    </div>
    <div class="stat-card__value mes-num">{{ value }}</div>
    <div v-if="delta" class="stat-card__delta" :class="deltaType">
      <el-icon :size="12"><component :is="deltaType === 'down' ? 'Bottom' : 'Top'" /></el-icon>
      {{ delta }}
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
  icon?: string
  delta?: string
  deltaType?: 'up' | 'down' | ''
}>()
</script>
