<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { useDebounceFn } from "@vueuse/core";
import { useFuse } from "@vueuse/integrations/useFuse";
import type { AxiosRequestConfig } from "axios";
import { get, has } from "es-toolkit/compat";
// TODO: remove lodash-es dependency in the future
import { type PropertyPath } from "lodash-es";
import {
  computed,
  onMounted,
  ref,
  shallowReactive,
  shallowRef,
  watch,
  type PropType,
} from "vue";
import { isFalse } from "./isFalse";
import { mapItemsToSelectOptions } from "./option-utils";
import SelectContainer from "./SelectContainer.vue";
import type {
  SelectActionRequest,
  SelectOption,
  SelectRemoteOption,
  SelectRemoteRequest,
  SelectResponse,
} from "./types";

interface SelectProps {
  /**
   * URL for asynchronous requests.
   */
  action?: string;

  /**
   * Configuration for asynchronous requests.
   */
  requestOption?: SelectActionRequest;

  /**
   * Enables remote search, controlled by `remoteOption` when enabled.
   * Differs from `action`, which controls the asynchronous request options.
   */
  remote?: boolean;

  /**
   * Configuration for remote search, required when `remote` is true.
   */
  remoteOption?: SelectRemoteOption;

  /**
   * Enables remote search optimization, default is true.
   */
  remoteOptimize?: boolean;

  /**
   * Allows the creation of new options, only available in local mode. Default is false.
   */
  allowCreate?: boolean;

  /**
   * Allows options to be cleared.
   */
  clearable?: boolean;

  /**
   * Enables multiple selection, default is false.
   */
  multiple?: boolean;

  /**
   * Maximum number of selections allowed in multiple mode, valid only when `multiple` is true.
   */
  maxCount?: number;

  /**
   * Allows sorting in multiple selection mode, default is true. Only valid when `multiple` is true.
   */
  sortable?: boolean;

  /**
   * Default placeholder text.
   */
  placeholder?: string;

  /**
   * Whether to enable search, default is false.
   */
  searchable?: boolean;

  /**
   * Whether to automatically select the first option. default is true.
   *
   * Only valid when `multiple` is false.
   */
  autoSelect?: boolean;
}

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const options = shallowRef<SelectOption[] | undefined>(undefined);
const selectOptions = shallowRef<SelectOption[] | undefined>(undefined);

const selectProps: SelectProps = shallowReactive({
  multiple: false,
  maxCount: NaN,
  sortable: true,
  placeholder: "",
});

const isRemote = computed(() => !!selectProps.action || !!selectProps.remote);
const hasMoreOptions = computed(
  () => options.value && options.value.length < total.value
);

const initSelectProps = () => {
  const nodeProps = props.context.node.props;
  selectProps.maxCount = nodeProps.maxCount ?? NaN;
  selectProps.placeholder = nodeProps.placeholder ?? "";
  selectProps.action = nodeProps.action ?? "";
  selectProps.remoteOptimize = !isFalse(nodeProps.remoteOptimize, true);
  selectProps.requestOption = {
    ...{
      method: "GET",
      itemsField: "items",
      labelField: "label",
      valueField: "value",
      totalField: "total",
      fieldSelectorKey: "metadata.name",
      pageField: "page",
      sizeField: "size",
      iconField: undefined,
      descriptionField: undefined,
      parseData: undefined,
    },
    ...(nodeProps.requestOption ?? {}),
  };
  selectProps.multiple = !isFalse(nodeProps.multiple);
  selectProps.sortable = !isFalse(nodeProps.sortable, true);
  selectProps.remote = !isFalse(nodeProps.remote);
  selectProps.allowCreate = !isFalse(nodeProps.allowCreate);
  selectProps.clearable = !isFalse(nodeProps.clearable);
  selectProps.searchable = !isFalse(nodeProps.searchable);
  selectProps.autoSelect = !isFalse(nodeProps.autoSelect, true);
  if (selectProps.remote) {
    if (!nodeProps.remoteOption) {
      throw new Error("remoteOption is required when remote is true.");
    }
    selectProps.remoteOption = nodeProps.remoteOption;
  }
};

const isLoading = ref(false);
const isFetchingMore = ref(false);
const page = ref(1);
const size = ref(20);
const total = ref(0);
const searchKeyword = ref("");
const noNeedFetchOptions = ref(false);
// be no need to fetch options when total is less than or equal to size, cache all options
const cacheAllOptions = ref<SelectOption[] | undefined>(undefined);

