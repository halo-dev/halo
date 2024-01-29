import { useUserStore } from "@/stores/user";
import { useQuery } from "@tanstack/vue-query";
import { computed } from "vue";

export function useSessionKeepAlive() {
  const { isAnonymous } = useUserStore();

  useQuery({
    queryKey: ["health", "keep-session-alive"],
    queryFn: () => fetch("/actuator/health"),
    refetchInterval: 1000 * 60 * 5, // 5 minutes
    refetchIntervalInBackground: true,
    refetchOnWindowFocus: true,
    enabled: computed(() => !isAnonymous),
  });
}
