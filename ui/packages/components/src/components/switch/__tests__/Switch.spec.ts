import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VSwitch } from "../index";

describe("Switch", () => {
  it("should render", () => {
    expect(mount(VSwitch)).toBeDefined();
  });
});
