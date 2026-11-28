import { flushPromises, mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { afterEach, beforeEach, expect, it, vi } from "vite-plus/test";
import { nextTick, type Ref } from "vue";
import { createMemoryHistory, createRouter } from "vue-router";
import BasicLayout from "../BasicLayout.vue";

interface MenuData {
  menus: unknown[];
  minimenus: unknown[];
}

const state = vi.hoisted(() => ({
  data: undefined as Ref<MenuData> | undefined,
  isLoading: undefined as Ref<boolean> | undefined,
}));

vi.mock("@/composables/use-route-menu-generator", async () => {
  const { ref } = await import("vue");
  state.data = ref<MenuData>({ menus: [], minimenus: [] });
  state.isLoading = ref(false);
  return {
    useRouteMenuGenerator: () => ({
      data: state.data,
      isLoading: state.isLoading,
    }),
  };
});

class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

beforeEach(() => {
  vi.stubGlobal("ResizeObserver", ResizeObserverMock);
});

afterEach(() => {
  vi.unstubAllGlobals();
});

it("scrolls the menu inside the sidebar and restores the offset after re-rendering", async () => {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: "/", component: { template: "<div data-route-view />" } }],
  });
  await router.push("/");
  await router.isReady();

  const wrapper = mount(BasicLayout, {
    attachTo: document.body,
    global: {
      plugins: [createPinia(), router],
      mocks: { $t: (key: string) => key },
      stubs: {
        RoutesMenu: { template: "<div data-menu-content />" },
        MenuLoading: true,
        MobileMenu: true,
        UserProfileBanner: true,
        ImpersonationBanner: true,
      },
    },
  });

  await flushPromises();

  const sidebar = wrapper.get(".sidebar");
  const scrollArea = sidebar.get(".sidebar__content");
  expect(scrollArea.classes()).toContain("v-scroll-area");
  expect(scrollArea.element.parentElement).toBe(sidebar.element);

  const viewport = scrollArea.get("[data-reka-scroll-area-viewport]");
  expect(viewport.find("[data-menu-content]").exists()).toBe(true);

  // The logo and profile stay outside the scrollable region.
  for (const selector of [".sidebar__logo-container", ".sidebar__profile"]) {
    const element = sidebar.get(selector).element;
    expect(scrollArea.element.contains(element)).toBe(false);
    expect(viewport.element.contains(element)).toBe(false);
  }

  viewport.element.scrollTop = 180;
  await viewport.trigger("scroll");

  viewport.element.scrollTop = 0;
  state.data!.value = {
    menus: [{ id: "content", name: "", items: [] }],
    minimenus: [],
  };
  await nextTick();
  await flushPromises();

  expect(viewport.element.scrollTop).toBe(180);

  wrapper.unmount();
});
