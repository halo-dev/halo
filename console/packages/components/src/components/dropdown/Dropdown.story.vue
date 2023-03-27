<script lang="ts" setup>
import { VButton } from "@/components/button";
import { VDropdown, VDropdownItem, VDropdownDivider } from "./index";
</script>
<template>
  <Story title="Dropdown">
    <template #default>
      <VDropdown>
        <VButton>Hello</VButton>
        <template #popper>
          <VDropdownItem>删除</VDropdownItem>
          <VDropdownDivider></VDropdownDivider>
          <VDropdownItem>删除</VDropdownItem>
          <VDropdownItem>编辑</VDropdownItem>
        </template>
      </VDropdown>
    </template>
  </Story>
</template>
