<template>
  <section class="section-card">
    <header class="section-card__head">
      <h3 class="section-card__title">{{ title }}</h3>
      <div class="section-card__extra"><slot name="extra" /></div>
    </header>
    <div class="section-card__body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>
