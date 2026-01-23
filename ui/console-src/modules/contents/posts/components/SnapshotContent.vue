<script setup lang="ts">
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { computed, toRefs } from "vue";

const props = withDefaults(
  defineProps<{
    postName?: string;
    names?: string[];
  }>(),
  {
    postName: undefined,
    names: undefined,
  }
);

const { postName, names } = toRefs(props);

const { data: snapshot, isLoading } = useQuery({
  queryKey: ["post-snapshot-by-name", postName, names],
  queryFn: async () => {
    if (!postName.value || !names.value?.length) {
      throw new Error("postName and names are required");
    }

    const { data } = await consoleApiClient.content.post.fetchPostContent({
      name: postName.value,
      snapshotName: names.value[0],
    });
    return data;
  },
  onError(err) {
    if (err instanceof Error) {
      Toast.error(err.message);
    }
  },
  enabled: computed(() => !!postName.value && !!names.value?.length),
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
