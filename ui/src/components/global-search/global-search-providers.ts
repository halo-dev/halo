import { useThemeStore } from "@console/stores/theme";
import {
  consoleApiClient,
  coreApiClient,
  paginate,
  type Category,
  type CategoryV1alpha1ApiListCategoryRequest,
  type Plugin,
  type PluginV1alpha1ApiListPluginRequest,
  type Post,
  type Setting,
  type SinglePage,
  type Theme,
} from "@halo-dev/api-client";
import {
  IconBookRead,
  IconFolder,
  IconLink,
  IconPages,
  IconPalette,
  IconSettings,
  IconUserSettings,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { storeToRefs } from "pinia";
import { markRaw } from "vue";
import { useI18n, type ComposerTranslation } from "vue-i18n";
import { useRouter, type RouteRecordNormalized } from "vue-router";
import {
  GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
  type CachedGlobalSearchProvider,
  type GlobalSearchProvider,
  type GlobalSearchResult,
  type LocalGlobalSearchProvider,
  type RemoteGlobalSearchProvider,
} from "./types";

/**
 * Dependencies required to build the global-search providers. Kept explicit
 * so the providers can be created and tested outside of component setup.
 */
export interface GlobalSearchProvidersContext {
  t: ComposerTranslation;
  routes: RouteRecordNormalized[];
  getActivatedTheme: () => Theme | undefined;
}

const MAX_EXCERPT_CONTEXT_LENGTH = 60;

/**
 * Resolves the concise match context for posts and single pages: the slug,
 * replaced by a truncated excerpt when only the excerpt explains the match.
 */
export function resolveContentMatchContext(options: {
  title: string;
  slug: string;
  excerpt?: string;
  keyword: string;
}): string {
  const { title, slug, excerpt, keyword } = options;
  const normalizedKeyword = keyword.trim().toLowerCase();
  if (
    excerpt &&
    normalizedKeyword &&
    !title.toLowerCase().includes(normalizedKeyword) &&
    !slug.toLowerCase().includes(normalizedKeyword) &&
    excerpt.toLowerCase().includes(normalizedKeyword)
  ) {
    return excerpt.length > MAX_EXCERPT_CONTEXT_LENGTH
      ? `${excerpt.slice(0, MAX_EXCERPT_CONTEXT_LENGTH).trimEnd()}…`
      : excerpt;
  }
  return slug;
}

/**
 * Checks whether the current user may open the destination route, mirroring
 * the router permission guard. Function-form predicates are evaluated with
 * the current user permissions; Promise-returning predicates cannot be
 * verified synchronously, so the route is excluded conservatively and the
 * router guard remains authoritative.
 */
function isRouteAccessible(route: RouteRecordNormalized): boolean {
  if (!route.meta?.title || !route.meta?.searchable) {
    return false;
  }
  const permissions = route.meta.permissions;
  if (!permissions) {
    return true;
  }
  if (typeof permissions !== "function") {
    return utils.permission.has(permissions);
  }
  let result: boolean | Promise<boolean>;
  try {
    result = permissions(utils.permission.getUserPermissions() ?? []);
  } catch {
    return false;
  }
  return typeof result === "boolean" ? result : false;
}

function createRoutesProvider(
  context: GlobalSearchProvidersContext
): LocalGlobalSearchProvider {
  return {
    id: "routes",
    mode: "local",
    isAvailable: () => true,
    getItems: () => {
      return context.routes.filter(isRouteAccessible).map((route) => ({
        id: `routes:${String(route.name)}`,
        sourceId: "routes",
        title: context.t(route.meta?.title as string),
        icon: { component: markRaw(IconLink) },
        group: context.t("core.components.global_search.groups.console"),
        route,
      }));
    },
  };
}

function createPluginsProvider(
  context: GlobalSearchProvidersContext
): CachedGlobalSearchProvider<Plugin[]> {
  return {
    id: "plugins",
    mode: "cached",
    isAvailable: () => utils.permission.has(["system:plugins:view"]),
    fetch: () =>
      paginate<PluginV1alpha1ApiListPluginRequest, Plugin>(
        (params) => coreApiClient.plugin.plugin.listPlugin(params),
        { size: 1000 }
      ),
    map: (plugins) =>
      plugins.map((plugin) => ({
        id: `plugins:${plugin.metadata.name}`,
        sourceId: "plugins",
        title: plugin.spec.displayName as string,
        icon: { src: plugin.status?.logo as string },
        group: context.t("core.components.global_search.groups.plugin"),
        route: {
          name: "PluginDetail",
          params: { name: plugin.metadata.name },
        },
      })),
  };
}

function createCategoriesProvider(
  context: GlobalSearchProvidersContext
): CachedGlobalSearchProvider<Category[]> {
  return {
    id: "categories",
    mode: "cached",
    isAvailable: () => utils.permission.has(["system:posts:view"]),
    // Categories have no Console keyword endpoint; keep loading the full
    // bounded list and cache it.
    fetch: () =>
      paginate<CategoryV1alpha1ApiListCategoryRequest, Category>(
        (params) => coreApiClient.content.category.listCategory(params),
        { size: 1000, sort: ["metadata.creationTimestamp,desc"] }
      ),
    map: (categories) =>
      categories.map((category) => ({
        id: `categories:${category.metadata.name}`,
        sourceId: "categories",
        title: category.spec.displayName,
        icon: { component: markRaw(IconBookRead) },
        group: context.t("core.components.global_search.groups.category"),
        route: {
          name: "Categories",
          query: { name: category.metadata.name },
        },
      })),
  };
}

function createSettingsProvider(
  context: GlobalSearchProvidersContext
): CachedGlobalSearchProvider<Setting> {
  return {
    id: "settings",
    mode: "cached",
    isAvailable: () =>
      utils.permission.has(
        ["system:settings:view", "system:configmaps:view"],
        false
      ),
    fetch: async () => {
      const { data } = await coreApiClient.setting.getSetting({
        name: "system",
      });
      return data;
    },
    map: (setting) =>
      (setting.spec?.forms ?? []).map((form) => ({
        id: `settings:${form.group}`,
        sourceId: "settings",
        title: form.label as string,
        icon: { component: markRaw(IconSettings) },
        group: context.t("core.components.global_search.groups.setting"),
        route: {
          name: "SystemSetting",
          params: { group: form.group },
        },
      })),
  };
}

function createThemeSettingsProvider(
  context: GlobalSearchProvidersContext
): CachedGlobalSearchProvider<Setting> {
  return {
    id: "theme-settings",
    mode: "cached",
    isAvailable: () => utils.permission.has(["system:themes:view"]),
    fetch: async () => {
      const { data } = await consoleApiClient.theme.theme.fetchThemeSetting({
        name: "-",
      });
      return data;
    },
    map: (setting) =>
      (setting.spec?.forms ?? []).map((form) => ({
        id: `theme-settings:${form.group}`,
        sourceId: "theme-settings",
        title: [context.getActivatedTheme()?.spec.displayName, form.label].join(
          " / "
        ),
        icon: { component: markRaw(IconPalette) },
        group: context.t("core.components.global_search.groups.theme_setting"),
        route: {
          name: "ThemeSetting",
          params: { group: form.group },
        },
      })),
  };
}

function createPostsProvider(
  context: GlobalSearchProvidersContext
): RemoteGlobalSearchProvider {
  return {
    id: "posts",
    mode: "remote",
    limit: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
    // Results navigate to the editor route, which requires the manage
    // permission.
    isAvailable: () => utils.permission.has(["system:posts:manage"]),
    search: async (keyword, signal) => {
      const { data } = await consoleApiClient.content.post.listPosts(
        {
          page: 1,
          size: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
          keyword,
          sort: ["metadata.creationTimestamp,desc"],
        },
        { signal }
      );
      return data.items.map(({ post }) =>
        mapContentToResult(post, keyword, {
          sourceId: "posts",
          group: context.t("core.components.global_search.groups.post"),
          icon: { component: markRaw(IconBookRead) },
          routeName: "PostEditor",
        })
      );
    },
  };
}

function createSinglePagesProvider(
  context: GlobalSearchProvidersContext
): RemoteGlobalSearchProvider {
  return {
    id: "single-pages",
    mode: "remote",
    limit: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
    // Results navigate to the editor route, which requires the manage
    // permission.
    isAvailable: () => utils.permission.has(["system:singlepages:manage"]),
    search: async (keyword, signal) => {
      const { data } =
        await consoleApiClient.content.singlePage.listSinglePages(
          {
            page: 1,
            size: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
            keyword,
            sort: ["creationTimestamp,desc"],
          },
          { signal }
        );
      return data.items.map(({ page }) =>
        mapContentToResult(page, keyword, {
          sourceId: "single-pages",
          group: context.t("core.components.global_search.groups.page"),
          icon: { component: markRaw(IconPages) },
          routeName: "SinglePageEditor",
        })
      );
    },
  };
}

function createAttachmentsProvider(
  context: GlobalSearchProvidersContext
): RemoteGlobalSearchProvider {
  return {
    id: "attachments",
    mode: "remote",
    limit: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
    isAvailable: () => utils.permission.has(["system:attachments:view"]),
    search: async (keyword, signal) => {
      const { data } =
        await consoleApiClient.storage.attachment.searchAttachments(
          {
            page: 1,
            size: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
            keyword,
            sort: ["metadata.creationTimestamp,desc"],
          },
          { signal }
        );
      return data.items.map((attachment) => ({
        id: `attachments:${attachment.metadata.name}`,
        sourceId: "attachments",
        title: attachment.spec.displayName as string,
        context: attachment.spec.mediaType,
        icon: { component: markRaw(IconFolder) },
        group: context.t("core.components.global_search.groups.attachment"),
        route: {
          name: "Attachments",
          query: { name: attachment.metadata.name },
        },
      }));
    },
  };
}

function createUsersProvider(
  context: GlobalSearchProvidersContext
): RemoteGlobalSearchProvider {
  return {
    id: "users",
    mode: "remote",
    limit: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
    isAvailable: () => utils.permission.has(["system:users:view"]),
    search: async (keyword, signal) => {
      const { data } = await consoleApiClient.user.listUsers(
        {
          page: 1,
          size: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
          keyword,
          sort: ["spec.displayName,asc"],
          labelSelector: ["!halo.run/hidden-user"],
        },
        { signal }
      );
      return data.items.map(({ user }) => ({
        id: `users:${user.metadata.name}`,
        sourceId: "users",
        title: user.spec.displayName,
        context: user.metadata.name,
        icon: { component: markRaw(IconUserSettings) },
        group: context.t("core.components.global_search.groups.user"),
        route: {
          name: "UserDetail",
          params: { name: user.metadata.name },
        },
      }));
    },
  };
}

function createTagsProvider(
  context: GlobalSearchProvidersContext
): RemoteGlobalSearchProvider {
  return {
    id: "tags",
    mode: "remote",
    limit: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
    isAvailable: () => utils.permission.has(["system:posts:view"]),
    search: async (keyword, signal) => {
      const { data } = await consoleApiClient.content.tag.listPostTags(
        {
          page: 1,
          size: GLOBAL_SEARCH_REMOTE_PROVIDER_LIMIT,
          keyword,
          sort: ["spec.displayName,asc"],
        },
        { signal }
      );
      return data.items.map((tag) => ({
        id: `tags:${tag.metadata.name}`,
        sourceId: "tags",
        title: tag.spec.displayName,
        context: tag.spec.slug,
        icon: { component: markRaw(IconBookRead) },
        group: context.t("core.components.global_search.groups.tag"),
        route: {
          name: "Tags",
          query: { name: tag.metadata.name },
        },
      }));
    },
  };
}

function mapContentToResult(
  content: Post | SinglePage,
  keyword: string,
  options: {
    sourceId: string;
    group: string;
    icon: GlobalSearchResult["icon"];
    routeName: "PostEditor" | "SinglePageEditor";
  }
): GlobalSearchResult {
  return {
    id: `${options.sourceId}:${content.metadata.name}`,
    sourceId: options.sourceId,
    title: content.spec.title,
    context: resolveContentMatchContext({
      title: content.spec.title,
      slug: content.spec.slug,
      // The backend keyword search matches the effective excerpt in
      // status.excerpt, not the raw excerpt in spec.excerpt.raw.
      excerpt: content.status?.excerpt,
      keyword,
    }),
    icon: options.icon,
    group: options.group,
    route: {
      name: options.routeName,
      query: { name: content.metadata.name },
    },
  };
}

/**
 * Creates all global-search providers from explicit dependencies.
 */
export function createGlobalSearchProviders(
  context: GlobalSearchProvidersContext
): GlobalSearchProvider[] {
  return [
    createRoutesProvider(context),
    createPluginsProvider(context),
    createCategoriesProvider(context),
    createSettingsProvider(context),
    createThemeSettingsProvider(context),
    createPostsProvider(context),
    createSinglePagesProvider(context),
    createAttachmentsProvider(context),
    createUsersProvider(context),
    createTagsProvider(context),
  ];
}

/**
 * Creates the default global-search providers from the current component
 * setup context. Must be called during component setup.
 */
export function useGlobalSearchProviders(): GlobalSearchProvider[] {
  const { t } = useI18n();
  const router = useRouter();
  const { activatedTheme } = storeToRefs(useThemeStore());

  return createGlobalSearchProviders({
    t,
    routes: router.getRoutes(),
    getActivatedTheme: () => activatedTheme.value,
  });
}
