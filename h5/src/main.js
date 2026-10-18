import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './styles/theme.css'

/**
 * H5 应用启动入口，注册 Pinia、路由和根组件。
 *
 * H5 application entry point that registers Pinia, the router, and the root component.
 */
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
