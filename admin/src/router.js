import Vue from "vue"
import Router from "vue-router"
import Login from "./views/login"
import Admin from "./views/admin"
import Welcome from "./views/admin/welcome"
import Chapter from "./views/admin/chapter"
import Section from "./views/admin/section"
import Course from "./views/admin/course"
import Category from "./views/admin/category"
import Content from "./views/admin/content.vue"
import Teacher from "./views/admin/teacher"
import File from "./views/admin/file"
import User from "./views/admin/user"
import Resource from "./views/admin/resource"
import Role from "./views/admin/role"
import Member from "./views/admin/member.vue"
import Sms from "./views/admin/sms"


Vue.use(Router);

export default new Router({
  mode: "history",
  base: process.env.BASE_URL,
  routes: [{
    path: "*",
    redirect: "/login",
  },{
    path: "",
    redirect: "/login",
  }, {
    path: "/login",
    component: Login
  }, {
    path: "/",
    name: "admin",
    component: Admin,
    meta :{
      loginRequire : true
    },
    children:[{
      path:"welcome",
      name: "welcome",
      component: Welcome,
    },{
      path:"business/chapter",
      name: "business/course",
      component: Chapter,
    },{
      path:"business/category",
      name: "business/course",
      component: Category,
    },{
      path:"business/section",
      name: "business/course",
      component: Section,
    },{
      path:"business/course",
      name: "business/course",
      component: Course,
    }, {
      path: "business/content",
      name: "business/content",
      component: Content,
    }, {
      path: "business/teacher",
      name: "business/teacher",
      component: Teacher,
    }, {
      path: "business/member",
      name: "business/member",
      component: Member,
    }, {
      path: "business/sms",
      name: "business/sms",
      component: Sms,
    }, {
      path: "file/file",
      name: "file/file",
      component: File,
    }, {
      path: "system/user",
      name: "system/user",
      component: User,
    }, {
      path: "system/resource",
      name: "system/resource",
      component: Resource,
    }, {
      path: "system/role",
      name: "system/role",
      component: Role,
    }]
  } ]
})
