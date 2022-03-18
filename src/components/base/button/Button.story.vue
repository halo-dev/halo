<template>
  <Story title="Button">
    <Variant title="Type">
      <template #default>
        <div class="themeable-default">
          <VButton type="primary"> Hello world </VButton>
          <VButton type="secondary"> Hello world </VButton>
        </div>
      </template>
    </Variant>
  </Story>
</template>
<script setup>
import { VButton } from "./index";
</script>
