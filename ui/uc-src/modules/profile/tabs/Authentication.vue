<script lang="ts" setup>
import AuthProviders from "./components/AuthProviders.vue";
import TwoFactor from "./components/TwoFactor.vue";
</script>

<template>
  <div class="flex flex-col gap-4">
    <AuthProviders />
    <TwoFactor />
  </div>
</template>
