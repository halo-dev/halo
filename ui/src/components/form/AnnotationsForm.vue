<script lang="ts" setup>
import {
  getNode,
  reset,
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

function findArrayItemOriginalKey(node: FormKitNode) {
  let parent = node.parent;
  while (parent) {
    const original = (
      parent.context as { arrayItemOriginal?: { key?: unknown } } | undefined
    )?.arrayItemOriginal;
    if (original) {
      return typeof original.key === "string" ? original.key : undefined;
    }
    parent = parent.parent;
  }
  return undefined;
}

function keyValidationRule(node: FormKitNode) {
  if (!node.value) {
    return true;
  }
  const originalKey = findArrayItemOriginalKey(node);
  let skippedOriginal = false;
  const savedKeys = customAnnotationsState.value
    .map((item) => item.key)
    .filter((key) => {
      if (!skippedOriginal && key === originalKey) {
        skippedOriginal = true;
        return false;
      }
      return true;
    });
  const usedKeys = [...Object.keys(annotations.value), ...savedKeys];
  return !usedKeys.includes(String(node.value));
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
const customAnnotationsId = `${utils.id.uuid()}-customAnnotations`;
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

let specSubmitResolver: (() => void) | undefined;
let customSubmitResolver: (() => void) | undefined;

const resolveSpecSubmit = () => {
  specSubmitResolver?.();
  specSubmitResolver = undefined;
};

const resolveCustomSubmit = () => {
  customSubmitResolver?.();
  customSubmitResolver = undefined;
};

const commitOpenCustomAnnotation = async () => {
  const arrayNode = getNode(customAnnotationsId);
  const commit = (
    arrayNode?.context as
      | {
          commitOpenAccordionItem?: () => Promise<boolean>;
        }
      | undefined
  )?.commitOpenAccordionItem;
  if (!commit) {
    return true;
  }
  return commit();
};

const submitCustomForm = async () => {
  const customFormNode = getNode(customFormId);
  if (!customFormNode) {
    customFormInvalid.value = true;
    return;
  }
  await new Promise<void>((resolve) => {
    customSubmitResolver = resolve;
    customFormNode.submit();
  });
};

const handleSubmit = async () => {
  if (!annotationSettings.value.length) {
    specFormInvalid.value = false;
  } else {
    const specFormNode = getNode(specFormId);
    if (!specFormNode) {
      specFormInvalid.value = true;
      return;
    }
    await new Promise<void>((resolve) => {
      specSubmitResolver = resolve;
      specFormNode.submit();
    });
    if (specFormInvalid.value) {
      return;
    }
  }
  if (!(await commitOpenCustomAnnotation())) {
    customFormInvalid.value = true;
    return;
  }
  await submitCustomForm();
  await nextTick();
};

const onSpecFormSubmitCheck = async (node?: FormKitNode) => {
  if (!node) {
    return;
  }
  const validations = getValidationMessages(node);
  specFormInvalid.value = validations.size > 0;
  resolveSpecSubmit();
};

const onSpecFormSubmit = () => {
  specFormInvalid.value = false;
  resolveSpecSubmit();
};

const onCustomFormSubmitCheck = async (node?: FormKitNode) => {
  if (!node) {
    return;
  }
  const validations = getValidationMessages(node);
  customFormInvalid.value = validations.size > 0;
  resolveCustomSubmit();
};

const onCustomFormSubmit = () => {
  customFormInvalid.value = false;
  resolveCustomSubmit();
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
      @submit="onSpecFormSubmit"
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
      variant="separated"
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
          @submit="onCustomFormSubmit"
        >
          <FormKit
            :id="customAnnotationsId"
            v-model="customAnnotationsState"
            type="array"
            item-form-mode="accordion"
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
