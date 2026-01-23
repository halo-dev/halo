<script setup lang="ts">
import { consoleApiClient } from "@halo-dev/api-client";
import {
  IconInformation,
  Toast,
  VDropdown,
  VLoading,
} from "@halo-dev/components";
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

    return {
      old: oldSnapshot,
      new: newSnapshot,
    };
  },
  onError(err) {
    if (err instanceof Error) {
      Toast.error(err.message);
    }
  },
  enabled: computed(() => !!postName.value && !!names.value?.length),
});

const diffContent = computed(() => {
  if (!snapshot.value) {
    return null;
  }
  const oldContent = document.createElement("div");
  oldContent.innerHTML = snapshot.value.old.content || "";
  const newContent = document.createElement("div");
  newContent.innerHTML = snapshot.value.new.content || "";

  const diffDocument = visualDomDiff(oldContent, newContent);

  const diffNode = document.createElement("div");
  diffNode.append(diffDocument.cloneNode(true));

  const html = diffNode.innerHTML;

  oldContent.remove();
  newContent.remove();
  diffNode.remove();

  return html;
});
</script>
<template>
  <div class="flex h-full flex-col">
    <div class="flex-none border-b px-4 py-3 font-semibold">对比模式</div>

    <div v-if="names?.length !== 2" class="flex justify-center py-10">
      <span class="text-gray-600">请选择两个版本进行对比</span>
    </div>

    <VLoading v-else-if="isLoading" />

    <div
      v-else
      class="grid h-full min-h-0 flex-1 shrink grid-cols-1 divide-x md:grid-cols-3"
    >
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div class="sticky top-0 border-b bg-white px-4 py-3">
          前一个版本（旧）
        </div>
        <div
          class="snapshot-content markdown-body"
          v-html="snapshot?.old.content"
        ></div>
      </OverlayScrollbarsComponent>
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div class="sticky top-0 border-b bg-white px-4 py-3">
          所选第一个版本（新）
        </div>
        <div
          class="snapshot-content markdown-body"
          v-html="snapshot?.new.content"
        ></div>
      </OverlayScrollbarsComponent>
      <OverlayScrollbarsComponent
        element="div"
        :options="{ scrollbars: { autoHide: 'scroll' } }"
        class="h-full w-full"
        defer
      >
        <div
          class="sticky top-0 flex items-center gap-2 border-b bg-white px-4 py-3"
        >
          <span> 差异 </span>
          <VDropdown :triggers="['hover']">
            <IconInformation class="size-4" />
            <template #popper>
              <div class="w-52">
                <ul class="flex flex-col gap-2">
                  <li class="rounded bg-[#ffe6e6] px-1 py-0.5 line-through">
                    该行代表被删除
                  </li>
                  <li class="rounded bg-[#e6ffe6] px-1 py-0.5">
                    该行代表被添加
                  </li>
                  <li class="rounded bg-[#e6f2ff] px-1 py-0.5">
                    该行代表被修改
                  </li>
                </ul>
              </div>
            </template>
          </VDropdown>
        </div>
        <div class="snapshot-content markdown-body" v-html="diffContent"></div>
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

  div > :first-child {
    margin-top: 0 !important;
  }

  .vdd-removed {
    background-color: #ffe6e6;
    text-decoration: line-through;
  }

  .vdd-added {
    background-color: #e6ffe6;
  }

  .vdd-modified {
    background-color: #e6f2ff;
  }
}
</style>
