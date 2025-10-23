import { stores } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import { computed } from "vue";

export function useSessionKeepAlive() {
  const { isAnonymous } = stores.currentUser();

  useQuery({
    queryKey: ["health", "keep-session-alive"],
    queryFn: () => fetch("/actuator/health"),
    refetchInterval: 1000 * 60 * 5, // 5 minutes
    refetchIntervalInBackground: true,
    refetchOnWindowFocus: true,
    enabled: computed(() => !isAnonymous),
  });
}
