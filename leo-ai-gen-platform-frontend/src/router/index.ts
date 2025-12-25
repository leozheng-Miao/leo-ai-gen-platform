import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserEditPage from '@/pages/user/UserEditPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/user/edit',
      name: '编辑用户信息',
      component: UserEditPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: `/app/edit/:id`,
      name: '应用编辑',
      component: AppEditPage,
    },
  ],
})

export default router
