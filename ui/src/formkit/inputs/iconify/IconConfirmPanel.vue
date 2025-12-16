<script lang="ts" setup>
import { IconClose, Toast, VButton } from "@halo-dev/components";
import { Icon as IconifyIcon } from "@iconify/vue";
import { refDefault } from "@vueuse/shared";
import { AxiosError } from "axios";
import { inject, onMounted, ref, type Ref } from "vue";
import { ICONIFY_BASE_URL, iconifyClient } from "./api";
import type { IconifyFormat, IconifyValue } from "./types";

const props = defineProps<{
  iconName: string;
}>();

const emit = defineEmits<{
  (e: "select", icon: IconifyValue): void;
}>();

const format = inject<Ref<IconifyFormat>>("format");
const currentIconifyValue = inject<Ref<IconifyValue | undefined>>(
  "currentIconifyValue"
);

const color = ref<string>("");
const width = refDefault<string | undefined>(ref(), "24");

onMounted(() => {
  if (currentIconifyValue?.value) {
    color.value = currentIconifyValue.value.color || "";
    width.value = currentIconifyValue.value.width;
  }
});

const isFetching = ref(false);

async function handleConfirm() {
  if (!format?.value) {
    return;
  }

  const iconifyValue: IconifyValue = {
    name: props.iconName,
    width: width.value,
    color: color.value,
    value: "",
  };

  const iconifyUrl = `${ICONIFY_BASE_URL}/${props.iconName}.svg?width=${width.value}&color=${encodeURIComponent(color.value!)}`;

  if (format.value === "name") {
    iconifyValue.value = props.iconName;
    emit("select", iconifyValue);
    return;
  }

  if (format.value === "url") {
    iconifyValue.value = iconifyUrl;
    emit("select", iconifyValue);
    return;
  }

  try {
    isFetching.value = true;
    const { data } = await iconifyClient.get(iconifyUrl);

    if (format.value === "svg") {
      iconifyValue.value = data;
    } else if (format.value === "dataurl") {
      iconifyValue.value = `data:image/svg+xml,${encodeURIComponent(data)}`;
    }
    emit("select", iconifyValue);
  } catch (error) {
    if (error instanceof AxiosError) {
      Toast.error(error.message);
    }
  } finally {
    isFetching.value = false;
  }
}
</script>
<template>
  <div class="flex items-start gap-3">
    <div
      class="inline-flex items-center justify-center rounded-base border p-2"
    >
      <IconifyIcon
        v-tooltip="iconName"
        :icon="iconName"
        :color="color"
        :width="width === 'none' ? undefined : width"
      />
    </div>
    <div class="space-y-3">
      <FormKit type="form" ignore>
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
