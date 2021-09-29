
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import ReservationManager from "./components/ReservationManager"
import StockDeliveryManager from "./components/StockDeliveryManager"

import ReservationView from "./components/ReservationView"
import RoomInfoManager from "./components/RoomInfoManager"
import OrderManager from "./components/OrderManager"

import PaymentManager from "./components/PaymentManager"
import OrderStatus from "./components/OrderStatus"
import PromoteManager from "./components/PromoteManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
                path: '/stockDeliveries',
                name: 'StockDeliveryManager',
                component: StockDeliveryManager
            },

            {
                path: '/reservationViews',
                name: 'ReservationView',
                component: ReservationView
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/roomInfos',
                name: 'RoomInfoManager',
                component: RoomInfoManager
                path: '/orderStatuses',
                name: 'OrderStatus',
                component: OrderStatus
            },

            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
                path: '/promotes',
                name: 'PromoteManager',
                component: PromoteManager
            },



    ]
})
