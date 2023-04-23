<script lang="ts" setup>
import {
  IconArrowDown,
  IconArrowLeft,
  IconArrowRight,
  IconEye,
  IconEyeOff,
  IconAddCircle,
  IconRefreshLine,
  IconExternalLinkLine,
  IconPages,
  VButton,
  VCard,
  VPagination,
  VSpace,
  Dialog,
  VEmpty,
  VAvatar,
  VStatusDot,
  VEntity,
  VEntityField,
  VLoading,
  VPageHeader,
  Toast,
  VDropdown,
  VDropdownItem,
} from "@halo-dev/components";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import UserDropdownSelector from "@/components/dropdown-selector/UserDropdownSelector.vue";
import { computed, ref, watch } from "vue";
import type { ListedSinglePage, SinglePage, User } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { RouterLink } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { usePermission } from "@/utils/permission";
import { singlePageLabels } from "@/constants/labels";
import FilterTag from "@/components/filter/FilterTag.vue";
import FilterCleanButton from "@/components/filter/FilterCleanButton.vue";
import { getNode } from "@formkit/core";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const settingModal = ref(false);
const selectedSinglePage = ref<SinglePage>();
const selectedPageNames = ref<string[]>([]);
const checkedAll = ref(false);

// Filters
interface VisibleItem {
  label: string;
  value?: "PUBLIC" | "INTERNAL" | "PRIVATE";
}

interface PublishStatusItem {
  label: string;
  value?: boolean;
}

interface SortItem {
  label: string;
  sort: "PUBLISH_TIME" | "CREATE_TIME";
  sortOrder: boolean;
}

const VisibleItems: VisibleItem[] = [
  {
    label: t("core.page.filters.visible.items.all"),
    value: undefined,
  },
  {
    label: t("core.page.filters.visible.items.public"),
    value: "PUBLIC",
  },
  {
    label: t("core.page.filters.visible.items.private"),
    value: "PRIVATE",
  },
];

const PublishStatusItems: PublishStatusItem[] = [
  {
    label: t("core.page.filters.status.items.all"),
    value: undefined,
  },
  {
    label: t("core.page.filters.status.items.published"),
    value: true,
  },
  {
    label: t("core.page.filters.status.items.draft"),
    value: false,
  },
];

const SortItems: SortItem[] = [
  {
    label: t("core.page.filters.sort.items.publish_time_desc"),
    sort: "PUBLISH_TIME",
    sortOrder: false,
  },
  {
    label: t("core.page.filters.sort.items.publish_time_asc"),
    sort: "PUBLISH_TIME",
    sortOrder: true,
  },
  {
    label: t("core.page.filters.sort.items.create_time_desc"),
    sort: "CREATE_TIME",
    sortOrder: false,
  },
  {
    label: t("core.page.filters.sort.items.create_time_asc"),
    sort: "CREATE_TIME",
    sortOrder: true,
  },
];

const selectedContributor = ref<User>();
const selectedVisibleItem = ref<VisibleItem>(VisibleItems[0]);
const selectedPublishStatusItem = ref<PublishStatusItem>(PublishStatusItems[0]);
const selectedSortItem = ref<SortItem>();
const keyword = ref("");

function handleVisibleItemChange(visibleItem: VisibleItem) {
  selectedVisibleItem.value = visibleItem;
  page.value = 1;
}

const handleSelectUser = (user?: User) => {
  selectedContributor.value = user;
  page.value = 1;
};

function handlePublishStatusItemChange(publishStatusItem: PublishStatusItem) {
  selectedPublishStatusItem.value = publishStatusItem;
  page.value = 1;
}

function handleSortItemChange(sortItem?: SortItem) {
  selectedSortItem.value = sortItem;
  page.value = 1;
}

function handleKeywordChange() {
  const keywordNode = getNode("keywordInput");
  if (keywordNode) {
    keyword.value = keywordNode._value as string;
  }
  page.value = 1;
}

function handleClearKeyword() {
  keyword.value = "";
  page.value = 1;
}

