<script lang="ts" setup>
import { coreApiClient } from "@halo-dev/api-client";
import {
  IconSettings,
  VButton,
  VCard,
  VPageHeader,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { computed, watch } from "vue";
import ExtensionDefinitionMultiInstanceView from "./components/extension-points/ExtensionDefinitionMultiInstanceView.vue";
import ExtensionDefinitionSingletonView from "./components/extension-points/ExtensionDefinitionSingletonView.vue";

const { data: extensionPointDefinitions } = useQuery({
  queryKey: ["extension-point-definitions"],
  queryFn: async () => {
    const { data } =
      await coreApiClient.plugin.extensionPointDefinition.listExtensionPointDefinition();
    return data;
  },
});

const selectedExtensionPointDefinitionName = useRouteQuery<string | undefined>(
  "extension-point-definition-name"
);

const selectedExtensionPointDefinition = computed(() => {
  return extensionPointDefinitions.value?.items.find(
    (item) => item.metadata.name === selectedExtensionPointDefinitionName.value
  );
});

watch(
  () => extensionPointDefinitions.value,
  (value) => {
    if (value?.items.length && !selectedExtensionPointDefinitionName.value) {
      selectedExtensionPointDefinitionName.value =
        value?.items[0].metadata.name;
    }
  },
  {
    immediate: true,
  }
);
</script>

<template>
  <VPageHeader :title="$t('core.plugin.extension-settings.title')">
    <template #icon>
      <IconSettings class="mr-2 self-center" />
    </template>
    <template #actions>
      <VButton size="sm" @click="$router.back()">
        {{ $t("core.common.buttons.back") }}
      </VButton>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard
      style="height: calc(100vh - 5.5rem)"
      :body-class="['h-full', '!p-0']"
    >
      <div class="flex h-full divide-x">
        <div class="w-72 flex-none">
          <div
            class="sticky top-0 z-10 flex h-12 items-center border-b bg-white px-4"
          >
            <h2 class="font-semibold text-gray-900">
              {{
                $t(
                  "core.plugin.extension-settings.extension-point-definition.title"
                )
              }}
            </h2>
          </div>
          <ul
            class="box-border h-full w-full divide-y divide-gray-100 overflow-auto pb-12"
            role="list"
          >
            <li
              v-for="extensionPointDefinition in extensionPointDefinitions?.items"
              :key="extensionPointDefinition.metadata.name"
              class="relative cursor-pointer"
              @click="
                selectedExtensionPointDefinitionName =
                  extensionPointDefinition.metadata.name
              "
            >
              <div
                v-show="
                  selectedExtensionPointDefinitionName ===
                  extensionPointDefinition.metadata.name
                "
                class="absolute inset-y-0 left-0 w-0.5 bg-primary"
              ></div>
              <div
                class="flex flex-col space-y-1.5 px-4 py-2.5 hover:bg-gray-50"
              >
                <h3
                  class="line-clamp-1 break-words text-sm font-medium text-gray-900"
                >
                  {{ extensionPointDefinition.spec.displayName }}
                </h3>
                <p class="line-clamp-2 text-xs text-gray-600">
                  {{ extensionPointDefinition.spec.description }}
                </p>
              </div>
            </li>
          </ul>
        </div>
        <div class="flex min-w-0 flex-1 shrink flex-col overflow-auto">
          <div
            class="sticky top-0 z-10 flex h-12 items-center space-x-3 border-b bg-white px-4"
          >
            <h2 class="font-semibold text-gray-900">
              {{ selectedExtensionPointDefinition?.spec.displayName }}
            </h2>
            <small class="line-clamp-1 text-gray-600">
              {{ selectedExtensionPointDefinition?.spec.description }}
            </small>
          </div>
          <ExtensionDefinitionSingletonView
            v-if="selectedExtensionPointDefinition?.spec.type === 'SINGLETON'"
            :extension-point-definition="selectedExtensionPointDefinition"
          />
          <ExtensionDefinitionMultiInstanceView
            v-else-if="
              selectedExtensionPointDefinition?.spec.type === 'MULTI_INSTANCE'
            "
            :extension-point-definition="selectedExtensionPointDefinition"
          />
        </div>
      </div>
    </VCard>
  </div>
</template>
