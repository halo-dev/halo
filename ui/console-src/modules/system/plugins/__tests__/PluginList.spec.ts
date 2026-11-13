import { consoleApiClient, type Plugin } from "@halo-dev/api-client";
import { VFilterDropdown } from "@halo-dev/components";
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
import { defineComponent, h } from "vue";
import { createI18n } from "vue-i18n";
import { createMemoryHistory, createRouter } from "vue-router";
import PluginList from "../PluginList.vue";

vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => true } },
}));
vi.mock("../composables/use-plugin", () => ({
  usePluginBatchOperations: () => ({
    handleChangeStatusInBatch: vi.fn(),
    handleUninstallInBatch: vi.fn(),
  }),
}));
vi.mock("../components/PluginInstallationModal.vue", () => ({
  default: { template: "<div />" },
}));
vi.mock("../components/PluginListItem.vue", () => ({
  default: {
    props: ["plugin", "isSelected"],
    template:
      '<div data-plugin :data-selected="isSelected">{{ plugin.metadata.name }}</div>',
  },
}));

function plugin(name: string, author?: string, enabled = true): Plugin {
  return {
    apiVersion: "plugin.halo.run/v1alpha1",
    kind: "Plugin",
    metadata: { name },
    spec: {
      version: "1.0.0",
      displayName: name,
      enabled,
      author: author === undefined ? undefined : { name: author },
    },
    status: { phase: "STARTED" },
  };
}

const plugins = [
  plugin("bravo", "Bravo"),
  plugin("alpha-gallery", "Alpha"),
  plugin("alpha-editor", "Alpha"),
  plugin("alphabet-gallery", "Alphabet", false),
  plugin("no-author"),
  plugin("blank-author", "  "),
];

const cleanups: (() => void)[] = [];

beforeEach(() => {
  vi.spyOn(consoleApiClient.plugin.plugin, "listPlugins").mockImplementation(
    async (params) => {
      const items = plugins.filter(
        (item) =>
          params?.enabled === undefined || item.spec.enabled === params.enabled
      );
      return { data: { items, hasNext: false } } as Awaited<
        ReturnType<typeof consoleApiClient.plugin.plugin.listPlugins>
      >;
    }
  );
});

afterEach(() => {
  cleanups.splice(0).forEach((cleanup) => cleanup());
  vi.restoreAllMocks();
});

async function mountList(query: Record<string, string> = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: "/plugins", component: PluginList }],
  });
  await router.push({ path: "/plugins", query });
  await router.isReady();
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const wrapper = mount(PluginList, {
    global: {
      plugins: [
        router,
        [VueQueryPlugin, { queryClient: client }],
        createI18n({ legacy: false, missingWarn: false, fallbackWarn: false }),
      ],
      stubs: {
        HasPermission: { template: "<div><slot /></div>" },
        SearchInput: true,
        DropdownItem: true,
        VDropdown: defineComponent({
          setup(_, { slots, expose }) {
            expose({ show: vi.fn(), hide: vi.fn() });
            return () => h("div", [slots.default?.(), slots.popper?.()]);
          },
        }),
      },
      directives: { permission: () => {}, tooltip: () => {} },
    },
  });
  cleanups.push(() => {
    wrapper.unmount();
    client.clear();
  });
  await flushPromises();
  const authorFilter = wrapper
    .findAllComponents(VFilterDropdown)
    .find(
      (filter) => filter.props("label") === "core.plugin.filters.author.label"
    )!;
  const names = () =>
    wrapper.findAll("[data-plugin]").map((item) => item.text());
  return { wrapper, router, authorFilter, names };
}

