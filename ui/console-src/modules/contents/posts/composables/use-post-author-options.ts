import type { FormKitOptionsList } from "@formkit/inputs";
import {
  consoleApiClient,
  paginate,
  type ListedUser,
  type UserV1alpha1ConsoleApiListUsersRequest,
} from "@halo-dev/api-client";
import {
  computed,
  ref,
  toRef,
  toValue,
  watch,
  type MaybeRefOrGetter,
} from "vue";

const POST_AUTHOR_EFFECTIVE_ROLE = "role-template-post-contributor";
const EXCLUDED_USER_FIELD_SELECTORS = ["name!=anonymousUser", "name!=ghost"];

interface UsePostAuthorOptionsOptions {
  currentOwner?: MaybeRefOrGetter<string | undefined>;
  enabled?: MaybeRefOrGetter<boolean | undefined>;
}

export function usePostAuthorOptions(
  options: UsePostAuthorOptionsOptions = {}
) {
  const currentOwner = toRef(() =>
    options.currentOwner ? toValue(options.currentOwner) : undefined
  );
  const enabled = toRef(() =>
    options.enabled === undefined ? true : toValue(options.enabled) === true
  );

  const authorUsers = ref<ListedUser[]>([]);
  const currentOwnerUser = ref<ListedUser["user"]>();

  const authorOptions = computed(() => {
    const optionMap = new Map<string, string>();

    authorUsers.value.forEach(({ user }) => {
      addUserOption(optionMap, user);
    });

    if (currentOwnerUser.value) {
      addUserOption(optionMap, currentOwnerUser.value);
    }

    return Array.from(optionMap, ([value, label]) => ({
      label,
      value,
    })) as FormKitOptionsList;
  });

  async function fetchAuthorUsers() {
    if (!enabled.value) {
      authorUsers.value = [];
      currentOwnerUser.value = undefined;
      return;
    }

    try {
      authorUsers.value = await paginate<
        UserV1alpha1ConsoleApiListUsersRequest,
        ListedUser
      >((params) => consoleApiClient.user.listUsers(params), {
        effectiveRole: POST_AUTHOR_EFFECTIVE_ROLE,
        fieldSelector: EXCLUDED_USER_FIELD_SELECTORS,
        size: 1000,
      });
    } catch (error) {
      console.error("Failed to fetch post author candidates", error);
      authorUsers.value = [];
      currentOwnerUser.value = undefined;
      return;
    }

    await fetchCurrentOwnerUser();
  }

  async function fetchCurrentOwnerUser() {
    const owner = currentOwner.value;

    if (!enabled.value || !owner) {
      currentOwnerUser.value = undefined;
      return;
    }

    const ownerInCandidates = authorUsers.value.some(({ user }) => {
      return user.metadata.name === owner;
    });

    if (ownerInCandidates) {
      currentOwnerUser.value = undefined;
      return;
    }

    try {
      const { data } = await consoleApiClient.user.getUserDetail({
        name: owner,
      });
      currentOwnerUser.value = data.user;
    } catch (error) {
      console.error("Failed to fetch current post owner", error);
      currentOwnerUser.value = undefined;
    }
  }

  watch(
    enabled,
    () => {
      void fetchAuthorUsers();
    },
    { immediate: true }
  );
  watch(currentOwner, () => {
    void fetchCurrentOwnerUser();
  });

  return {
    authorOptions,
    refresh: fetchAuthorUsers,
  };
}

function addUserOption(
  optionMap: Map<string, string>,
  user: ListedUser["user"]
) {
  const name = user.metadata.name;
  if (!name) {
    return;
  }

  const displayName = user.spec.displayName || name;
  optionMap.set(name, `${displayName}(${name})`);
}
