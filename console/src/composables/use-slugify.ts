import { slugify } from "transliteration";
import { watch, type Ref } from "vue";
import ShortUniqueId from "short-unique-id";
// import { useGlobalInfoStore } from "@/stores/global-info";

const Strategy = {
  generateByTitle: (value: string) => {
    return slugify(value, { trim: true });
  },
  shortUUID: () => {
    const uid = new ShortUniqueId({ length: 8 });
    return uid();
  },
  UUID: () => {
    const uid = new ShortUniqueId({ length: 18 });
    return uid();
  },
  timestamp: () => {
    const uid = new ShortUniqueId();
    return uid.stamp(32);
  },
};

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

  const handleGenerateSlug = () => {
    // const globalInfo = useGlobalInfoStore().globalInfo;
    // TODO: change mock to globalinfoOption
    const mode = "UUID";
    if (!source.value) {
      return;
    }
    target.value = Strategy[mode]();
  };

  return {
    handleGenerateSlug,
  };
}
