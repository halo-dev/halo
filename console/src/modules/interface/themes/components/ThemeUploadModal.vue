<script lang="ts" setup>
import { Toast, VModal } from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { computed, ref, watch } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import { useThemeStore } from "@/stores/theme";

const { t } = useI18n();
const queryClient = useQueryClient();
const themeStore = useThemeStore();

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

const uploadVisible = ref(false);

const modalTitle = computed(() => {
  return props.upgradeTheme
    ? t("core.theme.upload_modal.titles.upgrade", {
        display_name: props.upgradeTheme.spec.displayName,
      })
    : t("core.theme.upload_modal.titles.install");
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const endpoint = computed(() => {
  if (props.upgradeTheme) {
    return `/apis/api.console.halo.run/v1alpha1/themes/${props.upgradeTheme.metadata.name}/upgrade`;
  }
  return "/apis/api.console.halo.run/v1alpha1/themes/install";
});

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      uploadVisible.value = true;
    } else {
      const uploadVisibleTimer = setTimeout(() => {
        uploadVisible.value = false;
        clearTimeout(uploadVisibleTimer);
      }, 200);
    }
  }
);

const onUploaded = () => {
  Toast.success(
    t(
      props.upgradeTheme
        ? "core.common.toast.upgrade_success"
        : "core.common.toast.install_success"
    )
  );

  queryClient.invalidateQueries({ queryKey: ["themes"] });
  themeStore.fetchActivatedTheme();

  handleVisibleChange(false);
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="600"
    :title="modalTitle"
    @update:visible="handleVisibleChange"
  >
    <UppyUpload
      v-if="uploadVisible"
      :restrictions="{
        maxNumberOfFiles: 1,
        allowedFileTypes: ['.zip'],
      }"
      :endpoint="endpoint"
      auto-proceed
      @uploaded="onUploaded"
    />
  </VModal>
</template>
