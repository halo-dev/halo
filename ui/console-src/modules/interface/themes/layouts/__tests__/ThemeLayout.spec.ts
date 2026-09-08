/* eslint-disable vue/one-component-per-file -- Local component stubs for the selection integration test. */
import type { Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { QueryClient, VueQueryPlugin } from "@tanstack/vue-query";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, expect, it, vi } from "vite-plus/test";
import {
  defineComponent,
  h,
  inject,
  ref,
  reactive,
  toRefs,
  type Ref,
} from "vue";
import { createI18n } from "vue-i18n";
import { createMemoryHistory, createRouter, RouterView } from "vue-router";
import ThemeLayout from "../ThemeLayout.vue";

const { themes, state } = vi.hoisted(() => ({
  state: {
    activeIndex: 0 as number | undefined,
    formLoad: undefined as Promise<void> | undefined,
  },
  themes: [
    {
      apiVersion: "theme.halo.run/v1alpha1",
      kind: "Theme",
      metadata: { name: "active" },
      spec: { displayName: "Active", settingName: "active-setting" },
    },
    {
      apiVersion: "theme.halo.run/v1alpha1",
      kind: "Theme",
      metadata: { name: "other" },
      spec: { displayName: "Other", settingName: "other-setting" },
    },
    {
      apiVersion: "theme.halo.run/v1alpha1",
      kind: "Theme",
      metadata: { name: "empty" },
      spec: { displayName: "Empty" },
    },
  ] as Theme[],
}));

vi.mock("@console/layouts/BasicLayout.vue", () => ({
  default: { template: "<slot />" },
}));
vi.mock("@console/stores/theme", () => ({
  useThemeStore: () =>
    reactive({
      activatedTheme:
        state.activeIndex === undefined
          ? undefined
          : structuredClone(themes[state.activeIndex]),
    }),
}));
vi.mock("pinia", () => ({ storeToRefs: (store: object) => toRefs(store) }));
vi.mock("@/stores/plugin", () => ({
  usePluginModuleStore: () => ({ pluginModules: [] }),
}));
vi.mock("../../composables/use-theme", () => ({
  useThemeLifeCycle: () => ({ loading: ref(false), isActivated: ref(true) }),
}));
vi.mock("../../components/preview/ThemePreviewModal.vue", () => ({
  default: { template: "<div />" },
}));
vi.mock("@halo-dev/ui-shared", () => ({
  utils: { permission: { has: () => true } },
}));
vi.mock("@halo-dev/api-client", async (importOriginal) => ({
  ...(await importOriginal<typeof import("@halo-dev/api-client")>()),
  consoleApiClient: {
    theme: { theme: { fetchThemeSetting: vi.fn(), listThemes: vi.fn() } },
  },
}));
vi.mock("../../components/list-tabs/InstalledThemes.vue", () => ({
  __esModule: true,
  default: defineComponent({
    setup() {
      const selected = inject<Ref<Theme | undefined>>("selectedTheme")!;
      return () =>
        themes.map((theme) =>
          h(
            "button",
            {
              "data-theme": theme.metadata.name,
              onClick: () => {
                selected.value = theme;
              },
            },
            theme.spec.displayName
          )
        );
    },
  }),
}));

const SlotContainer = {
  template: "<div><slot name='header' /><slot /><slot name='actions' /></div>",
};
const closeModal = vi.fn<(complete: () => void) => void>();
const Modal = defineComponent({
  emits: ["close"],
  setup(_, { emit, expose, slots }) {
    expose({ close: () => closeModal(() => emit("close")) });
    return () => h("div", { role: "dialog" }, slots.default?.());
  },
});

