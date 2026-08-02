import { useQuery } from "@tanstack/vue-query";
import { refDebounced } from "@vueuse/core";
import Fuse from "fuse.js";
import { computed, ref, watch } from "vue";
import { useGlobalSearchProviders } from "./global-search-providers";
import {
  GLOBAL_SEARCH_CACHED_STALE_TIME,
  GLOBAL_SEARCH_DEBOUNCE_MS,
  GLOBAL_SEARCH_LOCAL_LIMIT,
  GLOBAL_SEARCH_REMOTE_GC_TIME,
  GLOBAL_SEARCH_REMOTE_ROTATION,
  GLOBAL_SEARCH_REMOTE_STALE_TIME,
  GLOBAL_SEARCH_TOTAL_LIMIT,
  type CachedGlobalSearchProvider,
  type GlobalSearchProvider,
  type GlobalSearchResult,
  type RemoteGlobalSearchProvider,
} from "./types";

/**
 * Error thrown when a remote response resolves after the current keyword has
 * already changed, so late responses never replace current results.
 */
export class StaleKeywordError extends Error {
  constructor() {
    super("Global search keyword changed before the response completed");
    this.name = "StaleKeywordError";
  }
}

/**
 * Merges local results and remote provider pools into a deterministic result
 * list: at most 8 local or cached results, remote results shared through a
 * stable rotation, and at most 20 results in total. Remote pools are
 * expected to be capped to their provider limit by the caller.
 */
export function mergeGlobalSearchResults(
  localResults: GlobalSearchResult[],
  remotePools: GlobalSearchResult[][]
): GlobalSearchResult[] {
  const merged = localResults.slice(0, GLOBAL_SEARCH_LOCAL_LIMIT);
  const queues = remotePools.map((pool) => [...pool]);

  let remaining = GLOBAL_SEARCH_TOTAL_LIMIT - merged.length;
  let progressed = true;
  while (remaining > 0 && progressed) {
    progressed = false;
    for (const queue of queues) {
      if (remaining <= 0) {
        break;
      }
      const next = queue.shift();
      if (next) {
        merged.push(next);
        remaining--;
        progressed = true;
      }
    }
  }

  return merged;
}

/**
 * Orchestrates the hybrid global search: keyword state, provider query
 * lifecycles, deterministic result composition and stable selection.
 *
 * @param providers - Optional provider overrides, mainly for testing. When
 * omitted, the default providers are created from the current component
 * setup context.
 */
