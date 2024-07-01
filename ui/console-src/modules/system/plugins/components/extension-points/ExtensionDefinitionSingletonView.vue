<script lang="ts" setup>
import {
  coreApiClient,
  type ExtensionPointDefinition,
} from "@halo-dev/api-client";
import { Toast, VEmpty, VLoading } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, toRefs, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { useExtensionDefinitionFetch } from "../../composables/use-extension-definition-fetch";
import ExtensionDefinitionListItem from "./ExtensionDefinitionListItem.vue";

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

    const { data } = await coreApiClient.configMap.getConfigMap({
      name: "system",
    });

    const extensionPointEnabled = JSON.parse(
      data.data?.["extensionPointEnabled"] || "{}"
    );

    const extensionPointValue =
      extensionPointEnabled[extensionPointDefinition.value?.metadata.name];

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

  if (!extensionPointDefinition.value) return;

  isSubmitting.value = true;

  try {
    const { data: configMap } = await coreApiClient.configMap.getConfigMap({
      name: "system",
    });

    const extensionPointEnabled = JSON.parse(
      configMap.data?.["extensionPointEnabled"] || "{}"
    );

    extensionPointEnabled[extensionPointDefinition.value?.metadata.name] = [
      value,
    ];

    await coreApiClient.configMap.patchConfigMap({
      name: "system",
      jsonPatchInner: [
        {
          op: "add",
          path: "/data/extensionPointEnabled",
          value: JSON.stringify(extensionPointEnabled),
        },
      ],
    });

    Toast.success(t("core.common.toast.save_success"));

    queryClient.invalidateQueries({
      queryKey: Q_KEY(extensionPointDefinitionName),
    });
  } catch (error) {
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
      <ul
        class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
        role="list"
      >
        <li
          v-for="item in extensionDefinitions?.items"
          :key="item.metadata.name"
        >
          <label
            class="cursor-pointer transition-all"
            :class="{ 'pointer-events-none opacity-50': isSubmitting }"
            @click.stop
          >
            <ExtensionDefinitionListItem :extension-definition="item">
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
            </ExtensionDefinitionListItem>
          </label>
        </li>
      </ul>
    </Transition>
  </div>
</template>