const requestOptions = async (
  searchParams: SelectRemoteRequest
): Promise<SelectResponse> => {
  const responseData = {
    options: [],
    page: 1,
    size: 20,
    total: 0,
  };
  if (!selectProps.action) {
    return responseData;
  }
  const requestConfig: AxiosRequestConfig = {
    method: selectProps.requestOption?.method || "GET",
    url: selectProps.action,
    [selectProps.requestOption?.method === "GET" ? "params" : "data"]:
      searchParams,
  };
  const response = await axiosInstance.request(requestConfig);
  const { data } = response;
  const parseSelectData = parseSelectResponse(data);
  if (!parseSelectData) {
    throw new Error(
      "Error parsing response, please check the requestOption object."
    );
  }
  return parseSelectData;
};

const parseSelectResponse = (data: object): SelectResponse | undefined => {
  if (!selectProps.requestOption) {
    return;
  }
  const { parseData } = selectProps.requestOption;
  if (parseData) {
    return parseData(data);
  }
  const { itemsField } = selectProps.requestOption;
  if (!has(data, itemsField as PropertyPath)) {
    console.error(
      `itemsField: ${itemsField?.toString()} not found in response data.`
    );
    return;
  }
  const items = get(data, itemsField as PropertyPath);
  return {
    options: mapItemsToSelectOptions(items, selectProps.requestOption),
    page: get(data, selectProps.requestOption.pageField as PropertyPath, "1"),
    size: get(data, selectProps.requestOption.sizeField as PropertyPath, "20"),
    total: get(data, selectProps.requestOption.totalField as PropertyPath, "0"),
  };
};

/**
 * Retrieves the mapping of selected values and available options.
 *
 * If the selected value is found in the current options, it will be converted to a label and value format.
 * If the selected value is not found in the current options, the `mapUnresolvedOptions` method will be used.
 */
const fetchSelectedOptions = async (): Promise<SelectOption[] | undefined> => {
  const node = props.context.node;
  const value = node.value;

  const selectedValues: Array<unknown> = [];
  if (Array.isArray(value)) {
    selectedValues.push(...value);
  } else if (
    typeof value === "string" ||
    typeof value === "number" ||
    typeof value === "boolean" ||
    value === void 0
  ) {
    selectedValues.push(value);
  }

  const currentOptions = options.value?.filter((option) =>
    selectedValues.includes(option.value)
  );

  // Get options that are not yet mapped.
  const unmappedSelectValues = selectedValues
    .filter(
      (value) => !currentOptions?.find((option) => option.value === value)
    )
    .filter(Boolean);
  if (unmappedSelectValues.length === 0) {
    if (!currentOptions || currentOptions.length === 0) {
      return;
    }
    return currentOptions?.sort((a, b) =>
      selectedValues.indexOf(a.value) > selectedValues.indexOf(b.value) ? 1 : -1
    );
  }

  // Map the unresolved options to label and value format.
  const mappedSelectOptions = await mapUnresolvedOptions(
    unmappedSelectValues.map(String)
  );
  // Merge currentOptions and mappedSelectOptions, then sort them according to selectValues order.
  return [...(currentOptions || []), ...mappedSelectOptions].sort((a, b) =>
    selectedValues.indexOf(a.value) > selectedValues.indexOf(b.value) ? 1 : -1
  );
};

/**
 * Maps unresolved options to label and value format.
 *
 * There are several possible scenarios:
 *
 * 1. If it's an asynchronous request for options, fetch the label and value via an API call.
 *  a. If all selected values are found in the response, return the data directly.
 *  b. If only some of the values are found, check if new options can be created.
 *     If allowed, create new options for the remaining values.
 * 2. If it's a static option and creating new options is allowed, create new options for the remaining values.
 *    If not allowed, return an empty array.
 *
 * @param unmappedSelectValues Unresolved options
 */