const cleanups: (() => void)[] = [];
afterEach(() => {
  cleanups.splice(0).forEach((cleanup) => cleanup());
});
beforeEach(() => {
  state.activeIndex = 0;
  state.formLoad = undefined;
  vi.clearAllMocks();
  closeModal.mockImplementation((complete) => complete());
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockResolvedValue({
    data: { items: structuredClone(themes), hasNext: false },
  } as never);
  vi.mocked(consoleApiClient.theme.theme.fetchThemeSetting).mockImplementation(
    async ({ name }) =>
      ({
        data: { spec: { forms: [{ group: "style", label: `${name} style` }] } },
      }) as never
  );
});

const SettingsPage = defineComponent({
  async setup() {
    const selected = inject<Ref<Theme>>("selectedTheme")!;
    const draft = ref(selected.value.metadata.name);
    await state.formLoad;
    return () =>
      h("input", {
        "data-draft": "",
        value: draft.value,
        onInput: (event: Event) => {
          draft.value = (event.target as HTMLInputElement).value;
        },
      });
  },
});

async function setup(url = "/theme") {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: "/theme",
        component: ThemeLayout,
        children: [
          { path: "", name: "ThemeDetail", component: { template: "<div />" } },
          {
            path: "settings/:group",
            name: "ThemeSetting",
            component: SettingsPage,
          },
        ],
      },
    ],
  });
  await router.push(url);
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false, staleTime: Infinity } },
  });
  const wrapper = mount(RouterView, {
    global: {
      plugins: [
        router,
        createI18n({
          legacy: false,
          locale: "en",
          missingWarn: false,
          fallbackWarn: false,
        }),
        [VueQueryPlugin, { queryClient }],
      ],
      directives: { permission: () => {} },
      stubs: {
        PageHeader: {
          ...SlotContainer,
          props: ["title"],
          template: "<header>{{ title }}<slot name='actions' /></header>",
        },
        Card: SlotContainer,
        Button: { template: "<button><slot /></button>" },
        Modal,
        Empty: {
          props: ["title"],
          template: "<div>{{ title }}<slot name='actions' /></div>",
        },
        Tabbar: {
          props: ["items"],
          emits: ["change"],
          template:
            "<nav><button v-for='item in items' :key='item.id' @click='$emit(\"change\", item.id)'>{{ item.label }}</button></nav>",
        },
      },
    },
  });
  cleanups.push(() => {
    wrapper.unmount();
    queryClient.clear();
  });
  await flushPromises();
  async function select(name: string) {
    if (!wrapper.find("[role='dialog']").exists()) {
      await wrapper
        .findAll("button")
        .find((button) => button.text() === "core.theme.actions.management")!
        .trigger("click");
      await flushPromises();
    }
    await wrapper.get(`[data-theme='${name}']`).trigger("click");
    await flushPromises();
  }
  async function clickTab(label: string) {
    await wrapper
      .findAll("nav button")
      .find((button) => button.text() === label)!
      .trigger("click");
    await flushPromises();
  }
  return { wrapper, router, queryClient, select, clickTab };
}

it("keeps repeated selections and cached settings while recording explicit choices", async () => {
  const { wrapper, router, select, clickTab } = await setup(
    "/theme/settings/style?other=value"
  );
  expect(wrapper.text()).toContain("active style");
  await wrapper.get("[data-draft]").setValue("unsaved");
  for (let index = 0; index < 2; index++) {
    await select("active");
    expect(wrapper.find("[role='dialog']").exists()).toBe(false);
    expect(
      (wrapper.get("[data-draft]").element as HTMLInputElement).value
    ).toBe("unsaved");
    expect(router.currentRoute.value.name).toBe("ThemeSetting");
    expect(router.currentRoute.value.query).toEqual({ other: "value" });
  }
  await select("other");
  expect(router.currentRoute.value.query).toEqual({
    theme: "other",
    other: "value",
  });
  expect(router.currentRoute.value.name).toBe("ThemeDetail");
  expect(wrapper.text()).toContain("other style");
  await clickTab("other style");
  expect(router.currentRoute.value.query.theme).toBe("other");
  await select("empty");
  expect(wrapper.text()).not.toContain("style");
  await select("active");
  expect(wrapper.text()).toContain("active style");
  await select("active");
  expect(wrapper.find("[role='dialog']").exists()).toBe(false);
  expect(consoleApiClient.theme.theme.fetchThemeSetting).toHaveBeenCalledTimes(
    2
  );
});

