import { describe, expect, it, vi } from "vitest";
import { VButton } from "../index";
import { mount } from "@vue/test-utils";
import { IconSettings } from "../../../icons/icons";

describe("Button", () => {
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
      emits: { click: onClick },
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
