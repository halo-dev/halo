<script lang="ts" setup>
import {
  consoleApiClient,
  type ExtensionPointDefinition,
} from "@halo-dev/api-client";
import {
  Toast,
  VButton,
  VEmpty,
  VEntityContainer,
  VEntityField,
  VLoading,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, toRefs, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { useExtensionDefinitionFetch } from "../../composables/use-extension-definition-fetch";
import ExtensionDefinitionListItem from "./ExtensionDefinitionListItem.vue";

const EXTENSION_POINT_ENABLED_GROUP = "extensionPointEnabled";

const { t } = useI18n();
const queryClient = useQueryClient();

const Q_KEY = (name?: Ref<string | undefined>) => [
  "extension-point-value",
  name,
];

const props = withDefaults(
  defineProps<{ extensionPointDefinition?: ExtensionPointDefinition }>(),
  { extensionPointDefinition: undefined }
);

const extensionPointDefinitionName = computed(() => {
  return extensionPointDefinition.value?.metadata.name;
});

const { extensionPointDefinition } = toRefs(props);

const { data: extensionDefinitions, isLoading } = useExtensionDefinitionFetch(
  extensionPointDefinition
);

const { data: value } = useQuery({
  queryKey: Q_KEY(extensionPointDefinitionName),
  queryFn: async () => {
    if (!extensionPointDefinition.value) return null;

    const { data: extensionPointEnabled } =
      await consoleApiClient.configMap.system.getSystemConfigByGroup({
        group: EXTENSION_POINT_ENABLED_GROUP,
      });

    const extensionPointValue =
      extensionPointEnabled?.[extensionPointDefinition.value?.metadata.name];

    // check is array
    if (Array.isArray(extensionPointValue)) {
      return extensionPointValue[0];
    }

    return null;
  },
  enabled: computed(() => !!extensionPointDefinition.value),
});

const isSubmitting = ref(false);

async function onExtensionChange(e: Event) {
  const value = (e.target as HTMLInputElement).value;
  await handleChange(value);
}

async function handleChange(value: string) {
  if (!extensionPointDefinition.value) return;

  isSubmitting.value = true;

  try {
    const { data: extensionPointEnabled } =
      await consoleApiClient.configMap.system.getSystemConfigByGroup({
        group: EXTENSION_POINT_ENABLED_GROUP,
      });

    await consoleApiClient.configMap.system.updateSystemConfigByGroup({
      group: EXTENSION_POINT_ENABLED_GROUP,
      body: {
        ...extensionPointEnabled,
        [extensionPointDefinition.value?.metadata.name]: [value],
      },
    });

    Toast.success(t("core.common.toast.save_success"));

    queryClient.invalidateQueries({
      queryKey: Q_KEY(extensionPointDefinitionName),
    });
  } catch (_) {
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <div class="p-4">
    <VLoading v-if="isLoading"></VLoading>
    <Transition
      v-else-if="!extensionDefinitions?.items.length"
      appear
      name="fade"
    >
      <VEmpty
        :title="
          $t('core.plugin.extension-settings.extension-definition.empty.title')
        "
      ></VEmpty>
    </Transition>
    <Transition v-else name="fade" appear>
      <div
        class="overflow-hidden rounded-base border"
        :class="{ 'pointer-events-none opacity-50': isSubmitting }"
      >
        <VEntityContainer>
          <ExtensionDefinitionListItem
            v-for="item in extensionDefinitions?.items"
            :key="item.metadata.name"
            :extension-definition="item"
          >
            <template #selection-indicator>
              <input
                :value="item.metadata.name"
                type="radio"
                name="activated-extension"
                :checked="item.metadata.name === value"
                :disabled="isSubmitting"
                @change="onExtensionChange"
              />
            </template>
            <template #end>
              <VEntityField v-if="item.metadata.name !== value">
                <template #description>
                  <VButton size="sm" @click="handleChange(item.metadata.name)">
                    {{
                      $t(
                        "core.plugin.extension-settings.extension-definition.operation.use.button"
                      )
                    }}
                  </VButton>
                </template>
              </VEntityField>
            </template>
          </ExtensionDefinitionListItem>
        </VEntityContainer>
      </div>
    </Transition>
  </div>
</template>
