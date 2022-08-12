<script lang="ts" setup>
import {
  IconAddCircle,
  IconMore,
  VButton,
  VModal,
  VSpace,
} from "@halo-dev/components";
import AttachmentLocalStrategyEditingModal from "./AttachmentLocalStrategyEditingModal.vue";
import AttachmentAliOSSStrategyEditingModal from "./AttachmentAliOSSStrategyEditingModal.vue";
import { ref } from "vue";

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

const localStrategyVisible = ref(false);
const aliOSSStrategyVisible = ref(false);

function onVisibleChange(visible: boolean) {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
}
</script>
<template>
  <VModal
    :visible="visible"
    :width="750"
    title="存储策略"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <FloatingDropdown>
        <div v-tooltip="`添加存储策略`" class="modal-header-action">
          <IconAddCircle />
        </div>
        <template #popper>
          <div class="w-72 p-4">
            <ul class="space-y-1">
              <li
                v-close-popper
                class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                @click="localStrategyVisible = true"
              >
                <span class="truncate">本地</span>
              </li>
              <li
                v-close-popper
                class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
                @click="aliOSSStrategyVisible = true"
              >
                <span class="truncate">阿里云 OSS</span>
              </li>
              <li
                v-close-popper
                class="flex cursor-pointer items-center rounded px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 hover:text-gray-900"
              >
                <span class="truncate">Amazon S3</span>
              </li>
            </ul>
          </div>
        </template>
      </FloatingDropdown>
    </template>
    <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
      <li v-for="(strategy, index) in strategies" :key="index">
        <div
          class="relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50"
        >
          <div class="relative flex flex-row items-center">
            <div class="flex-1">
              <div class="flex flex-col sm:flex-row">
                <span
                  class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
                >
                  {{ strategy.name }}
                </span>
              </div>
              <div class="mt-1 flex">
                <span class="text-xs text-gray-500">
                  {{ strategy.description }}
                </span>
              </div>
            </div>
            <div class="flex">
              <div
                class="inline-flex flex-col flex-col-reverse items-end gap-4 sm:flex-row sm:items-center sm:gap-6"
              >
                <time class="text-sm text-gray-500" datetime="2020-01-07">
                  2020-01-07
                </time>
                <span class="cursor-pointer">
                  <FloatingDropdown>
                    <IconMore />
                    <template #popper>
                      <div class="w-48 p-2">
                        <VSpace class="w-full" direction="column">
                          <VButton v-close-popper block type="secondary">
                            编辑
                          </VButton>
                          <VButton v-close-popper block type="danger">
                            删除
                          </VButton>
                        </VSpace>
                      </div>
                    </template>
                  </FloatingDropdown>
                </span>
              </div>
            </div>
          </div>
        </div>
      </li>
    </ul>
    <template #footer>
      <VButton @click="onVisibleChange(false)">关闭 Esc</VButton>
    </template>
  </VModal>

  <AttachmentLocalStrategyEditingModal v-model:visible="localStrategyVisible" />
  <AttachmentAliOSSStrategyEditingModal
    v-model:visible="aliOSSStrategyVisible"
  />
</template>
