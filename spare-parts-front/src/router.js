import {createRouter, createWebHashHistory} from "vue-router";

const router = createRouter({
        history: createWebHashHistory(import.meta.env.BASE_URL),
        routes: [
            {
                path: '/',
                name: 'welcome',
                component: () => import('@/components/WelcomeView.vue'),
                children: [
                    {
                        path: '',
                        name: 'welcome-login',
                        component: () => import('@/components/Login.vue')
                    }
                ]
            },{
                path: '',
                name: '',
                component: () => import('')
            },{
                path: '',
                name: '',
                component: () => import('')
            },

        ]

    }
)
export default router;