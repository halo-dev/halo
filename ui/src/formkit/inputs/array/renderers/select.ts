import type { FormKitNode } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";
import { get, has } from "es-toolkit/compat";
import type { PropertyPath } from "lodash-es";
import { findOptions } from "./helpers/findOption";
import type { LabelValueResult } from "./types";

const formatOptionsData = (
  items: Array<object>,
  labelField: PropertyPath,
  valueField: PropertyPath
): Array<{ label: string; value: unknown }> | undefined => {
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
 * Parse action remote select response data
 */
const parseActionRemoteSelectResponse = (
  node: FormKitNode<unknown>,
  data: unknown
): Array<{ label: string; value: unknown }> | undefined => {
  const { requestOption } = node.props;
  const { parseData } = requestOption;
  if (parseData) {
    return parseData(data);
  }
  const { labelField, valueField, itemsField } = requestOption;
  if (!has(data, itemsField as PropertyPath)) {
    console.error(
      `itemsField: ${itemsField?.toString()} not found in response data.`
    );
    return;
  }
  const items = get(data, itemsField as PropertyPath);
  return formatOptionsData(
    items,
    labelField as PropertyPath,
    valueField as PropertyPath
  );
};

/**
 * Fetch remote mapped options through action property
 */
const fetchRemoteMappedOptions = async (
  node: FormKitNode<unknown>,
  unmappedSelectValues: unknown[]
): Promise<Array<{ label: string; value: unknown }> | undefined> => {
  const { requestOption, action } = node.props;
  if (!requestOption || !action) {
    return;
  }
  const requestConfig: AxiosRequestConfig = {
    method: requestOption?.method || "GET",
    url: action,
  };
  if (requestConfig.method === "GET") {
    requestConfig.params = {
      fieldSelector: `${requestOption?.fieldSelectorKey?.toString()}=(${unmappedSelectValues.join(
        ","
      )})`,
    };
  } else {
    requestConfig.data = {
      fieldSelector: `${requestOption?.fieldSelectorKey?.toString()}=(${unmappedSelectValues.join(
        ","
      )})`,
    };
  }
  const response = await axiosInstance.request(requestConfig);
  const { data } = response;
  return parseActionRemoteSelectResponse(node, data);
};

/**
 * Find the selected option by remote data fetching
 */
async function findSelectedOptionByRemote(
  node: FormKitNode<unknown>,
  value: unknown
): Promise<
  | {
      label: string;
      value: unknown;
      group?: unknown;
    }
  | undefined
> {
  // select remote option
  if ("remote" in node.props && node.props.remote) {
    const { search, findOptionsByValues } = node.props.remoteOption;
    if (findOptionsByValues) {
      const options = await findOptionsByValues([value]);
      if (options.length > 0) {
        return {
          label: options[0].label,
          value: options[0].value,
        };
      }
    }
    if (search) {
      const options = await search({
        keyword: "",
        page: 1,
        size: 20,
      });
      if (options.length > 0) {
        const selectedOption = findSelectedOption(options, value);
        if (selectedOption) {
          return selectedOption;
        }
      }
    }
  }

  // select action option
  if ("action" in node.props && node.props.action) {
    const mappedOptions = await fetchRemoteMappedOptions(node, [value]);
    if (mappedOptions) {
      const selectedOption = findOptions(mappedOptions, value);
      if (selectedOption) {
        return selectedOption;
      }
    }
  }
}

/**
 * Render select label value
 *
 * Select has multiple ways to get options, such as:
 * - Get options through the `remote` property
 * - Get options through the `action` property
 * - Get options through the `options` property
 */
export async function renderSelectLabelValue({
  node,
  value,
}: {
  node: FormKitNode<unknown>;
  value: unknown;
}): Promise<LabelValueResult> {
  const remoteSelectedOption = await findSelectedOptionByRemote(node, value);
  if (remoteSelectedOption) {
    return {
      value: remoteSelectedOption.label,
    };
  }
  const options = node.context?.attrs.options;
  if (typeof value === "string") {
    const selectedOption = findOptions(options, value);
    if (selectedOption) {
      return {
        value: selectedOption.label,
      };
    }
  }

  if (Array.isArray(value)) {
    return {
      value: value.map((v) => {
        const selectedOption = findOptions(options, v);
        return selectedOption?.label;
      }),
    };
  }

  return {
    value,
  };
}
