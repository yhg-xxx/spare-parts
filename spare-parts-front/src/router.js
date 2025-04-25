 import {createRouter, createWebHashHistory} from "vue-router";

const router = createRouter({
        history: createWebHashHistory(import.meta.env.BASE_URL),
        routes: [
            {
                path: '/',
                name: 'login',
                component: () => import('@/components/Login.vue'),

             },
            {
                path: '/view',
                name: 'view',
                component: () => import('@/components/IndexView.vue'),
                children: [
                    {
                        path: 'purchase',
                        name: 'purchase',
                        component: () => import('@/components/Purchase.vue')
                    },{
                        path: 'warehouse',
                        name: 'warehouse',
                        component: () => import('@/components/Warehouse.vue')
                    },{
                        path: 'inventory',
                        name: 'inventory',
                        component: () => import('@/components/Inventory.vue')
                    },{
                        path: 'transferManagement',
                        name: 'transferManagement',
                        component: () => import('@/components/TransferRecord.vue'),
                    },{
                        path: 'transferManagement1',
                        name: 'transferManagement1',
                        component: () => import('@/components/TransferRecord1.vue'),
                    },{
                        path: 'sparepart',
                        name: 'sparepart',
                        component: () => import('@/components/Sparepart.vue'),
                    },{
                        path: 'UsageRequestApply',
                        name: 'UsageRequestApply',
                        component: () => import('@/components/UsageApply.vue'),
                    },{
                        path: 'UsageRequestReview',
                        name: 'UsageRequestReview',
                        component: () => import('@/components/UsageReview.vue'),
                    },{
                        path: 'inbound',
                        name: 'inbound',
                        component: () => import('@/components/Inbound.vue'),
                    },{
                        path: 'faultorder',
                        name: 'faultorder',
                        component: () => import('@/components/Faultorder.vue'),
                    },
            ]
            }
        ]

    }
)
export default router;