it("restores deep links and browser history, remounting forms between themes", async () => {
  const { wrapper, router, clickTab } = await setup(
    "/theme/settings/style?theme=other"
  );
  expect(wrapper.text()).toContain("other style");
  expect(router.currentRoute.value.name).toBe("ThemeSetting");
  await router.push("/theme/settings/style?theme=active");
  await flushPromises();
  await wrapper.get("[data-draft]").setValue("active draft");
  router.back();
  await vi.waitFor(() =>
    expect(router.currentRoute.value.query.theme).toBe("other")
  );
  await flushPromises();
  expect((wrapper.get("[data-draft]").element as HTMLInputElement).value).toBe(
    "other"
  );
  router.forward();
  await vi.waitFor(() =>
    expect(router.currentRoute.value.query.theme).toBe("active")
  );
  await flushPromises();
  expect((wrapper.get("[data-draft]").element as HTMLInputElement).value).toBe(
    "active"
  );
  await clickTab("core.theme.tabs.detail");
  expect(router.currentRoute.value.query.theme).toBe("active");
});

it("does not commit a selection while navigation is pending or cancelled", async () => {
  const { wrapper, router, select } = await setup("/theme/settings/style");
  let finish!: (allow: boolean) => void;
  const removeGuard = router.beforeEach(
    () =>
      new Promise<boolean>((resolve) => {
        finish = resolve;
      })
  );
  await select("other");
  expect(wrapper.get("header").text()).toContain("Active");
  expect(router.currentRoute.value.query.theme).toBeUndefined();
  finish(false);
  await flushPromises();
  expect(wrapper.text()).toContain("active style");
  expect(wrapper.find("[role='dialog']").exists()).toBe(true);
  expect(closeModal).not.toHaveBeenCalled();
  removeGuard();
  await select("other");
  expect(router.currentRoute.value.query.theme).toBe("other");
  expect(wrapper.find("[role='dialog']").exists()).toBe(false);
});

it.each(["?theme=missing", "?theme=", "?theme", "?theme=active&theme=other"])(
  "shows an empty state for invalid selection %s without falling back",
  async (query) => {
    const { wrapper, router } = await setup(`/theme${query}`);
    expect(wrapper.text()).toContain("core.common.toast.not_found");
    expect(wrapper.text()).not.toContain("active style");
    expect(router.currentRoute.value.query).toHaveProperty("theme");
    expect(
      consoleApiClient.theme.theme.fetchThemeSetting
    ).not.toHaveBeenCalled();
  }
);

it("allows selecting a theme when none is activated", async () => {
  state.activeIndex = undefined;
  const { wrapper, router, select } = await setup();
  expect(wrapper.text()).toContain("core.theme.empty.title");
  await select("other");
  expect(router.currentRoute.value.query.theme).toBe("other");
  expect(wrapper.text()).toContain("other style");
});

it("replaces unavailable setting groups after loading and keeps the selection", async () => {
  const { router } = await setup("/theme/settings/missing?theme=other");
  await flushPromises();
  expect(router.currentRoute.value.name).toBe("ThemeDetail");
  expect(router.currentRoute.value.query.theme).toBe("other");
});

it("clears a deleted selection after the installed list is invalidated", async () => {
  const { wrapper, queryClient } = await setup("/theme?theme=other");
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockResolvedValue({
    data: { items: [themes[0]], hasNext: false },
  } as never);
  await queryClient.invalidateQueries({ queryKey: ["installed-themes"] });
  await flushPromises();
  expect(wrapper.text()).toContain("core.common.toast.not_found");
  expect(wrapper.text()).not.toContain("other style");
});