const mapUnresolvedOptions = async (
  unmappedSelectValues: string[]
): Promise<SelectOption[]> => {
  if (!isRemote.value) {
    if (selectProps.allowCreate) {
      // TODO: Add mapped values to options
      return unmappedSelectValues.map((value) => ({ label: value, value }));
    }
    // Creation not allowed but there are unmapped values, return an empty array and issue a warning.
    console.warn(
      `It is not allowed to create options but has unmapped values. ${unmappedSelectValues}`
    );
    return unmappedSelectValues.map((value) => ({ label: value, value }));
  }

  // Asynchronous request for options, fetch label and value via API.
  let mappedOptions: SelectOption[] | undefined = undefined;
  if (noNeedFetchOptions.value) {
    mappedOptions = cacheAllOptions.value?.filter((option) =>
      unmappedSelectValues.includes(option.value)
    );
  } else {
    if (selectProps.action) {
      mappedOptions = await fetchRemoteMappedOptions(unmappedSelectValues);
    } else if (selectProps.remote) {
      const remoteOption = selectProps.remoteOption as SelectRemoteOption;
      mappedOptions =
        await remoteOption.findOptionsByValues(unmappedSelectValues);
    }
  }

  if (!mappedOptions) {
    return unmappedSelectValues.map((value) => ({ label: value, value }));
  }
  // Get values that are still unresolved.
  const unmappedValues = unmappedSelectValues.filter(
    (value) => !mappedOptions.find((option) => option.value === value)
  );
  if (unmappedValues.length === 0) {
    return mappedOptions;
  }

  if (!selectProps.allowCreate) {
    console.warn(
      `It is not allowed to create options but has unmapped values. ${unmappedSelectValues}`
    );
    return mappedOptions;
  }

  // Create new options for remaining values.
  return [
    ...mappedOptions,
    ...unmappedValues.map((value) => ({ label: value, value })),
  ];
};

const fetchRemoteMappedOptions = async (
  unmappedSelectValues: string[]
): Promise<SelectOption[]> => {
  const requestConfig: AxiosRequestConfig = {
    method: selectProps.requestOption?.method || "GET",
    url: selectProps.action,
  };
  if (requestConfig.method === "GET") {
    requestConfig.params = {
      fieldSelector: `${selectProps.requestOption?.fieldSelectorKey?.toString()}=(${unmappedSelectValues.join(
        ","
      )})`,
    };
  } else {
    requestConfig.data = {
      fieldSelector: `${selectProps.requestOption?.fieldSelectorKey?.toString()}=(${unmappedSelectValues.join(
        ","
      )})`,
    };
  }
  const response = await axiosInstance.request(requestConfig);
  const { data } = response;
  const parsedData = parseSelectResponse(data);
  if (!parsedData) {
    throw new Error(
      "fetchRemoteMappedOptions error, please check the requestOption object."
    );
  }
  return parsedData.options;
};

onMounted(async () => {
  initSelectProps();
  if (!isRemote.value) {
    options.value = props.context.attrs.options;
  } else {
    const response = await fetchOptions();
    if (response) {
      options.value = response.options;
      if (selectProps.remoteOptimize) {
        if (total.value !== 0 && total.value <= size.value) {
          noNeedFetchOptions.value = true;
          cacheAllOptions.value = response.options;
        }
      }
    }
  }
});

const getAutoSelectedOption = (): SelectOption | undefined => {
  if (!options.value || options.value.length === 0) {
    return;
  }

  // Find the first option that is not disabled.
  return options.value.find((option) => {
    const attrs = option.attrs as Record<string, unknown>;
    return isFalse(attrs?.disabled as string | boolean | undefined);
  });
};

watch(
  () => props.context.value,
  async (newValue) => {
    const selectedValues = selectOptions.value?.map((item) => item.value) || [];
    if (selectedValues.length > 0 && selectedValues.includes(newValue)) {
      return;
    }
    const selectedOption = await fetchSelectedOptions();
    selectOptions.value = selectedOption;
  }
);

watch(
  () => options.value,
  async (newOptions) => {
    if (newOptions && newOptions.length > 0) {
      const selectedOption = await fetchSelectedOptions();
      if (selectedOption) {
        selectOptions.value = selectedOption;
      }
    }
  }
);

const enableAutoSelect = () => {
  if (!selectProps.autoSelect) {
    return false;
  }
  if (selectProps.multiple || selectProps.placeholder) {
    return false;
  }

  const value = props.context.node.value;
  if (value === void 0 || value === null) {
    return true;
  }

  return false;
};

watch(
  () => options.value,
  async (newOptions) => {
    if (newOptions && newOptions.length > 0) {
      if (enableAutoSelect()) {
        // Automatically select the first option when the selected value is empty.
        const autoSelectedOption = getAutoSelectedOption();
        if (autoSelectedOption) {
          handleUpdate([autoSelectedOption]);
        }
      }
    }
  },
  { once: true }
);

// When attr options are processed asynchronously, it is necessary to monitor
// changes in attr options and update options accordingly.
watch(
  () => props.context.attrs.options,
  async (attrOptions) => {
    if (!isRemote.value) {
      options.value = attrOptions;
    }
  }
);

