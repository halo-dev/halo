<script lang="ts" setup>
import type { Secret } from "@halo-dev/api-client";
import {
  VDropdownDivider,
  VDropdownItem,
  VEntity,
  VEntityField,
} from "@halo-dev/components";

const props = withDefaults(
  defineProps<{
    secret: Secret;
  }>(),
  {}
);
</script>

<template>
  <VEntity>
    <template #start>
      <VEntityField :title="secret.metadata.name"></VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem>编辑</VDropdownItem>
      <VDropdownDivider />
      <VDropdownItem type="danger">删除</VDropdownItem>
    </template>
  </VEntity>
</template>
