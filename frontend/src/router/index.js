import { createRouter, createWebHistory } from 'vue-router'



const routes = [
  {
    path: '/',
    name: 'HomePage',
    component: ()=> import('../HomePage.vue')
    // component: Home
  },
  {
    path: '/login',
    name: 'LoginPage',
    component: ()=>import('../views/LoginPage.vue')
  },
  {
    path: '/itemInfo/:id',
    name: 'ItemInfo',
    component: ()=>import('../views/ItemInfo.vue')
  },
  {
    path: '/itemCart',
    name: 'itemCart',
    component: ()=>import('../views/ItemCart.vue')
  },
  {
    path: '/publishItem',
    name: 'publishItem',
    component: ()=>import('../views/PublishItem.vue')
  },
  {
    path: '/myInbox',
    name: 'myInbox',
    component: ()=>import('../views/MyInbox.vue')
  },
  {
    path: '/inboxPrompt',
    name: 'inboxPrompt',
    component: ()=>import('../views/InboxPrompt.vue')
  }

]

// --- Vue 3 的写法是 createRouter ---
const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// --- 路由守卫 ---
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem("user");
  if (to.meta.requiresAuth && !user) {
    next('/login');
  } else {
    next();
  }
})

export default router