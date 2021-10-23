import Vue from 'vue'
import App from './app.vue'
import router from './router'
import axios from 'axios'
import filter from './filter/filter'


Vue.config.productionTip = false
//注册内置属性
Vue.prototype.$ajax = axios;

// 解决每次ajax请求，对应的sessionId不一致的问题(未能解决)
axios.defaults.withCredentials = true

/**
 * axios拦截器
 */
//请求拦截器，每个请求带上token
axios.interceptors.request.use(function (config) {
  console.log("请求：", config);
  let token = Tool.getLoginUser().token;
  if (Tool.isNotEmpty(token)) {
    config.headers.token = token;
    console.log("请求headers增加token:", token);
  }
  return config;
}, error => {});
//打印一下返回结果
axios.interceptors.response.use(function (response) {
  console.log("返回结果：", response);
  return response;
}, error => {});

// 全局过滤器
Object.keys(filter).forEach(key => {
  Vue.filter(key, filter[key])
});

// 路由登录拦截
router.beforeEach((to, from, next) => {
  // 要不要对meta.loginRequire属性做监控拦截
  if (to.matched.some(function (item) {
    return item.meta.loginRequire
  })) {
    //检查跳转的页面是否需要登录
    let loginUser = Tool.getLoginUser();
    if (Tool.isEmpty(loginUser)) {
      next('/login');
    } else {
      next();
    }
  } else {
    next();
  }
});



// 全局过滤器,引入filter.js
Object.keys(filter).forEach(key => {
  Vue.filter(key, filter[key])
});


new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
