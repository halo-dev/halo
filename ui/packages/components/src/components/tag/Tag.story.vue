<template>
  <Story title="Tag">
    <Variant title="Theme">
      <template #default>
        <VSpace>
          <VTag theme="default">Halo</VTag>
          <VTag theme="primary">Halo</VTag>
          <VTag theme="secondary">Halo</VTag>
          <VTag theme="danger">Halo</VTag>
        </VSpace>
      </template>
    </Variant>
    <Variant title="Icon">
      <template #default>
        <VSpace>
          <VTag rounded theme="default">
            <template #leftIcon>
              <IconSettings />
            </template>
            Halo
          </VTag>
          <VTag rounded theme="primary">
            <template #leftIcon>
              <IconSettings />
            </template>
            Halo
          </VTag>
          <VTag theme="secondary">
            <template #leftIcon>
              <IconSettings />
            </template>
            Halo
          </VTag>
          <VTag theme="danger">
            <template #leftIcon>
              <IconSettings />
            </template>
            Halo
          </VTag>
        </VSpace>
      </template>
    </Variant>
  </Story>
</template>
<script lang="ts" setup>
import { VTag } from "../../components/tag";
import { VSpace } from "../../components/space";
import { IconSettings } from "../../icons/icons";
</script>
