<script lang="ts" setup>
import {
  useEditorExtensionPoints,
  type EditorProvider,
} from "@/composables/use-editor-extension-points";
import { VAvatar, VSpace, IconExchange } from "@halo-dev/components";

withDefaults(
  defineProps<{
    provider?: EditorProvider;
  }>(),
  {
    provider: undefined,
  }
);

const emit = defineEmits<{
  (event: "select", provider: EditorProvider): void;
}>();

const { editorProviders } = useEditorExtensionPoints();
</script>

<template>
  <FloatingDropdown>
    <div
      class="group flex w-full cursor-pointer items-center gap-2 rounded p-1 hover:bg-gray-100"
    >
      <VAvatar v-if="provider?.logo" :src="provider.logo" size="xs"></VAvatar>
      <div class="select-none text-sm text-gray-600 group-hover:text-gray-900">
        {{ provider?.displayName }}
      </div>
      <IconExchange class="h-4 w-4 text-gray-600 group-hover:text-gray-900" />
    </div>
    <template #popper>
      <div class="w-48 p-2">
        <VSpace class="w-full" direction="column">
          <div
            v-for="(editorProvider, index) in editorProviders"
            :key="index"
            v-close-popper
            class="group flex w-full cursor-pointer items-center gap-2 rounded p-1 hover:bg-gray-100"
            :class="{
              'bg-gray-100': editorProvider.name === provider?.name,
            }"
            @click="emit('select', editorProvider)"
          >
            <VAvatar :src="editorProvider.logo" size="xs"></VAvatar>
            <div
              class="text-sm text-gray-600 group-hover:text-gray-900"
              :class="{
                'text-gray-900': editorProvider.name === provider?.name,
              }"
            >
              {{ editorProvider.displayName }}
            </div>
          </div>
        </VSpace>
      </div>
    </template>
  </FloatingDropdown>
</template>
