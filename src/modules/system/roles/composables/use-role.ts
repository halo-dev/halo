import type { Role } from "@halo-dev/api-client";
import { computed, onMounted, ref } from "vue";
import { roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import { apiClient } from "@halo-dev/admin-shared";

interface RoleTemplateGroup {
  module: string | null | undefined;
  roles: Role[];
}

const initialFormState: Role = {
  apiVersion: "v1alpha1",
  kind: "Role",
  metadata: {
    name: "",
    labels: {},
    annotations: {
      [rbacAnnotations.DEPENDENCIES]: "",
      [rbacAnnotations.DISPLAY_NAME]: "",
    },
  },
  rules: [],
};

export function useRoleForm() {
  const formState = ref<Role>(initialFormState);
  const saving = ref(false);

  const isUpdateMode = computed(() => {
    return !!formState.value.metadata.creationTimestamp;
  });

  const handleCreateOrUpdate = async () => {
    try {
      saving.value = true;
      if (isUpdateMode.value) {
        await apiClient.extension.role.updatev1alpha1Role(
          formState.value.metadata.name,
          formState.value
        );
      } else {
        await apiClient.extension.role.createv1alpha1Role(formState.value);
      }
    } catch (e) {
      console.error(e);
    } finally {
      saving.value = false;
    }
  };

  return {
    formState,
    initialFormState,
    saving,
    isUpdateMode,
    handleCreateOrUpdate,
  };
}

export function useRoleTemplateSelection() {
  const rawRoles = ref<Role[]>([] as Role[]);
  const selectedRoleTemplates = ref<Set<string>>(new Set());

  // Get all role templates based on the condition that `metadata.labels.[halo.run/role-template] === 'true'`
  const roleTemplates = computed<Role[]>(() => {
    return rawRoles.value.filter(
      (role) =>
        role.metadata.labels?.[roleLabels.TEMPLATE] === "true" &&
        role.metadata.labels?.["halo.run/hidden"] !== "true"
    );
  });

  /**
   * Grouping role templates by module
   * Example:
   * {
   *   "module": "Users Management",
   *   "roles": [
   *     {
   *       "rules": [
   *         {
   *           "apiGroups": [
   *             ""
   *           ],
   *           "resources": [
   *             "users"
   *           ],
   *           "resourceNames": [],
   *           "nonResourceURLs": [],
   *           "verbs": [
   *             "create",
   *             "patch",
   *             "update",
   *             "delete",
   *             "deletecollection"
   *           ]
   *         }
   *       ],
   *       "apiVersion": "v1alpha1",
   *       "kind": "Role",
   *       "metadata": {
   *         "name": "role-template-manage-users",
   *         "labels": {
   *           "halo.run/role-template": "true"
   *         },
   *         "annotations": {
   *           "rbac.authorization.halo.run/dependencies": "[ \"role-template-view-users\", \"role-template-change-password\" ]\n",
   *           "rbac.authorization.halo.run/module": "Users Management",
   *           "rbac.authorization.halo.run/display-name": "User manage",
   *           "rbac.authorization.halo.run/ui-permissions": "[\"system:users:manage\"]\n",
   *           "rbac.authorization.halo.run/dependency-rules": "[{\"apiGroups\":[\"\"],\"resources\":[\"users\"],\"resourceNames\":[],\"nonResourceURLs\":[],\"verbs\":[\"get\",\"list\"]},{\"apiGroups\":[\"api.halo.run\"],\"resources\":[\"users/password\"],\"resourceNames\":[],\"nonResourceURLs\":[],\"verbs\":[\"update\"]}]",
   *           "rbac.authorization.halo.run/ui-permissions-aggregated": "[\"system:users:view\"]"
   *         },
   *         "version": 9
   *       }
   *     },
   *     {
   *       "rules": [
   *         {
   *           "apiGroups": [
   *             ""
   *           ],
   *           "resources": [
   *             "users"
   *           ],
   *           "resourceNames": [],
   *           "nonResourceURLs": [],
   *           "verbs": [
   *             "get",
   *             "list"
   *           ]
   *         }
   *       ],
   *       "apiVersion": "v1alpha1",
   *       "kind": "Role",
   *       "metadata": {
   *         "name": "role-template-view-users",
   *         "labels": {
   *           "halo.run/role-template": "true"
   *         },
   *         "annotations": {
   *           "rbac.authorization.halo.run/module": "Users Management",
   *           "rbac.authorization.halo.run/display-name": "User View",
   *           "rbac.authorization.halo.run/ui-permissions": "[\"system:users:view\"]\n",
   *           "rbac.authorization.halo.run/dependency-rules": "[]",
   *           "rbac.authorization.halo.run/ui-permissions-aggregated": "[]"
   *         },
   *         "version": 9
   *       }
   *     }
   *   ]
   * }
   */
  const roleTemplateGroups = computed<RoleTemplateGroup[]>(() => {
    const groups: RoleTemplateGroup[] = [];
    roleTemplates.value.forEach((role) => {
      const group = groups.find(
        (group) =>
          group.module === role.metadata.annotations?.[rbacAnnotations.MODULE]
      );
      if (group) {
        group.roles.push(role);
      } else {
        groups.push({
          module: role.metadata.annotations?.[rbacAnnotations.MODULE],
          roles: [role],
        });
      }
    });
    return groups;
  });

  const handleFetchRoles = async () => {
    try {
      const { data } = await apiClient.extension.role.listv1alpha1Role();
      rawRoles.value = data.items;
    } catch (e) {
      console.error(e);
    }
  };

  const handleRoleTemplateSelect = async (e: Event) => {
    const { checked, value } = e.target as HTMLInputElement;
    if (!checked) {
      return;
    }
    const role = rawRoles.value.find((role) => role.metadata.name === value);
    const dependencies =
      role?.metadata.annotations?.[rbacAnnotations.DEPENDENCIES];
    if (!dependencies) {
      return;
    }
    const dependenciesArray = JSON.parse(dependencies);
    dependenciesArray.forEach((role) => {
      selectedRoleTemplates.value.add(role);
    });
  };

  onMounted(handleFetchRoles);

  return {
    rawRoles,
    selectedRoleTemplates,
    roleTemplates,
    roleTemplateGroups,
    handleRoleTemplateSelect,
  };
}
