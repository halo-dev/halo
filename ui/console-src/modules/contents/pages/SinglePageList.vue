<script lang="ts" setup>
import {
  Dialog,
  IconAddCircle,
  IconArrowLeft,
  IconArrowRight,
  IconPages,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import SinglePageSettingModal from "./components/SinglePageSettingModal.vue";
import type { Ref } from "vue";
import { computed, provide, ref, watch } from "vue";
import type { ListedSinglePage, SinglePage } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { singlePageLabels } from "@/constants/labels";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";
import UserFilterDropdown from "@/components/filter/UserFilterDropdown.vue";
import SinglePageListItem from "./components/SinglePageListItem.vue";
import { useRouteQuery } from "@vueuse/router";

const { t } = useI18n();

const settingModal = ref(false);
const selectedSinglePage = ref<SinglePage>();
const selectedPageNames = ref<string[]>([]);
const checkedAll = ref(false);

provide<Ref<string[]>>("selectedPageNames", selectedPageNames);

// Filters
const selectedContributor = useRouteQuery<string | undefined>("contributor");
const selectedVisible = useRouteQuery<
  "PUBLIC" | "INTERNAL" | "PRIVATE" | undefined
>("visible");
const selectedPublishStatus = useRouteQuery<string | undefined>("status");
const selectedSort = useRouteQuery<string | undefined>("sort");
const keyword = useRouteQuery<string>("keyword", "");

watch(
  () => [
    selectedContributor.value,
    selectedVisible.value,
    selectedPublishStatus.value,
    selectedSort.value,
    keyword.value,
  ],
  () => {
    page.value = 1;
  }
);

const hasFilters = computed(() => {
  return (
    selectedContributor.value ||
    selectedVisible.value ||
    selectedPublishStatus.value !== undefined ||
    selectedSort.value
  );
});

function handleClearFilters() {
  selectedContributor.value = undefined;
  selectedVisible.value = undefined;
  selectedPublishStatus.value = undefined;
  selectedSort.value = undefined;
}

const page = useRouteQuery<number>("page", 1, {
  transform: Number,
});
const size = useRouteQuery<number>("size", 20, {
  transform: Number,
});
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
    selectedPublishStatus,
    page,
    size,
    selectedVisible,
    selectedSort,
    keyword,
  ],
  queryFn: async () => {
    let contributors: string[] | undefined;
    const labelSelector: string[] = ["content.halo.run/deleted=false"];

    if (selectedContributor.value) {
      contributors = [selectedContributor.value];
    }

    if (selectedPublishStatus.value !== undefined) {
      labelSelector.push(
        `${singlePageLabels.PUBLISHED}=${selectedPublishStatus.value}`
      );
    }

    const { data } = await apiClient.singlePage.listSinglePages({
      labelSelector,
      page: page.value,
      size: size.value,
      visible: selectedVisible.value,
      sort: [selectedSort.value].filter(Boolean) as string[],
      keyword: keyword.value,
      contributor: contributors,
    });

    total.value = data.total;
    hasNext.value = data.hasNext;
    hasPrevious.value = data.hasPrevious;

    return data.items;
  },
  refetchInterval(data) {
    const hasAbnormalSinglePage = data?.some((singlePage) => {
      const { spec, metadata } = singlePage.page;
      return (
        spec.deleted ||
        metadata.labels?.[singlePageLabels.PUBLISHED] !== spec.publish + ""
      );
    });
    return hasAbnormalSinglePage ? 1000 : false;
  },
});

const handleOpenSettingModal = async (singlePage: SinglePage) => {
  const { data } =
    await apiClient.extension.singlePage.getContentHaloRunV1alpha1SinglePage({
      name: singlePage.metadata.name,
    });
  selectedSinglePage.value = data;
  settingModal.value = true;
};

const onSettingModalClose = () => {
  selectedSinglePage.value = undefined;
  settingModal.value = false;
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
      await apiClient.extension.singlePage.getContentHaloRunV1alpha1SinglePage({
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
      await apiClient.extension.singlePage.getContentHaloRunV1alpha1SinglePage({
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

          return apiClient.extension.singlePage.updateContentHaloRunV1alpha1SinglePage(
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

watch(selectedPageNames, (newValue) => {
  checkedAll.value = newValue.length === singlePages.value?.length;
});
</script>

<template>
  <SinglePageSettingModal
    v-if="settingModal"
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
            class="relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div
              v-permission="['system:singlepages:manage']"
              class="hidden items-center sm:flex"
            >
              <input
                v-model="checkedAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class="flex w-full flex-1 items-center sm:w-auto">
              <SearchInput v-if="!selectedPageNames.length" v-model="keyword" />
              <VSpace v-else>
                <VButton type="danger" @click="handleDeleteInBatch">
                  {{ $t("core.common.buttons.delete") }}
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class="flex-wrap">
              <FilterCleanButton
                v-if="hasFilters"
                @click="handleClearFilters"
              />
              <FilterDropdown
                v-model="selectedPublishStatus"
                :label="$t('core.common.filters.labels.status')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.all'),
                    value: undefined,
                  },
                  {
                    label: t('core.page.filters.status.items.published'),
                    value: 'true',
                  },
                  {
                    label: t('core.page.filters.status.items.draft'),
                    value: 'false',
                  },
                ]"
              />
              <FilterDropdown
                v-model="selectedVisible"
                :label="$t('core.page.filters.visible.label')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.all'),
                    value: undefined,
                  },
                  {
                    label: t('core.page.filters.visible.items.public'),
                    value: 'PUBLIC',
                  },
                  {
                    label: t('core.page.filters.visible.items.private'),
                    value: 'PRIVATE',
                  },
                ]"
              />
              <HasPermission :permissions="['system:users:view']">
                <UserFilterDropdown
                  v-model="selectedContributor"
                  :label="$t('core.page.filters.author.label')"
                />
              </HasPermission>
              <FilterDropdown
                v-model="selectedSort"
                :label="$t('core.common.filters.labels.sort')"
                :items="[
                  {
                    label: t('core.common.filters.item_labels.default'),
                  },
                  {
                    label: t('core.page.filters.sort.items.publish_time_desc'),
                    value: 'publishTime,desc',
                  },
                  {
                    label: t('core.page.filters.sort.items.publish_time_asc'),
                    value: 'publishTime,asc',
                  },
                  {
                    label: t('core.page.filters.sort.items.create_time_desc'),
                    value: 'creationTimestamp,desc',
                  },
                  {
                    label: t('core.page.filters.sort.items.create_time_asc'),
                    value: 'creationTimestamp,asc',
                  },
                ]"
              />
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
          <li
            v-for="singlePage in singlePages"
            :key="singlePage.page.metadata.name"
          >
            <SinglePageListItem
              :single-page="singlePage"
              :is-selected="checkSelection(singlePage.page)"
              @open-setting-modal="handleOpenSettingModal"
            />
          </li>
        </ul>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          :page-label="$t('core.components.pagination.page_label')"
          :size-label="$t('core.components.pagination.size_label')"
          :total-label="
            $t('core.components.pagination.total_label', { total: total })
          "
          :total="total"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>
</template>
