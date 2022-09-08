<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import { apiClient } from "@halo-dev/admin-shared";
import { computed, ref } from "vue";

withDefaults(
  defineProps<{
    visible: boolean;
  }>(),
  {
    visible: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const FilePondUploadRef = ref();

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
    FilePondUploadRef.value.handleRemoveFiles();
  }
};

const uploadHandler = computed(() => {
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
    title="安装主题"
    @update:visible="handleVisibleChange"
  >
    <FilePondUpload
      ref="FilePondUploadRef"
      :allow-multiple="false"
      :handler="uploadHandler"
      label-idle="点击选择文件或者拖拽文件到此处"
    />
  </VModal>
</template>
