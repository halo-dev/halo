import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { VDialog } from "../index";

describe("Dialog", () => {
  it("should render", () => {
    expect(mount(VDialog)).toBeDefined();
  });
});
