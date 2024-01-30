import { rbacAnnotations } from "@/constants/annotations";
import type { Role } from "@halo-dev/api-client";

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
