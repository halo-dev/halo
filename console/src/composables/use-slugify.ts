import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
import ShortUniqueId from "short-unique-id";
// import { useGlobalInfoStore } from "@/stores/global-info";

const Strategy = {
  generateByTitle: (value: string) => {
    return slugify(value, { trim: true });
  },
  shortUUID: (value: string) => {
    if (!value) return;
    const uid = new ShortUniqueId({ length: 8 });
    return uid();
  },
  UUID: (value: string) => {
    if (!value) return;
    const uid = new ShortUniqueId({ length: 18 });
    return uid();
  },
  timestamp: (value: string) => {
    if (!value) return;
    const uid = new ShortUniqueId();
    return uid.stamp(32);
  },
};
const onceList = ["shortUUID", "UUID", "timestamp"];
export default function useSlugify(
  source: Ref<string>,
  target: Ref<string>,
  auto: Ref<boolean>
) {
  watch(
    () => source.value,
    () => {
      if (auto.value) {
        handleGenerateSlug();
      }
    }
  );

  const handleGenerateSlug = (forceUpdate = false) => {
    // const globalInfo = useGlobalInfoStore().globalInfo;
    // TODO: change mock to globalinfoOption
    const mode = "generateByTitle";
    if (forceUpdate) {
      target.value = Strategy[mode](source.value);
      return;
    }
    if (onceList.includes(mode) && target.value) return;
    target.value = Strategy[mode](source.value);
  };

  return {
    handleGenerateSlug,
  };
}
