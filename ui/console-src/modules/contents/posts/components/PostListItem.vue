<script lang="ts" setup>
import EntityFieldItems from "@/components/entity-fields/EntityFieldItems.vue";
import StatusDotField from "@/components/entity-fields/StatusDotField.vue";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { postLabels } from "@/constants/labels";
import { usePermission } from "@/utils/permission";
import { useEntityFieldItemExtensionPoint } from "@console/composables/use-entity-extension-points";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { ListedPost, Post } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VDropdownDivider,
  VDropdownItem,
  VEntity,
} from "@halo-dev/components";
import type { EntityFieldItem, OperationItem } from "@halo-dev/console-shared";
import { useQueryClient } from "@tanstack/vue-query";
import type { Ref } from "vue";
import { computed, inject, markRaw, ref, toRefs } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import ContributorsField from "./entity-fields/ContributorsField.vue";
import PublishStatusField from "./entity-fields/PublishStatusField.vue";
import PublishTimeField from "./entity-fields/PublishTimeField.vue";
import TitleField from "./entity-fields/TitleField.vue";
import VisibleField from "./entity-fields/VisibleField.vue";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();
const queryClient = useQueryClient();
const router = useRouter();

const props = withDefaults(
  defineProps<{
    post: ListedPost;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const { post } = toRefs(props);

const emit = defineEmits<{
  (event: "open-setting-modal", post: Post): void;
}>();

const selectedPostNames = inject<Ref<string[]>>("selectedPostNames", ref([]));

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.post.operations.delete.title"),
    description: t("core.post.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await consoleApiClient.content.post.recyclePost({
        name: props.post.post.metadata.name,
      });
      await queryClient.invalidateQueries({ queryKey: ["posts"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const { operationItems } = useOperationItemExtensionPoint<ListedPost>(
  "post:list-item:operation:create",
  post,
  computed((): OperationItem<ListedPost>[] => [
    {
      priority: 0,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.publish"),
      action: async () => {
        await consoleApiClient.content.post.publishPost({
          name: props.post.post.metadata.name,
        });

        Toast.success(t("core.common.toast.publish_success"));

        queryClient.invalidateQueries({
          queryKey: ["posts"],
        });
      },
      hidden:
        props.post.post.metadata.labels?.[postLabels.PUBLISHED] == "true" ||
        props.post.post.metadata.labels?.[postLabels.SCHEDULING_PUBLISH] ==
          "true",
    },
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.edit"),
      permissions: [],
      action: () => {
        router.push({
          name: "PostEditor",
          query: { name: props.post.post.metadata.name },
        });
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.setting"),
      permissions: [],
      action: () => {
        emit("open-setting-modal", props.post.post);
      },
    },
    {
      priority: 30,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 40,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.cancel_publish"),
      action: async () => {
        await consoleApiClient.content.post.unpublishPost({
          name: props.post.post.metadata.name,
        });

        Toast.success(t("core.common.toast.cancel_publish_success"));

        queryClient.invalidateQueries({
          queryKey: ["posts"],
        });
      },
      hidden:
        props.post.post.metadata.labels?.[postLabels.PUBLISHED] !== "true" &&
        props.post.post.metadata.labels?.[postLabels.SCHEDULING_PUBLISH] !==
          "true",
    },
    {
      priority: 50,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      permissions: [],
      action: handleDelete,
    },
  ])
);

const { startFields, endFields } = useEntityFieldItemExtensionPoint<ListedPost>(
  "post:list-item:field:create",
  post,
  computed((): EntityFieldItem[] => [
    {
      priority: 10,
      position: "start",
      component: markRaw(TitleField),
      props: {
        post: props.post,
      },
    },
    {
      priority: 10,
      position: "end",
      component: markRaw(ContributorsField),
      props: {
        post: props.post,
      },
    },
    {
      priority: 20,
      position: "end",
      component: markRaw(PublishStatusField),
      props: {
        post: props.post,
      },
    },
    {
      priority: 30,
      position: "end",
      component: markRaw(VisibleField),
      permissions: ["system:posts:manage"],
      props: {
        post: props.post,
      },
    },
    {
      priority: 40,
      position: "end",
      component: markRaw(StatusDotField),
      props: {
        tooltip: t("core.common.status.deleting"),
        state: "warning",
        animate: true,
      },
      hidden: !props.post.post.spec.deleted,
    },
    {
      priority: 50,
      position: "end",
      component: markRaw(PublishTimeField),
      props: {
        post: props.post,
      },
    },
  ])
);
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:posts:manage'])"
      #checkbox
    >
      <input
        v-model="selectedPostNames"
        :value="post.post.metadata.name"
        name="post-checkbox"
        type="checkbox"
      />
    </template>
    <template #start>
      <EntityFieldItems :fields="startFields" />
    </template>
    <template #end>
      <EntityFieldItems :fields="endFields" />
    </template>
    <template
      v-if="currentUserHasPermission(['system:posts:manage'])"
      #dropdownItems
    >
      <EntityDropdownItems :dropdown-items="operationItems" :item="post" />
    </template>
  </VEntity>
</template>
