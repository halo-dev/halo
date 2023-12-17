import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VEntityField } from "..";

describe("EntityField", () => {
  it("should render", () => {
    expect(mount(VEntityField)).toBeDefined();
  });
});
