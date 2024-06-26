import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VMenuLabel } from "../index";

describe("MenuLabel", () => {
  it("should render", () => {
    expect(VMenuLabel).toBeDefined();
    expect(
      mount(VMenuLabel, { slots: { default: "Hello Halo" } }).html()
    ).toMatchSnapshot();
  });
});
