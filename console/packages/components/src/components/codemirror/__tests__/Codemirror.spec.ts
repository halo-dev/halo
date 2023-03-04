import { describe, expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import { VCodemirror } from "../index";

describe("Codemirror", () => {
  it("should render", () => {
    expect(mount(VCodemirror)).toBeDefined();
  });
});
