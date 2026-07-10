import type { Role } from "@halo-dev/api-client";
import { describe, expect, it } from "vite-plus/test";
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { isConsoleAccessDisallowed } from "../role";

function createRole(name: string, disallowAccessConsole = false): Role {
  return {
    apiVersion: "v1alpha1",
    kind: "Role",
    metadata: {
      name,
      annotations: disallowAccessConsole
        ? { [rbacAnnotations.DISALLOW_ACCESS_CONSOLE]: "true" }
        : {},
    },
    rules: [],
  };
}

describe("isConsoleAccessDisallowed", () => {
  it("does not disallow access when roles are missing", () => {
    expect(isConsoleAccessDisallowed()).toBe(false);
    expect(isConsoleAccessDisallowed([])).toBe(false);
  });

  it("disallows access when every role disallows Console access", () => {
    expect(isConsoleAccessDisallowed([createRole("post-author", true)])).toBe(
      true
    );
    expect(
      isConsoleAccessDisallowed([
        createRole("post-author", true),
        createRole("post-contributor", true),
      ])
    ).toBe(true);
  });

  it("allows access when any role allows Console access", () => {
    expect(isConsoleAccessDisallowed([createRole("post-editor")])).toBe(false);
    expect(
      isConsoleAccessDisallowed([
        createRole("post-editor"),
        createRole("post-author", true),
      ])
    ).toBe(false);
  });

  it("allows access for the super role", () => {
    expect(
      isConsoleAccessDisallowed([
        createRole(SUPER_ROLE_NAME),
        createRole("post-author", true),
      ])
    ).toBe(false);
  });
});
