<template>
  <Story :init-state="initState" title="Space">
    <template #default="{ state }">
      <VSpace
        :align="state.align"
        :direction="state.direction"
        :spacing="state.spacing"
      >
        <div>Control：</div>
        <VButton type="primary">确定</VButton>
        <VButton>取消</VButton>
      </VSpace>
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VSpace } from "../../components/space";
import { VButton } from "../../components/button";

function initState() {
  return {
    direction: "row",
    spacing: "xs",
    align: "center",
  };
}
</script>
