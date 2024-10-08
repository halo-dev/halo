<script lang="ts" setup>
import { usePluginModuleStore } from "@/stores/plugin";
import { VButton, VModal, VSpace, VTabbar } from "@halo-dev/components";
import type {
  AttachmentLike,
  AttachmentSelectProvider,
} from "@halo-dev/console-shared";
import { computed, markRaw, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import CoreSelectorProvider from "./selector-providers/CoreSelectorProvider.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    visible: boolean;
    accepts?: string[];
    min?: number;
    max?: number;
  }>(),
  {
    visible: false,
    accepts: () => ["*/*"],
    min: undefined,
    max: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "select", attachments: AttachmentLike[]): void;
}>();

const selected = ref<AttachmentLike[]>([] as AttachmentLike[]);

const attachmentSelectProviders = ref<AttachmentSelectProvider[]>([
  {
    id: "core",
    label: t("core.uc_attachment.select_modal.providers.default.label"),
    component: markRaw(CoreSelectorProvider),
  },
]);

// resolve plugin extension points
const { pluginModules } = usePluginModuleStore();

onMounted(async () => {
  for (const pluginModule of pluginModules) {
    try {
      const callbackFunction =
        pluginModule?.extensionPoints?.["attachment:selector:create"];

      if (typeof callbackFunction !== "function") {
        continue;
      }

      const providers = await callbackFunction();
      attachmentSelectProviders.value.push(...providers);
    } catch (error) {
      console.error(`Error processing plugin module:`, pluginModule, error);
    }
  }
});

const activeId = ref(attachmentSelectProviders.value[0].id);

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const onChangeProvider = (providerId: string) => {
  const provider = attachmentSelectProviders.value.find(
    (provider) => provider.id === providerId
  );

  if (!provider) {
    return;
  }

  activeId.value = providerId;
};

const handleConfirm = () => {
  emit("select", Array.from(selected.value));
  onVisibleChange(false);
};

const confirmDisabled = computed(() => {
  if (props.min === undefined) {
    return false;
  }
  return selected.value.length < props.min;
});

const confirmCountMessage = computed(() => {
  if (!props.min && !props.max) {
    return selected.value.length;
  }
  return `${selected.value.length} / ${props.max || props.min}`;
});
</script>
<template>
  <VModal
    :visible="visible"
    :width="1240"
    :mount-to-body="true"
    :layer-closable="true"
    :title="$t('core.uc_attachment.select_modal.title')"
    height="calc(100vh - 20px)"
    @update:visible="onVisibleChange"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="
        attachmentSelectProviders.map((provider) => ({
          id: provider.id,
          label: provider.label,
        }))
      "
      class="w-full"
      type="outline"
    ></VTabbar>

    <div v-if="visible" class="mt-2">
      <template
        v-for="(provider, index) in attachmentSelectProviders"
        :key="index"
      >
        <Suspense>
          <component
            :is="provider.component"
            v-if="activeId === provider.id"
            v-model:selected="selected"
            :accepts="accepts"
            :min="min"
            :max="max"
            @change-provider="onChangeProvider"
          ></component>
          <template #fallback>
            {{ $t("core.common.status.loading") }}
          </template>
        </Suspense>
      </template>
    </div>
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          :disabled="confirmDisabled"
          @click="handleConfirm"
        >
          {{ $t("core.common.buttons.confirm") }}
          <span v-if="selected.length || props.min || props.max">
            {{
              $t("core.uc_attachment.select_modal.operations.select.result", {
                count: confirmCountMessage,
              })
            }}
          </span>
        </VButton>
        <VButton @click="onVisibleChange(false)">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
