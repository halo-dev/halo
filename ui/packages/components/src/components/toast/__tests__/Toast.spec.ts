import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import Toast from "../Toast.vue";

describe("Toast", () => {
  it("announces non-error messages politely", () => {
    const wrapper = mount(Toast, { props: { content: "Saved" } });
    const toast = wrapper.get(".toast-wrapper");

    expect(toast.attributes("role")).toBe("status");
    expect(toast.attributes("aria-live")).toBe("polite");
    expect(toast.attributes("aria-atomic")).toBe("true");
  });

  it("announces errors assertively", () => {
    const wrapper = mount(Toast, {
      props: { content: "Failed", type: "error" },
    });
    const toast = wrapper.get(".toast-wrapper");

    expect(toast.attributes("role")).toBe("alert");
    expect(toast.attributes("aria-live")).toBe("assertive");
  });

  it("exposes the close control as a keyboard-operable button", async () => {
    const wrapper = mount(Toast, { props: { content: "Saved" } });
    const close = wrapper.get('[role="button"][aria-label="Close"]');

    expect(close.attributes("tabindex")).toBe("0");

    await close.trigger("keydown", { key: "Enter" });

    expect(wrapper.emitted("close")).toHaveLength(1);
  });
});
