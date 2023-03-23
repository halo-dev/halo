<script setup lang="ts">
import Exception from "./components/Exception.vue";
</script>
<template>
  <Exception code="404" :message="$t('core.exception.not_found.message')" />
</template>
