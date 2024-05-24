<script setup lang="ts">
import { useQuery } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { computed, toRefs } from "vue";
import { Toast, VLoading } from "@halo-dev/components";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

const props = withDefaults(
  defineProps<{
    postName?: string;
    snapshotName?: string;
  }>(),
  {
    postName: undefined,
    snapshotName: undefined,
  }
);

const { postName, snapshotName } = toRefs(props);

const { data: snapshot, isLoading } = useQuery({
  queryKey: ["post-snapshot-by-name", postName, snapshotName],
  queryFn: async () => {
    if (!postName.value || !snapshotName.value) {
      throw new Error("postName and snapshotName are required");
    }

    const { data } = await apiClient.post.fetchPostContent({
      name: postName.value,
      snapshotName: snapshotName.value,
    });
    return data;
  },
  onError(err) {
    if (err instanceof Error) {
      Toast.error(err.message);
    }
  },
  enabled: computed(() => !!postName.value && !!snapshotName.value),
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
