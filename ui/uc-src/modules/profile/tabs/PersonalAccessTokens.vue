<script lang="ts" setup>
import type { PersonalAccessToken } from "@halo-dev/api-client";
import { ucApiClient } from "@halo-dev/api-client";
import {
  IconAddCircle,
  VButton,
  VEmpty,
  VEntityContainer,
  VLoading,
  VSpace,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { ref } from "vue";
import PersonalAccessTokenCreationModal from "../components/PersonalAccessTokenCreationModal.vue";
import PersonalAccessTokenListItem from "../components/PersonalAccessTokenListItem.vue";

const {
  data: pats,
  isLoading,
  refetch,
} = useQuery<PersonalAccessToken[]>({
  queryKey: ["personal-access-tokens"],
  queryFn: async () => {
    const { data } =
      await ucApiClient.security.personalAccessToken.obtainPats();
    return data;
  },
  refetchInterval(data) {
    const deletingTokens = data?.filter(
      (token) => !!token.metadata.deletionTimestamp
    );
    return deletingTokens?.length ? 1000 : false;
  },
});

const creationModal = ref(false);
</script>
<template>
  <div v-if="pats?.length" class="my-5 flex justify-end">
    <VButton type="secondary" @click="creationModal = true">
      <template #icon>
        <IconAddCircle />
      </template>
      {{ $t("core.common.buttons.new") }}
    </VButton>
  </div>

  <VLoading v-if="isLoading" />

  <Transition v-else-if="!pats?.length" appear name="fade">
    <VEmpty
      :message="$t('core.uc_profile.pat.list.empty.message')"
      :title="$t('core.uc_profile.pat.list.empty.title')"
    >
      <template #actions>
        <VSpace>
          <VButton @click="refetch">
            {{ $t("core.common.buttons.refresh") }}
          </VButton>
          <VButton type="secondary" @click="creationModal = true">
            <template #icon>
              <IconAddCircle />
            </template>
            {{ $t("core.common.buttons.new") }}
          </VButton>
        </VSpace>
      </template>
    </VEmpty>
  </Transition>

  <Transition v-else appear name="fade">
    <div class="overflow-hidden rounded-base border">
      <VEntityContainer>
        <PersonalAccessTokenListItem
          v-for="token in pats"
          :key="token.metadata.name"
          :token="token"
        />
      </VEntityContainer>
    </div>
  </Transition>

  <PersonalAccessTokenCreationModal
    v-if="creationModal"
    @close="creationModal = false"
  />
</template>
