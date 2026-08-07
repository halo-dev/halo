<script setup lang="ts">
defineProps<{
  label: string;
}>();
</script>

<template>
  <section
    class="table-menu-segmented-control box-border flex w-full min-w-0 max-w-full flex-col gap-[0.4375rem] border-b px-1.5 pb-2.5 pt-2 last:border-b-0"
  >
    <span
      class="table-menu-segmented-control-label text-xs font-semibold leading-4"
    >
      {{ label }}
    </span>
    <div
      class="table-menu-segmented-control-options box-border flex w-full min-w-0 max-w-full gap-[0.1875rem] rounded-lg border p-[0.1875rem]"
      role="group"
      :aria-label="label"
    >
      <slot />
    </div>
  </section>
</template>

<style scoped>
.table-menu-segmented-control {
  border-color: var(--halo-table-menu-border, rgb(229 231 235));
}

.table-menu-segmented-control-label {
  color: var(--halo-table-menu-muted, rgb(107 114 128));
}

.table-menu-segmented-control-options {
  border-color: var(--halo-table-menu-border, rgb(229 231 235));
  background: var(--halo-table-menu-segment-background, rgb(243 244 246));
}
</style>
