import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VDialog } from "../index";

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
});
