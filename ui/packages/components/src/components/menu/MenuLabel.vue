<template>
  <li class="menu-label flex flex-col">
    <slot />
  </li>
</template>
<style lang="scss">
.menu-label {
  padding-top: 10px;
  padding-bottom: 10px;
  color: theme("colors.gray.400");
  font-size: 14px;
  font-weight: 400;
}
</style>
