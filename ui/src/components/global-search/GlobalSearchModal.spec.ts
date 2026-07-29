import messages from "@intlify/unplugin-vue-i18n/messages";
import { flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h, ref } from "vue";
import { createI18n } from "vue-i18n";
import GlobalSearchModal from "./GlobalSearchModal.vue";
import type { GlobalSearchResult } from "./types";
import { useGlobalSearch } from "./use-global-search";

const { pushMock, goMock, modalCloseMock } = vi.hoisted(() => ({
  pushMock: vi.fn(),
  goMock: vi.fn(),
  modalCloseMock: vi.fn(),
}));

let currentRouteName = "Dashboard";

vi.mock("vue-router", () => ({
  useRouter: () => ({
    push: pushMock,
    go: goMock,
    getRoutes: () => [],
  }),
  useRoute: () => ({ name: currentRouteName }),
}));

vi.mock("./use-global-search", () => ({
  useGlobalSearch: vi.fn(),
}));

const useGlobalSearchMock = vi.mocked(useGlobalSearch);

function createResult(
  id: string,
  title = id,
  route: GlobalSearchResult["route"] = { name: "Somewhere" }
): GlobalSearchResult {
  return {
    id,
    sourceId: id.split(":")[0],
    title,
    icon: { src: "icon.png" },
    group: "group",
    route,
  };
}

function setupSearchState(
  overrides: Partial<ReturnType<typeof createState>> = {}
) {
  const state = createState();
  Object.assign(state, overrides);
  useGlobalSearchMock.mockReturnValue(
    state as unknown as ReturnType<typeof useGlobalSearch>
  );
  return state;
}

function createState() {
  return {
    keyword: ref(""),
    normalizedKeyword: ref(""),
    results: ref<GlobalSearchResult[]>([]),
    selectedId: ref<string | null>(null),
    selectedIndex: ref(-1),
    selectedResult: ref<GlobalSearchResult | null>(null),
    selectNext: vi.fn(),
    selectPrevious: vi.fn(),
    isInitial: ref(true),
    isSearching: ref(false),
    hasPartialFailure: ref(false),
    isFinalEmpty: ref(false),
  };
}

// VTU matches stubs for script-setup components by their inferred `__name`,
// which is "Modal" for the VModal SFC.
const VModalStub = defineComponent({
  name: "VModal",
  emits: ["close"],
  setup(_, { slots, expose }) {
    expose({ close: modalCloseMock });
    return () => h("div", slots.default?.());
  },
});

const mountedWrappers: Array<{ unmount: () => void }> = [];

function mountModal() {
  const wrapper = mount(GlobalSearchModal, {
    global: {
      plugins: [createI18n({ legacy: false, locale: "en", messages })],
      stubs: { Modal: VModalStub },
    },
  });
  mountedWrappers.push(wrapper);
  return wrapper;
}

describe("GlobalSearchModal", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    currentRouteName = "Dashboard";
  });

  afterEach(() => {
    // The modal registers a global keydown listener; unmount so listeners
    // and query observers from earlier tests cannot pollute later ones.
    mountedWrappers.splice(0).forEach((wrapper) => wrapper.unmount());
  });

  it("renders the initial state when the keyword is empty", () => {
    setupSearchState();
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("Enter keywords to start searching");
    expect(wrapper.text()).not.toContain("No search results");
  });

  it("renders results immediately with a loading indicator while searching", () => {
    setupSearchState({
      results: ref([createResult("posts:1", "Hello post")]),
      isInitial: ref(false),
      isSearching: ref(true),
    });
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("Hello post");
    expect(wrapper.text()).toContain("Searching…");
    expect(wrapper.text()).not.toContain("No search results");
  });

  it("renders the partial-failure message while keeping results visible", () => {
    setupSearchState({
      results: ref([createResult("tags:1", "Halo tag")]),
      isInitial: ref(false),
      hasPartialFailure: ref(true),
    });
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("Halo tag");
    expect(wrapper.text()).toContain(
      "Some search results are temporarily unavailable"
    );
  });

  it("renders the loading indicator together with the partial-failure message", () => {
    setupSearchState({
      results: ref([createResult("tags:1", "Halo tag")]),
      isInitial: ref(false),
      isSearching: ref(true),
      hasPartialFailure: ref(true),
    });
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("Searching…");
    expect(wrapper.text()).toContain(
      "Some search results are temporarily unavailable"
    );
  });

  it("renders the final empty state when nothing matched", () => {
    setupSearchState({
      isInitial: ref(false),
      isFinalEmpty: ref(true),
    });
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("No search results");
  });

  it("renders the optional match context of a result", () => {
    setupSearchState({
      results: ref([
        { ...createResult("posts:1", "Hello"), context: "hello-world" },
      ]),
      isInitial: ref(false),
    });
    const wrapper = mountModal();

    expect(wrapper.text()).toContain("hello-world");
  });

  it("moves the selection with arrow and Ctrl+J / Ctrl+K shortcuts", () => {
    const state = setupSearchState({
      results: ref([createResult("posts:1"), createResult("posts:2")]),
      isInitial: ref(false),
    });
    mountModal();

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "ArrowDown" }));
    expect(state.selectNext).toHaveBeenCalledTimes(1);

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "ArrowUp" }));
    expect(state.selectPrevious).toHaveBeenCalledTimes(1);

    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "j", ctrlKey: true })
    );
    expect(state.selectNext).toHaveBeenCalledTimes(2);

    window.dispatchEvent(
      new KeyboardEvent("keydown", { key: "k", ctrlKey: true })
    );
    expect(state.selectPrevious).toHaveBeenCalledTimes(2);
  });

  it("performs no navigation and no error on Enter without a result", () => {
    setupSearchState({
      isInitial: ref(false),
      selectedResult: ref(null),
    });
    mountModal();

    expect(() =>
      window.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter" }))
    ).not.toThrow();
    expect(pushMock).not.toHaveBeenCalled();
    expect(goMock).not.toHaveBeenCalled();
  });

  it("navigates to the selected result route on Enter and closes", async () => {
    const item = createResult("posts:1", "Hello", {
      name: "PostEditor",
      query: { name: "post-1" },
    });
    setupSearchState({
      results: ref([item]),
      isInitial: ref(false),
      selectedResult: ref(item),
    });
    mountModal();

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter" }));
    await flushPromises();

    expect(pushMock).toHaveBeenCalledWith(item.route);
    expect(modalCloseMock).toHaveBeenCalled();
    expect(goMock).not.toHaveBeenCalled();
  });

  it("refreshes the page when navigating to the same route", async () => {
    currentRouteName = "Tags";
    const item = createResult("tags:1", "Halo", {
      name: "Tags",
      query: { name: "tag-1" },
    });
    setupSearchState({
      results: ref([item]),
      isInitial: ref(false),
      selectedResult: ref(item),
    });
    mountModal();

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "Enter" }));
    await flushPromises();

    expect(pushMock).toHaveBeenCalledWith(item.route);
    expect(goMock).toHaveBeenCalledWith(0);
  });

  it("closes the modal on Escape", () => {
    setupSearchState();
    mountModal();

    window.dispatchEvent(new KeyboardEvent("keydown", { key: "Escape" }));
    expect(modalCloseMock).toHaveBeenCalled();
  });

  it("navigates when a result is clicked", async () => {
    const item = createResult("posts:1", "Hello", { name: "PostEditor" });
    setupSearchState({
      results: ref([item]),
      isInitial: ref(false),
    });
    const wrapper = mountModal();

    await wrapper.find("li").trigger("click");
    expect(pushMock).toHaveBeenCalledWith(item.route);
  });
});
