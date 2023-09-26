<script lang="ts" setup>
import {
  IconAddCircle,
  VButton,
  VEmpty,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import { ref } from "vue";
import { apiClient } from "@/utils/api-client";
import type { PersonalAccessToken } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import PersonalAccessTokenCreationModal from "./components/PersonalAccessTokenCreationModal.vue";
import { nextTick } from "vue";
import PersonalAccessTokenListItem from "./components/PersonalAccessTokenListItem.vue";

const {
  data: pats,
  isLoading,
  refetch,
} = useQuery<PersonalAccessToken[]>({
  queryKey: ["personal-access-tokens"],
  queryFn: async () => {
    const { data } = await apiClient.pat.obtainPats();
    return data;
  },
  refetchInterval(data) {
    const deletingTokens = data?.filter(
      (token) => !!token.metadata.deletionTimestamp
    );
    return deletingTokens?.length ? 1000 : false;
  },
});

// fixme: Refactor VModal component to simplify the code
// use v-if to control the visibility of the modal
const creationModal = ref(false);
const creationModalVisible = ref(false);

function handleOpenCreationModal() {
  creationModal.value = true;
  nextTick(() => {
    creationModalVisible.value = true;
  });
}

function onCreationModalClose() {
  creationModalVisible.value = false;
  setTimeout(() => {
    creationModal.value = false;
  }, 200);
}
</script>
<template>
  <div v-if="pats?.length" class="my-5 flex justify-end">
    <VButton type="secondary" @click="handleOpenCreationModal">
      <template #icon>
        <IconAddCircle class="h-full w-full" />
      </template>
      {{ $t("core.common.buttons.new") }}
    </VButton>
  </div>

  <VLoading v-if="isLoading" />

  <Transition v-else-if="!pats?.length" appear name="fade">
    <VEmpty
      :message="$t('core.user.pat.list.empty.message')"
      :title="$t('core.user.pat.list.empty.title')"
    >
      <template #actions>
        <VSpace>
          <VButton @click="refetch">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
          <VButton type="primary" @click="handleOpenCreationModal">
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
      class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
      role="list"
    >
      <li v-for="(token, index) in pats" :key="index">
        <PersonalAccessTokenListItem :token="token" />
      </li>
    </ul>
  </Transition>

  <PersonalAccessTokenCreationModal
    v-if="creationModal"
    v-model="creationModalVisible"
    @close="onCreationModalClose"
  />
</template>
