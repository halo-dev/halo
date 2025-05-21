<script lang="ts" setup>
import type { ExtensionPointDefinition } from "@halo-dev/api-client";
import { VEmpty, VEntityContainer, VLoading } from "@halo-dev/components";
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
      <div class="overflow-hidden rounded-base border">
        <VEntityContainer>
          <ExtensionDefinitionListItem
            v-for="item in extensionDefinitions?.items"
            :key="item.metadata.name"
            :extension-definition="item"
          />
        </VEntityContainer>
      </div>
    </Transition>
  </div>
</template>
