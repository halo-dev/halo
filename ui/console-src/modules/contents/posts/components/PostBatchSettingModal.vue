<script lang="ts" setup>
import {
  coreApiClient,
  type JsonPatchInner,
  type ListedPost,
} from "@halo-dev/api-client";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useMutation, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

type ArrayPatchOp = "add" | "replace" | "removeAll";

interface FormData {
  category: {
    enabled: boolean;
    names?: string[];
    op: ArrayPatchOp;
  };
  tag: {
    enabled: boolean;
    names?: string[];
    op: ArrayPatchOp;
  };
  owner: {
    enabled: boolean;
    value: string;
  };
  visible: {
    enabled: boolean;
    value: "PUBLIC" | "PRIVATE";
  };
  allowComment: {
    enabled: boolean;
    value: boolean;
  };
}

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(defineProps<{ posts: ListedPost[] }>(), {});

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const { mutate, isLoading } = useMutation({
  mutationKey: ["batch-update-posts"],
  mutationFn: async ({ data }: { data: FormData }) => {
    for (const key in props.posts) {
      const post = props.posts[key];
      const jsonPatchInner: JsonPatchInner[] = [];
      if (data.category.enabled) {
        jsonPatchInner.push({
          op: "add",
          path: "/spec/categories",
          value: computeArrayPatchValue(
            data.category.op,
            post.post.spec.categories || [],
            data.category.names || []
          ),
        });
      }

      if (data.tag.enabled) {
        jsonPatchInner.push({
          op: "add",
          path: "/spec/tags",
          value: computeArrayPatchValue(
            data.tag.op,
            post.post.spec.tags || [],
            data.tag.names || []
          ),
        });
      }

      if (data.owner.enabled) {
        jsonPatchInner.push({
          op: "add",
          path: "/spec/owner",
          value: data.owner.value,
        });
      }

      if (data.visible.enabled) {
        jsonPatchInner.push({
          op: "add",
          path: "/spec/visible",
          value: data.visible.value,
        });
      }

      if (data.allowComment.enabled) {
        jsonPatchInner.push({
          op: "add",
          path: "/spec/allowComment",
          value: data.allowComment.value,
        });
      }

      await coreApiClient.content.post.patchPost({
        name: post.post.metadata.name,
        jsonPatchInner,
      });
    }

    Toast.success(t("core.common.toast.save_success"));
  },
  onSuccess() {
    queryClient.invalidateQueries({ queryKey: ["posts"] });
    modal.value?.close();
  },
  onError() {
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  },
});

function computeArrayPatchValue(
  op: ArrayPatchOp,
  oldValue: string[],
  newValue: string[]
) {
  if (op === "add") {
    return Array.from(new Set([...oldValue, ...newValue]));
  } else if (op === "replace") {
    return newValue;
  } else if (op === "removeAll") {
    return [];
  }
}

function onSubmit(data: FormData) {
  mutate({ data });
}
</script>

<template>
  <VModal
    ref="modal"
    height="calc(100vh - 20px)"
    :title="$t('core.post.batch_setting_modal.title')"
    :width="700"
    @close="emit('close')"
  >
    <FormKit
      id="post-batch-settings-form"
      type="form"
      name="post-batch-settings-form"
      @submit="onSubmit"
    >
      <FormKit
        v-slot="{ value }"
        name="category"
        type="group"
        :label="$t('core.post.batch_setting_modal.fields.category_group')"
      >
        <FormKit
          :value="false"
          :label="$t('core.post.batch_setting_modal.fields.common.enabled')"
          type="checkbox"
          name="enabled"
        ></FormKit>
        <FormKit
          v-if="value?.enabled"
          type="select"
          :options="[
            {
              value: 'add',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.add'
              ),
            },
            {
              value: 'replace',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.replace'
              ),
            },
            {
              value: 'removeAll',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.remove_all'
              ),
            },
          ]"
          :label="$t('core.post.batch_setting_modal.fields.common.op.label')"
          name="op"
          value="add"
        ></FormKit>
        <FormKit
          v-if="value?.enabled && value?.op !== 'removeAll'"
          :label="$t('core.post.batch_setting_modal.fields.category_names')"
          type="categorySelect"
          :multiple="true"
          name="names"
          validation="required"
        ></FormKit>
      </FormKit>
      <FormKit
        v-slot="{ value }"
        type="group"
        name="tag"
        :label="$t('core.post.batch_setting_modal.fields.tag_group')"
      >
        <FormKit
          :value="false"
          :label="$t('core.post.batch_setting_modal.fields.common.enabled')"
          type="checkbox"
          name="enabled"
        ></FormKit>
        <FormKit
          v-if="value?.enabled"
          type="select"
          :options="[
            {
              value: 'add',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.add'
              ),
            },
            {
              value: 'replace',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.replace'
              ),
            },
            {
              value: 'removeAll',
              label: $t(
                'core.post.batch_setting_modal.fields.common.op.options.remove_all'
              ),
            },
          ]"
          :label="$t('core.post.batch_setting_modal.fields.common.op.label')"
          name="op"
          value="add"
        ></FormKit>
        <FormKit
          v-if="value?.enabled && value?.op !== 'removeAll'"
          :label="$t('core.post.batch_setting_modal.fields.tag_names')"
          type="tagSelect"
          :multiple="true"
          name="names"
          validation="required"
        ></FormKit>
      </FormKit>
      <FormKit
        v-slot="{ value }"
        type="group"
        name="owner"
        :label="$t('core.post.batch_setting_modal.fields.owner_group')"
      >
        <FormKit
          :value="false"
          :label="$t('core.post.batch_setting_modal.fields.common.enabled')"
          type="checkbox"
          name="enabled"
        ></FormKit>
        <FormKit
          v-if="value?.enabled"
          :label="$t('core.post.batch_setting_modal.fields.owner_value')"
          name="value"
          type="userSelect"
        ></FormKit>
      </FormKit>
      <FormKit
        v-slot="{ value }"
        type="group"
        name="visible"
        :label="$t('core.post.batch_setting_modal.fields.visible_group')"
      >
        <FormKit
          :value="false"
          :label="$t('core.post.batch_setting_modal.fields.common.enabled')"
          type="checkbox"
          name="enabled"
        ></FormKit>
        <FormKit
          v-if="value?.enabled"
          :options="[
            { label: $t('core.common.select.public'), value: 'PUBLIC' },
            {
              label: $t('core.common.select.private'),
              value: 'PRIVATE',
            },
          ]"
          :label="$t('core.post.batch_setting_modal.fields.visible_value')"
          name="value"
          type="select"
          value="PUBLIC"
        ></FormKit>
      </FormKit>
      <FormKit
        v-slot="{ value }"
        type="group"
        name="allowComment"
        :label="$t('core.post.batch_setting_modal.fields.allow_comment_group')"
      >
        <FormKit
          :value="false"
          :label="$t('core.post.batch_setting_modal.fields.common.enabled')"
          type="checkbox"
          name="enabled"
        ></FormKit>
        <FormKit
          v-if="value?.enabled"
          :options="[
            { label: $t('core.common.radio.yes'), value: true },
            { label: $t('core.common.radio.no'), value: false },
          ]"
          :label="
            $t('core.post.batch_setting_modal.fields.allow_comment_value')
          "
          name="value"
          type="radio"
          :value="true"
        ></FormKit>
      </FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <VButton
          type="secondary"
          :loading="isLoading"
          @click="$formkit.submit('post-batch-settings-form')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
        <VButton @click="modal?.close()">
          {{ $t("core.common.buttons.cancel") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
