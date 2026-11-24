<script lang="ts" setup>
import {
  reset,
  submitForm,
  type FormKitNode,
  type FormKitSchemaDefinition,
  type FormKitSchemaNode,
} from "@formkit/core";
import { getValidationMessages } from "@formkit/validation";
import type { AnnotationSetting } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import { VAccordion, VAccordionItem } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { cloneDeep } from "es-toolkit";
import { computed, nextTick, onMounted, ref, watch } from "vue";

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
    formData?: unknown;
  }>(),
  {
    value: null,
    formData: undefined,
  }
);

const annotationSettings = ref<AnnotationSetting[]>([] as AnnotationSetting[]);

const handleFetchAnnotationSettings = async () => {
  try {
    const { data } =
      await ucApiClient.core.annotationSetting.listAvailableAnnotationSettings({
        targetRef: `${[props.group, props.kind].join("/")}`,
      });
    annotationSettings.value = data;
  } catch (error) {
    console.error("Failed to fetch annotation settings", error);
  }
};

const specFormId = `${utils.id.uuid()}-specForm`;
const customFormId = `${utils.id.uuid()}-customForm`;
const annotations = ref<{
  [key: string]: string;
}>({});
const customAnnotationsState = ref<{ key: string; value: string }[]>([]);

const customAnnotations = computed(() => {
  return customAnnotationsState.value.reduce(
    (acc, cur) => {
      acc[cur.key] = cur.value;
      return acc;
    },
    {} as { [key: string]: string }
  );
});

const handleProcessCustomAnnotations = () => {
  let formSchemas: FormKitSchemaNode[] = [];

  annotationSettings.value.forEach((annotationSetting) => {
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
    .reduce(
      (acc, cur) => {
        if (cur) {
          acc[cur.key] = cur.value;
        }
        return acc;
      },
      {} as { [key: string]: string }
    );
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
  if (annotationSettings.value.length) {
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

const customFormOpen = computed({
  get: () => (showCustomForm.value ? "custom" : undefined),
  set: (value: string | string[] | undefined) => {
    showCustomForm.value = value === "custom";
  },
});
</script>

<template>
  <div class="flex flex-col gap-3 divide-y divide-gray-100">
    <FormKit
      v-if="annotations && annotationSettings.length > 0"
      :id="specFormId"
      v-model="annotations"
      type="form"
      :preserve="true"
      @submit-invalid="onSpecFormSubmitCheck"
      @submit="specFormInvalid = false"
    >
      <template v-for="annotationSetting in annotationSettings">
        <FormKitSchema
          v-if="annotationSetting.spec?.formSchema"
          :key="annotationSetting.metadata.name"
          :schema="
            annotationSetting.spec?.formSchema as FormKitSchemaDefinition
          "
          :data="{
            formData,
          }"
        />
      </template>
    </FormKit>

    <VAccordion
      v-model="customFormOpen"
      type="single"
      collapsible
      variant="plain"
    >
      <VAccordionItem
        value="custom"
        header-as="div"
        :title="
          $t(
            showCustomForm
              ? 'core.components.annotations_form.buttons.collapse'
              : 'core.components.annotations_form.buttons.expand'
          )
        "
      >
        <FormKit
          v-if="annotations"
          :id="customFormId"
          type="form"
          :preserve="true"
          :form-class="`${annotationSettings.length ? 'py-4' : ''}`"
          @submit-invalid="onCustomFormSubmitCheck"
          @submit="customFormInvalid = false"
        >
          <FormKit
            v-model="customAnnotationsState"
            type="array"
            :label="$t('core.components.annotations_form.custom_fields.label')"
            :item-labels="[{ type: 'text', label: '$value.key' }]"
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
      </VAccordionItem>
    </VAccordion>
  </div>
</template>