it("waits for setting data before resolving a deep-linked group", async () => {
  const pending =
    Promise.withResolvers<
      Awaited<ReturnType<typeof consoleApiClient.theme.theme.fetchThemeSetting>>
    >();
  vi.mocked(consoleApiClient.theme.theme.fetchThemeSetting).mockReturnValue(
    pending.promise
  );
  const { router, wrapper } = await setup("/theme/settings/style?theme=other");
  expect(router.currentRoute.value.name).toBe("ThemeSetting");
  expect(wrapper.find("[data-draft]").exists()).toBe(false);
  pending.resolve({
    data: { spec: { forms: [{ group: "style", label: "other style" }] } },
  } as never);
  await flushPromises();
  expect(router.currentRoute.value.name).toBe("ThemeSetting");
  expect(wrapper.text()).toContain("other style");
});

it("keeps the default theme's draft mounted while the shared installed list loads", async () => {
  const { wrapper, queryClient } = await setup("/theme/settings/style");
  await wrapper.get("[data-draft]").setValue("unsaved");
  const pending =
    Promise.withResolvers<
      Awaited<ReturnType<typeof consoleApiClient.theme.theme.listThemes>>
    >();
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockReturnValue(
    pending.promise
  );
  // The installed-themes modal starts this same query while the layout observer is disabled.
  const fetching = queryClient.fetchQuery({ queryKey: ["installed-themes"] });
  await flushPromises();
  expect((wrapper.get("[data-draft]").element as HTMLInputElement).value).toBe(
    "unsaved"
  );
  pending.resolve({ data: { items: themes, hasNext: false } } as never);
  await fetching;
  await flushPromises();
  expect((wrapper.get("[data-draft]").element as HTMLInputElement).value).toBe(
    "unsaved"
  );
});

it("allows retry after an installed-themes request fails without selecting the active theme", async () => {
  vi.mocked(consoleApiClient.theme.theme.listThemes).mockRejectedValueOnce(
    new Error("network failure")
  );
  const { wrapper, router } = await setup("/theme?theme=other");
  expect(wrapper.text()).toContain("core.common.status.loading_error");
  expect(wrapper.text()).not.toContain("Active");
  await wrapper
    .findAll("button")
    .find((button) => button.text() === "core.common.buttons.retry")!
    .trigger("click");
  await flushPromises();
  expect(wrapper.text()).toContain("other style");
  expect(router.currentRoute.value.query.theme).toBe("other");
});

it("removes the previous form while the next theme's async form loads", async () => {
  const { wrapper, router, queryClient } = await setup("/theme/settings/style");
  queryClient.setQueryData(["installed-themes"], themes);
  queryClient.setQueryData(["theme-setting", themes[1]], {
    spec: { forms: [{ group: "style", label: "other style" }] },
  });
  const pending = Promise.withResolvers<void>();
  state.formLoad = pending.promise;
  await router.push("/theme/settings/style?theme=other");
  await flushPromises();
  expect(wrapper.find("[data-draft]").exists()).toBe(false);
  pending.resolve();
  await flushPromises();
  expect((wrapper.get("[data-draft]").element as HTMLInputElement).value).toBe(
    "other"
  );
});

it("replaces theme selections in history and waits for the modal close event", async () => {
  const { wrapper, router, select, clickTab } = await setup();
  await clickTab("active style");
  let finishClose!: () => void;
  closeModal.mockImplementationOnce((complete) => {
    finishClose = complete;
  });
  await select("other");
  expect(router.currentRoute.value.query.theme).toBe("other");
  expect(closeModal).toHaveBeenCalledOnce();
  expect(wrapper.find("[role='dialog']").exists()).toBe(true);
  finishClose();
  await flushPromises();
  expect(wrapper.find("[role='dialog']").exists()).toBe(false);
  await select("other");
  expect(closeModal).toHaveBeenCalledTimes(2);
  router.back();
  await vi.waitFor(() =>
    expect(router.currentRoute.value.fullPath).toBe("/theme")
  );
});
