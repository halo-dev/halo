<script lang="ts" setup>
import {
  IconAddCircle,
  IconRefreshLine,
  IconDeleteBin,
  VButton,
  VCard,
  VPagination,
  VSpace,
  Dialog,
  VEmpty,
  VAvatar,
  VEntity,
  VEntityField,
  VPageHeader,
  VStatusDot,
  VLoading,
  Toast,
  VDropdownItem,
} from "@halo-dev/components";
import { ref, watch } from "vue";
import type { ListedSinglePage, SinglePage } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { formatDatetime } from "@/utils/date";
import { RouterLink } from "vue-router";
import cloneDeep from "lodash.clonedeep";
import { usePermission } from "@/utils/permission";
import { getNode } from "@formkit/core";
import FilterTag from "@/components/filter/FilterTag.vue";
import { useQuery } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { currentUserHasPermission } = usePermission();
const { t } = useI18n();

const selectedPageNames = ref<string[]>([]);
const checkedAll = ref(false);
const keyword = ref("");

const page = ref(1);
const size = ref(20);
const total = ref(0);

const {
  data: singlePages,
  isLoading,
  isFetching,
  refetch,
} = useQuery<ListedSinglePage[]>({
  queryKey: ["deleted-singlePages", page, size, keyword],
  queryFn: async () => {
    const { data } = await apiClient.singlePage.listSinglePages({
      labelSelector: [`content.halo.run/deleted=true`],
      page: page.value,
      size: size.value,
      keyword: keyword.value,
    });

    total.value = data.total;

    return data.items;
  },
  refetchInterval(data) {
    const deletedSinglePages = data?.filter(
      (singlePage) =>
        !!singlePage.page.metadata.deletionTimestamp ||
        !singlePage.page.spec.deleted
    );
    return deletedSinglePages?.length ? 3000 : false;
  },
});

const checkSelection = (singlePage: SinglePage) => {
  return selectedPageNames.value.includes(singlePage.metadata.name);
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

const handleDeletePermanently = async (singlePage: SinglePage) => {
  Dialog.warning({
    title: t("core.deleted_page.operations.delete.title"),
    description: t("core.deleted_page.operations.delete.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.extension.singlePage.deletecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePage.metadata.name,
        }
      );
      await refetch();

      Toast.success(t("core.common.toast.delete_success"));
    },
  });
};

const handleDeletePermanentlyInBatch = async () => {
  Dialog.warning({
    title: t("core.deleted_page.operations.delete_in_batch.title"),
    description: t("core.deleted_page.operations.delete_in_batch.description"),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await Promise.all(
        selectedPageNames.value.map((name) => {
          return apiClient.extension.singlePage.deletecontentHaloRunV1alpha1SinglePage(
            {
              name,
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

const handleRecovery = async (singlePage: SinglePage) => {
  Dialog.warning({
    title: t("core.deleted_page.operations.recovery.title"),
    description: t("core.deleted_page.operations.recovery.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const singlePageToUpdate = cloneDeep(singlePage);
      singlePageToUpdate.spec.deleted = false;
      await apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: singlePageToUpdate.metadata.name,
          singlePage: singlePageToUpdate,
        }
      );
      await refetch();

      Toast.success(t("core.common.toast.recovery_success"));
    },
  });
};

const handleRecoveryInBatch = async () => {
  Dialog.warning({
    title: t("core.deleted_page.operations.recovery_in_batch.title"),
    description: t(
      "core.deleted_page.operations.recovery_in_batch.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await Promise.all(
        selectedPageNames.value.map((name) => {
          const singlePage = singlePages.value?.find(
            (item) => item.page.metadata.name === name
          )?.page;

          if (!singlePage) {
            return Promise.resolve();
          }

          return apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
            {
              name: singlePage.metadata.name,
              singlePage: {
                ...singlePage,
                spec: {
                  ...singlePage.spec,
                  deleted: false,
                },
              },
            }
          );
        })
      );
      await refetch();
      selectedPageNames.value = [];

      Toast.success(t("core.common.toast.recovery_success"));
    },
  });
};

watch(selectedPageNames, (newValue) => {
  checkedAll.value = newValue.length === singlePages.value?.length;
});

// Filters
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
</script>

<template>
  <VPageHeader :title="$t('core.deleted_page.title')">
    <template #icon>
      <IconDeleteBin class="mr-2 self-center text-green-600" />
    </template>
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'SinglePages' }" size="sm">
          {{ $t("core.common.buttons.back") }}
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
              </div>
              <VSpace v-else>
                <VButton type="danger" @click="handleDeletePermanentlyInBatch">
                  {{ $t("core.common.buttons.delete_permanently") }}
                </VButton>
                <VButton type="default" @click="handleRecoveryInBatch">
                  {{ $t("core.common.buttons.recovery") }}
                </VButton>
              </VSpace>
            </div>
            <div class="mt-4 flex sm:mt-0">
              <VSpace spacing="lg">
                <div class="flex flex-row gap-2">
                  <div
                    class="group cursor-pointer rounded p-1 hover:bg-gray-200"
                    @click="refetch()"
                  >
                    <IconRefreshLine
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
          :message="$t('core.deleted_page.empty.message')"
          :title="$t('core.deleted_page.empty.title')"
        >
          <template #actions>
            <VSpace>
              <VButton @click="refetch">
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
              <VButton
                v-permission="['system:singlepages:view']"
                :route="{ name: 'SinglePages' }"
                type="primary"
              >
                {{ $t("core.common.buttons.back") }}
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
                <VEntityField :title="singlePage.page.spec.title">
                  <template #description>
                    <VSpace>
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
                <VEntityField v-if="!singlePage?.page?.spec.deleted">
                  <template #description>
                    <VStatusDot
                      v-tooltip="$t('core.common.tooltips.recovering')"
                      state="success"
                      animate
                    />
                  </template>
                </VEntityField>
                <VEntityField
                  v-if="singlePage?.page?.metadata.deletionTimestamp"
                >
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
                <VDropdownItem
                  type="danger"
                  @click="handleDeletePermanently(singlePage.page)"
                >
                  {{ $t("core.common.buttons.delete_permanently") }}
                </VDropdownItem>
                <VDropdownItem @click="handleRecovery(singlePage.page)">
                  {{ $t("core.common.buttons.recovery") }}
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
