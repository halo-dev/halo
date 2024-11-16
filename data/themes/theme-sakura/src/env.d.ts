/// <reference types="vite/client" />

import { type Sakura } from "./main";

declare global {
  interface Window {
    sakura: Sakura;
  }
}