<script lang="ts" setup>
import { useRoute } from "vue-router";

const route = useRoute();

if (route.query.redirect_uri) {
  window.location.href = route.query.redirect_uri as string;
}
</script>
<template>
  <div id="loader"></div>
</template>
