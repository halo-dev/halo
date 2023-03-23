import type { Role } from "@halo-dev/api-client";
import { onUnmounted, type ComputedRef, type Ref } from "vue";
import { computed, onMounted, ref } from "vue";
import { roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";
import { apiClient } from "@/utils/api-client";
import { Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";

interface RoleTemplateGroup {
  module: string | null | undefined;
  roles: Role[];
}

const initialFormState: Role = {
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
};

interface useFetchRoleReturn {
  roles: Ref<Role[]>;
  loading: Ref<boolean>;
  handleFetchRoles: () => void;
}

interface useRoleFormReturn {
  formState: Ref<Role>;
  initialFormState: Role;
  saving: Ref<boolean>;
  isUpdateMode: ComputedRef<boolean>;
  handleCreateOrUpdate: () => void;
}

interface useRoleTemplateSelectionReturn {
  selectedRoleTemplates: Ref<Set<string>>;
  roleTemplates: Ref<Role[]>;
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

      const { data } = await apiClient.extension.role.listv1alpha1Role({
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
        }, 3000);
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

  const formState = ref<Role>(initialFormState);
  const saving = ref(false);

  const isUpdateMode = computed(() => {
    return !!formState.value.metadata.creationTimestamp;
  });

  const handleCreateOrUpdate = async () => {
    try {
      saving.value = true;
      if (isUpdateMode.value) {
        const { data } = await apiClient.extension.role.updatev1alpha1Role({
          name: formState.value.metadata.name,
          role: formState.value,
        });

        formState.value = data;
      } else {
        const { data } = await apiClient.extension.role.createv1alpha1Role({
          role: formState.value,
        });

        formState.value = data;
      }

      Toast.success(t("core.common.toast.save_success"));
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

/**
 * Logic for selecting role templates
 *
 * @returns {useRoleTemplateSelectionReturn}
 */
export function useRoleTemplateSelection(): useRoleTemplateSelectionReturn {
  const roleTemplates = ref<Role[]>([] as Role[]);
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

  /**
   * Get all role templates based on the condition that `metadata.labels.[halo.run/role-template] = 'true'` and `!halo.run/hidden`
   */
  const handleFetchRoles = async () => {
    try {
      const { data } = await apiClient.extension.role.listv1alpha1Role({
        page: 0,
        size: 0,
        labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
      });
      roleTemplates.value = data.items;
    } catch (e) {
      console.error(e);
    }
  };

  const handleRoleTemplateSelect = async (e: Event) => {
    const { checked, value } = e.target as HTMLInputElement;
    if (!checked) {
      return;
    }
    const role = roleTemplates.value.find(
      (role) => role.metadata.name === value
    );
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
    selectedRoleTemplates,
    roleTemplates,
    roleTemplateGroups,
    handleRoleTemplateSelect,
  };
}
