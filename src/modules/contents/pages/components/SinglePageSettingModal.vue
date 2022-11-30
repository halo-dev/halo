<script lang="ts" setup>
import { VButton, VModal, VSpace, VTabItem, VTabs } from "@halo-dev/components";
import { computed, ref, watchEffect } from "vue";
import type { SinglePage } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useThemeCustomTemplates } from "@/modules/interface/themes/composables/use-theme";
import { singlePageLabels } from "@/constants/labels";
import { randomUUID } from "@/utils/id";
import { toDatetimeLocal, toISOString } from "@/utils/date";

const initialFormState: SinglePage = {
  spec: {
    title: "",
    slug: "",
    template: "",
    cover: "",
    deleted: false,
    publish: false,
    publishTime: "",
    pinned: false,
    allowComment: true,
    visible: "PUBLIC",
    priority: 0,
    excerpt: {
      autoGenerate: true,
      raw: "",
    },
    htmlMetas: [],
  },
  apiVersion: "content.halo.run/v1alpha1",
  kind: "SinglePage",
  metadata: {
    name: randomUUID(),
  },
};

const props = withDefaults(
  defineProps<{
    visible: boolean;
    singlePage?: SinglePage;
    publishSupport?: boolean;
    onlyEmit?: boolean;
  }>(),
  {
    visible: false,
    singlePage: undefined,
    publishSupport: true,
    onlyEmit: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "saved", singlePage: SinglePage): void;
  (event: "published", singlePage: SinglePage): void;
}>();

const activeTab = ref("general");
const formState = ref<SinglePage>(cloneDeep(initialFormState));
const saving = ref(false);
const publishing = ref(false);
const publishCanceling = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    setTimeout(() => {
      activeTab.value = "general";
    }, 200);
    emit("close");
  }
};

const handleSave = async () => {
  if (props.onlyEmit) {
    emit("saved", formState.value);
    return;
  }

  try {
    saving.value = true;

    saving.value = true;

    const { data } = isUpdateMode.value
      ? await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
          {
            name: formState.value.metadata.name,
            singlePage: formState.value,
          }
        )
      : await apiClient.extension.singlePage.createcontentHaloRunV1alpha1SinglePage(
          {
            singlePage: formState.value,
          }
        );

    formState.value = data;
    emit("saved", data);

    onVisibleChange(false);
  } catch (error) {
    console.error("Failed to save single page", error);
  } finally {
    saving.value = false;
  }
};

const handleSwitchPublish = async (publish: boolean) => {
  if (props.onlyEmit) {
    emit("published", formState.value);
    return;
  }

  try {
    if (publish) {
      publishing.value = true;
    } else {
      publishCanceling.value = true;
    }

    if (publish) {
      formState.value.spec.releaseSnapshot = formState.value.spec.headSnapshot;
    }

    const { data } =
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: formState.value.metadata.name,
          singlePage: {
            ...formState.value,
            spec: {
              ...formState.value.spec,
              publish: publish,
            },
          },
        }
      );

    formState.value = data;

    if (publish) {
      emit("published", data);
    }

    onVisibleChange(false);
  } catch (error) {
    console.error("Failed to publish single page", error);
  } finally {
    publishing.value = false;
    publishCanceling.value = false;
  }
};

watchEffect(() => {
  if (props.singlePage) {
    formState.value = cloneDeep(props.singlePage);
  }
});

// custom templates
const { templates } = useThemeCustomTemplates("page");

// publishTime
const publishTime = computed(() => {
  const { publishTime } = formState.value.spec;
  if (publishTime) {
    return toDatetimeLocal(publishTime);
  }
  return "";
});

const onPublishTimeChange = (value: string) => {
  formState.value.spec.publishTime = toISOString(value);
};
</script>

<template>
  <VModal
    :visible="visible"
    :width="700"
    title="页面设置"
    :centered="false"
    @update:visible="onVisibleChange"
  >
    <template #actions>
      <slot name="actions"></slot>
    </template>

    <VTabs v-model:active-id="activeTab" type="outline">
      <VTabItem id="general" label="常规">
        <FormKit
          id="basic"
          name="basic"
          :actions="false"
          :preserve="true"
          type="form"
        >
          <FormKit
            v-model="formState.spec.title"
            label="标题"
            type="text"
            name="title"
            validation="required|length:0,100"
          ></FormKit>
          <FormKit
            v-model="formState.spec.slug"
            label="别名"
            name="slug"
            type="text"
            validation="required|length:0,100"
          ></FormKit>
          <FormKit
            v-model="formState.spec.excerpt.autoGenerate"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            name="autoGenerate"
            label="自动生成摘要"
            type="radio"
          >
          </FormKit>
          <FormKit
            v-if="!formState.spec.excerpt.autoGenerate"
            v-model="formState.spec.excerpt.raw"
            name="raw"
            label="自定义摘要"
            type="textarea"
            validation="length:0,1024"
            :rows="5"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <FormKit
          id="advanced"
          name="advanced"
          :actions="false"
          :preserve="true"
          type="form"
        >
          <FormKit
            v-model="formState.spec.allowComment"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            name="allowComment"
            label="允许评论"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.spec.pinned"
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="是否置顶"
            name="pinned"
            type="radio"
          ></FormKit>
          <FormKit
            v-model="formState.spec.visible"
            :options="[
              { label: '公开', value: 'PUBLIC' },
              { label: '私有', value: 'PRIVATE' },
            ]"
            label="可见性"
            name="visible"
            type="select"
          ></FormKit>
          <FormKit
            :value="publishTime"
            label="发表时间"
            type="datetime-local"
            name="publishTime"
            @input="onPublishTimeChange"
          ></FormKit>
          <FormKit
            v-model="formState.spec.template"
            :options="templates"
            label="自定义模板"
            type="select"
            name="template"
          ></FormKit>
          <FormKit
            v-model="formState.spec.cover"
            label="封面图"
            type="attachment"
            name="cover"
            validation="length:0,1024"
          ></FormKit>
        </FormKit>
        <!--TODO: add SEO/Metas/Inject Code form-->
      </VTabItem>
    </VTabs>

    <template #footer>
      <VSpace>
        <template v-if="publishSupport">
          <VButton
            v-if="
              formState.metadata.labels?.[singlePageLabels.PUBLISHED] !== 'true'
            "
            :loading="publishing"
            type="secondary"
            @click="handleSwitchPublish(true)"
          >
            发布
          </VButton>
          <VButton
            v-else
            :loading="publishCanceling"
            type="danger"
            @click="handleSwitchPublish(false)"
          >
            取消发布
          </VButton>
        </template>
        <VButton :loading="saving" type="secondary" @click="handleSave">
          保存
        </VButton>
        <VButton size="sm" type="default" @click="onVisibleChange(false)">
          关闭
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
