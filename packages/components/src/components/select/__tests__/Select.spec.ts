import { describe, expect, it } from "vitest";
import { VSelect } from "../index";
import { mount } from "@vue/test-utils";

describe("Select", () => {
  it("should render", () => {
    expect(VSelect).toBeDefined();
    expect(mount(VSelect).html()).toMatchSnapshot();
  });

  it("should work with size prop", function () {
    ["lg", "md", "sm", "xs"].forEach((size) => {
      const select = mount(VSelect, { props: { size } });

      expect(select.html()).toMatchSnapshot();
      expect(select.find("select").classes()).toContain(`select-${size}`);
      select.unmount();
    });
  });
});
