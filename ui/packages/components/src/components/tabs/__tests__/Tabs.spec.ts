import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { h } from "vue";
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
});
