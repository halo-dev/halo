<script lang="ts" setup>
import { useColorScheme } from "@/composables/use-color-scheme";
import { VDropdown } from "@halo-dev/components";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import IconDeviceDesktop from "~icons/tabler/device-desktop";
import IconMoon from "~icons/tabler/moon";
import IconSun from "~icons/tabler/sun";

const { t } = useI18n();
const { preferredScheme, currentScheme, setScheme } = useColorScheme();

const currentIcon = computed(() => {
  if (preferredScheme.value === "system") {
    return IconDeviceDesktop;
  }
  return currentScheme.value === "dark" ? IconMoon : IconSun;
});

const schemes = computed(() => [
  {
    value: "light",
    label: t("core.common.scheme.light"),
    icon: IconSun,
  },
  {
    value: "dark",
    label: t("core.common.scheme.dark"),
    icon: IconMoon,
  },
  {
    value: "system",
    label: t("core.common.scheme.system"),
    icon: IconDeviceDesktop,
  },
]);

const handleSchemeChange = (scheme: "light" | "dark" | "system") => {
  setScheme(scheme);
};
</script>

<template>
  <VDropdown :triggers="['click']">
    <button
      v-tooltip="t('core.common.scheme.tooltip')"
      class="color-scheme-switcher"
      :aria-label="t('core.common.scheme.tooltip')"
    >
      <component :is="currentIcon" class="color-scheme-switcher__icon" />
    </button>
    <template #popper>
      <div class="color-scheme-switcher__popper">
        <div class="color-scheme-switcher__header">
          {{ t("core.common.scheme.title") }}
        </div>
        <div class="color-scheme-switcher__options">
          <button
            v-for="scheme in schemes"
            :key="scheme.value"
            :class="{
              'color-scheme-switcher__option': true,
              'color-scheme-switcher__option--active':
                preferredScheme === scheme.value,
            }"
            @click="handleSchemeChange(scheme.value as any)"
          >
            <component
              :is="scheme.icon"
              class="color-scheme-switcher__option-icon"
            />
            <span class="color-scheme-switcher__option-label">
              {{ scheme.label }}
            </span>
          </button>
        </div>
      </div>
    </template>
  </VDropdown>
</template>

<style lang="scss">
.color-scheme-switcher {
  display: inline-block;
  cursor: pointer;
  border-radius: 9999px;
  padding: 0.375rem;
  transition: all;

  &:hover {
    background-color: theme("colors.gray.100");
  }

  &__icon {
    height: 1.25rem;
    width: 1.25rem;
    color: theme("colors.gray.600");

    .color-scheme-switcher:hover & {
      color: theme("colors.gray.900");
    }
  }

  &__popper {
    padding: 0.25rem;
    min-width: 10rem;
  }

  &__header {
    color: theme("colors.gray.600");
    font-size: 0.875rem;
    font-weight: 600;
    border-bottom: 1px solid theme("colors.gray.100");
    padding-bottom: 0.375rem;
    margin-bottom: 0.375rem;
  }

  &__options {
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }

  &__option {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    border-radius: 0.25rem;
    cursor: pointer;
    transition: all;
    text-align: left;
    width: 100%;
    font-size: 0.875rem;

    &:hover {
      background-color: theme("colors.gray.50");
    }

    &--active {
      background-color: theme("colors.primary / 10%");
      color: theme("colors.primary");
    }
  }

  &__option-icon {
    height: 1rem;
    width: 1rem;
    flex-shrink: 0;
  }

  &__option-label {
    flex: 1;
  }
}
</style>
