import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VEntityField } from "..";

describe("EntityField", () => {
  it("should render", () => {
    expect(mount(VEntityField)).toBeDefined();
  });
});
