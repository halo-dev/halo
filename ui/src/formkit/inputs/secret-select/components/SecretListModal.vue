<script lang="ts" setup>
import { VButton, VModal } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { coreApiClient } from "@halo-dev/api-client";
import { ref } from "vue";
import SecretListItem from "./SecretListItem.vue";

const modal = ref<InstanceType<typeof VModal> | null>(null);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const { data } = useQuery({
  queryKey: ["secrets"],
  queryFn: async () => {
    const { data } = await coreApiClient.secret.listSecret();
    return data;
  },
});
</script>

<template>
  <VModal
    ref="modal"
    :body-class="['!p-0']"
    title="管理 Secret"
    :width="650"
    @close="emit('close')"
  >
    <ul class="box-border h-full w-full divide-y divide-gray-100" role="list">
      <li v-for="secret in data?.items" :key="secret.metadata.name">
        <SecretListItem :secret="secret" />
      </li>
    </ul>
    <template #footer>
      <VButton @click="modal?.close()">关闭</VButton>
    </template>
  </VModal>
</template>
