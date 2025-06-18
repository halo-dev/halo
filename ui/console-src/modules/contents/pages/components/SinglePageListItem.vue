<script lang="ts" setup>
import EntityFieldItems from "@/components/entity-fields/EntityFieldItems.vue";
import StatusDotField from "@/components/entity-fields/StatusDotField.vue";
import EntityDropdownItems from "@/components/entity/EntityDropdownItems.vue";
import { usePermission } from "@/utils/permission";
import { useEntityFieldItemExtensionPoint } from "@console/composables/use-entity-extension-points";
import { useOperationItemExtensionPoint } from "@console/composables/use-operation-extension-points";
import type { ListedSinglePage, SinglePage } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
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
import CoverField from "./entity-fields/CoverField.vue";
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
    singlePage: ListedSinglePage;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const { singlePage } = toRefs(props);

const emit = defineEmits<{
  (event: "open-setting-modal", singlePage: SinglePage): void;
}>();

const selectedPageNames = inject<Ref<string[]>>("selectedPageNames", ref([]));

const handleDelete = async () => {
  Dialog.warning({
    title: t("core.page.operations.delete.title"),
    description: t("core.page.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await coreApiClient.content.singlePage.patchSinglePage({
        name: props.singlePage.page.metadata.name,
        jsonPatchInner: [
          {
            op: "add",
            path: "/spec/deleted",
            value: true,
          },
        ],
      });

      await queryClient.invalidateQueries({ queryKey: ["singlePages"] });

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const { startFields, endFields } =
  useEntityFieldItemExtensionPoint<ListedSinglePage>(
    "single-page:list-item:field:create",
    singlePage,
    computed((): EntityFieldItem[] => [
      {
        priority: 10,
        position: "start",
        component: markRaw(CoverField),
        hidden: !props.singlePage.page.spec.cover,
        props: {
          singlePage: props.singlePage,
        },
      },
      {
        priority: 20,
        position: "start",
        component: markRaw(TitleField),
        props: {
          singlePage: props.singlePage,
        },
      },
      {
        priority: 10,
        position: "end",
        component: markRaw(ContributorsField),
        props: {
          singlePage: props.singlePage,
        },
      },
      {
        priority: 20,
        position: "end",
        component: markRaw(PublishStatusField),
        props: {
          singlePage: props.singlePage,
        },
      },
      {
        priority: 30,
        position: "end",
        component: markRaw(VisibleField),
        permissions: ["system:singlepages:manage"],
        props: {
          singlePage: props.singlePage,
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
        hidden: !props.singlePage?.page?.spec.deleted,
      },
      {
        priority: 50,
        position: "end",
        component: markRaw(PublishTimeField),
        hidden: !props.singlePage.page.spec.publishTime,
        props: {
          singlePage: props.singlePage,
        },
      },
    ])
  );

const { operationItems } = useOperationItemExtensionPoint<ListedSinglePage>(
  "single-page:list-item:operation:create",
  singlePage,
  computed((): OperationItem<ListedSinglePage>[] => [
    {
      priority: 0,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.edit"),
      action: async () => {
        router.push({
          name: "SinglePageEditor",
          query: { name: props.singlePage.page.metadata.name },
        });
      },
    },
    {
      priority: 10,
      component: markRaw(VDropdownItem),
      label: t("core.common.buttons.setting"),
      action: () => {
        emit("open-setting-modal", props.singlePage.page);
      },
    },
    {
      priority: 20,
      component: markRaw(VDropdownDivider),
    },
    {
      priority: 30,
      component: markRaw(VDropdownItem),
      props: {
        type: "danger",
      },
      label: t("core.common.buttons.delete"),
      action: handleDelete,
    },
  ])
);
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template
      v-if="currentUserHasPermission(['system:singlepages:manage'])"
      #checkbox
    >
      <input
        v-model="selectedPageNames"
        :value="singlePage.page.metadata.name"
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
      v-if="currentUserHasPermission(['system:singlepages:manage'])"
      #dropdownItems
    >
      <EntityDropdownItems
        :dropdown-items="operationItems"
        :item="singlePage"
      />
    </template>
  </VEntity>
</template>
