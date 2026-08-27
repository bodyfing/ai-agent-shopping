import { createApp } from 'vue'
import App from './App.vue'   // <--- 1. 改这里：引入 App.vue，不是 Home
import router from './router'

// 2. 改这里：创建 App 的实例
const app = createApp(App)

app.use(router)
app.mount('#app')