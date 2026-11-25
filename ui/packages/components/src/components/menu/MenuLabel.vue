<template>
  <li class="menu-label flex flex-col">
    <slot />
  </li>
</template>
<style lang="scss">
.menu-label {
  @apply py-2.5 text-sm font-normal text-gray-500;
}
</style>
