import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import HomeView from "@/views/HomeView.vue";

const router = createRouter({
    // history: createWebHistory(import.meta.env.BASE_URL),
    history: createWebHashHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: HomeView,
            children: [
                {
                    path: 'branch',
                    name: 'branch',
                    component: () => import('../views/page/Branch.vue')
                },
                {
                    path: 'credentialsProvider',
                    name: 'CredentialsProvider',
                    component: () => import('@/views/page/CredentialsProvider.vue')
                },
                {
                    path: 'analysis',
                    name: 'Analysis',
                    component: () => import('@/views/page/Analysis.vue')
                },
            ]
        }
    ]
})
export default router