const hasFilters = computed(() => {
  return (
    selectedContributor.value ||
    selectedVisibleItem.value.value ||
    selectedPublishStatusItem.value.value !== undefined ||
    selectedSortItem.value ||
    keyword.value
  );
});

function handleClearFilters() {
  selectedContributor.value = undefined;
  selectedVisibleItem.value = VisibleItems[0];
  selectedPublishStatusItem.value = PublishStatusItems[0];
  selectedSortItem.value = undefined;
  keyword.value = "";
  page.value = 1;
}

const page = ref(1);
const size = ref(20);
const total = ref(0);
const hasNext = ref(false);
const hasPrevious = ref(false);

const {
  data: singlePages,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedSinglePage[]>({
  queryKey: [
    "singlePages",
    selectedContributor,
    selectedPublishStatusItem,
    page,
    size,
    selectedVisibleItem,
    selectedSortItem,
    keyword,
  ],
  queryFn: async () => {
    let contributors: string[] | undefined;
    const labelSelector: string[] = ["content.halo.run/deleted=false"];

    if (selectedContributor.value) {
      contributors = [selectedContributor.value.metadata.name];
    }

    if (selectedPublishStatusItem.value.value !== undefined) {
      labelSelector.push(
        `${singlePageLabels.PUBLISHED}=${selectedPublishStatusItem.value.value}`
      );
    }

    const { data } = await apiClient.singlePage.listSinglePages({
      labelSelector,
      page: page.value,
      size: size.value,
      visible: selectedVisibleItem.value.value,
      sort: selectedSortItem.value?.sort,
      sortOrder: selectedSortItem.value?.sortOrder,
      keyword: keyword.value,
      contributor: contributors,
    });

    total.value = data.total;
    hasNext.value = data.hasNext;
    hasPrevious.value = data.hasPrevious;

    return data.items;
  },
  refetchInterval(data) {
    const abnormalSinglePages = data?.filter((singlePage) => {
      const { spec, metadata, status } = singlePage.page;
      return (
        spec.deleted ||
        (spec.publish &&
          metadata.labels?.[singlePageLabels.PUBLISHED] !== "true") ||
        (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
      );
    });
    return abnormalSinglePages?.length ? 3000 : false;
  },
});

const handleOpenSettingModal = async (singlePage: SinglePage) => {
  const { data } =
    await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
      name: singlePage.metadata.name,
    });
  selectedSinglePage.value = data;
  settingModal.value = true;
};

const onSettingModalClose = () => {
  selectedSinglePage.value = undefined;
  refetch();
};

const handleSelectPrevious = async () => {
  if (!singlePages.value) return;

  const index = singlePages.value.findIndex(
    (singlePage) =>
      singlePage.page.metadata.name === selectedSinglePage.value?.metadata.name
  );
  if (index > 0) {
    const { data } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: singlePages.value[index - 1].page.metadata.name,
      });
    selectedSinglePage.value = data;
    return;
  }
  if (index === 0 && hasPrevious.value) {
    page.value--;
    await refetch();
    selectedSinglePage.value =
      singlePages.value[singlePages.value.length - 1].page;
  }
};

const handleSelectNext = async () => {
  if (!singlePages.value) return;

  const index = singlePages.value.findIndex(
    (singlePage) =>
      singlePage.page.metadata.name === selectedSinglePage.value?.metadata.name
  );
  if (index < singlePages.value.length - 1) {
    const { data } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: singlePages.value[index + 1].page.metadata.name,
      });
    selectedSinglePage.value = data;
    return;
  }
  if (index === singlePages.value.length - 1 && hasNext.value) {
    page.value++;
    await refetch();
    selectedSinglePage.value = singlePages.value[0].page;
  }
};

const checkSelection = (singlePage: SinglePage) => {
  return (
    singlePage.metadata.name === selectedSinglePage.value?.metadata.name ||
    selectedPageNames.value.includes(singlePage.metadata.name)
  );
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;

  if (checked) {
    selectedPageNames.value =
      singlePages.value?.map((singlePage) => {
        return singlePage.page.metadata.name;
      }) || [];
  } else {
    selectedPageNames.value = [];
  }
};

