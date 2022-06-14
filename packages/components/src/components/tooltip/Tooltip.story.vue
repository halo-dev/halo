<script lang="ts" setup>
import { VTooltip } from "../../components/tooltip";
import { VButton } from "../../components/button";
</script>
<template>
  <Story title="Tooltip">
    <template #default>
      <VTooltip content="Hello Halo">
        <VButton>Hello</VButton>
      </VTooltip>
      <VTooltip content="Hello Halo">
        <div class="inline-flex">Halo</div>
      </VTooltip>
    </template>
  </Story>
</template>
