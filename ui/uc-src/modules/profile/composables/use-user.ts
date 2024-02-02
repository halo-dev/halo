import type { Ref } from "vue";
import { onMounted, ref } from "vue";
import type { User } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";

interface useUserFetchReturn {
  users: Ref<User[]>;
  loading: Ref<boolean>;
  handleFetchUsers: () => void;
}

export function useUserFetch(options?: {
  fetchOnMounted: boolean;
}): useUserFetchReturn {
  const { fetchOnMounted } = options || {};

  const users = ref<User[]>([] as User[]);
  const loading = ref(false);

  const ANONYMOUSUSER_NAME = "anonymousUser";

  const handleFetchUsers = async () => {
    try {
      loading.value = true;
      const { data } = await apiClient.extension.user.listv1alpha1User({
        fieldSelector: [`name!=${ANONYMOUSUSER_NAME}`],
      });
      users.value = data.items;
    } catch (e) {
      console.error("Failed to fetch users", e);
    } finally {
      loading.value = false;
    }
  };

  onMounted(() => {
    fetchOnMounted && handleFetchUsers();
  });

  return {
    users,
    loading,
    handleFetchUsers,
  };
}
