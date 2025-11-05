// stores/cart.js
import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    order: null
  }),
  actions: {
    setOrder(order) {
      this.order = order
    },
    clearOrder() {
      this.order = null
    }
  }
})

