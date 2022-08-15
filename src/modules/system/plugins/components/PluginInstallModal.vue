<script lang="ts" setup>
import { useDialog, VModal } from "@halo-dev/components";
import VueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import { apiClient } from "@halo-dev/admin-shared";
import type { Plugin } from "@halo-dev/api-client";

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

const dialog = useDialog();

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

    apiClient.plugin.installPlugin(file).then((response) => {
      load(response);

      const plugin: Plugin = response.data as unknown as Plugin;

      handleVisibleChange(false);

      dialog.success({
        title: "上传成功",
        description: "是否启动当前安装的插件？",
        onConfirm: async () => {
          try {
            const { data: pluginToUpdate } =
              await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin(
                plugin.metadata.name
              );
            pluginToUpdate.spec.enabled = true;

            await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin(
              pluginToUpdate.metadata.name,
              pluginToUpdate
            );

            window.location.reload();
          } catch (e) {
            console.error(e);
          }
        },
      });
    });

    return {};
  },
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="500"
    title="安装插件"
    @update:visible="handleVisibleChange"
  >
    <file-pond
      ref="pond"
      :allow-multiple="false"
      :server="server"
      label-idle="Drop JAR file here..."
      name="file"
    />
  </VModal>
</template>
