import { mount } from "@vue/test-utils";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { setComponentsLocale } from "../../../locales";
import { VDialog } from "../index";

afterEach(() => {
  setComponentsLocale("en");
});

describe("Dialog", () => {
  it("should render", () => {
    expect(mount(VDialog)).toBeDefined();
  });

  it("labels the modal with the dialog title", () => {
    const wrapper = mount(VDialog, {
      props: { visible: true, title: "Delete post" },
    });

    expect(wrapper.get(".modal-wrapper").attributes("aria-label")).toBe(
      "Delete post"
    );
  });

  it("falls back to localized confirm and cancel labels", () => {
    setComponentsLocale("zh-CN");
    const wrapper = mount(VDialog, {
      props: { visible: true },
    });

    const buttons = wrapper.findAll("button");

    expect(buttons[0].text()).toBe("确定");
    expect(buttons[1].text()).toBe("取消");
  });
});
