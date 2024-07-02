<script lang="ts" setup>
import type { Plugin, Setting } from "@halo-dev/api-client";
import { IconLink, VButton, VModal, VTabbar } from "@halo-dev/components";
import { provide, ref, toRefs, type Ref } from "vue";
import { usePluginDetailTabs } from "../composables/use-plugin";

const props = withDefaults(defineProps<{ name: string }>(), {});

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { name } = toRefs(props);

const { plugin, setting, tabs, activeTab } = usePluginDetailTabs(name, false);

provide<Ref<string>>("activeTab", activeTab);
provide<Ref<Plugin | undefined>>("plugin", plugin);
provide<Ref<Setting | undefined>>("setting", setting);
</script>

<template>
  <VModal
    ref="modal"
    :title="plugin?.spec.displayName"
    :centered="true"
    :width="920"
    height="calc(100vh - 20px)"
    mount-to-body
    @close="emit('close')"
  >
    <template #actions>
      <span>
        <RouterLink
          :to="{
            name: 'PluginDetail',
            params: { name },
          }"
        >
          <IconLink />
        </RouterLink>
      </span>
    </template>
    <VTabbar
      v-model:active-id="activeTab"
      :items="
        tabs.map((tab) => {
          return { label: tab.label, id: tab.id };
        })
      "
      type="outline"
    />
    <div class="-m-4 mt-2">
      <template v-for="tab in tabs" :key="tab.id">
        <component :is="tab.component" v-if="activeTab === tab.id" />
      </template>
    </div>
    <template #footer>
      <VButton @click="modal?.close()">
        {{ $t("core.common.buttons.close") }}
      </VButton>
    </template>
  </VModal>
</template>
