import type {
  Attachment,
  ListedPost,
  ListedSinglePage,
  ListedUser,
  Plugin,
  Post,
  Setting,
  SinglePage,
  Tag,
  Theme,
  User,
} from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import type { ComposerTranslation } from "vue-i18n";
import type { RouteRecordNormalized } from "vue-router";
import {
  createGlobalSearchProviders,
  resolveContentMatchContext,
  type GlobalSearchProvidersContext,
} from "./global-search-providers";
import type {
  CachedGlobalSearchProvider,
  LocalGlobalSearchProvider,
  RemoteGlobalSearchProvider,
} from "./types";

const {
  listPostsMock,
  listSinglePagesMock,
  searchAttachmentsMock,
  listUsersMock,
  listPostTagsMock,
  listPluginMock,
  listCategoryMock,
  getSettingMock,
  fetchThemeSettingMock,
} = vi.hoisted(() => ({
  listPostsMock: vi.fn(),
  listSinglePagesMock: vi.fn(),
  searchAttachmentsMock: vi.fn(),
  listUsersMock: vi.fn(),
  listPostTagsMock: vi.fn(),
  listPluginMock: vi.fn(),
  listCategoryMock: vi.fn(),
  getSettingMock: vi.fn(),
  fetchThemeSettingMock: vi.fn(),
}));

vi.mock("@halo-dev/api-client", () => ({
  consoleApiClient: {
    content: {
      post: { listPosts: listPostsMock },
      singlePage: { listSinglePages: listSinglePagesMock },
      tag: { listPostTags: listPostTagsMock },
    },
    storage: { attachment: { searchAttachments: searchAttachmentsMock } },
    user: { listUsers: listUsersMock },
    theme: { theme: { fetchThemeSetting: fetchThemeSettingMock } },
  },
  coreApiClient: {
    plugin: { plugin: { listPlugin: listPluginMock } },
    content: { category: { listCategory: listCategoryMock } },
    setting: { getSetting: getSettingMock },
  },
  paginate: async (
    listFn: (params: Record<string, unknown>) => Promise<{
      data: { items: unknown[]; hasNext: boolean };
    }>,
    params?: Record<string, unknown>
  ) => {
    const result: unknown[] = [];
    let page = 1;
    let hasNext = true;
    while (hasNext) {
      const { data } = await listFn({ ...params, page });
      result.push(...data.items);
      page += 1;
      hasNext = data.hasNext;
    }
    return result;
  },
}));

function createTestContext(
  overrides: Partial<GlobalSearchProvidersContext> = {}
): GlobalSearchProvidersContext {
  return {
    t: ((key: string) => key) as ComposerTranslation,
    routes: [],
    getActivatedTheme: () => undefined,
    ...overrides,
  };
}

function createRoute(
  name: string,
  meta: Record<string, unknown>
): RouteRecordNormalized {
  return { name, path: `/${name}`, meta } as RouteRecordNormalized;
}

function getProvider<T extends { isAvailable: () => boolean }>(
  id: string,
  context = createTestContext()
): T {
  const provider = createGlobalSearchProviders(context).find(
    (provider) => provider.id === id
  );
  if (!provider) {
    throw new Error(`Provider ${id} not found`);
  }
  return provider as unknown as T;
}

