import type { User } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { Ref } from "vue";
import { onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

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
      const { data } = await coreApiClient.user.listUser({
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
    if (fetchOnMounted) {
      handleFetchUsers();
    }
  });

  return {
    users,
    loading,
    handleFetchUsers,
  };
}

export function useUserEnableDisable() {
  const { t } = useI18n();

  const handleEnableOrDisableUser = ({
    name,
    operation,
    onSuccess,
  }: {
    name: string;
    operation: "enable" | "disable";
    onSuccess?: () => void;
  }) => {
    const operations = {
      enable: {
        title: t("core.user.operations.enable.title"),
        description: t("core.user.operations.enable.description"),
        value: false,
      },
      disable: {
        title: t("core.user.operations.disable.title"),
        description: t("core.user.operations.disable.description"),
        value: true,
      },
    };

    const operationConfig = operations[operation];

    Dialog.warning({
      title: operationConfig.title,
      description: operationConfig.description,
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          if (operation == "enable") {
            await consoleApiClient.user.enableUser({
              username: name,
            });
          } else {
            await consoleApiClient.user.disableUser({
              username: name,
            });
          }

          Toast.success(t("core.common.toast.operation_success"));
          onSuccess?.();
        } catch (e) {
          console.error("Failed to enable or disable user", e);
        }
      },
    });
  };

  return {
    handleEnableOrDisableUser,
  };
}
