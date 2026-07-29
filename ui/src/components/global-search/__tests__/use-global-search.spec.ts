import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { defineComponent, h, nextTick } from "vue";
import type {
  CachedGlobalSearchProvider,
  GlobalSearchProvider,
  GlobalSearchResult,
  LocalGlobalSearchProvider,
  RemoteGlobalSearchProvider,
} from "../types";
import {
  mergeGlobalSearchResults,
  useGlobalSearch,
} from "../use-global-search";

function createResult(id: string, title = id): GlobalSearchResult {
  return {
    id,
    sourceId: id.split(":")[0],
    title,
    icon: { src: "icon.png" },
    group: "group",
    route: { name: "Route" },
  };
}

function createLocalProvider(
  items: GlobalSearchResult[]
): LocalGlobalSearchProvider {
  return {
    id: "local",
    mode: "local",
    isAvailable: () => true,
    getItems: () => items,
  };
}

function createRemoteProvider(
  id: string,
  search: RemoteGlobalSearchProvider["search"],
  isAvailable: () => boolean = () => true
): RemoteGlobalSearchProvider {
  return {
    id,
    mode: "remote",
    limit: 4,
    isAvailable,
    search,
  };
}

const mountedWrappers: Array<{ unmount: () => void }> = [];

function mountUseGlobalSearch(
  providers: GlobalSearchProvider[],
  queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false, networkMode: "always" },
    },
  })
) {
  let api!: ReturnType<typeof useGlobalSearch>;
  const wrapper = mount(
    defineComponent({
      setup() {
        api = useGlobalSearch(providers);
        return () => h("div");
      },
    }),
    {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    }
  );
  mountedWrappers.push(wrapper);
  return { api, wrapper, queryClient };
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe("mergeGlobalSearchResults", () => {
  it("keeps at most 8 local results", () => {
    const local = Array.from({ length: 10 }, (_, i) =>
      createResult(`local:${i}`)
    );
    const merged = mergeGlobalSearchResults(local, []);
    expect(merged).toHaveLength(8);
    expect(merged[0].id).toBe("local:0");
  });

  it("keeps at most 20 results in total", () => {
    const local = Array.from({ length: 8 }, (_, i) =>
      createResult(`local:${i}`)
    );
    const pools = ["posts", "pages", "attachments", "users", "tags"].map(
      (source) =>
        Array.from({ length: 4 }, (_, i) => createResult(`${source}:${i}`))
    );
    const merged = mergeGlobalSearchResults(local, pools);
    expect(merged).toHaveLength(20);
  });

  it("shares the remaining capacity through a stable provider rotation", () => {
    const local = Array.from({ length: 8 }, (_, i) =>
      createResult(`local:${i}`)
    );
    const pools = ["posts", "pages", "attachments", "users", "tags"].map(
      (source) =>
        Array.from({ length: 4 }, (_, i) => createResult(`${source}:${i}`))
    );
    const merged = mergeGlobalSearchResults(local, pools);

    const remoteIds = merged.slice(8).map((item) => item.id);
    // 12 remaining positions filled in rotation: each provider gets at
    // least 2 before posts and pages receive a third.
    expect(remoteIds).toEqual([
      "posts:0",
      "pages:0",
      "attachments:0",
      "users:0",
      "tags:0",
      "posts:1",
      "pages:1",
      "attachments:1",
      "users:1",
      "tags:1",
      "posts:2",
      "pages:2",
    ]);
  });

  it("skips exhausted pools during the rotation", () => {
    const merged = mergeGlobalSearchResults(
      [],
      [
        [createResult("posts:0")],
        [createResult("pages:0"), createResult("pages:1")],
        [],
      ]
    );
    expect(merged.map((item) => item.id)).toEqual([
      "posts:0",
      "pages:0",
      "pages:1",
    ]);
  });
});

describe("useGlobalSearch", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    mountedWrappers.splice(0).forEach((wrapper) => wrapper.unmount());
    vi.useRealTimers();
  });

  it("trims the keyword before searching remotely", async () => {
    const search = vi.fn().mockResolvedValue([]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "  hello  ";
    await vi.advanceTimersByTimeAsync(300);

    expect(search).toHaveBeenCalledWith("hello", expect.anything());
  });

  it("debounces remote searches by 300 ms", async () => {
    const search = vi.fn().mockResolvedValue([]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "hello";
    await vi.advanceTimersByTimeAsync(299);
    expect(search).not.toHaveBeenCalled();

    await vi.advanceTimersByTimeAsync(1);
    expect(search).toHaveBeenCalledTimes(1);
  });

  it("does not search remotely for an empty or whitespace keyword", async () => {
    const search = vi.fn().mockResolvedValue([]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "   ";
    await vi.advanceTimersByTimeAsync(300);

    expect(search).not.toHaveBeenCalled();
    expect(api.isInitial.value).toBe(true);
  });

  it("treats a single character as a valid remote-search keyword", async () => {
    const search = vi.fn().mockResolvedValue([]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "a";
    await vi.advanceTimersByTimeAsync(300);

    expect(search).toHaveBeenCalledWith("a", expect.anything());
  });

  it("does not query providers whose destination is not permitted", async () => {
    const search = vi.fn().mockResolvedValue([]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search, () => false),
    ]);

    api.keyword.value = "hello";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    expect(search).not.toHaveBeenCalled();
  });

  it("ignores late responses for older keywords", async () => {
    const first = deferred<GlobalSearchResult[]>();
    const second = deferred<GlobalSearchResult[]>();
    const search = vi
      .fn()
      .mockImplementation((keyword: string) =>
        keyword === "first" ? first.promise : second.promise
      );
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "first";
    await vi.advanceTimersByTimeAsync(300);
    expect(search).toHaveBeenCalledWith("first", expect.anything());

    api.keyword.value = "second";
    await vi.advanceTimersByTimeAsync(300);
    expect(search).toHaveBeenCalledWith("second", expect.anything());

    second.resolve([createResult("posts:current", "current")]);
    await flushPromises();
    expect(api.results.value.map((item) => item.id)).toEqual(["posts:current"]);

    // The stale response for the older keyword resolves late and must not
    // replace the current results nor surface as a partial failure.
    first.resolve([createResult("posts:stale", "stale")]);
    await flushPromises();
    expect(api.results.value.map((item) => item.id)).toEqual(["posts:current"]);
    expect(api.hasPartialFailure.value).toBe(false);
  });

  it("keeps successful results visible when one provider fails and does not retry", async () => {
    const failingSearch = vi.fn().mockRejectedValue(new Error("boom"));
    const successfulSearch = vi
      .fn()
      .mockResolvedValue([createResult("tags:1", "tag result")]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", failingSearch),
      createRemoteProvider("tags", successfulSearch),
    ]);

    api.keyword.value = "result";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    expect(api.results.value.map((item) => item.id)).toEqual(["tags:1"]);
    expect(api.hasPartialFailure.value).toBe(true);
    expect(failingSearch).toHaveBeenCalledTimes(1);
  });

  it("updates local results when cached source data arrives after typing", async () => {
    const fetchDeferred = deferred<GlobalSearchResult[]>();
    const cachedProvider: CachedGlobalSearchProvider<GlobalSearchResult[]> = {
      id: "plugins",
      mode: "cached",
      isAvailable: () => true,
      fetch: () => fetchDeferred.promise,
      map: (data) => data,
    };
    const { api } = mountUseGlobalSearch([cachedProvider]);

    api.keyword.value = "cached";
    await vi.advanceTimersByTimeAsync(300);
    expect(api.results.value).toHaveLength(0);

    fetchDeferred.resolve([createResult("plugins:1", "cached plugin")]);
    await flushPromises();

    expect(api.results.value.map((item) => item.id)).toEqual(["plugins:1"]);
  });

  it("does not fetch cached sources the user cannot access", async () => {
    const fetch = vi.fn().mockResolvedValue([]);
    const cachedProvider: CachedGlobalSearchProvider<GlobalSearchResult[]> = {
      id: "plugins",
      mode: "cached",
      isAvailable: () => false,
      fetch,
      map: (data) => data,
    };
    mountUseGlobalSearch([cachedProvider]);
    await flushPromises();

    expect(fetch).not.toHaveBeenCalled();
  });

  it("reuses recent remote results for a repeated keyword", async () => {
    const search = vi.fn().mockResolvedValue([createResult("posts:1")]);
    const { api } = mountUseGlobalSearch([
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "repeat";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();
    expect(search).toHaveBeenCalledTimes(1);

    api.keyword.value = "other";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();
    expect(search).toHaveBeenCalledTimes(2);

    api.keyword.value = "repeat";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();
    // Still within the 30 second freshness window, no new request.
    expect(search).toHaveBeenCalledTimes(2);
    expect(api.results.value.map((item) => item.id)).toEqual(["posts:1"]);
  });

  it("merges local and remote results with the 8/4/20 limits", async () => {
    const localItems = Array.from({ length: 10 }, (_, i) =>
      createResult(`local:${i}`, `search local ${i}`)
    );
    const remoteSearch = (source: string) =>
      vi
        .fn()
        .mockResolvedValue(
          Array.from({ length: 4 }, (_, i) =>
            createResult(`${source}:${i}`, `${source} ${i}`)
          )
        );
    const { api } = mountUseGlobalSearch([
      createLocalProvider(localItems),
      createRemoteProvider("posts", remoteSearch("posts")),
      createRemoteProvider("single-pages", remoteSearch("pages")),
      createRemoteProvider("attachments", remoteSearch("attachments")),
      createRemoteProvider("users", remoteSearch("users")),
      createRemoteProvider("tags", remoteSearch("tags")),
    ]);

    api.keyword.value = "search";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    const results = api.results.value;
    expect(results.length).toBeLessThanOrEqual(20);
    expect(
      results.filter((item) => item.sourceId === "local").length
    ).toBeLessThanOrEqual(8);
    for (const source of ["posts", "pages", "attachments", "users", "tags"]) {
      expect(
        results.filter((item) => item.sourceId === source).length
      ).toBeLessThanOrEqual(4);
    }
  });

  it("slices remote pools by the provider limit before merging", async () => {
    const { api } = mountUseGlobalSearch([
      createLocalProvider(
        Array.from({ length: 8 }, (_, i) =>
          createResult(`local:${i}`, `search local ${i}`)
        )
      ),
      createRemoteProvider(
        "posts",
        vi
          .fn()
          .mockResolvedValue(
            Array.from({ length: 6 }, (_, i) =>
              createResult(`posts:${i}`, `search posts ${i}`)
            )
          )
      ),
    ]);

    api.keyword.value = "search";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    // 8 local results plus at most the provider limit of 4 posts, even
    // though the provider returned 6.
    expect(api.results.value.map((item) => item.id)).toEqual([
      "local:0",
      "local:1",
      "local:2",
      "local:3",
      "local:4",
      "local:5",
      "local:6",
      "local:7",
      "posts:0",
      "posts:1",
      "posts:2",
      "posts:3",
    ]);
  });

  it("does not show or select previous-keyword results during the debounce window", async () => {
    const second = deferred<GlobalSearchResult[]>();
    const search = vi
      .fn()
      .mockImplementation((keyword: string) =>
        keyword === "second"
          ? second.promise
          : Promise.resolve([createResult(`posts:${keyword}`)])
      );
    const { api } = mountUseGlobalSearch([
      createLocalProvider([createResult("local:a", "second local")]),
      createRemoteProvider("posts", search),
    ]);

    api.keyword.value = "first";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();
    expect(api.results.value.map((item) => item.id)).toEqual(["posts:first"]);

    // The new keyword is typed but the debounce has not elapsed yet: the
    // remote results for the previous keyword must disappear immediately,
    // so they cannot be selected or navigated.
    api.keyword.value = "second";
    await nextTick();
    expect(api.results.value.map((item) => item.id)).toEqual(["local:a"]);
    expect(api.selectedResult.value?.id).toBe("local:a");

    // The new remote search has started but not completed yet, so only
    // local results remain visible.
    await vi.advanceTimersByTimeAsync(300);
    expect(api.results.value.map((item) => item.id)).toEqual(["local:a"]);

    second.resolve([createResult("posts:second")]);
    await flushPromises();
    expect(api.results.value.map((item) => item.id)).toEqual([
      "local:a",
      "posts:second",
    ]);
  });

  it("does not report the final empty state while a bounded source is loading", async () => {
    const fetchDeferred = deferred<GlobalSearchResult[]>();
    const cachedProvider: CachedGlobalSearchProvider<GlobalSearchResult[]> = {
      id: "plugins",
      mode: "cached",
      isAvailable: () => true,
      fetch: () => fetchDeferred.promise,
      map: (data) => data,
    };
    const { api } = mountUseGlobalSearch([
      cachedProvider,
      createRemoteProvider("posts", vi.fn().mockResolvedValue([])),
    ]);

    api.keyword.value = "nothing matches";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    // The remote provider has completed without results, but the cached
    // plugins are still loading: the search is not finished yet.
    expect(api.isSearching.value).toBe(true);
    expect(api.isFinalEmpty.value).toBe(false);

    fetchDeferred.resolve([]);
    await flushPromises();

    expect(api.isSearching.value).toBe(false);
    expect(api.isFinalEmpty.value).toBe(true);
  });

  it("preserves the selection by stable id while results arrive", async () => {
    const remoteDeferred = deferred<GlobalSearchResult[]>();
    const { api } = mountUseGlobalSearch([
      createLocalProvider([
        createResult("local:a", "search target one"),
        createResult("local:b", "search target two"),
      ]),
      createRemoteProvider("posts", () => remoteDeferred.promise),
    ]);

    api.keyword.value = "search";
    await nextTick();
    expect(api.results.value.length).toBe(2);

    api.selectNext();
    const selectedId = api.selectedResult.value?.id;
    expect(selectedId).toBe(api.results.value[1].id);

    await vi.advanceTimersByTimeAsync(300);
    remoteDeferred.resolve([createResult("posts:1", "search remote")]);
    await flushPromises();

    expect(api.results.value.length).toBe(3);
    expect(api.selectedResult.value?.id).toBe(selectedId);
  });

  it("resets the selection to the first result when the keyword changes", async () => {
    const { api } = mountUseGlobalSearch([
      createLocalProvider([
        createResult("local:a", "search target one"),
        createResult("local:b", "search target two"),
      ]),
    ]);

    api.keyword.value = "search";
    await nextTick();
    api.selectNext();
    const previousSelected = api.selectedResult.value?.id;

    api.keyword.value = "target";
    await nextTick();

    expect(api.selectedResult.value?.id).toBe(api.results.value[0].id);
    expect(api.selectedResult.value?.id).not.toBe(previousSelected);
  });

  it("moves the selection within the available result bounds", async () => {
    const { api } = mountUseGlobalSearch([
      createLocalProvider([
        createResult("local:a", "search target one"),
        createResult("local:b", "search target two"),
      ]),
    ]);

    api.keyword.value = "search";
    await nextTick();
    expect(api.selectedIndex.value).toBe(0);

    api.selectPrevious();
    expect(api.selectedIndex.value).toBe(0);

    api.selectNext();
    expect(api.selectedIndex.value).toBe(1);

    api.selectNext();
    expect(api.selectedIndex.value).toBe(1);
  });

  it("exposes the searching state while remote providers are pending", async () => {
    const remoteDeferred = deferred<GlobalSearchResult[]>();
    const { api } = mountUseGlobalSearch([
      createLocalProvider([createResult("local:a", "search target")]),
      createRemoteProvider("posts", () => remoteDeferred.promise),
    ]);

    api.keyword.value = "search";
    await nextTick();
    // Local results are already visible while the debounce is pending.
    expect(api.results.value.map((item) => item.id)).toEqual(["local:a"]);
    expect(api.isSearching.value).toBe(true);

    await vi.advanceTimersByTimeAsync(300);
    expect(api.isSearching.value).toBe(true);

    remoteDeferred.resolve([]);
    await flushPromises();
    expect(api.isSearching.value).toBe(false);
  });

  it("exposes the final empty state when every provider completes without results", async () => {
    const { api } = mountUseGlobalSearch([
      createLocalProvider([createResult("local:a", "search target")]),
      createRemoteProvider("posts", vi.fn().mockResolvedValue([])),
    ]);

    api.keyword.value = "nothing matches";
    await nextTick();
    expect(api.isFinalEmpty.value).toBe(false);

    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    expect(api.results.value).toHaveLength(0);
    expect(api.isFinalEmpty.value).toBe(true);
  });

  it("counts a background refresh of stale results as searching", async () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, networkMode: "always" },
      },
    });
    const search = vi.fn().mockResolvedValue([]);
    const providers = [createRemoteProvider("posts", search)];

    // First open: the remote query completes without results.
    const first = mountUseGlobalSearch(providers, queryClient);
    first.api.keyword.value = "keyword";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();
    expect(first.api.isFinalEmpty.value).toBe(true);
    first.wrapper.unmount();

    // Let the cached empty result go stale, then reopen the search with the
    // same query client: the stale data is shown while refetching.
    await vi.advanceTimersByTimeAsync(31 * 1000);
    const secondDeferred = deferred<GlobalSearchResult[]>();
    search.mockImplementation(() => secondDeferred.promise);

    const second = mountUseGlobalSearch(providers, queryClient);
    second.api.keyword.value = "keyword";
    await vi.advanceTimersByTimeAsync(300);

    expect(second.api.isSearching.value).toBe(true);
    expect(second.api.isFinalEmpty.value).toBe(false);

    secondDeferred.resolve([createResult("posts:1")]);
    await flushPromises();
    expect(second.api.isSearching.value).toBe(false);
    expect(second.api.results.value.map((item) => item.id)).toEqual([
      "posts:1",
    ]);
  });

  it("reports a partial failure and not the final empty state when a cached source fails", async () => {
    const failingCachedProvider: CachedGlobalSearchProvider<
      GlobalSearchResult[]
    > = {
      id: "plugins",
      mode: "cached",
      isAvailable: () => true,
      fetch: vi.fn().mockRejectedValue(new Error("boom")),
      map: (data) => data,
    };
    const { api } = mountUseGlobalSearch([
      failingCachedProvider,
      createRemoteProvider("posts", vi.fn().mockResolvedValue([])),
    ]);

    api.keyword.value = "nothing matches";
    await vi.advanceTimersByTimeAsync(300);
    await flushPromises();

    expect(api.hasPartialFailure.value).toBe(true);
    expect(api.isFinalEmpty.value).toBe(false);
  });
});