const handleDelete = async (singlePage: SinglePage) => {
  Dialog.warning({
    title: t("core.page.operations.delete.title"),
    description: t("core.page.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const singlePageToUpdate = cloneDeep(singlePage);
      singlePageToUpdate.spec.deleted = true;
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePage.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );
      await refetch();

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const handleDeleteInBatch = async () => {
  Dialog.warning({
    title: t("core.page.operations.delete_in_batch.title"),
    description: t("core.page.operations.delete_in_batch.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await Promise.all(
        selectedPageNames.value.map((name) => {
          const page = singlePages.value?.find(
            (item) => item.page.metadata.name === name
          )?.page;

          if (!page) {
            return Promise.resolve();
          }

          return apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
            {
              name: page.metadata.name,
              singlePage: {
                ...page,
                spec: {
                  ...page.spec,
                  deleted: true,
                },
              },
            }
          );
        })
      );
      await refetch();
      selectedPageNames.value = [];

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const getPublishStatus = (singlePage: SinglePage) => {
  const { labels } = singlePage.metadata;
  return labels?.[singlePageLabels.PUBLISHED] === "true"
    ? t("core.page.filters.status.items.published")
    : t("core.page.filters.status.items.draft");
};

const isPublishing = (singlePage: SinglePage) => {
  const { spec, status, metadata } = singlePage;
  return (
    (spec.publish &&
      metadata.labels?.[singlePageLabels.PUBLISHED] !== "true") ||
    (spec.releaseSnapshot === spec.headSnapshot && status?.inProgress)
  );
};

watch(selectedPageNames, (newValue) => {
  checkedAll.value = newValue.length === singlePages.value?.length;
});

const { mutate: changeVisibleMutation } = useMutation({
  mutationFn: async (singlePage: SinglePage) => {
    const { data } =
      await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage({
        name: singlePage.metadata.name,
      });
    data.spec.visible = data.spec.visible === "PRIVATE" ? "PUBLIC" : "PRIVATE";
    await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
      {
        name: singlePage.metadata.name,
        singlePage: data,
      },
      {
        mute: true,
      }
    );
    await refetch();
  },
  retry: 3,
  onSuccess: () => {
    Toast.success(t("core.common.toast.operation_success"));
  },
  onError: () => {
    Toast.error(t("core.common.toast.operation_failed"));
  },
});
</script>

<template>
  <SinglePageSettingModal
    v-model:visible="settingModal"
    :single-page="selectedSinglePage"
    @close="onSettingModalClose"
  >
    <template #actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft v-tooltip="$t('core.common.buttons.previous')" />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight v-tooltip="$t('core.common.buttons.next')" />
      </span>
    </template>
  </SinglePageSettingModal>

  <VPageHeader :title="$t('core.page.title')">
    <template #icon>
      <IconPages class="mr-2 self-center" />
    </template>
    <template #actions>
      <VSpace>
        <VButton
          v-permission="['system:singlepages:view']"
          :route="{ name: 'DeletedSinglePages' }"
          size="sm"
        >
          {{ $t("core.page.actions.recycle_bin") }}
        </VButton>
        <VButton
          v-permission="['system:singlepages:manage']"
          :route="{ name: 'SinglePageEditor' }"
          type="secondary"
        >
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          {{ $t("core.common.buttons.new") }}
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class="m-0 md:m-4">
    <VCard :body-class="['!p-0']">
      <template #header>
        <div class="block w-full bg-gray-50 px-4 py-3">
          <div
            class="relative flex flex-col items-start sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:singlepages:manage']"
              class="mr-4 hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <div
                v-if="!selectedPageNames.length"
                class="flex items-center gap-2"
              >
                <FormKit
                  id="keywordInput"
                  outer-class="!p-0"
                  :placeholder="$t('core.common.placeholder.search')"
                  type="text"
                  name="keyword"
                  :model-value="keyword"
                  @keyup.enter="handleKeywordChange"
                ></FormKit>

                <FilterTag v-if="keyword" @close="handleClearKeyword()">
                  {{
                    $t("core.common.filters.results.keyword", {
                      keyword: keyword,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedPublishStatusItem.value !== undefined"
                  @close="handlePublishStatusItemChange(PublishStatusItems[0])"
                >
                  {{
                    $t("core.common.filters.results.status", {
                      status: selectedPublishStatusItem.label,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedVisibleItem.value"
                  @close="handleVisibleItemChange(VisibleItems[0])"
                >
                  {{
                    $t("core.page.filters.visible.result", {
                      visible: selectedVisibleItem.label,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedContributor"
                  @close="handleSelectUser()"
                >
                  {{
                    $t("core.page.filters.author.result", {
                      author: selectedContributor.spec.displayName,
                    })
                  }}
                </FilterTag>

                <FilterTag
                  v-if="selectedSortItem"
                  @close="handleSortItemChange()"
                >
                  {{
                    $t("core.common.filters.results.sort", {
                      sort: selectedSortItem.label,
                    })
                  }}
                </FilterTag>

                <FilterCleanButton
                  v-if="hasFilters"
                  @click="handleClearFilters"
                />
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.common.filters.labels.status") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(filterItem, index) in PublishStatusItems"
                      :key="index"
                      :selected="
                        filterItem.value === selectedPublishStatusItem.value
                      "
                      @click="handlePublishStatusItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.page.filters.visible.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(filterItem, index) in VisibleItems"
                      :key="index"
                      :selected="filterItem.value === selectedVisibleItem.value"
                      @click="handleVisibleItemChange(filterItem)"
                    >
                      {{ filterItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <UserDropdownSelector
                  v-model:selected="selectedContributor"
                  @select="handleSelectUser"
                >
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.page.filters.author.label") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                </UserDropdownSelector>
                <VDropdown>
                  <div
                    class="flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
                  >
                    <span class="mr-0.5">
                      {{ $t("core.common.filters.labels.sort") }}
                    </span>
                    <span>
                      <IconArrowDown />
                    </span>
                  </div>
                  <template #popper>
                    <VDropdownItem
                      v-for="(sortItem, index) in SortItems"
                      :key="index"
                      :selected="
                        sortItem.sort === selectedSortItem?.sort &&
                        sortItem.sortOrder === selectedSortItem?.sortOrder
                      "
                      @click="handleSortItemChange(sortItem)"
                    >
                      {{ sortItem.label }}
                    </VDropdownItem>
                  </template>
                </VDropdown>
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
                      v-tooltip="$t('core.common.buttons.refresh')"
                      :class="{ 'animate-spin text-gray-900': isFetching }"
                      class="h-4 w-4 text-gray-600 group-hover:text-gray-900"
                    />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </div>
      </template>
      <VLoading v-if="isLoading" />
      <Transition v-else-if="!singlePages?.length" appear name="fade">
        <VEmpty
          :message="$t('core.page.empty.message')"
          :title="$t('core.page.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:singlepages:manage']"
                :route="{ name: 'SinglePageEditor' }"
                type="primary"
              >
                <template #icon>
                  <IconAddCircle class="h-full w-full" />
                </template>
                {{ $t("core.common.buttons.new") }}
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <ul
          class="box-border h-full w-full divide-y divide-gray-100"
          role="list"
        >
          <li v-for="(singlePage, index) in singlePages" :key="index">
            <VEntity :is-selected="checkSelection(singlePage.page)">
              <template
                v-if="currentUserHasPermission(['system:singlepages:manage'])"
                #checkbox
              >
                <input
                  v-model="selectedPageNames"
                  :value="singlePage.page.metadata.name"
                  class="h-4 w-4 rounded border-gray-300 text-indigo-600"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField
                  :title="singlePage.page.spec.title"
                  :route="{
                    name: 'SinglePageEditor',
                    query: { name: singlePage.page.metadata.name },
                  }"
                >
                  <template #extra>
                    <VSpace>
                      <RouterLink
                        v-if="singlePage.page.status?.inProgress"
                        v-tooltip="
                          $t('core.common.tooltips.unpublished_content_tip')
                        "
                        :to="{
                          name: 'SinglePageEditor',
                          query: { name: singlePage.page.metadata.name },
                        }"
                        class="flex items-center"
                      >
                        <VStatusDot state="success" animate />
                      </RouterLink>
                      <a
                        v-if="singlePage.page.status?.permalink"
                        target="_blank"
                        :href="singlePage.page.status?.permalink"
                        :title="singlePage.page.status?.permalink"
                        class="hidden text-gray-600 transition-all hover:text-gray-900 group-hover:inline-block"
                      >
                        <IconExternalLinkLine class="h-3.5 w-3.5" />
                      </a>
                    </VSpace>
                  </template>
                  <template #description>
                    <div class="flex w-full flex-col gap-1">
                      <VSpace class="w-full">
                        <span class="text-xs text-gray-500">
                          {{
                            $t("core.page.list.fields.visits", {
                              visits: singlePage.stats.visit || 0,
                            })
                          }}
                        </span>
                        <span class="text-xs text-gray-500">
                          {{
                            $t("core.page.list.fields.comments", {
                              comments: singlePage.stats.totalComment || 0,
                            })
                          }}
                        </span>
                      </VSpace>
                    </div>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField>
                  <template #description>
                    <RouterLink
                      v-for="(
                        contributor, contributorIndex
                      ) in singlePage.contributors"
                      :key="contributorIndex"
                      :to="{
                        name: 'UserDetail',
                        params: { name: contributor.name },
                      }"
                      class="flex items-center"
                    >
                      <VAvatar
                        v-tooltip="contributor.displayName"
                        size="xs"
                        :src="contributor.avatar"
                        :alt="contributor.displayName"
                        circle
                      ></VAvatar>
                    </RouterLink>
                  </template>
                </VEntityField>
                <VEntityField :description="getPublishStatus(singlePage.page)">
                  <template v-if="isPublishing(singlePage.page)" #description>
                    <VStatusDot
                      :text="$t('core.common.tooltips.publishing')"
                      animate
                    />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <IconEye
                      v-if="singlePage.page.spec.visible === 'PUBLIC'"
                      v-tooltip="$t('core.page.filters.visible.items.public')"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                      @click="changeVisibleMutation(singlePage.page)"
                    />
                    <IconEyeOff
                      v-if="singlePage.page.spec.visible === 'PRIVATE'"
                      v-tooltip="$t('core.page.filters.visible.items.private')"
                      class="cursor-pointer text-sm transition-all hover:text-blue-600"
                      @click="changeVisibleMutation(singlePage.page)"
                    />
                  </template>
                </VEntityField>
                <VEntityField v-if="singlePage?.page?.spec.deleted">
                  <template #description>
                    <VStatusDot
                      v-tooltip="$t('core.common.status.deleting')"
                      state="warning"
                      animate
                    />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class="truncate text-xs tabular-nums text-gray-500">
                      {{ formatDatetime(singlePage.page.spec.publishTime) }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template
                v-if="currentUserHasPermission(['system:singlepages:manage'])"
                #dropdownItems
              >
                <VDropdownItem @click="handleOpenSettingModal(singlePage.page)">
                  {{ $t("core.common.buttons.setting") }}
                </VDropdownItem>
                <VDropdownItem
                  type="danger"
                  @click="handleDelete(singlePage.page)"
                >
                  {{ $t("core.common.buttons.delete") }}
                </VDropdownItem>
              </template>
            </VEntity>
          </li>
        </ul>
      </Transition>

      <template #footer>
        <div class="bg-white sm:flex sm:items-center sm:justify-end">
          <VPagination
            v-model:page="page"
            v-model:size="size"
            :page-label="$t('core.components.pagination.page_label')"
            :size-label="$t('core.components.pagination.size_label')"
            :total="total"
            :size-options="[20, 30, 50, 100]"
          />
        </div>
      </template>
    </VCard>
  </div>
</template>
