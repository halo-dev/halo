<template>
  <Story title="Button">
    <Variant title="Type">
      <template #default>
        <VButton type="primary"> primary </VButton>
        <VButton type="secondary"> secondary </VButton>
        <VButton type="danger"> danger </VButton>
      </template>
    </Variant>
    <Variant title="Size">
      <template #default>
        <VButton type="secondary" size="lg"> lg </VButton>
        <VButton type="secondary"> default </VButton>
        <VButton type="secondary" size="sm"> sm </VButton>
        <VButton type="secondary" size="xs"> xs </VButton>
      </template>
    </Variant>
    <Variant title="Circle">
      <template #default>
        <VButton type="secondary" size="lg" circle> lg </VButton>
        <VButton type="secondary" circle> d </VButton>
        <VButton type="secondary" size="sm" circle> sm </VButton>
        <VButton type="secondary" size="xs" circle> xs </VButton>
      </template>
    </Variant>
    <Variant title="Block">
      <template #default>
        <VButton type="primary" block> primary </VButton>
        <VButton type="secondary" block> secondary </VButton>
        <VButton type="danger" block> danger </VButton>
      </template>
    </Variant>
    <Variant title="Disabled">
      <template #default>
        <VButton type="primary" disabled> primary </VButton>
        <VButton type="secondary" disabled> secondary </VButton>
        <VButton type="danger" disabled> danger </VButton>
      </template>
    </Variant>
  </Story>
</template>
<script setup>
import { VButton } from "./index";
</script>
