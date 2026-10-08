import type { AxiosError } from "axios";
import { beforeEach, describe, expect, it } from "vite-plus/test";
import { nextTick } from "vue";
import {
  cancelSudoConfirm,
  completeSudoConfirm,
  isSudoRequiredError,
  requestSudoConfirm,
  SUDO_REQUIRED_TYPE,
  sudoConfirmVisible,
} from "../use-sudo-confirm";

function sudoError(): AxiosError {
  return {
    response: {
      status: 403,
      data: {
        type: SUDO_REQUIRED_TYPE,
      },
    },
  } as AxiosError;
}

describe("use-sudo-confirm", () => {
  beforeEach(() => {
    cancelSudoConfirm();
  });

  it("detects sudo-required problem type", () => {
    expect(isSudoRequiredError(sudoError())).toBe(true);
    expect(
      isSudoRequiredError({
        response: { status: 403, data: { type: "about:blank" } },
      } as AxiosError)
    ).toBe(false);
  });

  it("reuses a single in-flight confirmation", async () => {
    const first = requestSudoConfirm();
    const second = requestSudoConfirm();
    await nextTick();
    expect(sudoConfirmVisible.value).toBe(true);
    completeSudoConfirm();
    await expect(first).resolves.toBeUndefined();
    await expect(second).resolves.toBeUndefined();
    expect(sudoConfirmVisible.value).toBe(false);
  });

  it("rejects when confirmation is cancelled", async () => {
    const pending = requestSudoConfirm();
    await nextTick();
    cancelSudoConfirm();
    await expect(pending).rejects.toThrow("sudo cancelled");
  });
});
