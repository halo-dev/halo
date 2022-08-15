<script lang="ts" setup>
import {
  IconArrowLeft,
  IconArrowRight,
  VButton,
  VModal,
  VSpace,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { ref, unref, watch } from "vue";
import type { Post } from "@halo-dev/admin-api";

interface FormState {
  post: Post | Record<string, unknown>;
  saving: boolean;
}

const props = withDefaults(
  defineProps<{
    visible: boolean;
    post: Post | Record<string, unknown> | null;
  }>(),
  {
    visible: false,
    post: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
  (event: "previous"): void;
  (event: "next"): void;
}>();

const settingActiveId = ref("general");
const formState = ref<FormState>({
  post: {},
  saving: false,
});

watch([() => props.visible, () => props.post], () => {
  formState.value.post = unref(props.post) || {};
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};
</script>
<template>
  <VModal
    :visible="visible"
    :width="680"
    title="文章设置"
    @update:visible="handleVisibleChange"
  >
    <template #actions>
      <div class="modal-header-action" @click="emit('previous')">
        <IconArrowLeft />
      </div>
      <div class="modal-header-action" @click="emit('next')">
        <IconArrowRight />
      </div>
    </template>

    <VTabs v-model:active-id="settingActiveId" type="outline">
      <VTabItem id="general" label="常规">
        <FormKit
          id="basic"
          :actions="false"
          :model-value="formState.post"
          :preserve="true"
          type="form"
        >
          <FormKit
            label="标题"
            name="title"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit
            label="别名"
            name="slug"
            type="text"
            validation="required"
          ></FormKit>
          <FormKit label="分类目录" type="select"></FormKit>
          <FormKit label="标签" type="select"></FormKit>
          <FormKit label="摘要" name="summary" type="textarea"></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="advanced" label="高级">
        <FormKit
          id="advanced"
          :actions="false"
          :model-value="formState.post"
          :preserve="true"
          type="form"
        >
          <FormKit
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="禁止评论"
            name="disallowComment"
            type="radio"
          ></FormKit>
          <FormKit
            :options="[
              { label: '是', value: true },
              { label: '否', value: false },
            ]"
            label="是否置顶"
            name="topPriority"
            type="radio"
          ></FormKit>
          <FormKit
            label="发表时间"
            name="createTime"
            type="datetime-local"
          ></FormKit>
          <FormKit label="自定义模板" type="select"></FormKit>
          <FormKit label="封面图" name="thumbnail" type="text"></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="seo" label="SEO">
        <FormKit
          id="seo"
          :actions="false"
          :model-value="formState.post"
          :preserve="true"
          type="form"
        >
          <FormKit
            label="自定义关键词"
            name="metaKeywords"
            type="textarea"
          ></FormKit>
          <FormKit
            label="自定义描述"
            name="metaDescription"
            type="textarea"
          ></FormKit>
        </FormKit>
      </VTabItem>
      <VTabItem id="metas" label="元数据"></VTabItem>
      <VTabItem id="inject-code" label="代码注入">
        <FormKit
          id="inject-code"
          :actions="false"
          :model-value="formState.post"
          :preserve="true"
          type="form"
        >
          <FormKit label="CSS" type="textarea"></FormKit>
          <FormKit label="JavaScript" type="textarea"></FormKit>
        </FormKit>
      </VTabItem>
    </VTabs>

    <template #footer>
      <VSpace>
        <VButton
          :loading="formState.saving"
          type="secondary"
          @click="formState.saving = !formState.saving"
        >
          保存
        </VButton>
        <VButton type="default" @click="handleVisibleChange(false)"
          >取消
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
