import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vite-plus/test";
import { VDialog } from "../index";

describe("Dialog", () => {
  it("should render", () => {
    expect(mount(VDialog)).toBeDefined();
  });
});
