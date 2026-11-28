import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import SelectOption from "../SelectOption.vue";

describe("select option keyboard", () => {
  it("lets buttons keep their native Enter behavior", () => {
    const wrapper = mount(SelectOption, {
      props: {
        options: [{ label: "Option 1", value: "1" }],
        multiple: false,
        maxCount: NaN,
        nextLoading: false,
      },
    });
    const button = document.createElement("button");
    document.body.appendChild(button);

    button.dispatchEvent(
      new KeyboardEvent("keydown", { key: "Enter", bubbles: true })
    );

    expect(wrapper.emitted("selected")).toBeUndefined();
    button.remove();
    wrapper.unmount();
  });
});
