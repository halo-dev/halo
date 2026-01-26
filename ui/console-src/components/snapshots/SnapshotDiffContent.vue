<script setup lang="ts">
import { type ContentWrapper } from "@halo-dev/api-client";
import {
  IconInformation,
  Toast,
  VDropdown,
  VLoading,
} from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { useLocalStorage } from "@vueuse/core";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";
import { visualDomDiff } from "visual-dom-diff";
import { computed, nextTick, toRefs, useTemplateRef, watch } from "vue";
import { SNAPSHOT_DIFF_QUERY_KEY } from "./query-keys";

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
  queryKey: SNAPSHOT_DIFF_QUERY_KEY(cacheKey, name, snapshotNames),
  queryFn: async () => {
    if (snapshotNames.value?.length !== 2) {
      throw new Error("Please select two snapshots to compare");
    }

    const newSnapshot = await props.getApi(snapshotNames.value[0]);

    const oldSnapshot = await props.getApi(snapshotNames.value[1]);

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
  enabled: computed(() => !!name.value && !!snapshotNames.value?.length),
});

const diffContent = computed(() => {
  if (!snapshot.value) {
    return null;
  }
  const oldContent = document.createElement("div");
  oldContent.innerHTML = snapshot.value.old.content || "";
  const newContent = document.createElement("div");
  newContent.innerHTML = snapshot.value.new.content || "";

  const diffDocument = visualDomDiff(oldContent, newContent, {
    skipModified: true,
  });

  const diffNode = document.createElement("div");
  diffNode.append(diffDocument.cloneNode(true));

  const html = diffNode.innerHTML;

  oldContent.remove();
  newContent.remove();
  diffNode.remove();

  return html;
});

const onlyDiff = useLocalStorage("snapshot-diff-only-diff", false);
const enableSyncScroll = useLocalStorage("snapshot-diff-sync-scroll", true);

const oldScrollRef =
  useTemplateRef<InstanceType<typeof OverlayScrollbarsComponent>>(
    "oldScrollRef"
  );
const newScrollRef =
  useTemplateRef<InstanceType<typeof OverlayScrollbarsComponent>>(
    "newScrollRef"
  );
const diffScrollRef =
  useTemplateRef<InstanceType<typeof OverlayScrollbarsComponent>>(
    "diffScrollRef"
  );

let isSyncing = false;

const syncScroll = (
  sourceRef: typeof oldScrollRef,
  targetRefs: (typeof oldScrollRef)[]
) => {
  if (isSyncing || !enableSyncScroll.value || onlyDiff.value) return;

  const sourceInstance = sourceRef.value?.osInstance();
  if (!sourceInstance) return;

  const sourceViewport = sourceInstance.elements().viewport;
  const scrollTop = sourceViewport.scrollTop;
  const scrollHeight = sourceViewport.scrollHeight;
  const clientHeight = sourceViewport.clientHeight;

  if (scrollHeight <= clientHeight) return;

  const scrollPercentage = scrollTop / (scrollHeight - clientHeight);

  isSyncing = true;

  targetRefs.forEach((targetRef) => {
    const targetInstance = targetRef.value?.osInstance();
    if (!targetInstance) return;

    const targetViewport = targetInstance.elements().viewport;
    const targetScrollHeight = targetViewport.scrollHeight;
    const targetClientHeight = targetViewport.clientHeight;

    if (targetScrollHeight <= targetClientHeight) return;

    const targetScrollTop =
      scrollPercentage * (targetScrollHeight - targetClientHeight);
    targetViewport.scrollTop = targetScrollTop;
  });

  setTimeout(() => {
    isSyncing = false;
  }, 10);
};

watch([oldScrollRef, newScrollRef, diffScrollRef, onlyDiff, snapshot], () => {
  nextTick(() => {
    setTimeout(() => {
      const oldInstance = oldScrollRef.value?.osInstance();
      const newInstance = newScrollRef.value?.osInstance();
      const diffInstance = diffScrollRef.value?.osInstance();

      if (oldInstance) {
        const viewport = oldInstance.elements().viewport;
        viewport.removeEventListener("scroll", handleOldScroll);
        viewport.addEventListener("scroll", handleOldScroll);
      }

      if (newInstance) {
        const viewport = newInstance.elements().viewport;
        viewport.removeEventListener("scroll", handleNewScroll);
        viewport.addEventListener("scroll", handleNewScroll);
      }

      if (diffInstance) {
        const viewport = diffInstance.elements().viewport;
        viewport.removeEventListener("scroll", handleDiffScroll);
        viewport.addEventListener("scroll", handleDiffScroll);
      }
    }, 100);
  });
});

const handleOldScroll = () => {
  const targets = onlyDiff.value
    ? [diffScrollRef]
    : [newScrollRef, diffScrollRef];
  syncScroll(oldScrollRef, targets);
};

const handleNewScroll = () => {
  const targets = onlyDiff.value
    ? [diffScrollRef]
    : [oldScrollRef, diffScrollRef];
  syncScroll(newScrollRef, targets);
};

const handleDiffScroll = () => {
  const targets = onlyDiff.value ? [] : [oldScrollRef, newScrollRef];
  syncScroll(diffScrollRef, targets);
};
</script>
<template>
  <div class="flex h-full flex-col">
    <div class="flex flex-none items-center justify-between border-b px-4 py-3">
      <div class="font-semibold">对比模式</div>
      <div class="flex items-center gap-4">
        <FormKit
          v-model="onlyDiff"
          type="checkbox"
          label="只显示差异"
          :classes="{
            outer: '!py-0',
            wrapper: '!mb-0',
          }"
        ></FormKit>
        <FormKit
          v-model="enableSyncScroll"
          type="checkbox"
          label="同步滚动"
          :disabled="onlyDiff"
          :classes="{
            outer: '!py-0',
            wrapper: '!mb-0',
          }"
        ></FormKit>
      </div>
    </div>

    <div v-if="snapshotNames?.length !== 2" class="flex justify-center py-10">
      <span class="text-gray-600">请选择两个版本进行对比</span>
    </div>

    <VLoading v-else-if="isLoading" />

    <div
      v-else
      class="grid h-full min-h-0 flex-1 shrink grid-cols-1 divide-x"
      :class="{
        'grid-cols-1': onlyDiff,
        'md:grid-cols-3': !onlyDiff,
      }"
    >
      <OverlayScrollbarsComponent
        v-if="!onlyDiff"
        ref="oldScrollRef"
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
        v-if="!onlyDiff"
        ref="newScrollRef"
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
        ref="diffScrollRef"
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
