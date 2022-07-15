import { describe, expect, it } from "vitest";
import { hasPermission } from "../permission";

describe("hasPermission", () => {
  it("should return true if user has permission", () => {
    const uiPermissions = ["system:post:manage", "system:post:view"];
    expect(hasPermission(uiPermissions, ["*"], false)).toBe(true);
    expect(hasPermission(uiPermissions, ["*"], true)).toBe(true);
    expect(hasPermission(uiPermissions, ["system:post:manage"], false)).toBe(
      false
    );
    expect(hasPermission(uiPermissions, ["system:post:view"], false)).toBe(
      false
    );
    expect(hasPermission(uiPermissions, ["system:post:view"], true)).toBe(true);
    expect(
      hasPermission(
        uiPermissions,
        ["system:post:manage", "system:post:view"],
        true
      )
    ).toBe(true);
    expect(
      hasPermission(
        uiPermissions,
        ["system:post:manage", "system:post:view"],
        false
      )
    ).toBe(true);
  });
});
