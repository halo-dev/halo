import type { Ref } from "vue";

export const SNAPSHOTS_QUERY_KEY = (
  cacheKey: Ref<string>,
  name?: Ref<string>
) => [`core:${cacheKey}:snapshots`, name];

export const SNAPSHOT_QUERY_KEY = (
  cacheKey: Ref<string>,
  name?: Ref<string>,
  snapshotNames?: Ref<string[]>
) => [`core:${cacheKey}:snapshot`, name, snapshotNames];

export const SNAPSHOT_DIFF_QUERY_KEY = (
  cacheKey: Ref<string>,
  name?: Ref<string>,
  snapshotNames?: Ref<string[]>
) => [`core:${cacheKey}:snapshot-diff`, name, snapshotNames];