const handleUpdate = async (value: SelectOption[]) => {
  const oldSelectValue = selectOptions.value;
  if (
    oldSelectValue &&
    value.length === oldSelectValue.length &&
    value.every((item, index) => item.value === oldSelectValue[index].value)
  ) {
    return;
  }
  handleSetNodeValue(value);
  await props.context.node.settled;
  props.context.attrs.onChange?.(value);
};

const handleSetNodeValue = (value: SelectOption[]) => {
  const values = value.map((item) => item.value);
  selectOptions.value = value;
  if (selectProps.multiple) {
    props.context.node.input(values);
    return;
  }
  if (values.length === 0) {
    props.context.node.input("");
    return;
  }
  props.context.node.input(values[0]);
};

const fetchOptions = async (
  tempKeyword = searchKeyword.value,
  tempPage = page.value,
  tempSize = size.value
): Promise<SelectResponse | undefined> => {
  if (isLoading.value || !isRemote.value) {
    return;
  }
  // If the total number of options is less than the page size, no more requests are made.
  if (noNeedFetchOptions.value) {
    const { results } = useFuse<SelectOption>(
      tempKeyword,
      cacheAllOptions.value || [],
      {
        fuseOptions: {
          keys: ["label", "value"],
          threshold: 0,
          ignoreLocation: true,
        },
        matchAllWhenSearchEmpty: true,
      }
    );
    const filterOptions = results.value?.map((fuseItem) => fuseItem.item) || [];
    return {
      options: filterOptions || [],
      page: page.value,
      size: size.value,
      total: filterOptions.length || 0,
    };
  }
  isLoading.value = true;
  try {
    let response: SelectResponse | undefined;
    if (selectProps.action) {
      response = await requestOptions({
        page: tempPage,
        size: tempSize,
        keyword: tempKeyword,
      });
    }
    if (selectProps.remote) {
      const remoteOption = selectProps.remoteOption as SelectRemoteOption;
      response = await remoteOption.search({
        keyword: tempKeyword,
        page: tempPage,
        size: tempSize,
      });
    }
    page.value = response?.page || 1;
    size.value = response?.size || 20;
    total.value = response?.total || 0;
    return response as SelectResponse;
  } catch (error) {
    console.error("fetchOptions error", error);
  } finally {
    isLoading.value = false;
  }
};

const debouncedFetchOptions = useDebounceFn(async () => {
  const response = await fetchOptions(searchKeyword.value, 1);
  if (!response) {
    return;
  }
  options.value = response.options;
}, 500);

const handleSearch = async (value: string, event?: Event) => {
  if (event && event instanceof InputEvent) {
    if (event.isComposing) {
      return;
    }
  }
  // When the search keyword does not change, the data is no longer requested.
  if (
    value === searchKeyword.value &&
    options.value &&
    options.value?.length > 0
  ) {
    return;
  }
  searchKeyword.value = value;
  if (selectProps.action || selectProps.remote) {
    if (noNeedFetchOptions.value) {
      const response = await fetchOptions(searchKeyword.value, 1);
      if (!response) {
        return;
      }
      options.value = response.options;
    } else {
      debouncedFetchOptions();
    }
  }
};

const hasNextPage = computed(() => {
  const totalPages = Math.ceil(total.value / size.value);
  return (
    hasMoreOptions.value &&
    !isFetchingMore.value &&
    !isLoading.value &&
    page.value < totalPages
  );
});

const handleNextPage = async () => {
  if (!hasNextPage.value) {
    return;
  }
  isFetchingMore.value = true;
  const response = await fetchOptions(searchKeyword.value, page.value + 1);
  isLoading.value = false;
  isFetchingMore.value = false;
  if (!response) {
    return;
  }
  options.value = [...(options.value || []), ...response.options];
};
</script>
<template>
  <SelectContainer
    :allow-create="selectProps.allowCreate"
    :max-count="selectProps.maxCount"
    :multiple="selectProps.multiple"
    :sortable="selectProps.sortable"
    :placeholder="selectProps.placeholder"
    :loading="isLoading"
    :next-loading="isFetchingMore"
    :options="options"
    :selected="selectOptions"
    :remote="isRemote"
    :clearable="selectProps.clearable"
    :searchable="selectProps.searchable"
    :auto-select="selectProps.autoSelect"
    @update="handleUpdate"
    @search="handleSearch"
    @load-more="handleNextPage"
  />
</template>
