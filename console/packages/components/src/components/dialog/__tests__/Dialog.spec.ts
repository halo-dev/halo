import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { VDialog } from "../index";

describe("Dialog", () => {
  it("should render", () => {
    expect(mount(VDialog)).toBeDefined();
  });
});
