<script lang="ts" setup>
import StatusDot from "./StatusDot.vue";
import { VSpace } from "../space";
</script>

<template>
  <Story title="StatusDot">
    <Variant title="State">
      <template #default>
        <VSpace direction="column">
          <StatusDot state="default" text="默认" />

          <StatusDot state="success" text="成功" />

          <StatusDot state="warning" text="警告" />

          <StatusDot state="error" text="错误" />
        </VSpace>
      </template>
    </Variant>
    <Variant title="Animate">
      <template #default>
        <VSpace direction="column">
          <StatusDot state="default" text="默认" animate />

          <StatusDot state="success" text="成功" animate />

          <StatusDot state="warning" text="警告" animate />

          <StatusDot state="error" text="错误" animate />
        </VSpace>
      </template>
    </Variant>
  </Story>
</template>
