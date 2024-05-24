<script lang="ts" setup>
import {
  type FormKitNode,
  type FormKitSchemaCondition,
  type FormKitSchemaNode,
  reset,
  submitForm,
} from "@formkit/core";

import { IconArrowRight } from "@halo-dev/components";

import { computed, nextTick, onMounted, ref, watch } from "vue";
import { apiClient } from "@/utils/api-client";
import type { AnnotationSetting } from "@halo-dev/api-client";
import { cloneDeep } from "lodash-es";
import { getValidationMessages } from "@formkit/validation";
import { useThemeStore } from "@console/stores/theme";
import { randomUUID } from "@/utils/id";

const themeStore = useThemeStore();

function keyValidationRule(node: FormKitNode) {
  const validAnnotations = [
    ...Object.keys(annotations.value),
    ...customAnnotationsState.value.map((item) => item.key),
  ];
  const count = validAnnotations.filter((item) => item === node.value);
  return count.length < 2;
}

const props = withDefaults(
  defineProps<{
    group: string;
    kind: string;
    value?: {
      [key: string]: string;
    } | null;
  }>(),
  {
    value: null,
  }
);

const annotationSettings = ref<AnnotationSetting[]>([] as AnnotationSetting[]);

const avaliableAnnotationSettings = computed(() => {
  return annotationSettings.value.filter((setting) => {
    if (!setting.metadata.labels?.["theme.halo.run/theme-name"]) {
      return true;
    }
    return (
      setting.metadata.labels?.["theme.halo.run/theme-name"] ===
      themeStore.activatedTheme?.metadata.name
    );
  });
});

const handleFetchAnnotationSettings = async () => {
  try {
    const { data } =
      await apiClient.extension.annotationSetting.listV1alpha1AnnotationSetting(
        {
          labelSelector: [
            `halo.run/target-ref=${[props.group, props.kind].join("/")}`,
          ],
        }
      );
    annotationSettings.value = data.items;
  } catch (error) {
    console.error("Failed to fetch annotation settings", error);
  }
};

const specFormId = `${randomUUID()}-specForm`;
const customFormId = `${randomUUID()}-customForm`;
const annotations = ref<{
  [key: string]: string;
}>({});
const customAnnotationsState = ref<{ key: string; value: string }[]>([]);

const customAnnotations = computed(() => {
  return customAnnotationsState.value.reduce((acc, cur) => {
    acc[cur.key] = cur.value;
    return acc;
  }, {} as { [key: string]: string });
});

const handleProcessCustomAnnotations = () => {
  let formSchemas: FormKitSchemaNode[] = [];

  avaliableAnnotationSettings.value.forEach((annotationSetting) => {
    formSchemas = formSchemas.concat(
      annotationSetting.spec?.formSchema as FormKitSchemaNode[]
    );
  });

  customAnnotationsState.value = Object.entries(props.value || {})
    .map(([key, value]) => {
      const fromThemeSpec = formSchemas.some((item) => {
        if (typeof item === "object" && "$formkit" in item) {
          return item.name === key;
        }
        return false;
      });
      if (!fromThemeSpec) {
        return {
          key,
          value,
        };
      }
      return {
        key: "",
        value: "",
      };
    })
    .filter((item) => item.key)
    .filter(
      (item, index, self) => self.findIndex((t) => t.key === item.key) === index
    );

  annotations.value = formSchemas
    .map((item) => {
      if (typeof item === "object" && "$formkit" in item) {
        if (props.value && item.name in props.value) {
          return {
            key: item.name,
            value: props.value[item.name],
          };
        } else {
          return {
            key: item.name,
            value: item.value,
          };
        }
      }
    })
    .filter(Boolean)
    .reduce((acc, cur) => {
      if (cur) {
        acc[cur.key] = cur.value;
      }
      return acc;
    }, {} as { [key: string]: string });
};

onMounted(async () => {
  annotations.value = cloneDeep(props.value) || {};
  await handleFetchAnnotationSettings();
  handleProcessCustomAnnotations();
});