export function useGlobalSearch(providers?: GlobalSearchProvider[]) {
  const resolvedProviders = providers ?? useGlobalSearchProviders();

  const localProviders = resolvedProviders.filter(
    (provider): provider is GlobalSearchProvider & { mode: "local" } =>
      provider.mode === "local"
  );
  const cachedProviders = resolvedProviders.filter(
    (provider): provider is CachedGlobalSearchProvider =>
      provider.mode === "cached"
  );
  const remoteProviders = resolvedProviders
    .filter(
      (provider): provider is RemoteGlobalSearchProvider =>
        provider.mode === "remote"
    )
    .sort((a, b) => {
      const rotation = GLOBAL_SEARCH_REMOTE_ROTATION as readonly string[];
      const aIndex = rotation.indexOf(a.id);
      const bIndex = rotation.indexOf(b.id);
      return (
        (aIndex === -1 ? rotation.length : aIndex) -
        (bIndex === -1 ? rotation.length : bIndex)
      );
    });

  const keyword = ref("");
  const normalizedKeyword = computed(() => keyword.value.trim());
  const debouncedKeyword = refDebounced(
    normalizedKeyword,
    GLOBAL_SEARCH_DEBOUNCE_MS
  );

  const isDebouncePending = computed(
    () => normalizedKeyword.value !== debouncedKeyword.value
  );

  // Keyword generation guard: incremented whenever the debounced keyword
  // changes, so late responses for older keywords can be discarded.
  const searchGeneration = ref(0);
  watch(debouncedKeyword, () => {
    searchGeneration.value += 1;
  });

  const cachedQueries = cachedProviders.map((provider) =>
    useQuery({
      queryKey: ["core", "global-search", provider.id],
      queryFn: () => provider.fetch(),
      enabled: computed(() => provider.isAvailable()),
      staleTime: GLOBAL_SEARCH_CACHED_STALE_TIME,
    })
  );

  const localAndCachedItems = computed(() => [
    ...localProviders
      .filter((provider) => provider.isAvailable())
      .flatMap((provider) => provider.getItems()),
    ...cachedProviders.flatMap((provider, index) => {
      const data = cachedQueries[index].data.value;
      return data === undefined ? [] : provider.map(data);
    }),
  ]);

  const fuse = computed(
    () =>
      new Fuse(localAndCachedItems.value, {
        keys: ["title", "group", "route.path", "route.name"],
        useExtendedSearch: true,
        threshold: 0.2,
      })
  );

  const localResults = computed((): GlobalSearchResult[] => {
    if (!normalizedKeyword.value) {
      return [];
    }
    return fuse.value
      .search(normalizedKeyword.value, { limit: GLOBAL_SEARCH_LOCAL_LIMIT })
      .map((result) => result.item);
  });

  const hasRemoteKeyword = computed(() => debouncedKeyword.value.length > 0);

  const remoteQueries = remoteProviders.map((provider) =>
    useQuery<GlobalSearchResult[]>({
      queryKey: ["core", "global-search", provider.id, debouncedKeyword],
      queryFn: async ({ queryKey, signal }) => {
        const generation = searchGeneration.value;
        const results = await provider.search(queryKey[3] as string, signal);
        if (generation !== searchGeneration.value) {
          throw new StaleKeywordError();
        }
        return results;
      },
      enabled: computed(() => hasRemoteKeyword.value && provider.isAvailable()),
      staleTime: GLOBAL_SEARCH_REMOTE_STALE_TIME,
      cacheTime: GLOBAL_SEARCH_REMOTE_GC_TIME,
      retry: false,
    })
  );

  // Remote providers that are permitted for the current user, in rotation
  // order. Their query state only participates in the merged state when a
  // debounced keyword is present.
  const participatingRemote = computed(() =>
    remoteProviders
      .map((provider, index) => ({ provider, query: remoteQueries[index] }))
      .filter(({ provider }) => provider.isAvailable())
  );

  const participatingRemoteQueries = computed(() =>
    participatingRemote.value.map(({ query }) => query)
  );

  const remoteResultPools = computed(() => {
    // While a new keyword is waiting for the debounce, the pools still
    // belong to the previous keyword and must not be merged or navigated.
    if (!hasRemoteKeyword.value || isDebouncePending.value) {
      return [];
    }
    return participatingRemote.value.map(({ provider, query }) =>
      (query.data.value ?? []).slice(0, provider.limit)
    );
  });

  const results = computed(() =>
    mergeGlobalSearchResults(localResults.value, remoteResultPools.value)
  );

  const selectedId = ref<string | null>(null);

  watch(
    [normalizedKeyword, results],
    ([currentKeyword, currentResults], [previousKeyword]) => {
      if (
        currentKeyword !== previousKeyword ||
        !selectedId.value ||
        !currentResults.some((result) => result.id === selectedId.value)
      ) {
        selectedId.value = currentResults[0]?.id ?? null;
      }
    },
    { immediate: true }
  );

  const selectedIndex = computed(() =>
    results.value.findIndex((result) => result.id === selectedId.value)
  );

  const selectedResult = computed(
    () => results.value.find((result) => result.id === selectedId.value) ?? null
  );

  function selectNext() {
    const currentResults = results.value;
    if (!currentResults.length) {
      return;
    }
    const nextIndex = Math.min(
      currentResults.length - 1,
      selectedIndex.value + 1
    );
    selectedId.value = currentResults[nextIndex].id;
  }

  function selectPrevious() {
    const currentResults = results.value;
    if (!currentResults.length) {
      return;
    }
    const previousIndex = Math.max(0, selectedIndex.value - 1);
    selectedId.value = currentResults[previousIndex].id;
  }

  const isInitial = computed(() => normalizedKeyword.value.length === 0);

  const participatingCachedQueries = computed(() =>
    cachedProviders
      .map((provider, index) => ({ provider, query: cachedQueries[index] }))
      .filter(({ provider }) => provider.isAvailable())
      .map(({ query }) => query)
  );

  // Any in-flight request of a permitted provider, including a background
  // refresh of stale cached data, means the search is not complete yet.
  const isSearching = computed(
    () =>
      !isInitial.value &&
      (isDebouncePending.value ||
        participatingCachedQueries.value.some(
          (query) => query.isFetching.value
        ) ||
        (hasRemoteKeyword.value &&
          participatingRemoteQueries.value.some(
            (query) => query.isFetching.value
          )))
  );

  const hasPartialFailure = computed(
    () =>
      participatingCachedQueries.value.some((query) => query.isError.value) ||
      (hasRemoteKeyword.value &&
        // A remote failure belongs to the keyword it failed for; do not
        // show it while a new keyword is waiting for the debounce.
        !isDebouncePending.value &&
        participatingRemoteQueries.value.some((query) => query.isError.value))
  );

  const isFinalEmpty = computed(
    () =>
      !isInitial.value &&
      !isSearching.value &&
      results.value.length === 0 &&
      participatingCachedQueries.value.every(
        (query) => query.isSuccess.value
      ) &&
      participatingRemoteQueries.value.every((query) => query.isSuccess.value)
  );

  return {
    keyword,
    normalizedKeyword,
    results,
    selectedId,
    selectedIndex,
    selectedResult,
    selectNext,
    selectPrevious,
    isInitial,
    isSearching,
    hasPartialFailure,
    isFinalEmpty,
  };
}
