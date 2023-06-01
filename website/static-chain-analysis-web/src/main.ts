import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import service from "./axios/request"
import 'element-plus/theme-chalk/el-notification.css'

const app = createApp(App)
declare module '@vue/runtime-core' {
    interface ComponentCustomProperties {
        service: any
    }
}
app.use(router)
// app.use(ElementPlus)
app.config.globalProperties.service = service
app.mount('#app')
