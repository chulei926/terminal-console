import {createRouter, createWebHashHistory, RouteRecordRaw} from 'vue-router'

const routes: Array<RouteRecordRaw> = [

]

const router = createRouter({
    // history: createWebHistory(),    // 使用history模式
    history: createWebHashHistory(),	 // 使用hash模式
    routes
})

export default router
