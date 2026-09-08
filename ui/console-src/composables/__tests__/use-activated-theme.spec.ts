import { useInstalledThemes } from "@console/modules/interface/themes/composables/use-installed-themes";
import { useThemeCustomTemplates } from "@console/modules/interface/themes/composables/use-theme";
import { FormKit, defaultConfig, plugin as formKitPlugin } from "@formkit/vue";
import { consoleApiClient, type Theme } from "@halo-dev/api-client";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { AxiosError } from "axios";
import { afterEach, beforeEach, expect, it, vi } from "vite-plus/test";
import { defineComponent, h, ref, type RenderFunction } from "vue";
import { createI18n } from "vue-i18n";
import {
  useActivatedTheme,
  invalidateThemeQueries,
} from "../use-activated-theme";

const permission = vi.hoisted(() => ({ allowed: true }));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => permission.allowed } },
}));
vi.mock("@halo-dev/api-client", async (original) => ({
  ...(await original<typeof import("@halo-dev/api-client")>()),
  consoleApiClient: {
    theme: { theme: { fetchActivatedTheme: vi.fn(), listThemes: vi.fn() } },
  },
}));
const theme = {
  metadata: { name: "earth" },
  spec: {
    displayName: "Earth",
    customTemplates: { post: [{ name: "Custom", file: "custom.html" }] },
  },
} as Theme;
const cleanups: (() => void)[] = [];
afterEach(() => cleanups.splice(0).forEach((fn) => fn()));
beforeEach(() => {
  vi.resetAllMocks();
  permission.allowed = true;
  vi.mocked(consoleApiClient.theme.theme.fetchActivatedTheme).mockResolvedValue(
    { data: theme } as never
  );
});
async function setup(
  run: () => RenderFunction | void,
  client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
) {
  const wrapper = mount(
    defineComponent({
      setup() {
        return run() || (() => h("div"));
      },
    }),
    {
      global: {
        plugins: [
          [VueQueryPlugin, { queryClient: client }],
          [formKitPlugin, defaultConfig()],
          createI18n({
            legacy: false,
            locale: "en",
            missingWarn: false,
            fallbackWarn: false,
          }),
        ],
      },
    }
  );
  cleanups.push(() => {
    wrapper.unmount();
    client.clear();
  });
  await flushPromises();
  return { wrapper, client };
}
function httpError(status: number) {
  return new AxiosError("request failed", undefined, undefined, undefined, {
    status,
  } as never);
}

it("does not request until enabled and shares in-flight requests and fresh data", async () => {
  const enabled = ref(false);
  const pending = Promise.withResolvers<{ data: Theme }>();
  vi.mocked(consoleApiClient.theme.theme.fetchActivatedTheme).mockReturnValue(
    pending.promise as never
  );
  let first!: ReturnType<typeof useActivatedTheme>;
  let second!: ReturnType<typeof useActivatedTheme>;
  const { client } = await setup(() => {
    first = useActivatedTheme(enabled);
    second = useActivatedTheme(enabled);
  });
  expect(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).not.toHaveBeenCalled();
  enabled.value = true;
  await flushPromises();
  expect(first.isInitialLoading.value).toBe(true);
  expect(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).toHaveBeenCalledOnce();
  pending.resolve({ data: theme });
  await flushPromises();
  expect(first.data.value).toEqual(theme);
  expect(second.data.value).toEqual(theme);
  await setup(() => {
    useActivatedTheme();
  }, client);
  expect(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).toHaveBeenCalledOnce();
});

it("does not request without theme view permission", async () => {
  permission.allowed = false;
  await setup(() => {
    useActivatedTheme();
  });
  expect(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).not.toHaveBeenCalled();
});

it("clears an old active theme when the server reports none", async () => {
  let query!: ReturnType<typeof useActivatedTheme>;
  const { client } = await setup(() => {
    query = useActivatedTheme();
  });
  vi.mocked(consoleApiClient.theme.theme.fetchActivatedTheme).mockRejectedValue(
    httpError(404)
  );
  await invalidateThemeQueries(client);
  expect(query.data.value).toBeNull();
  expect(query.isError.value).toBe(false);
});

it("exposes real failures and supports retry", async () => {
  vi.mocked(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).mockRejectedValueOnce(httpError(503));
  let query!: ReturnType<typeof useActivatedTheme>;
  await setup(() => {
    query = useActivatedTheme();
  });
  expect(query.isError.value).toBe(true);
  expect(query.data.value).toBeUndefined();
  await query.refetch();
  expect(query.data.value).toEqual(theme);
  expect(query.isError.value).toBe(false);
});

it("reorders installed themes when activation arrives without changing the cached list", async () => {
  const other = { ...theme, metadata: { name: "other" } };
  const pending = Promise.withResolvers<{ data: Theme }>();
  vi.mocked(consoleApiClient.theme.theme.fetchActivatedTheme).mockReturnValue(
    pending.promise as never
  );
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockResolvedValue({
    data: { items: [other, theme], hasNext: false },
  } as never);
  let list!: ReturnType<typeof useInstalledThemes>;
  const { client } = await setup(() => {
    list = useInstalledThemes();
  });
  expect(list.data.value?.[0].metadata.name).toBe("other");
  pending.resolve({ data: theme });
  await flushPromises();
  expect(list.data.value?.[0].metadata.name).toBe("earth");
  expect(
    client.getQueryData<Theme[]>(["installed-themes"])?.[0].metadata.name
  ).toBe("other");
});

it("preserves an existing FormKit template value during loading and failure", async () => {
  const pending = Promise.withResolvers<{ data: Theme }>();
  vi.mocked(
    consoleApiClient.theme.theme.fetchActivatedTheme
  ).mockReturnValueOnce(pending.promise as never);
  const current = ref("custom.html");
  let templates!: ReturnType<typeof useThemeCustomTemplates>;
  const { wrapper } = await setup(() => {
    templates = useThemeCustomTemplates("post", current);
    return () =>
      h(FormKit, {
        type: "select",
        modelValue: current.value,
        "onUpdate:modelValue": (value: string) => (current.value = value),
        options: templates.templates.value,
        disabled: templates.isInitialLoading.value || templates.isError.value,
      });
  });
  expect(current.value).toBe("custom.html");
  expect(wrapper.get("select").element.disabled).toBe(true);
  pending.reject(httpError(503));
  await flushPromises();
  expect(current.value).toBe("custom.html");
  expect(wrapper.get("select").element.disabled).toBe(true);
  await templates.refetch();
  await flushPromises();
  expect(wrapper.get("select").element.value).toBe("custom.html");
  expect(wrapper.get("select").element.disabled).toBe(false);
  expect(wrapper.text()).toContain("Custom");
});

it("invalidates all theme-dependent caches after a mutation", async () => {
  const client = new QueryClient();
  const keys = [
    ["activated-theme"],
    ["installed-themes"],
    ["themes"],
    ["not-installed-themes"],
    ["theme-setting", "earth"],
    ["core:theme:configMap:data", "earth"],
    ["core", "global-search", "theme-settings"],
  ];
  keys.forEach((key) => client.setQueryData(key, {}));
  await invalidateThemeQueries(client);
  keys.forEach((key) =>
    expect(client.getQueryState(key)?.isInvalidated).toBe(true)
  );
  client.clear();
});
