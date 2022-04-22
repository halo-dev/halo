<template>
  <Story title="Space" :init-state="initState">
    <template #default="{ state }">
      <VRadio
        v-for="(option, index) in ['row', 'column']"
        :key="index"
        v-model="state.direction"
        :label="option"
        :value="option"
        name="direction"
      ></VRadio>
      <VRadio
        v-for="(option, index) in ['start', 'center', 'end', 'stretch']"
        :key="index"
        v-model="state.align"
        :label="option"
        :value="option"
        name="align"
      ></VRadio>
      <VRadio
        v-for="(option, index) in ['xs', 'sm', 'md', 'lg']"
        :key="index"
        v-model="state.spacing"
        :label="option"
        :value="option"
        name="spacing"
      ></VRadio>
      <VSpace
        :direction="state.direction"
        :spacing="state.spacing"
        :align="state.align"
      >
        <div>Control：</div>
        <VButton type="primary">确定</VButton>
        <VButton>取消</VButton>
      </VSpace>
    </template>
  </Story>
</template>
<script lang="ts" setup>
import { VSpace } from "@/components/base/space/index";
import { VButton } from "@/components/base/button";
import { VRadio } from "@/components/base/radio";

function initState() {
  return {
    direction: "row",
    spacing: "xs",
    align: "center",
  };
}
</script>
