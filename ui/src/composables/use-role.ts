import { rbacAnnotations } from "@/constants/annotations";
import { resolveDeepDependencies } from "@/utils/role";
import type { Role } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { computed, ref, type ComputedRef, type Ref } from "vue";
import { useI18n } from "vue-i18n";

interface RoleTemplateGroup {
  module: string | null | undefined;
  roles: Role[];
}

interface useRoleFormReturn {
  formState: Ref<Role>;
  isSubmitting: Ref<boolean>;
  isUpdateMode: ComputedRef<boolean>;
  handleCreateOrUpdate: () => Promise<void>;
}

interface useRoleTemplateSelectionReturn {
  selectedRoleTemplates: Ref<Set<string>>;
  roleTemplateGroups: ComputedRef<RoleTemplateGroup[]>;
  handleRoleTemplateSelect: (e: Event) => void;
}

/**
 * Create or update a role.
 *
 * @returns {useRoleFormReturn}
 */
export function useRoleForm(): useRoleFormReturn {
  const { t } = useI18n();

  const formState = ref<Role>({
    apiVersion: "v1alpha1",
    kind: "Role",
    metadata: {
      name: "",
      generateName: "role-",
      labels: {},
      annotations: {
        [rbacAnnotations.DEPENDENCIES]: "",
        [rbacAnnotations.DISPLAY_NAME]: "",
      },
    },
    rules: [],
  });
  const isSubmitting = ref(false);

  const isUpdateMode = computed(() => {
    return !!formState.value.metadata.creationTimestamp;
  });

  const handleCreateOrUpdate = async () => {
    try {
      isSubmitting.value = true;
      if (isUpdateMode.value) {
        const { data } = await coreApiClient.role.updateRole({
          name: formState.value.metadata.name,
          role: formState.value,
        });

        formState.value = data;
      } else {
        const { data } = await coreApiClient.role.createRole({
          role: formState.value,
        });

        formState.value = data;
      }

      Toast.success(t("core.common.toast.save_success"));
    } catch (e) {
      console.error(e);
    } finally {
      isSubmitting.value = false;
    }
  };

  return {
    formState,
    isSubmitting,
    isUpdateMode,
    handleCreateOrUpdate,
  };
}

/**
 * Logic for selecting role templates
 *
 * @returns {useRoleTemplateSelectionReturn}
 */
export function useRoleTemplateSelection(
  roleTemplates: Ref<Role[] | undefined>
): useRoleTemplateSelectionReturn {
  const selectedRoleTemplates = ref<Set<string>>(new Set());

  /**
   * Grouping role templates by module <br />
   * Example:
   * ```json
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
   * ```
   */
  const roleTemplateGroups = computed<RoleTemplateGroup[]>(() => {
    const groups: RoleTemplateGroup[] = [];
    roleTemplates.value?.forEach((role) => {
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

  const handleRoleTemplateSelect = async (e: Event) => {
    const { checked, value } = e.target as HTMLInputElement;
    if (!checked) {
      return;
    }
    const role = roleTemplates.value?.find(
      (role) => role.metadata.name === value
    );

    if (!role) {
      return;
    }

    selectedRoleTemplates.value.add(role.metadata.name);
    resolveDeepDependencies(role, roleTemplates.value || []).forEach((name) => {
      selectedRoleTemplates.value.add(name);
    });
  };

  return {
    selectedRoleTemplates,
    roleTemplateGroups,
    handleRoleTemplateSelect,
  };
}
