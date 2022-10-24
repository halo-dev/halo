<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import { apiClient } from "@/utils/api-client";
import { computed, mergeProps, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    upgradeTheme?: Theme;
  }>(),
  {
    visible: false,
    upgradeTheme: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const FilePondUploadRef = ref();

const modalTitle = computed(() => {
  return props.upgradeTheme
    ? `升级主题（${props.upgradeTheme.spec.displayName}）`
    : "安装主题";
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
    FilePondUploadRef.value.handleRemoveFiles();
  }
};

const uploadHandler = computed(() => {
  if (props.upgradeTheme) {
    return (file, config) =>
      apiClient.theme.upgradeTheme(
        {
          name: props.upgradeTheme.metadata.name as string,
          file: file,
        },
        config
      );
  }
  return (file, config) =>
    apiClient.theme.installTheme(
      {
        file: file,
      },
      config
    );
});
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    :title="modalTitle"
    @update:visible="handleVisibleChange"
  >
    <FilePondUpload
      v-if="visible && uploadHandler"
      ref="FilePondUploadRef"
      :allow-multiple="false"
      :handler="uploadHandler"
      label-idle="点击选择文件或者拖拽文件到此处"
    />
  </VModal>
</template>
