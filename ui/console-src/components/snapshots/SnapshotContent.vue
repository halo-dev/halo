<script setup lang="ts">
import { type ContentWrapper } from "@halo-dev/api-client";
import { Toast, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, toRefs } from "vue";
import { SNAPSHOT_QUERY_KEY } from "./query-keys";

const props = withDefaults(
  defineProps<{
    cacheKey: string;
    name: string;
    snapshotNames?: string[];
    getApi: (snapshotName: string) => Promise<ContentWrapper>;
  }>(),
  {
    snapshotNames: () => [],
  }
);

const { name, snapshotNames, cacheKey } = toRefs(props);

const { data: snapshot, isLoading } = useQuery({
  queryKey: SNAPSHOT_QUERY_KEY(cacheKey, name, snapshotNames),
  queryFn: async () => {
    if (!snapshotNames.value?.length) {
      throw new Error("Please select a snapshot");
    }

    return await props.getApi(snapshotNames.value[0]);
  },
  onError(err) {
    if (err instanceof Error) {
      Toast.error(err.message);
    }
  },
  enabled: computed(() => !!name.value && !!snapshotNames.value?.length),
});
</script>
<template>
  <OverlayScrollbarsComponent
    element="div"
    :options="{ scrollbars: { autoHide: 'scroll' } }"
    class="h-full w-full"
    defer
  >
    <VLoading v-if="isLoading" />
    <div
      v-else
      class="snapshot-content markdown-body h-full w-full p-4"
      v-html="snapshot?.content"
    ></div>
  </OverlayScrollbarsComponent>
</template>

<style scoped lang="scss">
::v-deep(.snapshot-content) {
  p {
    margin-top: 0.75em;
    margin-bottom: 0;
  }

  pre {
    background: #0d0d0d;
    padding: 0.75rem 1rem;
    margin: 0;

    code {
      color: #ccc;
      background: none;
      font-size: 0.8rem;
      padding: 0 !important;
      border-radius: 0;
    }
  }

  ul[data-type="taskList"] {
    list-style: none;
    padding: 0;

    p {
      margin: 0;
    }

    li {
      display: flex;

      > label {
        flex: 0 0 auto;
        margin-right: 0.5rem;
        user-select: none;
      }

      > div {
        flex: 1 1 auto;
      }
    }
  }

  ul {
    list-style: disc !important;
  }

  ol {
    list-style: decimal !important;
  }

  code br {
    display: initial;
  }
}
</style>
