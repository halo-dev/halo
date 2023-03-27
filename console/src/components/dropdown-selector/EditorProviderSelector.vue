<script lang="ts" setup>
import {
  useEditorExtensionPoints,
  type EditorProvider,
} from "@/composables/use-editor-extension-points";
import {
  VAvatar,
  IconExchange,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";

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
  <VDropdown>
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
      <VDropdownItem
        v-for="(editorProvider, index) in editorProviders"
        :key="index"
        :selected="provider?.name === editorProvider.name"
        @click="emit('select', editorProvider)"
      >
        <template #prefix-icon>
          <VAvatar :src="editorProvider.logo" size="xs"></VAvatar>
        </template>
        {{ editorProvider.displayName }}
      </VDropdownItem>
    </template>
  </VDropdown>
</template>