describe("PluginList author filter", () => {
  it("deduplicates and searches authors, filters by exact name and persists the selection", async () => {
    const { router, authorFilter, names } = await mountList();
    expect(
      authorFilter
        .findAll('[role="option"]')
        .map((item) => item.get("span").text())
    ).toEqual([
      "core.common.filters.item_labels.all",
      "Alpha",
      "Alphabet",
      "Bravo",
    ]);
    await authorFilter.get("input").setValue("Alpha");
    expect(
      authorFilter
        .findAll('[role="option"]')
        .map((item) => item.get("span").text())
    ).toEqual(["Alpha", "Alphabet"]);
    await authorFilter.findAll('[role="option"]')[0].trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.query.author).toBe("Alpha");
    expect(names()).toEqual(["alpha-gallery", "alpha-editor"]);
    expect(consoleApiClient.plugin.plugin.listPlugins).toHaveBeenCalledTimes(1);
  });

  it("shows websites and counts, orders by count, and counts independently of the keyword", async () => {
    const items = [
      plugin("photography", "Zulu"),
      plugin("alpha-first", "Alpha"),
      plugin("beta-first", "Beta"),
      plugin("zulu-second", "Zulu"),
      plugin("zulu-third", "Zulu"),
      plugin("no-author"),
      plugin("blank-author", "  "),
    ];
    items[0].spec.homepage = "https://plugins.example/zulu";
    items[0].spec.author!.website = "https://zulu.example";
    items[3].spec.author!.website = "https://zulu.example";
    items[4].spec.author!.website = "https://zulu.example";
    items[1].spec.author!.website = "  ";
    items[1].spec.homepage = "https://plugins.example/alpha";
    vi.mocked(consoleApiClient.plugin.plugin.listPlugins).mockResolvedValue({
      data: { items, hasNext: false },
    } as Awaited<
      ReturnType<typeof consoleApiClient.plugin.plugin.listPlugins>
    >);

    const { authorFilter, names, router } = await mountList({
      keyword: "photography",
    });
    expect(names()).toEqual(["photography"]);
    expect(authorFilter.props("options")).toEqual([
      {
        label: "Zulu",
        value: '["Zulu","https://zulu.example"]',
        description: "https://zulu.example",
        count: 3,
      },
      {
        label: "Alpha",
        value: '["Alpha",""]',
        description: "https://plugins.example/alpha",
        count: 1,
      },
      { label: "Beta", value: '["Beta",""]', description: undefined, count: 1 },
    ]);
    const options = authorFilter.findAll('[role="option"]');
    expect(options[0].text()).toBe("core.common.filters.item_labels.all");
    expect(options[1].text()).toContain("https://zulu.example");
    expect(options[1].text()).not.toContain("https://plugins.example/zulu");
    expect(options[1].get("span.shrink-0").text()).toBe("3");
    expect(options[2].text()).toContain("https://plugins.example/alpha");
    expect(options[2].get("span.shrink-0").text()).toBe("1");
    expect(options[3].find("span[title]").exists()).toBe(false);

    await options[1].trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.query.author).toBe("Zulu");
    expect(router.currentRoute.value.query.authorWebsite).toBe(
      "https://zulu.example"
    );
    expect(authorFilter.get("button").text()).toBe(
      "core.plugin.filters.author.label：Zulu"
    );
  });

  it("groups and filters by both name and website, including URL restoration and clearing", async () => {
    const firstWebsite = "https://first.example/?a=1&b=2";
    const secondWebsite = "https://second.example";
    const items = [
      plugin("first-gallery", "Shared"),
      plugin("first-editor", "Shared", false),
      plugin("second-editor", "Shared"),
      plugin("without-website", "Shared"),
      plugin("another-homepage", "Shared"),
    ];
    items[0].spec.author!.website = firstWebsite;
    items[1].spec.author!.website = firstWebsite;
    items[2].spec.author!.website = secondWebsite;
    items[3].spec.homepage = "https://plugins.example/one";
    items[4].spec.homepage = "https://plugins.example/two";
    vi.mocked(consoleApiClient.plugin.plugin.listPlugins).mockImplementation(
      async (params) =>
        ({
          data: {
            items: items.filter(
              (item) =>
                params?.enabled === undefined ||
                item.spec.enabled === params.enabled
            ),
            hasNext: false,
          },
        }) as Awaited<
          ReturnType<typeof consoleApiClient.plugin.plugin.listPlugins>
        >
    );

    const { wrapper, router, authorFilter, names } = await mountList({
      author: "Shared",
      authorWebsite: firstWebsite,
    });
    expect(names()).toEqual(["first-gallery", "first-editor"]);
    const options = authorFilter.props("options");
    expect(options).toHaveLength(3);
    expect(options).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ description: firstWebsite, count: 2 }),
        expect.objectContaining({ description: secondWebsite, count: 1 }),
        expect.objectContaining({
          description: "https://plugins.example/one",
          count: 2,
        }),
      ])
    );
    await wrapper.get('input[type="checkbox"]').setValue(true);
    expect(wrapper.findAll('[data-selected="true"]')).toHaveLength(2);
    await authorFilter
      .findAll('[role="option"]')
      .find((option) => option.text().includes(secondWebsite))!
      .trigger("click");
    await flushPromises();
    expect(names()).toEqual(["second-editor"]);
    expect(router.currentRoute.value.query).toEqual({
      author: "Shared",
      authorWebsite: secondWebsite,
    });
    expect(wrapper.findAll('[data-selected="true"]')).toHaveLength(0);

    await router.replace({
      query: { ...router.currentRoute.value.query, enabled: "false" },
    });
    await flushPromises();
    expect(names()).toEqual([]);
    expect(authorFilter.get("button").text()).toBe(
      "core.plugin.filters.author.label：Shared"
    );
    expect(router.currentRoute.value.query.authorWebsite).toBe(secondWebsite);
    await wrapper
      .get('[aria-label="core.common.buttons.reset"]')
      .trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.query).toEqual({});
    expect(names()).toEqual(items.map((item) => item.metadata.name));

    await authorFilter
      .findAll('[role="option"]')
      .find((option) => option.text().includes("https://plugins.example/one"))!
      .trigger("click");
    await flushPromises();
    expect(names()).toEqual(["without-website", "another-homepage"]);
    expect(router.currentRoute.value.query).toEqual({ author: "Shared" });
  });

  it("restores author and keyword from the URL and retains an unavailable author until reset", async () => {
    const { wrapper, router, authorFilter, names } = await mountList({
      author: "Alpha",
      keyword: "gallery",
      enabled: "true",
      sort: "creationTimestamp,desc",
    });
    expect(names()).toEqual(["alpha-gallery"]);
    expect(authorFilter.text()).toContain("Bravo");
    expect(consoleApiClient.plugin.plugin.listPlugins).toHaveBeenLastCalledWith(
      expect.objectContaining({
        enabled: true,
        sort: ["creationTimestamp,desc"],
      })
    );
    await router.replace({
      query: { ...router.currentRoute.value.query, enabled: "false" },
    });
    await flushPromises();
    expect(names()).toEqual([]);
    expect(authorFilter.get("button").text()).toContain("Alpha");
    expect(router.currentRoute.value.query.author).toBe("Alpha");
    expect(wrapper.text()).toContain("core.plugin.empty.title");
    await wrapper
      .get('[aria-label="core.common.buttons.reset"]')
      .trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.query).toEqual({ keyword: "gallery" });
    expect(names()).toEqual(["alpha-gallery", "alphabet-gallery"]);
  });

  it("clears batch selection when the author changes, including empty results, and can clear the author alone", async () => {
    const { wrapper, router, authorFilter, names } = await mountList();
    await wrapper.get('input[type="checkbox"]').setValue(true);
    expect(wrapper.findAll('[data-selected="true"]')).toHaveLength(
      plugins.length
    );
    await authorFilter.findAll('[role="option"]')[1].trigger("click");
    await flushPromises();
    expect(wrapper.findAll('[data-selected="true"]')).toHaveLength(0);
    expect(
      (wrapper.get('input[type="checkbox"]').element as HTMLInputElement)
        .checked
    ).toBe(false);
    await wrapper.get('input[type="checkbox"]').setValue(true);
    await router.replace({ query: { author: "Missing" } });
    await flushPromises();
    expect(names()).toEqual([]);
    expect(
      (wrapper.get('input[type="checkbox"]').element as HTMLInputElement)
        .checked
    ).toBe(false);
    await authorFilter.findAll('[role="option"]')[0].trigger("click");
    await flushPromises();
    expect(router.currentRoute.value.query.author).toBeUndefined();
    expect(names()).toEqual(plugins.map((item) => item.metadata.name));
    expect(wrapper.findAll('[data-selected="true"]')).toHaveLength(0);
  });
});
