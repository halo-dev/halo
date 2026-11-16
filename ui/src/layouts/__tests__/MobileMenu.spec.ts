import { enableAutoUnmount, mount } from "@vue/test-utils";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { nextTick } from "vue";
import MobileMenu from "../MobileMenu.vue";

vi.mock("vue-router", () => ({
  useRoute: () => ({ matched: [], path: "/" }),
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("vue-i18n", () => ({
  useI18n: () => ({ t: (key: string) => key }),
}));

enableAutoUnmount(afterEach);

describe("MobileMenu", () => {
  it("labels both navigation landmarks", async () => {
    const wrapper = mount(MobileMenu, {
      props: {
        menus: [
          {
            name: "",
            items: [{ name: "Posts", path: "/posts" }],
          },
        ],
        minimenus: [],
      },
      global: {
        mocks: {
          $t: (key: string) => key,
        },
        stubs: {
          OverlayScrollbarsComponent: { template: "<div><slot /></div>" },
          UserProfileBanner: true,
        },
      },
    });

    await wrapper.get(".mobile-nav__item").trigger("click");
    await nextTick();

    const labels = Array.from(
      document.body.querySelectorAll('[role="navigation"]')
    ).map((element) => element.getAttribute("aria-label"));

    expect(labels).toEqual(["Home", "Main menu"]);
  });
});
