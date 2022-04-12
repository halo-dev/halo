<script lang="ts" setup>
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<template>
  <div class="about">
    <pre>Route: {{ route }}</pre>
  </div>
</template>
