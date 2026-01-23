<script setup lang="ts">
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { visualDomDiff } from "visual-dom-diff";
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
  queryKey: ["post-snapshot-diff-by-name", postName, names],
  queryFn: async () => {
    if (!postName.value || names.value?.length !== 2) {
      throw new Error("Please select two versions to compare");
    }

    const { data: newSnapshot } =
      await consoleApiClient.content.post.fetchPostContent({
        name: postName.value,
        snapshotName: names.value[0],
      });

    const { data: oldSnapshot } =
      await consoleApiClient.content.post.fetchPostContent({
        name: postName.value,
        snapshotName: names.value[1],
      });

    const oldContent = document.createElement("div");
    oldContent.innerHTML = oldSnapshot.content || "";
    const newContent = document.createElement("div");
    newContent.innerHTML = newSnapshot.content || "";

    const diff = visualDomDiff(oldContent, newContent);

    const diffContent = document.createElement("div");
    diffContent.append(diff.cloneNode(true));

    return {
      oldContent: oldSnapshot.content,
      newContent: newSnapshot.content,
      diffContent: diffContent.innerHTML,
    };
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
  <div class="flex h-full flex-col">
    <div class="flex-none border-b p-3">对比模式</div>

    <div v-if="names?.length !== 2" class="flex justify-center py-10">
      <span class="text-gray-600">请选择两个版本进行对比</span>
    </div>

    <VLoading v-else-if="isLoading" />

    <div
      v-else
      class="grid h-full min-h-0 flex-1 shrink grid-cols-1 md:grid-cols-3"
    >
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div
          class="snapshot-content markdown-body"
          v-html="snapshot?.oldContent"
        ></div>
      </OverlayScrollbarsComponent>
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div
          class="snapshot-content markdown-body"
          v-html="snapshot?.newContent"
        ></div>
      </OverlayScrollbarsComponent>
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div
          class="snapshot-content markdown-body"
          v-html="snapshot?.diffContent"
        ></div>
      </OverlayScrollbarsComponent>
    </div>
  </div>
</template>

<style scoped lang="scss">
::v-deep(.snapshot-content) {
  height: 100%;
  width: 100%;
  padding: 1rem;

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

  .vdd-removed {
    background-color: #ffe6e6;
  }

  .vdd-added {
    background-color: #e6ffe6;
  }

  .vdd-modified {
    background-color: #e6f2ff;
  }
}
</style>
