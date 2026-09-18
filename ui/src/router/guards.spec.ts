import { setupAuthCheckGuard as setupConsoleAuth } from "@console/router/guards/auth-check";
import { setupPermissionGuard as setupConsolePermission } from "@console/router/guards/permission";
import { setupAuthCheckGuard as setupUcAuth } from "@uc/router/guards/auth-check";
import { setupPermissionGuard as setupUcPermission } from "@uc/router/guards/permission";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import {
  createMemoryHistory,
  createRouter,
  isNavigationFailure,
  NavigationFailureType,
  type RouteMeta,
} from "vue-router";
import { setupProcessBarGuard } from "./process-bar";

const mocks = vi.hoisted(() => ({
  user: { isAnonymous: false },
  hasPermission: vi.fn(),
  disallowConsole: vi.fn(),
}));

vi.mock("@halo-dev/ui-shared", () => ({
  stores: { currentUser: () => mocks.user },
  utils: {
    permission: {
      getUserPermissions: () => ["test:view"],
      has: mocks.hasPermission,
    },
  },
}));
vi.mock("@/utils/role", () => ({
  isConsoleAccessDisallowed: mocks.disallowConsole,
}));

beforeEach(() => {
  mocks.user.isAnonymous = false;
  mocks.hasPermission.mockReturnValue(true);
  mocks.disallowConsole.mockReturnValue(false);
});

afterEach(() => {
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

describe.each([
  ["Console", setupConsoleAuth, setupConsolePermission],
  ["UC", setupUcAuth, setupUcPermission],
] as const)("%s global guards", (_, setupAuth, setupPermission) => {
  function routerWithGuards(meta: RouteMeta = {}) {
    const component = { render: () => null };
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: "/target", component, meta },
        { path: "/forbidden", name: "Forbidden", component },
      ],
    });
    setupAuth(router);
    setupPermission(router);
    setupProcessBarGuard(router);
    return router;
  }

  it("allows navigation without deprecated next warnings", async () => {
    const warn = vi.spyOn(console, "warn").mockImplementation(() => {});
    const router = routerWithGuards();
    await router.push("/target");
    expect(router.currentRoute.value.path).toBe("/target");
    expect(warn).not.toHaveBeenCalled();
  });

  it.each([true, false])(
    "preserves array permission result %s",
    async (allowed) => {
      mocks.hasPermission.mockReturnValue(allowed);
      const router = routerWithGuards({ permissions: ["test:view"] });
      await router.push("/target");
      expect(router.currentRoute.value.path).toBe(
        allowed ? "/target" : "/forbidden"
      );
    }
  );

  it.each([true, false])(
    "awaits permission function result %s",
    async (allowed) => {
      const permissions = vi.fn(async () => allowed);
      const router = routerWithGuards({ permissions });
      await router.push("/target");
      expect(permissions).toHaveBeenCalledWith(["test:view"]);
      expect(router.currentRoute.value.path).toBe(
        allowed ? "/target" : "/forbidden"
      );
    }
  );

  it("denies navigation when a permission function rejects", async () => {
    vi.spyOn(console, "error").mockImplementation(() => {});
    const router = routerWithGuards({
      permissions: async () => {
        throw new Error("Permission check failed");
      },
    });
    await router.push("/target");
    expect(router.currentRoute.value.name).toBe("Forbidden");
  });

  it("cancels anonymous navigation while redirecting to login", async () => {
    const href = "http://localhost/console/target?tab=test";
    vi.stubGlobal("window", { location: { href } });
    mocks.user.isAnonymous = true;
    const router = routerWithGuards();
    const failure = await router.push("/target");
    expect(isNavigationFailure(failure, NavigationFailureType.aborted)).toBe(
      true
    );
    expect(window.location.href).toBe(
      `/login?redirect_uri=${encodeURIComponent(href)}`
    );
    expect(router.currentRoute.value.path).toBe("/");
  });
});

it("cancels Console navigation while redirecting a restricted user to UC", async () => {
  vi.stubGlobal("window", { location: { href: "http://localhost/console/" } });
  mocks.disallowConsole.mockReturnValue(true);
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: "/target", component: { render: () => null } }],
  });
  setupConsolePermission(router);
  const failure = await router.push("/target");
  expect(isNavigationFailure(failure, NavigationFailureType.aborted)).toBe(
    true
  );
  expect(window.location.href).toBe("/uc");
  expect(router.currentRoute.value.path).toBe("/");
});
