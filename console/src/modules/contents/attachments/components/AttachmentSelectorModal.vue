<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabbar } from "@halo-dev/components";
import { ref, markRaw, onMounted, computed } from "vue";
import CoreSelectorProvider from "./selector-providers/CoreSelectorProvider.vue";
import type {
  AttachmentLike,
  AttachmentSelectProvider,
  PluginModule,
} from "@halo-dev/console-shared";
import { usePluginModuleStore } from "@/stores/plugin";
import { useI18n } from "vue-i18n";

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
    label: t("core.attachment.select_modal.providers.default.label"),
    component: markRaw(CoreSelectorProvider),
  },
]);

// resolve plugin extension points
const { pluginModules } = usePluginModuleStore();

onMounted(() => {
  pluginModules.forEach((pluginModule: PluginModule) => {
    const { extensionPoints } = pluginModule;
    if (!extensionPoints?.["attachment:selector:create"]) {
      return;
    }

    const providers = extensionPoints[
      "attachment:selector:create"
    ]() as AttachmentSelectProvider[];

    if (providers) {
      providers.forEach((provider) => {
        attachmentSelectProviders.value.push(provider);
      });
    }
  });
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
    :title="$t('core.attachment.select_modal.title')"
    height="calc(100vh - 20px)"
    @update:visible="onVisibleChange"
  >
    <VTabbar
      v-model:active-id="activeId"
      :items="attachmentSelectProviders"
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
              $t("core.attachment.select_modal.operations.select.result", {
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
