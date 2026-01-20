import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './assets/css/common.css'
import Print from 'vue-print-nb'

Vue.config.productionTip = false
Vue.use(ElementUI)
Vue.use(Print)

// 全局错误处理器 - 捕获Vue组件中的错误
Vue.config.errorHandler = (err, vm, info) => {
  // 如果是我们自定义的错误对象（有message属性），只显示message
  if (err && typeof err === 'object' && err.message && !err.stack) {
    // 这是我们的自定义错误对象，已经在request.js中显示了Message.error
    // 这里不重复显示，只记录到控制台
    console.error('Vue Error:', err.message)
    return
  }
  // 其他错误正常处理
  console.error('Vue Error:', err, info)
}

// 全局Promise rejection处理器 - 捕获未处理的Promise rejection
window.addEventListener('unhandledrejection', (event) => {
  // 阻止所有未处理的Promise rejection显示错误覆盖层
  // 因为错误已经在request.js中通过Message.error显示了
  event.preventDefault()
  
  // 如果是我们自定义的错误对象（有message属性），只记录message
  if (event.reason && typeof event.reason === 'object' && event.reason.message) {
    console.error('Unhandled Promise Rejection:', event.reason.message)
    return
  }
  // 其他错误也记录，但不显示错误覆盖层
  console.error('Unhandled Promise Rejection:', event.reason)
})

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
