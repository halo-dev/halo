import { describe, expect, it } from "vitest";
import { formatDatetime } from "../date";

describe.skip("date#formatDatetime", () => {
  it("should return formatted datetime", () => {
    const formattedDatetime = formatDatetime("2022-08-17T06:01:16.511575Z");

    expect(formattedDatetime).toEqual("2022-08-17 14:01");
  });
});
