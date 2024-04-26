<script setup lang="ts">
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { computed, toRefs } from "vue";
import { Toast, VLoading } from "@halo-dev/components";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

const props = withDefaults(
  defineProps<{
    singlePageName?: string;
    snapshotName?: string;
  }>(),
  {
    singlePageName: undefined,
    snapshotName: undefined,
  }
);

const { singlePageName, snapshotName } = toRefs(props);

const { data: snapshot, isLoading } = useQuery({
  queryKey: ["singlePage-snapshot-by-name", singlePageName, snapshotName],
  queryFn: async () => {
    if (!singlePageName.value || !snapshotName.value) {
      throw new Error("singlePageName and snapshotName are required");
    }

    const { data } = await apiClient.singlePage.fetchSinglePageContent({
      name: singlePageName.value,
      snapshotName: snapshotName.value,
    });
    return data;
  },
  onError(err) {
    if (err instanceof Error) {
      Toast.error(err.message);
    }
  },
  enabled: computed(() => !!singlePageName.value && !!snapshotName.value),
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
