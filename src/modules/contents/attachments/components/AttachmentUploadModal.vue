<script lang="ts" setup>
import { VModal } from "@halo-dev/components";
import vueFilePond from "vue-filepond";
import "filepond/dist/filepond.min.css";
import FilePondPluginImagePreview from "filepond-plugin-image-preview";
import "filepond-plugin-image-preview/dist/filepond-plugin-image-preview.min.css";
import { ref } from "vue";

const FilePond = vueFilePond(FilePondPluginImagePreview);

defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["update:visible", "close"]);

const strategies = ref([
  {
    id: "1",
    name: "本地存储",
    description: "~/.halo/uploads",
  },
  {
    id: "2",
    name: "阿里云 OSS",
    description: "bucket/blog-attachments",
  },
  {
    id: "3",
    name: "阿里云 OSS",
    description: "bucket/blog-photos",
  },
]);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :body-class="['!p-0']"
    :visible="visible"
    :width="600"
    title="上传附件"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <FloatingDropdown>
        <div v-tooltip="`选择存储策略`" class="modal-header-action">
          <span class="text-sm">本地存储</span>
        </div>
        <template #popper>
          <div class="w-72 p-4">
            <ul class="space-y-1">
              <li
                v-for="(strategy, index) in strategies"
                :key="index"
                class="flex cursor-pointer flex-col rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
              >
                <span class="truncate">
                  {{ strategy.name }}
                </span>
                <span class="text-xs">
                  {{ strategy.description }}
                </span>
              </li>
              <li
                class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
              >
                <span class="truncate">新增存储策略</span>
              </li>
            </ul>
          </div>
        </template>
      </FloatingDropdown>
    </template>
    <div class="w-full p-4">
      <file-pond
        ref="pond"
        accepted-file-types="image/jpeg, image/png"
        label-idle="Drop files here..."
        name="test"
        server="/api"
        v-bind:allow-multiple="true"
      />
    </div>
  </VModal>
</template>
