<script lang="ts" setup>
import type { Theme } from "@halo-dev/api-client";
import {
  VDropdownItem,
  VEntity,
  VEntityField,
  VTag,
} from "@halo-dev/components";
import { UseImage } from "@vueuse/components";
import { toRefs } from "vue";
import { useThemeLifeCycle } from "../../composables/use-theme";

const props = withDefaults(
  defineProps<{
    theme: Theme;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "open-settings"): void;
}>();

const { theme } = toRefs(props);

const { isActivated, handleActiveTheme } = useThemeLifeCycle(theme);
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #start>
      <VEntityField v-if="theme.spec.logo">
        <template #description>
          <div class="w-20">
            <div
              class="group aspect-h-3 aspect-w-4 block w-full overflow-hidden rounded border bg-gray-100"
            >
              <UseImage
                :src="theme.spec.logo"
                :alt="theme.spec.displayName"
                class="pointer-events-none object-cover group-hover:opacity-75"
              >
                <template #loading>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-gray-400">
                      {{ $t("core.common.status.loading") }}...
                    </span>
                  </div>
                </template>
                <template #error>
                  <div
                    class="flex h-full items-center justify-center object-cover"
                  >
                    <span class="text-xs text-red-400">
                      {{ $t("core.common.status.loading_error") }}
                    </span>
                  </div>
                </template>
              </UseImage>
            </div>
          </div>
        </template>
      </VEntityField>
      <VEntityField
        :title="theme.spec.displayName"
        :description="theme.spec.version"
      >
        <template #extra>
          <VTag v-if="isActivated">
            {{ $t("core.common.status.activated") }}
          </VTag>
        </template>
      </VEntityField>
    </template>

    <template #dropdownItems>
      <VDropdownItem v-if="!isActivated" @click="handleActiveTheme()">
        {{ $t("core.common.buttons.activate") }}
      </VDropdownItem>
      <VDropdownItem @click="emit('open-settings')">
        {{ $t("core.common.buttons.setting") }}
      </VDropdownItem>
    </template>
  </VEntity>
</template>
