<script lang="ts" setup>
import { useDialog, VModal } from "@halo-dev/components";
import FilePondUpload from "@/components/upload/FilePondUpload.vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin } from "@halo-dev/api-client";
import { computed, ref } from "vue";
import type { AxiosResponse } from "axios";

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

const dialog = useDialog();
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
    apiClient.plugin.installPlugin(
      {
        file: file,
      },
      config
    );
});

const onUploaded = async (response: AxiosResponse) => {
  const plugin = response.data as Plugin;
  handleVisibleChange(false);
  dialog.success({
    title: "上传成功",
    description: "是否启动当前安装的插件？",
    onConfirm: async () => {
      try {
        const { data: pluginToUpdate } =
          await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
            name: plugin.metadata.name,
          });
        pluginToUpdate.spec.enabled = true;

        await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
          name: pluginToUpdate.metadata.name,
          plugin: pluginToUpdate,
        });

        window.location.reload();
      } catch (e) {
        console.error(e);
      }
    },
  });
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="安装插件"
    @update:visible="handleVisibleChange"
  >
    <FilePondUpload
      ref="FilePondUploadRef"
      :allow-multiple="false"
      :handler="uploadHandler"
      label-idle="点击选择文件或者拖拽文件到此处"
      @uploaded="onUploaded"
    />
  </VModal>
</template>
