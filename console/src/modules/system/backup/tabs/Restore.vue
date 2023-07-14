<script lang="ts" setup>
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { Dialog, Toast, VAlert } from "@halo-dev/components";
import axios from "axios";

const onUploaded = () => {
  Dialog.success({
    title: "恢复成功",
    description:
      "恢复成功之后，需要重启一下 Halo 才能够正常加载系统资源，点击确定之后我们会自动停止运行 Halo",
    async onConfirm() {
      await handleShutdown();
    },
    async onCancel() {
      handleShutdown();
    },
  });
};

async function handleShutdown() {
  await axios.post(`/actuator/shutdown`);
  Toast.success("已请求停止运行");
}
</script>

<template>
  <div class="px-4 py-3">
    <VAlert title="提示">
      <template #description>
        <ul>
          <li>1. 恢复过程可能会持续较长时间，期间请勿刷新页面。</li>
          <li>
            2.
            在恢复的过程中，虽然已有的数据不会被清理掉，但如果有冲突的数据将被覆盖。
          </li>
          <li>
            3. 恢复完成之后会提示停止运行 Halo，停止之后可能需要手动运行。
          </li>
        </ul>
      </template>
    </VAlert>
  </div>
  <div class="flex items-center justify-center px-4 py-3">
    <UppyUpload
      :restrictions="{
        maxNumberOfFiles: 1,
        allowedFileTypes: ['.zip'],
      }"
      endpoint="/apis/api.console.halo.run/v1alpha1/restorations"
      width="100%"
      @uploaded="onUploaded"
    />
  </div>
</template>
