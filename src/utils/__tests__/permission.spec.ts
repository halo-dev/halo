import { describe, expect, it } from "vitest";
import { hasPermission } from "../permission";

describe("hasPermission", () => {
  it("should return true if user has permission", () => {
    const uiPermissions = ["system:post:manage", "system:post:view"];

    expect(hasPermission(uiPermissions, ["system:post:manage"], false)).toBe(
      true
    );
    expect(hasPermission(uiPermissions, ["system:post:view"], false)).toBe(
      true
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

    // super admin has all permissions
    expect(hasPermission(["*"], ["system:post:manage"], false)).toBe(true);
    expect(
      hasPermission(["*"], ["system:post:manage", "system:links:manage"], true)
    ).toBe(true);
  });
});
