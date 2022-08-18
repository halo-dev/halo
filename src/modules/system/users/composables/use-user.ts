import type { Ref } from "vue";
import { onMounted, ref } from "vue";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";

interface useUserFetchReturn {
  users: Ref<User[]>;
  loading: Ref<boolean>;
  handleFetchUsers: () => void;
}

export function useUserFetch(): useUserFetchReturn {
  const users = ref<User[]>([] as User[]);
  const loading = ref(false);

  const handleFetchUsers = async () => {
    try {
      loading.value = true;
      const { data } = await apiClient.extension.user.listv1alpha1User();
      users.value = data.items;
    } catch (e) {
      console.error("Failed to fetch users", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(handleFetchUsers);

  return {
    users,
    loading,
    handleFetchUsers,
  };
}
