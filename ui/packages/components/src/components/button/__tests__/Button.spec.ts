import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vite-plus/test";
import { createMemoryHistory, createRouter } from "vue-router";
import { IconSettings } from "../../../icons/icons";
import { VButton } from "../index";

describe("Button", () => {
  it("should handle a route without a router", async () => {
    const onClick = vi.fn();
    const button = mount(VButton, { props: { route: "/target", onClick } });
    await button.trigger("click");
    expect(onClick).toHaveBeenCalledOnce();
  });

  it("should navigate when a route is provided", async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: "/target", component: { template: "<div />" } }],
    });
    const push = vi.spyOn(router, "push");
    const button = mount(VButton, {
      props: { route: "/target" },
      global: { plugins: [router] },
    });

    await button.trigger("click");
    expect(push).toHaveBeenCalledWith("/target");
  });

  it("should render", () => {
    expect(mount(VButton).html()).contains("button");
    expect(mount(VButton).html()).toMatchSnapshot();
  });

  it("should render with text", () => {
    expect(mount(VButton, { slots: { default: "Hello Halo" } }).text()).toBe(
      "Hello Halo"
    );
  });

  it("should work with type prop", () => {
    // default button type is default
    expect(mount(VButton).find(".btn").classes()).toContain("btn-default");

    ["primary", "secondary", "danger"].forEach((type) => {
      const button = mount(VButton, { props: { type } });

      expect(button.html()).toMatchSnapshot();
      expect(button.find(".btn").classes()).toContain(`btn-${type}`);
      button.unmount();
    });
  });

  it("should work with size prop", async () => {
    // default button size is md
    expect(mount(VButton).find(".btn").classes()).toContain("btn-md");

    ["lg", "sm", "xs"].forEach((size) => {
      const button = mount(VButton, { props: { size } });

      expect(button.html()).toMatchSnapshot();
      expect(button.find(".btn").classes()).toContain(`btn-${size}`);
      button.unmount();
    });
  });

  it("should work with circle prop", async () => {
    const button = mount(VButton);

    // default: false
    expect(button.find(".btn").classes()).not.toContain("btn-circle");

    await button.setProps({ circle: true });
    expect(button.html()).toMatchSnapshot();
    expect(button.find(".btn").classes()).toContain("btn-circle");
  });

  it("should work with shape prop", async () => {
    const button = mount(VButton);

    expect(button.find(".btn").classes()).not.toContain("btn-circle");
    expect(button.find(".btn").classes()).not.toContain("btn-round");

    await button.setProps({ shape: "circle" });
    expect(button.find(".btn").classes()).toContain("btn-circle");

    await button.setProps({ shape: "round" });
    expect(button.find(".btn").classes()).toContain("btn-round");
    expect(button.find(".btn").classes()).not.toContain("btn-circle");
  });

  it("should prefer the deprecated circle prop over shape", () => {
    const button = mount(VButton, { props: { circle: true, shape: "round" } });

    expect(button.find(".btn").classes()).toContain("btn-circle");
    expect(button.find(".btn").classes()).not.toContain("btn-round");
  });

  it("should work with variant prop", async () => {
    const button = mount(VButton);

    expect(button.find(".btn").classes()).not.toContain("btn-ghost");
    expect(button.find(".btn").classes()).not.toContain("btn-soft");

    await button.setProps({ variant: "soft" });
    expect(button.find(".btn").classes()).toContain("btn-soft");

    await button.setProps({ variant: "ghost" });
    expect(button.find(".btn").classes()).toContain("btn-ghost");
    expect(button.find(".btn").classes()).not.toContain("btn-soft");
  });

  it("should prefer the deprecated ghost prop over variant", () => {
    const button = mount(VButton, { props: { ghost: true, variant: "soft" } });

    expect(button.find(".btn").classes()).toContain("btn-ghost");
    expect(button.find(".btn").classes()).not.toContain("btn-soft");
  });

  it("should apply icon-only layout when only the icon slot is provided", () => {
    const button = mount(VButton, {
      props: { "aria-label": "设置" },
      slots: { icon: IconSettings },
    });

    expect(button.find(".btn").classes()).toContain("btn-icon-only");
  });

  it("should not apply icon-only layout when text content is provided", () => {
    const button = mount(VButton, {
      props: { "aria-label": "设置" },
      slots: { icon: IconSettings, default: "设置" },
    });

    expect(button.find(".btn").classes()).not.toContain("btn-icon-only");
  });

  it("should warn when an icon-only button has no accessible name", () => {
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});

    mount(VButton, { slots: { icon: IconSettings } });

    expect(warn).toHaveBeenCalledWith(
      expect.stringContaining("accessible name")
    );

    warn.mockRestore();
  });

  it("should not warn when an icon-only button has an accessible name", () => {
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});

    mount(VButton, {
      props: { "aria-label": "设置" },
      slots: { icon: IconSettings },
    });

    expect(warn).not.toHaveBeenCalled();

    warn.mockRestore();
  });

  it("should use the label prop as the accessible name", () => {
    const button = mount(VButton, { props: { label: "刷新" } });

    expect(button.find(".btn").attributes("aria-label")).toBe("刷新");
  });

  it("should not warn when an icon-only button has a label", () => {
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});

    mount(VButton, { props: { label: "设置" }, slots: { icon: IconSettings } });

    expect(warn).not.toHaveBeenCalled();

    warn.mockRestore();
  });

  it("should render as an anchor when the as prop is set", () => {
    const button = mount(VButton, { props: { as: "a", href: "/console" } });

    expect(button.element.tagName).toBe("A");
    expect(button.attributes("href")).toBe("/console");
    expect(button.attributes("type")).toBeUndefined();
  });

  it("should not navigate when a disabled anchor button is clicked", () => {
    const onClick = vi.fn();
    const button = mount(VButton, {
      props: { as: "a", href: "/console", disabled: true, onClick },
    });
    const event = new MouseEvent("click", {
      bubbles: true,
      cancelable: true,
    });

    button.element.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
    expect(onClick).not.toHaveBeenCalled();
    expect(button.attributes("aria-disabled")).toBe("true");
    expect(button.attributes("disabled")).toBeUndefined();
  });

  it("should work with block prop", async () => {
    const button = mount(VButton);

    // default: false
    expect(button.find(".btn").classes()).not.toContain("btn-block");

    await button.setProps({ block: true });
    expect(button.html()).toMatchSnapshot();
    expect(button.find(".btn").classes()).toContain("btn-block");
  });

  it("should work with disabled prop", async () => {
    const onClick = vi.fn(() => 1);

    // default: false
    const button = mount(VButton, {
      emits: { click: onClick() },
    });

    await button.trigger("click");
    expect(onClick).toHaveBeenCalled();

    onClick.mockReset();
    await button.setProps({ disabled: true });
    await button.trigger("click");

    expect(button.html()).toMatchSnapshot();
    expect(onClick).not.toHaveBeenCalled();
  });

  it("should work with loading prop", async function () {
    const wrapper = mount({
      data() {
        return {
          loading: true,
        };
      },
      template: `
        <v-button :loading="loading" >
          Hello
        </v-button>
      `,
      components: {
        VButton,
      },
    });

    expect(wrapper.find(".btn").classes()).toContain("btn-loading");
    expect(wrapper.find(".btn-icon").exists()).toBe(true);

    // set loading = false
    await wrapper.setData({ loading: false });
    expect(wrapper.find(".btn").classes()).not.toContain("btn-loading");
    expect(wrapper.find(".btn-icon").exists()).toBe(false);
  });

  it("should expose the loading state to assistive technology", async () => {
    const wrapper = mount(VButton, {
      props: { "aria-label": "加载中", loading: true },
    });

    expect(wrapper.get("button").attributes("aria-busy")).toBe("true");
    expect(wrapper.get("svg").attributes("aria-hidden")).toBe("true");

    await wrapper.setProps({ loading: false });

    expect(wrapper.get("button").attributes("aria-busy")).toBeUndefined();
  });

  it("should work with loading prop and icon slot", async function () {
    const wrapper = mount({
      data() {
        return {
          loading: false,
        };
      },
      template: `
        <v-button :loading="loading">
        <template #icon>
          IconSettings
        </template>
        Hello
        </v-button>
      `,
      components: {
        VButton,
        IconSettings,
      },
    });

    expect(wrapper.find(".btn-icon").exists()).toBe(true);
    expect(wrapper.find(".btn").classes()).not.toContain("btn-loading");

    await wrapper.setData({ loading: true });

    expect(wrapper.find(".btn-icon").exists()).toBe(true);
    expect(wrapper.find(".btn").classes()).toContain("btn-loading");
  });
});
