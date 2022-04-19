import { describe, expect, it } from "vitest";
import { VInput } from "../index";
import { mount } from "@vue/test-utils";

describe("Input", () => {
  it("should render", () => {
    expect(VInput).toBeDefined();
    expect(mount(VInput).html()).toMatchSnapshot();
  });

  it("should work with size prop", function () {
    ["lg", "md", "sm", "xs"].forEach((size) => {
      const input = mount(VInput, { props: { size } });

      expect(input.html()).toMatchSnapshot();
      expect(input.find("input").classes()).toContain(`input-${size}`);
      input.unmount();
    });
  });
});
