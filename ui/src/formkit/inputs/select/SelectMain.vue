<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { useDebounceFn } from "@vueuse/core";
import { useFuse } from "@vueuse/integrations/useFuse";
import type { AxiosRequestConfig } from "axios";
import { get, has, type PropertyPath } from "lodash-es";
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
import SelectContainer from "./SelectContainer.vue";

export interface SelectProps {
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

export interface SelectResponse {
  options: Array<
    Record<string, unknown> & {
      label: string;
      value: string;
    }
  >;

  page: number;

  size: number;

  total: number;
}

export interface SelectRemoteRequest {
  keyword: string;
  page: number;
  size: number;
}

export interface SelectRemoteOption {
  search: ({
    keyword,
    page,
    size,
  }: SelectRemoteRequest) => Promise<SelectResponse>;

  findOptionsByValues: (values: string[]) => Promise<
    Array<{
      label: string;
      value: string;
    }>
  >;
}

export interface SelectActionRequest {
  method?: "GET" | "POST";

  /**
   * Parses the returned data from the request.
   */
  parseData?: (data: unknown) => SelectResponse;

  /**
   * Field name for the page number in the request parameters, default is `page`.
   */
  pageField?: PropertyPath;

  /**
   * Field name for size, default is `size`.
   */
  sizeField?: PropertyPath;

  /**
   * Field name for total, default is `total`.
   */
  totalField?: PropertyPath;

  /**
   * Field name for items, default is `items`.
   */
  itemsField?: PropertyPath;

  /**
   * Field name for label, default is `label`.
   */
  labelField?: PropertyPath;

  /**
   * Field name for value, default is `value`.
   */
  valueField?: PropertyPath;

  /**
   * When using value to query detailed information, the default query
   * parameter key for fieldSelector is `metadata.name`.
   */
  fieldSelectorKey?: PropertyPath;
}

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const options = shallowRef<
  | Array<
      Record<string, unknown> & {
        label: string;
        value: string;
      }
    >
  | undefined
>(undefined);
const selectOptions = shallowRef<
  | Array<{
      label: string;
      value: string;
    }>
  | undefined
>(undefined);

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
const cacheAllOptions = ref<
  Array<{ label: string; value: string }> | undefined
>(undefined);

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
  const { labelField, valueField, itemsField } = selectProps.requestOption;
  if (!has(data, itemsField as PropertyPath)) {
    console.error(
      `itemsField: ${itemsField?.toString()} not found in response data.`
    );
    return;
  }
  const items = get(data, itemsField as PropertyPath);
  return {
    options: formatOptionsData(
      items,
      labelField as PropertyPath,
      valueField as PropertyPath
    ),
    page: get(data, selectProps.requestOption.pageField as PropertyPath, "1"),
    size: get(data, selectProps.requestOption.sizeField as PropertyPath, "20"),
    total: get(data, selectProps.requestOption.totalField as PropertyPath, "0"),
  };
};

const formatOptionsData = (
  items: Array<object>,
  labelField: PropertyPath,
  valueField: PropertyPath
) => {
  if (!items) {
    console.warn(
      "Select options: data items are empty, please check the itemsField."
    );
    return [];
  }
  return items.map((item) => {
    if (!has(item, labelField as PropertyPath)) {
      console.error(
        `labelField: ${labelField?.toString()} not found in response data items.`
      );
      return { label: "", value: "" };
    }
    if (!has(item, valueField as PropertyPath)) {
      console.error(
        `valueField: ${valueField?.toString()} not found in response data items.`
      );
      return { label: "", value: "" };
    }
    return {
      label: get(item, labelField),
      value: get(item, valueField),
    };
  });
};

/**
 * Retrieves the mapping of selected values and available options.
 *
 * If the selected value is found in the current options, it will be converted to a label and value format.
 * If the selected value is not found in the current options, the `mapUnresolvedOptions` method will be used.
 */
const fetchSelectedOptions = async (): Promise<
  | Array<{
      label: string;
      value: string;
    }>
  | undefined
> => {
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
): Promise<
  Array<{
    label: string;
    value: string;
  }>
> => {
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
  let mappedOptions:
    | Array<{
        label: string;
        value: string;
      }>
    | undefined = undefined;
  if (noNeedFetchOptions.value) {
    mappedOptions = cacheAllOptions.value?.filter((option) =>
      unmappedSelectValues.includes(option.value)
    );
  } else {
    if (selectProps.action) {
      mappedOptions = await fetchRemoteMappedOptions(unmappedSelectValues);
    } else if (selectProps.remote) {
      const remoteOption = selectProps.remoteOption as SelectRemoteOption;
      mappedOptions = await remoteOption.findOptionsByValues(
        unmappedSelectValues
      );
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
): Promise<Array<{ label: string; value: string }>> => {
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

const getAutoSelectedOption = ():
  | {
      label: string;
      value: string;
    }
  | undefined => {
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

const handleUpdate = async (value: Array<{ label: string; value: string }>) => {
  const oldSelectValue = selectOptions.value;
  if (
    oldSelectValue &&
    value.length === oldSelectValue.length &&
    value.every((item, index) => item.value === oldSelectValue[index].value)
  ) {
    return;
  }
  const newValue = value.map((item) => {
    return {
      label: item.label,
      value: item.value,
    };
  });
  handleSetNodeValue(newValue);
  await props.context.node.settled;
  props.context.attrs.onChange?.(newValue);
};

const handleSetNodeValue = (value: Array<{ label: string; value: string }>) => {
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
    const { results } = useFuse<{
      label: string;
      value: string;
    }>(tempKeyword, cacheAllOptions.value || [], {
      fuseOptions: {
        keys: ["label", "value"],
        threshold: 0,
        ignoreLocation: true,
      },
      matchAllWhenSearchEmpty: true,
    });
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

const handleNextPage = async () => {
  if (!hasMoreOptions.value || isFetchingMore.value || isLoading.value) {
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
