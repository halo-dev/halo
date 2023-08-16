<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { IconEye, IconEyeOff } from "@halo-dev/components";
import type { PropType } from "vue";
import { toRefs } from "vue";
const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const { context } = toRefs(props);

function handleChange() {
  context.value.node.props.type =
    context.value.node.props.type === "password" ? "text" : "password";
}
</script>

<template>
  <div
    class="group flex h-full cursor-pointer items-center px-3 transition-all"
    @click="handleChange"
  >
    <IconEye
      v-if="context.node.props.type === 'password'"
      class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
    />
    <IconEyeOff
      v-else
      class="h-4 w-4 text-gray-500 group-hover:text-gray-700"
    />
  </div>
</template>
