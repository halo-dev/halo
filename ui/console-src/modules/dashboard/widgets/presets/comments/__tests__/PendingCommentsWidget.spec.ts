import type { ListedComment } from "@halo-dev/api-client";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, expect, it, vi } from "vite-plus/test";
import { nextTick } from "vue";
import PendingCommentsWidget from "../PendingCommentsWidget.vue";

const { listComments } = vi.hoisted(() => ({
  listComments: vi.fn(),
}));

vi.mock("@halo-dev/api-client", () => ({
  consoleApiClient: {
    content: {
      comment: {
        listComments,
      },
    },
  },
}));

vi.mock("../CommentItem.vue", () => ({
  default: {
    props: ["comment"],
    template:
      '<div class="pending-comment-item" :data-comment-name="comment.comment.metadata.name" />',
  },
}));

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

function buildComments(count: number): ListedComment[] {
  return Array.from({ length: count }, (_, index) => ({
    comment: {
      apiVersion: "content.halo.run/v1alpha1",
      kind: "Comment",
      metadata: { name: `comment-${index}` },
      spec: { approved: false, content: `Comment ${index}` },
    },
    owner: {},
  })) as unknown as ListedComment[];
}

beforeEach(() => {
  vi.stubGlobal("ResizeObserver", ResizeObserverMock);
  listComments.mockReset();
  listComments.mockResolvedValue({ data: { items: buildComments(10) } });
});

afterEach(() => {
  vi.unstubAllGlobals();
});

it("keeps a long pending comment list inside a bounded scroll viewport", async () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  const wrapper = mount(PendingCommentsWidget, {
    global: {
      plugins: [[VueQueryPlugin, { queryClient }]],
      mocks: { $t: (key: string) => key },
    },
  });

  await flushPromises();
  await nextTick();

  expect(listComments).toHaveBeenCalledWith({
    fieldSelector: ["spec.approved=false"],
    page: 1,
    size: 10,
  });
  expect(wrapper.findAll(".pending-comment-item")).toHaveLength(10);

  const scrollArea = wrapper.get(".v-scroll-area");
  expect(scrollArea.classes()).toEqual(
    expect.arrayContaining(["h-full", "w-full"])
  );

  // The card body must keep a bounded height for the scroll area's h-full
  // to resolve; otherwise the list grows and the page scrolls instead.
  const body = scrollArea.element.parentElement as HTMLElement;
  expect(body.classList).toContain("min-h-0");
  expect(body.classList).toContain("flex-1");
  expect(body.classList).toContain("shrink");

  const viewport = scrollArea.get("[data-reka-scroll-area-viewport]");
  expect(viewport.classes()).toContain("overscroll-contain");
  expect(viewport.findAll(".pending-comment-item")).toHaveLength(10);

  viewport.element.scrollTop = 240;
  expect(viewport.element.scrollTop).toBe(240);

  wrapper.unmount();
  queryClient.clear();
});
