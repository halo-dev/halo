<script lang="ts" setup>
import type { ExtensionPointDefinition } from "@halo-dev/api-client";
import { VEmpty } from "@halo-dev/components";
import { toRefs } from "vue";
import { useExtensionDefinitionFetch } from "../../composables/use-extension-definition-fetch";
import ExtensionDefinitionListItem from "./ExtensionDefinitionListItem.vue";

const props = withDefaults(
  defineProps<{ extensionPointDefinition?: ExtensionPointDefinition }>(),
  { extensionPointDefinition: undefined }
);

const { extensionPointDefinition } = toRefs(props);

const { data: extensionDefinitions, isLoading } = useExtensionDefinitionFetch(
  extensionPointDefinition
);
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
          class="cursor-pointer"
        >
          <ExtensionDefinitionListItem :extension-definition="item" />
        </li>
      </ul>
    </Transition>
  </div>
</template>
