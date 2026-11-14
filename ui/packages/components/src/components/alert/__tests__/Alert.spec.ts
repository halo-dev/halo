import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VAlert } from "../index";

describe("Alert", () => {
  it("should render", () => {
    expect(VAlert).toBeDefined();
  });

  it("closes from the keyboard", async () => {
    const wrapper = mount(VAlert, { props: { title: "Hello Halo" } });
    const close = wrapper.get(".alert-close");

    expect(close.attributes("role")).toBe("button");
    expect(close.attributes("aria-label")).toBe("Close");

    await close.trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("close")).toHaveLength(1);
  });
});