watch(
  () => props.value,
  (value) => {
    reset(specFormId);
    reset(customFormId);
    annotations.value = cloneDeep(props.value) || {};
    if (value) {
      handleProcessCustomAnnotations();
    }
  }
);

// submit

const specFormInvalid = ref(true);
const customFormInvalid = ref(true);

const handleSubmit = async () => {
  if (avaliableAnnotationSettings.value.length) {
    submitForm(specFormId);
  } else {
    specFormInvalid.value = false;
  }
  submitForm(customFormId);
  await nextTick();
};

const onSpecFormSubmitCheck = async (node?: FormKitNode) => {
  if (!node) {
    return;
  }
  const validations = getValidationMessages(node);
  specFormInvalid.value = validations.size > 0;
};

const onCustomFormSubmitCheck = async (node?: FormKitNode) => {
  if (!node) {
    return;
  }
  const validations = getValidationMessages(node);
  customFormInvalid.value = validations.size > 0;
};

defineExpose({
  handleSubmit,
  specFormInvalid,
  customFormInvalid,
  annotations,
  customAnnotations,
});

const showCustomForm = ref(false);

function onCustomFormToggle(e: Event) {
  showCustomForm.value = (e.target as HTMLDetailsElement).open;
}
</script>

<template>
  <div class="flex flex-col gap-3 divide-y divide-gray-100">
    <FormKit
      v-if="annotations && avaliableAnnotationSettings.length > 0"
      :id="specFormId"
      v-model="annotations"
      type="form"
      :preserve="true"
      @submit-invalid="onSpecFormSubmitCheck"
      @submit="specFormInvalid = false"
    >
      <template
        v-for="(annotationSetting, index) in avaliableAnnotationSettings"
      >
        <FormKitSchema
          v-if="annotationSetting.spec?.formSchema"
          :key="index"
          :schema="annotationSetting.spec?.formSchema as (FormKitSchemaCondition| FormKitSchemaNode[])"
        />
      </template>
    </FormKit>

    <details
      :open="showCustomForm"
      class="flex cursor-pointer space-y-4 py-4 transition-all first:pt-0"
      @toggle="onCustomFormToggle"
    >
      <summary class="group flex items-center justify-between">
        <div class="block text-sm font-medium text-gray-700">
          {{
            $t(
              showCustomForm
                ? "core.components.annotations_form.buttons.collapse"
                : "core.components.annotations_form.buttons.expand"
            )
          }}
        </div>
        <div
          class="-mr-1 inline-flex items-center justify-center rounded-full p-1 group-hover:bg-gray-100"
          :class="{ 'bg-gray-100': showCustomForm }"
        >
          <IconArrowRight
            :class="{ 'rotate-90 !text-gray-900': showCustomForm }"
            class="text-gray-600 transition-all"
          />
        </div>
      </summary>

      <FormKit
        v-if="annotations"
        :id="customFormId"
        type="form"
        :preserve="true"
        :form-class="`${avaliableAnnotationSettings.length ? 'py-4' : ''}`"
        @submit-invalid="onCustomFormSubmitCheck"
        @submit="customFormInvalid = false"
      >
        <FormKit
          v-model="customAnnotationsState"
          type="repeater"
          :label="$t('core.components.annotations_form.custom_fields.label')"
        >
          <FormKit
            type="text"
            label="Key"
            name="key"
            validation="required:trim|keyValidationRule"
            :validation-rules="{ keyValidationRule }"
            :validation-messages="{
              keyValidationRule: $t(
                'core.components.annotations_form.custom_fields.validation'
              ),
            }"
          ></FormKit>
          <FormKit type="text" label="Value" name="value" value=""></FormKit>
        </FormKit>
      </FormKit>
    </details>
  </div>
</template>

<style scoped>
details > summary {
  list-style: none;
}

/** Hide the default marker **/
details > summary::-webkit-details-marker {
  display: none;
}
</style>
