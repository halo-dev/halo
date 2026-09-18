import { consoleApiClient, type Theme } from "@halo-dev/api-client";
import { IconPalette } from "@halo-dev/components";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, expect, it, vi } from "vite-plus/test";
import { createI18n } from "vue-i18n";
import ThemePreviewModal from "../ThemePreviewModal.vue";
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => true } },
}));
vi.mock("@halo-dev/api-client", async (original) => ({
  ...(await original<typeof import("@halo-dev/api-client")>()),
  consoleApiClient: {
    theme: {
      theme: {
        fetchActivatedTheme: vi.fn(),
        listThemes: vi.fn(),
        fetchThemeSetting: vi.fn(),
        fetchThemeJsonConfig: vi.fn(),
      },
    },
  },
}));
const earth = {
  metadata: { name: "earth" },
  spec: { displayName: "Earth", settingName: "earth-setting" },
} as Theme;
const other = { ...earth, metadata: { name: "other" } };
const cleanups: (() => void)[] = [];
afterEach(() => cleanups.splice(0).forEach((fn) => fn()));
beforeEach(() => {
  vi.resetAllMocks();
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockResolvedValue({
    data: { items: [other], hasNext: false },
  } as never);
  vi.mocked(consoleApiClient.theme.theme.fetchThemeSetting).mockResolvedValue({
    data: { spec: { forms: [{ group: "style" }] } },
  } as never);
  vi.mocked(
    consoleApiClient.theme.theme.fetchThemeJsonConfig
  ).mockResolvedValue({ data: {} } as never);
});
async function setup(theme?: Theme) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const wrapper = mount(ThemePreviewModal, {
    props: { theme },
    global: {
      plugins: [
        [VueQueryPlugin, { queryClient: client }],
        createI18n({
          legacy: false,
          locale: "en",
          missingWarn: false,
          fallbackWarn: false,
        }),
      ],
      directives: { tooltip: () => {}, permission: () => {} },
      stubs: {
        Modal: { template: "<div><slot name='actions'/><slot/></div>" },
        Tabbar: true,
        OverlayScrollbarsComponent: { template: "<div><slot/></div>" },
        ThemePreviewListItem: {
          props: ["theme"],
          emits: ["click"],
          template:
            "<button data-select @click='$emit(\"click\")'>Select</button>",
        },
        StickyBlock: true,
        FormKit: true,
        FormKitSchema: true,
      },
    },
  });
  cleanups.push(() => {
    wrapper.unmount();
    client.clear();
  });
  await flushPromises();
  return wrapper;
}
it("initializes from a late active theme response without loading an empty iframe", async () => {
  const pending = Promise.withResolvers<{ data: Theme }>();
  vi.mocked(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).mockReturnValueOnce(pending.promise as never);
  const wrapper = await setup();
  expect(wrapper.find("iframe").exists()).toBe(false);
  pending.resolve({ data: earth });
  await flushPromises();
  expect(wrapper.get("iframe").attributes("src")).toBe("/?preview-theme=earth");
});
it("uses an explicit theme without fetching the active theme", async () => {
  const wrapper = await setup(other);
  expect(wrapper.get("iframe").attributes("src")).toBe("/?preview-theme=other");
  expect(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).not.toHaveBeenCalled();
});
it("does not overwrite manual selection when the active theme response arrives", async () => {
  const pending = Promise.withResolvers<{ data: Theme }>();
  vi.mocked(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).mockReturnValueOnce(pending.promise as never);
  const wrapper = await setup();
  await wrapper.findComponent(IconPalette).trigger("click");
  await wrapper.get("[data-select]").trigger("click");
  await flushPromises();
  pending.resolve({ data: earth });
  await flushPromises();
  expect(wrapper.get("iframe").attributes("src")).toBe("/?preview-theme=other");
});
