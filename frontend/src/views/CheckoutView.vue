<script setup>
/**
 * CheckoutView - 結帳頁面
 */
import { inject } from "vue";
import { useRouter } from "vue-router";
import CheckoutPage from "../components/CheckoutPage.vue";

const router = useRouter();

// 從 App.vue 注入資料
const cart = inject("cart");
const user = inject("user");

// 從 App.vue 注入方法
const handlePlaceOrder = inject("handlePlaceOrder");
const handleUpdateQuantity = inject("handleUpdateQuantity");

// 返回首頁
const handleBack = () => {
  router.push("/");
};
</script>

<template>
  <div class="pt-24 min-h-screen bg-washi">
    <CheckoutPage
      :cart-items="cart"
      :user-email="user?.email"
      :user-name="user?.name"
      :user-phone="user?.phone"
      @place-order="handlePlaceOrder"
      @back="handleBack"
      @update-quantity="
        (variantId, delta) => handleUpdateQuantity(variantId, delta)
      "
    />
  </div>
</template>
