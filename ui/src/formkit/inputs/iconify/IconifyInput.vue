<script setup lang="ts">
import type { FormKitFrameworkContext } from "@formkit/core";
import {
  IconClose,
  VButton,
  VDropdown,
  type VDropdownPlacement,
} from "@halo-dev/components";
import { Icon } from "@iconify/vue";
import {
  computed,
  provide,
  useTemplateRef,
  type PropType,
  type Ref,
} from "vue";
import RiCodeSSlashLine from "~icons/ri/code-s-slash-line";
import IconifyPicker from "./IconifyPicker.vue";
import type { IconifyFormat, IconifyValue } from "./types";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const format = computed(() => props.context.format as IconifyFormat);
const valueOnly = computed(() => props.context.valueOnly as boolean);
const currentIconifyValue = computed(() => {
  const value = props.context._value;

  if (!value) {
    return undefined;
  }

  if (valueOnly.value) {
    return {
      value: value as string,
    };
  }
  return value as IconifyValue;
});

provide<Ref<IconifyFormat>>("format", format);

provide<Ref<IconifyValue | undefined>>(
  "currentIconifyValue",
  currentIconifyValue
);

const popperPlacement = computed(
  () => props.context.popperPlacement as VDropdownPlacement
);

const dropdown = useTemplateRef<InstanceType<typeof VDropdown>>("dropdown");

const onSelect = (icon: IconifyValue) => {
  if (valueOnly.value) {
    props.context.node.input(icon.value);
  } else {
    props.context.node.input(icon);
  }
  dropdown.value?.hide();
};

const editFormDropdown =
  useTemplateRef<InstanceType<typeof VDropdown>>("editFormDropdown");

function onEditFormSubmit({ value: iconValue }: { value: string }) {
  if (valueOnly.value) {
    props.context.node.input(iconValue);
    editFormDropdown.value?.hide();
    return;
  }

  const valueToUpdate: IconifyValue = {
    ...currentIconifyValue.value,
    value: iconValue,
  };

  if (format.value === "name") {
    valueToUpdate.name = iconValue;
  } else if (iconValue !== currentIconifyValue.value?.value) {
    valueToUpdate.name = "";
  }

  props.context.node.input(valueToUpdate);
  editFormDropdown.value?.hide();
}
</script>

<template>
  <div class="group/iconify-input inline-flex items-center gap-2">
    <VDropdown
      ref="dropdown"
      class="inline-flex"
      popper-class="w-full sm:w-auto"
      :placement="popperPlacement"
    >
      <button
        v-tooltip="$t('core.formkit.iconify.placeholder')"
        type="button"
        class="inline-flex h-9 items-center justify-center rounded-lg border bg-white px-2 transition-all hover:bg-gray-50 hover:shadow active:bg-gray-100"
        :aria-label="$t('core.formkit.iconify.placeholder')"
      >
        <div
          v-if="currentIconifyValue?.value"
          class="inline-flex size-full items-center justify-center [&>*]:size-5"
        >
          <img
            v-if="['url', 'dataurl'].includes(format)"
            :src="currentIconifyValue.value"
          />
          <Icon
            v-else-if="format === 'name'"
            :icon="currentIconifyValue.value"
          />
          <div
            v-else
            class="inline-flex size-full items-center justify-center"
            v-html="currentIconifyValue.value"
          ></div>
        </div>
        <div v-else class="text-sm text-gray-600">
          {{ $t("core.formkit.iconify.placeholder") }}
        </div>
      </button>
      <template #popper>
        <IconifyPicker @select="onSelect" />
      </template>
    </VDropdown>
    <div class="inline-flex items-center gap-1.5">
      <!-- @vue-ignore -->
      <VDropdown
        ref="editFormDropdown"
        :dispose-timeout="null"
        class="inline-flex"
      >
        <template #default="{ shown }">
          <button
            v-tooltip="$t('core.formkit.iconify.operations.edit')"
            type="button"
            :aria-label="$t('core.formkit.iconify.operations.edit')"
            class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/iconify-input:opacity-100"
            :class="{ '!text-gray-900 !opacity-100': shown }"
          >
            <RiCodeSSlashLine />
          </button>
        </template>
        <template #popper>
          <div class="w-96">
            <FormKit
              id="icon-edit-form"
              type="form"
              ignore
              name="icon-edit-form"
              @submit="onEditFormSubmit"
            >
              <FormKit
                v-if="format === 'svg'"
                type="code"
                height="120px"
                :model-value="currentIconifyValue?.value"
                language="html"
                name="value"
              />
              <FormKit
                v-else-if="['dataurl', 'url'].includes(format)"
                type="attachment"
                name="value"
                :model-value="currentIconifyValue?.value"
              ></FormKit>
              <FormKit
                v-else
                type="text"
                name="value"
                :model-value="currentIconifyValue?.value"
              ></FormKit>
            </FormKit>
            <div class="mt-4">
              <VButton
                type="secondary"
                @click="$formkit.submit('icon-edit-form')"
              >
                {{ $t("core.common.buttons.save") }}
              </VButton>
            </div>
          </div>
        </template>
      </VDropdown>
      <button
        v-if="currentIconifyValue"
        v-tooltip="$t('core.common.buttons.delete')"
        type="button"
        :aria-label="$t('core.common.buttons.delete')"
        class="text-gray-500 opacity-0 transition-all hover:text-gray-900 group-hover/iconify-input:opacity-100"
        @click="context.node.input(undefined)"
      >
        <IconClose />
      </button>
    </div>
  </div>
</template>
