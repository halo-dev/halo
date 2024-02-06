import { describe, expect, it } from "vitest";
import { VSwitch } from "../index";
import { mount } from "@vue/test-utils";

describe("Switch", () => {
  it("should render", () => {
    expect(mount(VSwitch)).toBeDefined();
  });
});
