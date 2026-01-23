/**
 * From options to find
 *
 * The options can be provided in 3 ways:
 * An array of strings, example: ['teddy', 'bear']
 * An object of value/label pairs, example: [{ teddy: 'Bear', bear: 'Teddy' }]
 * An array of objects with label and value properties, example: [{ label: 'Shawshank redemption', value: 'shawshank' }]
 */
export function findOptions(
  options: unknown[] | Record<string, string>,
  value: unknown
):
  | {
      label: string;
      value: unknown;
      group?: unknown;
    }
  | undefined {
  if (!options || options.length === 0) {
    return;
  }

  if (options instanceof Array) {
    return findOptionsInArray(options, value);
  }

  if (options instanceof Object) {
    return findOptionsInRecord(options, value);
  }
}

export function findOptionsInRecord(
  options: Record<string, string>,
  value: unknown
):
  | {
      label: string;
      value: unknown;
      group?: unknown;
    }
  | undefined {
  for (const [optionKey, optionValue] of Object.entries(options)) {
    if (optionKey === value) {
      return {
        label: optionKey,
        value: optionValue,
      };
    }
  }
}
export function findOptionsInArray(
  options: unknown[],
  value: unknown
):
  | {
      label: string;
      value: unknown;
      group?: unknown;
    }
  | undefined {
  for (const option of options) {
    if (!option) {
      continue;
    }

    // string option
    // example: ['teddy', 'bear']
    if (typeof option === "string") {
      if (option === value) {
        return {
          label: option,
          value: option,
        };
      }
    }

    // objects with label and value properties
    // example: [{ label: 'Shawshank redemption', value: 'shawshank' }]
    if (typeof option === "object") {
      if ("value" in option && "label" in option) {
        if (option.value === value) {
          return {
            label: option.label as string,
            value: option.value,
          };
        }
      }

      // group option
      // example: [{ group: 'Movies', options: [{ label: 'Shawshank redemption', value: 'shawshank' }] }]
      if ("group" in option && "options" in option) {
        const options = option.options as unknown[] | Record<string, string>;
        const groupOptions = findOptions(options, value);
        if (groupOptions) {
          return {
            label: groupOptions.label,
            value: groupOptions.value,
            group: option.group,
          };
        }
      }
    }
  }
}
