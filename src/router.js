import Vue from "vue";
import Router from "vue-router";
import Layout from "./views/layout/Layout";
import DashBoard from "./views/dashboard/Dashboard";

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: "/",
      name: "layout",
      component: Layout,
      children: [
        {
          path: "dashboard",
          name: "dashboard",
          component: DashBoard
        },
        {
          path: "posts",
          name: "posts-list",
          component: () => import("./views/post/PostList")
        }
      ]
    }
  ]
});
