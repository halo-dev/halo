<script lang="ts" setup>
import { useQuery } from "@tanstack/vue-query";
import { coreApiClient, type Secret } from "@halo-dev/api-client";
import { ref, type PropType } from "vue";
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  IconAddCircle,
  IconArrowRight,
  IconCheckboxCircle,
  IconClose,
  VButton,
  VModal,
  VTag,
} from "@halo-dev/components";
import { onClickOutside } from "@vueuse/core";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const { data } = useQuery({
  queryKey: ["secrets"],
  queryFn: async () => {
    const { data } = await coreApiClient.secret.listSecret();
    return data;
  },
});

const selectedSecret = ref<Secret>();
const dropdownVisible = ref(false);
const text = ref("");
const wrapperRef = ref<HTMLElement>();

onClickOutside(wrapperRef, () => {
  dropdownVisible.value = false;
});

function handleKeydown() {}

const handleSelect = (secret: Secret) => {
  props.context.node.input(
    secret.metadata.name === props.context._value ? "" : secret.metadata.name
  );
  dropdownVisible.value = false;
};

const onTextInput = (e: Event) => {
  text.value = (e.target as HTMLInputElement).value;
};

const manageModalVisible = ref(false);
</script>

<template>
  <VModal
    v-if="manageModalVisible"
    title="管理 Secret"
    :width="650"
    @close="manageModalVisible = false"
  >
    <template #footer>
      <VButton @click="manageModalVisible = false">关闭</VButton>
    </template>
  </VModal>
  <div
    ref="wrapperRef"
    class="flex w-full items-center"
    @keydown="handleKeydown"
  >
    <div class="flex w-full min-w-0 flex-1 shrink flex-wrap items-center">
      <div v-if="context._value" class="flex px-2">
        <VTag class="!h-6" rounded>
          {{ context._value }}
          <template #rightIcon>
            <IconClose
              class="h-4 w-4 cursor-pointer text-gray-600 hover:text-gray-900"
            />
          </template>
        </VTag>
      </div>
      <input
        :value="text"
        :class="context.classes.input"
        type="text"
        :placeholder="
          context._value ? '' : '搜索已存在的密钥或者输入内容以创建新的密钥'
        "
        @input="onTextInput"
        @focus="dropdownVisible = true"
      />
    </div>

    <div
      class="inline-flex h-full flex-none cursor-pointer items-center gap-2 px-1"
    >
      <div @click="manageModalVisible = true">
        <IconAddCircle class="text-gray-500 hover:text-gray-700" />
      </div>
      <div @click="dropdownVisible = !dropdownVisible">
        <IconArrowRight class="rotate-90 text-gray-500 hover:text-gray-700" />
      </div>
    </div>

    <div
      v-if="dropdownVisible"
      class="absolute bottom-auto right-0 top-full z-10 mt-1 max-h-96 w-full overflow-auto rounded bg-white shadow-lg ring-1 ring-gray-100"
    >
      <ul class="p-1">
        <li
          v-for="secret in data?.items"
          :id="secret.metadata.name"
          :key="secret.metadata.name"
          class="group flex cursor-pointer items-center justify-between rounded p-2 hover:bg-gray-100"
          :class="{
            'bg-gray-100': false,
          }"
          @click="handleSelect(secret)"
        >
          <div class="inline-flex items-center overflow-hidden text-sm">
            {{ secret.metadata.name }}
          </div>
          <IconCheckboxCircle
            v-if="context._value === secret.metadata.name"
            class="text-primary"
          />
        </li>
      </ul>
    </div>
  </div>
</template>
