<script lang="ts" setup>
import { RouterView } from "vue-router";
</script>

<template>
  <RouterView />
</template>

<style lang="scss">
body {
  overflow-y: overlay;
  background: #eff4f9;
}

*,
*::before,
*::after {
  box-sizing: border-box;
}

*::-webkit-scrollbar-track-piece {
  background-color: #f8f8f8;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

*::-webkit-scrollbar-thumb {
  background-color: #ddd;
  background-clip: padding-box;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

*::-webkit-scrollbar-thumb:hover {
  background-color: #bbb;
}

.v-popper__popper {
  outline: none;
}
</style>
