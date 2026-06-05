import type { FormKitLibrary, FormKitTypeDefinition } from "@formkit/core";
import type { PluginModule } from "@halo-dev/ui-shared";

export interface PluginFormKitInputSource {
  name: string;
  module: PluginModule;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isFormKitInputDefinition(
  value: unknown
): value is FormKitTypeDefinition {
  return (
    isRecord(value) &&
    typeof value.type === "string" &&
    ("schema" in value || "component" in value)
  );
}

function warn(message: string) {
  console.warn(`[Halo FormKit] ${message}`);
}

export function collectPluginFormKitInputs(
  sources: PluginFormKitInputSource[],
  registeredInputs: FormKitLibrary
): FormKitLibrary {
  const acceptedInputs: FormKitLibrary = {};
  const inputOwners = new Map<string, string>();

  for (const inputName of Object.keys(registeredInputs)) {
    inputOwners.set(inputName, "Halo built-in inputs");
  }

  for (const source of sources) {
    const formkit = (source.module as { formkit?: unknown }).formkit;

    if (formkit === undefined || formkit === null) {
      continue;
    }

    if (!isRecord(formkit)) {
      warn(
        `Skipped plugin "${source.name}" FormKit config because it is not an object.`
      );
      continue;
    }

    const { inputs } = formkit;

    if (inputs === undefined || inputs === null) {
      continue;
    }

    if (!isRecord(inputs)) {
      warn(
        `Skipped plugin "${source.name}" FormKit inputs because they are not an object.`
      );
      continue;
    }

    for (const [inputName, inputDefinition] of Object.entries(inputs)) {
      if (!isFormKitInputDefinition(inputDefinition)) {
        warn(
          `Skipped FormKit input "${inputName}" from plugin "${source.name}" because it is not a valid input definition.`
        );
        continue;
      }

      const existingOwner = inputOwners.get(inputName);
      if (existingOwner) {
        warn(
          `Skipped FormKit input "${inputName}" from plugin "${source.name}" because it conflicts with ${existingOwner}.`
        );
        continue;
      }

      acceptedInputs[inputName] = inputDefinition;
      inputOwners.set(inputName, `plugin "${source.name}"`);
    }
  }

  return acceptedInputs;
}
