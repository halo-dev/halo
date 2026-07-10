import type { Role } from "@halo-dev/api-client";
import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";

export function isConsoleAccessDisallowed(roles?: Role[]): boolean {
  if (roles?.some((role) => role.metadata.name === SUPER_ROLE_NAME)) {
    return false;
  }

  return (
    !!roles?.length &&
    roles.every(
      (role) =>
        role.metadata.annotations?.[rbacAnnotations.DISALLOW_ACCESS_CONSOLE] ===
        "true"
    )
  );
}

export function resolveDeepDependencies(
  role: Role,
  roleTemplates: Role[]
): string[] {
  if (!role) {
    return [];
  }

  const result: string[] = [];

  const dependencies: string[] = JSON.parse(
    role.metadata.annotations?.[rbacAnnotations.DEPENDENCIES] || "[]"
  );

  dependencies.forEach((depName) => {
    result.push(depName);
    const dep = roleTemplates.find((item) => item.metadata.name === depName);

    if (!dep) {
      return;
    }

    resolveDeepDependencies(dep, roleTemplates).forEach((nextDep) =>
      result.push(nextDep)
    );
  });

  return result;
}
