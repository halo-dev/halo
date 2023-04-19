<script lang="ts" setup>
import {
  VButton,
  IconAddCircle,
  IconCloseCircle,
  IconArrowUpCircleLine,
  IconArrowDownCircleLine,
} from "@halo-dev/components";
import type { FormKitFrameworkContext } from "@formkit/core";
import { group } from "@formkit/inputs";
import type { PropType } from "vue";
import cloneDeep from "lodash.clonedeep";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const handleAppend = (index: number) => {
  const value = cloneDeep(props.context._value);
  value.splice(index + 1, 0, {});
  props.context.node.input(value);
};

const handleRemove = (index: number) => {
  const value = cloneDeep(props.context._value);
  value.splice(index, 1);
  props.context.node.input(value);
};

const handleMoveUp = (index: number) => {
  if (index === 0) {
    return;
  }

  const value = cloneDeep(props.context._value);
  value.splice(index - 1, 0, value.splice(index, 1)[0]);
  props.context.node.input(value);
};

const handleMoveDown = (index: number) => {
  if (index === props.context._value.length - 1) {
    return;
  }

  const value = cloneDeep(props.context._value);
  value.splice(index + 1, 0, value.splice(index, 1)[0]);
  props.context.node.input(value);
};
</script>

<template>
  <ul :class="context.classes.items">
    <li
      v-for="(item, index) in context._value"
      :key="index"
      :class="context.classes.item"
    >
      <div :class="context.classes.content">
        <FormKit
          :id="`${context.node.name}-group-${index}`"
          :key="`${context.node.name}-group-${index}`"
          :model-value="item"
          :type="group"
        >
          <slot />
        </FormKit>
      </div>
      <div :class="context.classes.controls">
        <ul class="flex flex-col items-center justify-center gap-1.5 py-2">
          <li
            class="cursor-pointer text-gray-500 transition-all hover:text-primary"
            :class="{
              'cursor-not-allowed opacity-50 hover:!text-gray-500': index === 0,
            }"
          >
            <IconArrowUpCircleLine
              class="h-5 w-5"
              @click="handleMoveUp(index)"
            />
          </li>
          <li
            class="cursor-pointer text-gray-500 transition-all hover:text-primary"
          >
            <IconCloseCircle class="h-5 w-5" @click="handleRemove(index)" />
          </li>
          <li
            class="cursor-pointer text-gray-500 transition-all hover:text-primary"
          >
            <IconAddCircle class="h-5 w-5" @click="handleAppend(index)" />
          </li>
          <li
            class="cursor-pointer text-gray-500 transition-all hover:text-primary"
            :class="{
              'cursor-not-allowed opacity-50 hover:!text-gray-500':
                index === context._value.length - 1,
            }"
          >
            <IconArrowDownCircleLine
              class="h-5 w-5"
              @click="handleMoveDown(index)"
            />
          </li>
        </ul>
      </div>
    </li>
  </ul>
  <div :class="context.classes.add">
    <VButton type="secondary" @click="handleAppend(context._value.length)">
      <template #icon>
        <IconAddCircle class="h-full w-full" />
      </template>
      {{ $t("core.common.buttons.add") }}
    </VButton>
  </div>
</template>
