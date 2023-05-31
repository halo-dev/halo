<script lang="ts" setup>
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

if (allowRedirect()) {
  window.location.href = route.query.redirect_uri as string;
} else {
  router.push({
    name: "Dashboard",
  });
}

function allowRedirect() {
  const redirect_uri = route.query.redirect_uri as string;

  if (!redirect_uri || redirect_uri === window.location.href) {
    return false;
  }

  if (redirect_uri.startsWith("/")) {
    return true;
  }

  if (
    redirect_uri.startsWith("https://") ||
    redirect_uri.startsWith("http://")
  ) {
    const url = new URL(redirect_uri);
    if (url.origin === window.location.origin) {
      return true;
    }
  }

  return false;
}
</script>
<template>
  <div id="loader"></div>
</template>
