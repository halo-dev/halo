<script lang="ts" setup>
import { VButton, VModal, VTabbar } from "@halo-dev/components";
import { ref, markRaw } from "vue";
import CoreSelectorProvider from "./selector-providers/CoreSelectorProvider.vue";
import UploadSelectorProvider from "./selector-providers/UploadSelectorProvider.vue";
import type {
  AttachmentLike,
  AttachmentSelectorPublicState,
} from "@halo-dev/admin-shared";
import { useExtensionPointsState } from "@/composables/usePlugins";

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
  (event: "select", attachments: AttachmentLike[]): void;
}>();

const selected = ref<AttachmentLike[]>([] as AttachmentLike[]);

const attachmentSelectorPublicState = ref<AttachmentSelectorPublicState>({
  providers: [
    {
      id: "core",
      label: "附件库",
      component: markRaw(CoreSelectorProvider),
    },
    {
      id: "upload",
      label: "上传",
      component: markRaw(UploadSelectorProvider),
    },
  ],
});

useExtensionPointsState("ATTACHMENT_SELECTOR", attachmentSelectorPublicState);

const activeId = ref(attachmentSelectorPublicState.value.providers[0].id);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleConfirm = () => {
  console.log(Array.from(selected.value));
  emit("select", Array.from(selected.value));
  onVisibleChange(false);
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="1240"
    title="选择附件"
    height="calc(100vh - 20px)"
    @update:visible="onVisibleChange"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="attachmentSelectorPublicState.providers"
      class="w-full !rounded-none"
      type="outline"
    ></VTabbar>

    <div v-if="visible" class="mt-2">
      <template
        v-for="(provider, index) in attachmentSelectorPublicState.providers"
        :key="index"
      >
        <Suspense>
          <component
            :is="provider.component"
            v-if="activeId === provider.id"
            v-model:selected="selected"
          ></component>
          <template #fallback> 加载中 </template>
        </Suspense>
      </template>
    </div>
    <template #footer>
      <VButton type="secondary" @click="handleConfirm">
        确定
        <span v-if="selected.length">
          （已选择 {{ selected.length }} 项）
        </span>
      </VButton>
    </template>
  </VModal>
</template>
