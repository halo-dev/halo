<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import VueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import { apiClient } from "@halo-dev/admin-shared";

const FilePond = VueFilePond();

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

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const server = {
  process: (fieldName, file, metadata, load) => {
    const formData = new FormData();
    formData.append(fieldName, file, file.name);

    apiClient.theme.installTheme(file).then((response) => {
      load(response);
      handleVisibleChange(false);
    });

    return {};
  },
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="安装主题"
    @update:visible="handleVisibleChange"
  >
    <file-pond
      ref="pond"
      :allow-multiple="false"
      :server="server"
      label-idle="Drop ZIP file here..."
      name="file"
    />
  </VModal>
</template>
