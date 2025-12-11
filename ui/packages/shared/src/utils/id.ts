import { v7 as uuidv7 } from "uuid";

/**
 * Utilities for generating unique identifiers that remain sortable over time.
 *
 * Relies on the `uuid` package for RFC 4122 compliant UUID generation.
 */
export class IdUtils {
  /**
   * Generates a RFC 4122 version 7 UUID string using the `uuid` package.
   *
   * UUID v7 keeps the order of creation roughly aligned with chronological order,
   * making it a good fit for persistence stores or log entries that benefit from
   * monotonic sorting.
   *
   * @returns A lowercase UUIDv7 string such as "018f1c2e-4fcb-7d04-9f21-1a2b3c4d5e6f".
   */
  uuid(): string {
    return uuidv7();
  }
}
