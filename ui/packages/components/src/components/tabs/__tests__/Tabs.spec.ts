import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { Fragment, defineComponent, h, markRaw } from "vue";
import { VTabItem, VTabs } from "../index";

describe("Tabs", () => {
  it("should render", () => {
    expect(VTabs).toBeDefined();
  });

  it("associates each tab with its active panel", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
          h(
            VTabItem,
            { id: "advanced", label: "Advanced" },
            () => "Advanced panel"
          ),
        ],
      },
    });

    const tabs = wrapper.findAll('[role="tab"]');
    expect(tabs).toHaveLength(2);
    expect(tabs[0].attributes("id")).toBeTruthy();
    expect(tabs[0].attributes("aria-controls")).toBeTruthy();
    expect(tabs[0].attributes("id")).not.toBe(tabs[1].attributes("id"));
    expect(tabs[0].attributes("aria-controls")).not.toBe(
      tabs[1].attributes("aria-controls")
    );

    const panel = wrapper.get('[role="tabpanel"]');
    expect(panel.attributes("id")).toBe(tabs[0].attributes("aria-controls"));
    expect(panel.attributes("aria-labelledby")).toBe(tabs[0].attributes("id"));
  });

  it("associates the newly active tab with its panel", async () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
          h(
            VTabItem,
            { id: "advanced", label: "Advanced" },
            () => "Advanced panel"
          ),
        ],
      },
    });

    await wrapper.setProps({ activeId: "advanced" });

    const tabs = wrapper.findAll('[role="tab"]');
    const panel = wrapper.get('[role="tabpanel"]');
    expect(panel.attributes("id")).toBe(tabs[1].attributes("aria-controls"));
    expect(panel.attributes("aria-labelledby")).toBe(tabs[1].attributes("id"));
  });

  it("associates a tab and panel when the id is an empty string", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "" },
      slots: {
        default: () => [
          h(VTabItem, { id: "", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    const tab = wrapper.get('[role="tab"]');
    const panel = wrapper.get('[role="tabpanel"]');
    expect(panel.attributes("id")).toBe(tab.attributes("aria-controls"));
    expect(panel.attributes("aria-labelledby")).toBe(tab.attributes("id"));
  });

  it("collects tab items rendered by v-for", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(
            Fragment,
            null,
            ["basic", "advanced"].map((id) =>
              h(VTabItem, { id, label: id }, () => `${id} panel`)
            )
          ),
        ],
      },
    });

    expect(wrapper.findAll('[role="tab"]')).toHaveLength(2);
    expect(wrapper.findAll('[role="tab"]')[1].text()).toBe("advanced");
  });

  it("falls back to the first enabled tab when activeId does not match", async () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "missing" },
      slots: {
        default: () => [
          h(
            VTabItem,
            { id: "disabled", label: "Disabled", disabled: true },
            () => "Disabled panel"
          ),
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    await flushPromises();

    expect(wrapper.emitted("update:activeId")?.at(-1)).toEqual(["basic"]);
    expect(wrapper.get('[role="tabpanel"]').text()).toBe("Basic panel");
    expect(wrapper.findAll('[role="tab"]')[0].attributes("aria-disabled")).toBe(
      "true"
    );
  });

  it("keeps persistent panels mounted while hidden", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
          h(
            VTabItem,
            { id: "advanced", label: "Advanced", persistent: true },
            () => "Advanced panel"
          ),
        ],
      },
    });

    const panels = wrapper.findAll('[role="tabpanel"]');
    expect(panels).toHaveLength(2);
    expect(panels[1].attributes("style")).toContain("display: none");
  });

  it("passes icons through to the tab bar", () => {
    const icon = defineComponent({
      render: () => h("i", { class: "test-icon" }),
    });

    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(
            VTabItem,
            { id: "basic", label: "Basic", icon: markRaw(icon) },
            () => "Basic panel"
          ),
        ],
      },
    });

    expect(wrapper.find(".tabbar-item-icon .test-icon").exists()).toBe(true);
  });

  it("makes the panel focusable and labels the tab list", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    expect(wrapper.get('[role="tabpanel"]').attributes("tabindex")).toBe("0");
    expect(wrapper.get('[role="tablist"]').attributes("aria-label")).toBe(
      "Tab list"
    );
  });

  it("passes the flush form through to the tab bar", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic", flush: true },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    expect(wrapper.get(".tabbar-wrapper").classes()).toContain("is-flush");
  });

  it("passes the size and stretch options through to the tab bar", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic", size: "sm", stretch: true },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    const tabbar = wrapper.get(".tabbar-wrapper");
    expect(tabbar.classes()).toContain("tabbar-size-sm");
    expect(tabbar.classes()).toContain("is-stretch");
  });

  it("uses a custom label for the tab list", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic", ariaLabel: "订单状态" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    expect(wrapper.get('[role="tablist"]').attributes("aria-label")).toBe(
      "订单状态"
    );
  });

  it("passes the centered, divider and background options to the tab bar", () => {
    const wrapper = mount(VTabs, {
      props: {
        activeId: "basic",
        centered: true,
        divider: false,
        background: false,
        type: "outline",
      },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
      },
    });

    const tabbar = wrapper.get(".tabbar-wrapper");
    expect(tabbar.classes()).toContain("is-centered");
    expect(tabbar.classes()).toContain("is-dividerless");
    expect(tabbar.classes()).toContain("is-backgroundless");
  });

  it("renders the left and right slots around the tab bar", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
        ],
        left: () => h("button", { class: "left-extra" }, "Left"),
        right: () => h("button", { class: "right-extra" }, "Right"),
      },
    });

    expect(wrapper.get(".tabbar-extra-left .left-extra").text()).toBe("Left");
    expect(wrapper.get(".tabbar-extra-right .right-extra").text()).toBe(
      "Right"
    );
    expect(wrapper.findAll('[role="tab"]')).toHaveLength(1);
  });

  it("treats a bare disabled attribute as disabled", () => {
    const wrapper = mount(VTabs, {
      props: { activeId: "basic" },
      slots: {
        default: () => [
          h(VTabItem, { id: "basic", label: "Basic" }, () => "Basic panel"),
          h(
            VTabItem,
            {
              id: "advanced",
              label: "Advanced",
              disabled: "" as unknown as boolean,
            },
            () => "Advanced panel"
          ),
        ],
      },
    });

    expect(wrapper.findAll('[role="tab"]')[1].attributes("aria-disabled")).toBe(
      "true"
    );
  });
});
