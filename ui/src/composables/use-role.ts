import type { Role } from "@halo-dev/api-client";
import {
  computed,
  type ComputedRef,
  onMounted,
  onUnmounted,
  type Ref,
  ref,
} from "vue";
import { roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import { apiClient } from "@/utils/api-client";
import { Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";
import { resolveDeepDependencies } from "@/utils/role";

interface RoleTemplateGroup {
  module: string | null | undefined;
  roles: Role[];
}

interface useFetchRoleReturn {
  roles: Ref<Role[]>;
  loading: Ref<boolean>;
  handleFetchRoles: () => void;
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
 * Fetch all roles(without role templates) from the API.
 *
 * @returns {useFetchRoleReturn}
 */
export function useFetchRole(): useFetchRoleReturn {
  const roles = ref<Role[]>([]);
  const loading = ref(false);
  const refreshInterval = ref();

  const handleFetchRoles = async (options?: { mute?: boolean }) => {
    try {
      clearInterval(refreshInterval.value);

      if (!options?.mute) {
        loading.value = true;
      }

      const { data } = await apiClient.extension.role.listV1alpha1Role({
        page: 0,
        size: 0,
        labelSelector: [`!${roleLabels.TEMPLATE}`],
      });
      roles.value = data.items;

      const deletedRoles = roles.value.filter(
        (role) => !!role.metadata.deletionTimestamp
      );

      if (deletedRoles.length) {
        refreshInterval.value = setInterval(() => {
          handleFetchRoles({ mute: true });
        }, 1000);
      }
    } catch (e) {
      console.error("Failed to fetch roles", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(handleFetchRoles);

  onUnmounted(() => {
    clearInterval(refreshInterval.value);
  });

  return {
    roles,
    loading,
    handleFetchRoles,
  };
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
        const { data } = await apiClient.extension.role.updateV1alpha1Role({
          name: formState.value.metadata.name,
          role: formState.value,
        });

        formState.value = data;
      } else {
        const { data } = await apiClient.extension.role.createV1alpha1Role({
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
