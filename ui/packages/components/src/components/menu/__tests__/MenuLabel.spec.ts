import { describe, expect, it } from "vitest";
import { VMenuLabel } from "../index";
import { mount } from "@vue/test-utils";

describe("MenuLabel", () => {
  it("should render", () => {
    expect(VMenuLabel).toBeDefined();
    expect(
      mount(VMenuLabel, { slots: { default: "Hello Halo" } }).html()
    ).toMatchSnapshot();
  });
});
