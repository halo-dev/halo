<script lang="ts" setup>
import { IconClose, VButton, VDropdown } from "@halo-dev/components";
import { Icon as IconifyIcon } from "@iconify/vue";
import { refDefault } from "@vueuse/shared";
import { inject, ref, useTemplateRef, type Ref } from "vue";
import { ICONIFY_BASE_URL, iconifyClient } from "./api";
import type { IconifyFormat } from "./types";

const props = defineProps<{
  iconName: string;
}>();

const emit = defineEmits<{
  (e: "select", icon: string): void;
}>();

const dropdown = useTemplateRef<InstanceType<typeof VDropdown>>("dropdown");

const format = inject<Ref<IconifyFormat>>("format");

const color = ref<string>("");
const width = refDefault<string | undefined>(ref(), "24");

const isFetching = ref(false);

async function handleConfirm() {
  if (!format?.value) {
    return;
  }

  const iconifyUrl = `${ICONIFY_BASE_URL}/${props.iconName}.svg?width=${width.value}&color=${encodeURIComponent(color.value!)}`;

  if (format.value === "name") {
    emit("select", props.iconName);
    dropdown.value?.hide();
    return;
  }

  if (format.value === "url") {
    emit("select", iconifyUrl);
    dropdown.value?.hide();
    return;
  }

  try {
    isFetching.value = true;
    const { data } = await iconifyClient.get(iconifyUrl);

    if (format.value === "svg") {
      emit("select", data);
    } else if (format.value === "dataurl") {
      emit("select", `data:image/svg+xml,${encodeURIComponent(data)}`);
    }
  } catch (error) {
    console.error(error);
  } finally {
    isFetching.value = false;
    dropdown.value?.hide();
  }
}
</script>
<template>
  <VDropdown ref="dropdown" class="inline-flex">
    <button
      v-tooltip="iconName"
      type="button"
      class="inline-flex size-full items-center justify-center rounded-lg hover:bg-gray-100 active:bg-gray-200"
      :aria-label="`Select icon: ${iconName}`"
    >
      <IconifyIcon :icon="iconName" class="text-2xl" />
    </button>
    <template #popper>
      <div
        class="flex gap-3"
        :class="{
          'items-center': format === 'name',
          'items-start': format !== 'name',
        }"
      >
        <div
          class="inline-flex items-center justify-center rounded-base border p-2"
        >
          <IconifyIcon
            :icon="iconName"
            :color="color"
            :width="width === 'none' ? undefined : width"
          />
        </div>
        <div class="space-y-3">
          <FormKit v-if="format !== 'name'" type="form" ignore>
            <FormKit
              v-model="width"
              type="select"
              name="size"
              :label="$t('core.formkit.iconify.option_size')"
              allow-create
              searchable
              ignore
              :options="[
                {
                  label: $t('core.common.text.none'),
                  value: 'none',
                },
                {
                  label: '12px',
                  value: '12',
                },
                {
                  label: '24px',
                  value: '24',
                },
                {
                  label: '48px',
                  value: '48',
                },
                {
                  label: '96px',
                  value: '96',
                },
                {
                  label: '1em',
                  value: '1em',
                },
                {
                  label: '1.2em',
                  value: '1.2em',
                },
                {
                  label: '2em',
                  value: '2em',
                },
              ]"
              clearable
            ></FormKit>
            <FormKit
              v-model="color"
              type="color"
              name="color"
              :label="$t('core.formkit.iconify.option_color')"
              ignore
            >
              <template v-if="color !== ''" #suffixIcon>
                <IconClose aria-label="Clear color" @click="color = ''" />
              </template>
            </FormKit>
          </FormKit>
          <VButton :loading="isFetching" @click="handleConfirm()">
            {{ $t("core.common.buttons.confirm") }}
          </VButton>
        </div>
      </div>
    </template>
  </VDropdown>
</template>
