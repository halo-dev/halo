<script setup lang="ts">
import Exception from "./components/Exception.vue";
</script>
<template>
  <Exception code="403" :message="$t('core.exception.forbidden.message')" />
</template>
