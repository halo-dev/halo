<script lang="ts" setup>
import { useDialog, VModal } from "@halo-dev/components";
import VueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import { apiClient } from "@halo-dev/admin-shared";
import type { Plugin } from "@halo-dev/api-client";

const FilePond = VueFilePond();

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

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
          plugin.spec.enabled = true;

          try {
            await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin(
              plugin.metadata.name,
              plugin
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
