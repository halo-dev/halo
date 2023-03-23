package run.halo.app.core.extension;

import run.halo.app.infra.utils.JsonUtils;

/**
 * Roles to test.
 *
 * @author guqing
 * @since 2.0.0
 */
public class TestRole {

    public static Role getRoleManage() {
        return JsonUtils.jsonToObject("""
            {
                 "apiVersion": "v1alpha1",
                 "kind": "Role",
                 "metadata": {
                     "name": "role-template-apple-manage"
                 },
                 "rules": [{
                     "resources": ["apples"],
                     "verbs": ["create"]
                 }]
             }
            """, Role.class);
    }

    public static Role getRoleView() {
        return JsonUtils.jsonToObject("""
            {
                 "apiVersion": "v1alpha1",
                 "kind": "Role",
                 "metadata": {
                     "name": "role-template-apple-view"
                 },
                 "rules": [{
                     "resources": ["apples"],
                     "verbs": ["list"]
                 }]
             }
            """, Role.class);
    }

    public static Role getRoleOther() {
        return JsonUtils.jsonToObject("""
            {
                "apiVersion": "v1alpha1",
                "kind": "Role",
                "metadata": {
                    "name": "role-template-apple-other"
                },
                "rules": [{
                    "resources": ["apples"],
                    "verbs": ["update"]
                }]
            }
            """, Role.class);
    }
}