describe("createGlobalSearchProviders", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    utils.permission.setUserPermissions(["*"]);
  });

  describe("destination-permission gating", () => {
    it.each([
      ["posts", ["system:posts:view"], ["system:posts:manage"]],
      [
        "single-pages",
        ["system:singlepages:view"],
        ["system:singlepages:manage"],
      ],
      ["users", [], ["system:users:view"]],
      ["attachments", [], ["system:attachments:view"]],
      ["plugins", [], ["system:plugins:view"]],
      ["tags", [], ["system:posts:view"]],
      ["categories", [], ["system:posts:view"]],
      ["theme-settings", [], ["system:themes:view"]],
    ])(
      "provider %s is available only with the destination permission",
      (id, insufficient, sufficient) => {
        utils.permission.setUserPermissions(insufficient as string[]);
        expect(getProvider(id).isAvailable()).toBe(false);

        utils.permission.setUserPermissions(sufficient as string[]);
        expect(getProvider(id).isAvailable()).toBe(true);
      }
    );

    it("requires posts manage permission instead of only view for editor destinations", () => {
      utils.permission.setUserPermissions(["system:posts:view"]);
      expect(getProvider("posts").isAvailable()).toBe(false);
      expect(getProvider("single-pages").isAvailable()).toBe(false);
      // tags and categories navigate to list routes that only need view
      expect(getProvider("tags").isAvailable()).toBe(true);
      expect(getProvider("categories").isAvailable()).toBe(true);
    });

    it("requires both settings and configmaps view for the settings provider", () => {
      utils.permission.setUserPermissions(["system:settings:view"]);
      expect(getProvider("settings").isAvailable()).toBe(false);

      utils.permission.setUserPermissions(["system:configmaps:view"]);
      expect(getProvider("settings").isAvailable()).toBe(false);

      utils.permission.setUserPermissions([
        "system:settings:view",
        "system:configmaps:view",
      ]);
      expect(getProvider("settings").isAvailable()).toBe(true);
    });
  });

  describe("routes provider", () => {
    const context = createTestContext({
      routes: [
        createRoute("Posts", {
          title: "Posts",
          searchable: true,
          permissions: ["system:posts:view"],
        }),
        createRoute("Users", {
          title: "Users",
          searchable: true,
          permissions: ["system:users:view"],
        }),
        createRoute("Dashboard", { title: "Dashboard", searchable: true }),
        createRoute("NotSearchable", { title: "Hidden", searchable: false }),
        createRoute("NoTitle", { searchable: true }),
      ],
    });

    it("builds results from accessible searchable routes only", () => {
      utils.permission.setUserPermissions(["system:posts:view"]);
      const provider = getProvider<LocalGlobalSearchProvider>(
        "routes",
        context
      );
      const items = provider.getItems();

      expect(items.map((item) => item.id)).toEqual([
        "routes:Posts",
        "routes:Dashboard",
      ]);
      expect(items[0]).toMatchObject({
        sourceId: "routes",
        title: "Posts",
        group: "core.components.global_search.groups.console",
      });
      expect(items[0].route).toMatchObject({ name: "Posts" });
    });

    it("excludes routes that require permissions the user does not have", () => {
      utils.permission.setUserPermissions([]);
      const provider = getProvider<LocalGlobalSearchProvider>(
        "routes",
        context
      );
      expect(provider.getItems().map((item) => item.id)).toEqual([
        "routes:Dashboard",
      ]);
    });

    it("evaluates function-form route permissions", () => {
      const functionContext = createTestContext({
        routes: [
          createRoute("SyncAllowed", {
            title: "Sync allowed",
            searchable: true,
            permissions: (uiPermissions: string[]) =>
              uiPermissions.includes("system:posts:view"),
          }),
          createRoute("SyncDenied", {
            title: "Sync denied",
            searchable: true,
            permissions: () => false,
          }),
          createRoute("AsyncPermissions", {
            title: "Async",
            searchable: true,
            permissions: () => Promise.resolve(true),
          }),
          createRoute("ThrowingPermissions", {
            title: "Throwing",
            searchable: true,
            permissions: () => {
              throw new Error("boom");
            },
          }),
        ],
      });

      utils.permission.setUserPermissions(["system:posts:view"]);
      const provider = getProvider<LocalGlobalSearchProvider>(
        "routes",
        functionContext
      );

      // Synchronous verdicts are honored; Promise-returning or throwing
      // predicates cannot be verified synchronously and are excluded.
      expect(provider.getItems().map((item) => item.id)).toEqual([
        "routes:SyncAllowed",
      ]);
    });
  });

  describe("remote providers", () => {
    it("searches posts with page 1, size 4, keyword and newer-first ordering", async () => {
      const post = {
        metadata: { name: "post-1" },
        spec: {
          title: "Hello world",
          slug: "hello-world",
          excerpt: { autoGenerate: true, raw: "unrelated" },
        },
      } as Post;
      listPostsMock.mockResolvedValue({
        data: { items: [{ post } as ListedPost] },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("posts");
      const signal = new AbortController().signal;
      const results = await provider.search("hello", signal);

      expect(listPostsMock).toHaveBeenCalledWith(
        {
          page: 1,
          size: 4,
          keyword: "hello",
          sort: ["metadata.creationTimestamp,desc"],
        },
        { signal }
      );
      expect(results).toHaveLength(1);
      expect(results[0]).toMatchObject({
        id: "posts:post-1",
        sourceId: "posts",
        title: "Hello world",
        context: "hello-world",
        group: "core.components.global_search.groups.post",
        route: { name: "PostEditor", query: { name: "post-1" } },
      });
    });

    it("uses status.excerpt for the excerpt match context", async () => {
      const post = {
        metadata: { name: "post-1" },
        spec: {
          title: "Unrelated title",
          slug: "unrelated-slug",
          excerpt: { autoGenerate: true, raw: "keyword in the raw excerpt" },
        },
        status: { excerpt: "The keyword only appears in this excerpt" },
      } as unknown as Post;
      listPostsMock.mockResolvedValue({
        data: { items: [{ post } as ListedPost] },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("posts");
      const results = await provider.search("keyword");

      // The backend keyword search matches status.excerpt, so only that
      // excerpt can explain the match; the raw spec excerpt is ignored.
      expect(results[0].context).toBe(
        "The keyword only appears in this excerpt"
      );
    });

    it("falls back to the slug when only spec.excerpt.raw matches", async () => {
      const post = {
        metadata: { name: "post-1" },
        spec: {
          title: "Unrelated title",
          slug: "unrelated-slug",
          excerpt: { autoGenerate: false, raw: "keyword in the raw excerpt" },
        },
        status: {},
      } as unknown as Post;
      listPostsMock.mockResolvedValue({
        data: { items: [{ post } as ListedPost] },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("posts");
      const results = await provider.search("keyword");

      expect(results[0].context).toBe("unrelated-slug");
    });

    it("searches single pages with newer-first ordering and unwraps the page", async () => {
      const page = {
        metadata: { name: "page-1" },
        spec: {
          title: "About",
          slug: "about",
          excerpt: { autoGenerate: true },
        },
      } as SinglePage;
      listSinglePagesMock.mockResolvedValue({
        data: { items: [{ page } as ListedSinglePage] },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("single-pages");
      const results = await provider.search("about");

      expect(listSinglePagesMock).toHaveBeenCalledWith(
        {
          page: 1,
          size: 4,
          keyword: "about",
          sort: ["creationTimestamp,desc"],
        },
        { signal: undefined }
      );
      expect(results[0]).toMatchObject({
        id: "single-pages:page-1",
        title: "About",
        context: "about",
        route: { name: "SinglePageEditor", query: { name: "page-1" } },
      });
    });

    it("searches attachments with newer-first ordering and media-type context", async () => {
      searchAttachmentsMock.mockResolvedValue({
        data: {
          items: [
            {
              metadata: { name: "attachment-1" },
              spec: { displayName: "Cover", mediaType: "image/png" },
            } as Attachment,
          ],
        },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("attachments");
      const results = await provider.search("cover");

      expect(searchAttachmentsMock).toHaveBeenCalledWith(
        {
          page: 1,
          size: 4,
          keyword: "cover",
          sort: ["metadata.creationTimestamp,desc"],
        },
        { signal: undefined }
      );
      expect(results[0]).toMatchObject({
        id: "attachments:attachment-1",
        title: "Cover",
        context: "image/png",
        route: { name: "Attachments", query: { name: "attachment-1" } },
      });
    });

    it("searches users with display-name ordering and excludes hidden users", async () => {
      const user = {
        metadata: { name: "ryan" },
        spec: { displayName: "Ryan" },
      } as User;
      listUsersMock.mockResolvedValue({
        data: { items: [{ user } as ListedUser] },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("users");
      const results = await provider.search("ryan");

      expect(listUsersMock).toHaveBeenCalledWith(
        {
          page: 1,
          size: 4,
          keyword: "ryan",
          sort: ["spec.displayName,asc"],
          labelSelector: ["!halo.run/hidden-user"],
        },
        { signal: undefined }
      );
      expect(results[0]).toMatchObject({
        id: "users:ryan",
        title: "Ryan",
        context: "ryan",
        route: { name: "UserDetail", params: { name: "ryan" } },
      });
    });

    it("searches tags with display-name ordering and slug context", async () => {
      listPostTagsMock.mockResolvedValue({
        data: {
          items: [
            {
              metadata: { name: "tag-1" },
              spec: { displayName: "Halo", slug: "halo" },
            } as Tag,
          ],
        },
      });

      const provider = getProvider<RemoteGlobalSearchProvider>("tags");
      const results = await provider.search("halo");

      expect(listPostTagsMock).toHaveBeenCalledWith(
        {
          page: 1,
          size: 4,
          keyword: "halo",
          sort: ["spec.displayName,asc"],
        },
        { signal: undefined }
      );
      expect(results[0]).toMatchObject({
        id: "tags:tag-1",
        title: "Halo",
        context: "halo",
        route: { name: "Tags", query: { name: "tag-1" } },
      });
    });
  });

  describe("cached providers", () => {
    it("loads the full plugin list and maps results", async () => {
      listPluginMock.mockResolvedValue({
        data: {
          items: [
            {
              metadata: { name: "plugin-1" },
              spec: { displayName: "Plugin One" },
              status: { logo: "logo.png" },
            } as Plugin,
          ],
          hasNext: false,
        },
      });

      const provider =
        getProvider<CachedGlobalSearchProvider<Plugin[]>>("plugins");
      const plugins = await provider.fetch();
      const items = provider.map(plugins);

      expect(listPluginMock).toHaveBeenCalledWith({ size: 1000, page: 1 });
      expect(items[0]).toMatchObject({
        id: "plugins:plugin-1",
        title: "Plugin One",
        icon: { src: "logo.png" },
        route: { name: "PluginDetail", params: { name: "plugin-1" } },
      });
    });

    it("loads the full category list and maps results", async () => {
      listCategoryMock.mockResolvedValue({
        data: {
          items: [
            {
              metadata: { name: "category-1" },
              spec: { displayName: "Category One", slug: "category-one" },
            },
          ],
          hasNext: false,
        },
      });

      const provider = getProvider<CachedGlobalSearchProvider>("categories");
      const categories = await provider.fetch();
      const items = provider.map(categories as never);

      expect(listCategoryMock).toHaveBeenCalledWith({
        size: 1000,
        sort: ["metadata.creationTimestamp,desc"],
        page: 1,
      });
      expect(items[0]).toMatchObject({
        id: "categories:category-1",
        title: "Category One",
        route: { name: "Categories", query: { name: "category-1" } },
      });
    });

    it("maps system setting forms to results", async () => {
      getSettingMock.mockResolvedValue({
        data: {
          spec: { forms: [{ group: "basic", label: "Basic" }] },
        } as Setting,
      });

      const provider =
        getProvider<CachedGlobalSearchProvider<Setting>>("settings");
      const setting = await provider.fetch();
      const items = provider.map(setting);

      expect(getSettingMock).toHaveBeenCalledWith({ name: "system" });
      expect(items[0]).toMatchObject({
        id: "settings:basic",
        title: "Basic",
        route: { name: "SystemSetting", params: { group: "basic" } },
      });
    });

    it("maps theme setting forms with the activated theme name", async () => {
      fetchThemeSettingMock.mockResolvedValue({
        data: {
          spec: { forms: [{ group: "general", label: "General" }] },
        } as Setting,
      });

      const context = createTestContext({
        getActivatedTheme: () => ({ spec: { displayName: "Earth" } }) as Theme,
      });
      const provider = getProvider<CachedGlobalSearchProvider<Setting>>(
        "theme-settings",
        context
      );
      const setting = await provider.fetch();
      const items = provider.map(setting);

      expect(fetchThemeSettingMock).toHaveBeenCalledWith({ name: "-" });
      expect(items[0]).toMatchObject({
        id: "theme-settings:general",
        title: "Earth / General",
        route: { name: "ThemeSetting", params: { group: "general" } },
      });
    });
  });
});

describe("resolveContentMatchContext", () => {
  it("returns the slug when the keyword matches the title", () => {
    expect(
      resolveContentMatchContext({
        title: "Hello world",
        slug: "hello-world",
        excerpt: "hello appears here too",
        keyword: "hello",
      })
    ).toBe("hello-world");
  });

  it("returns the slug when the keyword matches the slug", () => {
    expect(
      resolveContentMatchContext({
        title: "Unrelated",
        slug: "hello-world",
        excerpt: "hello appears here too",
        keyword: "hello",
      })
    ).toBe("hello-world");
  });

  it("returns the excerpt when only the excerpt explains the match", () => {
    expect(
      resolveContentMatchContext({
        title: "Unrelated",
        slug: "unrelated",
        excerpt: "A hello appears in this excerpt",
        keyword: "hello",
      })
    ).toBe("A hello appears in this excerpt");
  });

  it("truncates a long excerpt", () => {
    const excerpt = `prefix ${"a".repeat(100)}`;
    const context = resolveContentMatchContext({
      title: "Unrelated",
      slug: "unrelated",
      excerpt,
      keyword: "prefix",
    });
    expect(context).toBe(`${excerpt.slice(0, 60).trimEnd()}…`);
    expect(context).toHaveLength(61);
  });

  it("returns the slug when there is no excerpt", () => {
    expect(
      resolveContentMatchContext({
        title: "Unrelated",
        slug: "unrelated",
        keyword: "hello",
      })
    ).toBe("unrelated");
  });
});
