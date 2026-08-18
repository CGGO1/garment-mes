<template>
  <span class="status-tag" :class="`status-tag--${tone}`">
    <span class="status-tag__dot" />
    <span>{{ text }}</span>
  </span>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  text: string
  tone?: 'ok' | 'warn' | 'danger' | 'info' | 'thread'
}>(), { tone: 'info' })
</script>
