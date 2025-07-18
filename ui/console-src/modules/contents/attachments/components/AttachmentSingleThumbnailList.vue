<script lang="ts" setup>
import { storageAnnotations } from "@/constants/annotations";
import {
  coreApiClient,
  LocalThumbnailSpecSizeEnum,
  LocalThumbnailStatusPhaseEnum,
  type Attachment,
} from "@halo-dev/api-client";
import { VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import sha256 from "crypto-js/sha256";
import { computed, toRefs } from "vue";
import AttachmentSingleThumbnailItem from "./AttachmentSingleThumbnailItem.vue";

const props = withDefaults(
  defineProps<{
    attachment?: Attachment;
  }>(),
  {
    attachment: undefined,
  }
);

const { attachment } = toRefs(props);

const imageSignature = computed(() => {
  const uri = attachment.value?.metadata.annotations?.[storageAnnotations.URI];
  if (!uri) {
    return undefined;
  }
  return sha256(uri);
});

const sizeOrder: Record<LocalThumbnailSpecSizeEnum, number> = {
  XL: 4,
  L: 3,
  M: 2,
  S: 1,
};

const { data: thumbnails, isLoading } = useQuery({
  queryKey: ["core:attachments:thumbnails", attachment, imageSignature],
  queryFn: async () => {
    const { data } =
      await coreApiClient.storage.localThumbnail.listLocalThumbnail({
        fieldSelector: [`spec.imageSignature=${imageSignature.value}`],
      });

    return data.items.sort((a, b) => {
      const aSize = a.spec.size as keyof typeof sizeOrder;
      const bSize = b.spec.size as keyof typeof sizeOrder;
      return (sizeOrder[bSize] || 0) - (sizeOrder[aSize] || 0);
    });
  },
  enabled: computed(() => !!imageSignature.value),
  refetchInterval: (data) => {
    const hasAbnormalData = data?.some(
      (thumbnail) =>
        thumbnail.status.phase !== LocalThumbnailStatusPhaseEnum.Succeeded
    );

    return hasAbnormalData ? 1000 : false;
  },
});
</script>
<template>
  <VLoading v-if="isLoading" />
  <ul v-else class="flex flex-col space-y-2">
    <AttachmentSingleThumbnailItem
      v-for="thumbnail in thumbnails"
      :key="thumbnail.metadata.name"
      :thumbnail="thumbnail"
    />
  </ul>
</template>
