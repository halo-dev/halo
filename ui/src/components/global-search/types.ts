import type { Component } from "vue";
import type { RouteLocationRaw } from "vue-router";

/**
 * Source modes of the hybrid global search.
 *
 * - `local`: searchable console routes derived from the router.
 * - `cached`: bounded collections (plugins, categories, system settings and
 *   theme settings) loaded once and cached for a short period.
 * - `remote`: unbounded collections (posts, single pages, attachments, users
 *   and tags) queried by keyword through the Console APIs.
 */
export type GlobalSearchSourceMode = "local" | "cached" | "remote";

export type GlobalSearchResultIcon = { component: Component } | { src: string };

/**
 * Shared result model of the hybrid global search.
 */
export interface GlobalSearchResult {
  /**
   * Stable identifier in the form of `<sourceId>:<resource name>`, used to
   * preserve the current selection while results are being updated.
   */
  id: string;
  /**
   * Identifier of the provider source, e.g. `posts` or `routes`.
   */
  sourceId: string;
  title: string;
  /**
   * Optional concise match context: slug or truncated excerpt for posts and
   * single pages, username for users, slug for tags and media type for
   * attachments.
   */
  context?: string;
  icon: GlobalSearchResultIcon;
  /**
   * Localized group label of the source.
   */
  group: string;
  route: RouteLocationRaw;
}

interface BaseGlobalSearchProvider {
  /**
   * Stable source identifier.
   */
  id: string;
  mode: GlobalSearchSourceMode;
  /**
   * Permission predicate based on the permissions required by the
   * destination route of this provider's results.
   */
  isAvailable: () => boolean;
}

export interface LocalGlobalSearchProvider extends BaseGlobalSearchProvider {
  mode: "local";
  /**
   * Builds the searchable items synchronously. Called inside reactive
   * computations so locale changes are picked up.
   */
  getItems: () => GlobalSearchResult[];
}

export interface CachedGlobalSearchProvider<
  TData = unknown,
> extends BaseGlobalSearchProvider {
  mode: "cached";
  /**
   * Loads the bounded source collection. Used as the query function of a
   * cached query.
   */
  fetch: () => Promise<TData>;
  /**
   * Maps the loaded collection into the shared result model. Called inside
   * reactive computations.
   */
  map: (data: TData) => GlobalSearchResult[];
}

export interface RemoteGlobalSearchProvider extends BaseGlobalSearchProvider {
  mode: "remote";
  /**
   * Maximum number of results this provider contributes to the merged list.
   * Also used as the page size of the remote request.
   */
  limit: number;
  /**
   * Searches the source remotely by keyword.
   */
  search: (
    keyword: string,
    signal?: AbortSignal
  ) => Promise<GlobalSearchResult[]>;
}

export type GlobalSearchProvider =
  | LocalGlobalSearchProvider
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  | CachedGlobalSearchProvider<any>
  | RemoteGlobalSearchProvider;

/**
 * Maximum number of merged local and cached results.
 */
export const GLOBAL_SEARCH_LOCAL_LIMIT = 8;

/**
 * Maximum number of merged results per remote provider.
 */
export const GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT = 4;

/**
 * Maximum number of merged results in total.
 */
export const GLOBAL_SEARCH_TOTAL_LIMIT = 20;

/**
 * Debounce delay before a non-empty keyword triggers remote searches.
 */
export const GLOBAL_SEARCH_DEBOUNCE_MS = 300;

/**
 * Freshness of bounded source caches.
 */
export const GLOBAL_SEARCH_CACHED_STALE_TIME = 60 * 1000;

/**
 * Freshness of remote keyword result caches.
 */
export const GLOBAL_SEARCH_REMOTE_STALE_TIME = 30 * 1000;

/**
 * Garbage collection time of inactive remote keyword result caches. Named
 * after the v5 option; `@tanstack/vue-query` v4 calls this `cacheTime`.
 */
export const GLOBAL_SEARCH_REMOTE_GC_TIME = 5 * 60 * 1000;

/**
 * Fixed rotation used to share the remaining result capacity between remote
 * providers: posts, single pages, attachments, users, then tags.
 */
export const GLOBAL_SEARCH_REMOTE_ROTATION = [
  "posts",
  "single-pages",
  "attachments",
  "users",
  "tags",
] as const;
