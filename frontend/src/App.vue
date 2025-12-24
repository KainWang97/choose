<script setup>
import { ref, computed, provide, onMounted, onUnmounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { api } from "./services/api.js";
import { setGetUserFunction, setAuthReady } from "./router/index.js";
import { encodeProductSlug } from "./utils/productSlug.js";

import Navbar from "./components/Navbar.vue";
import Footer from "./components/Footer.vue";
import AuthModal from "./components/AuthModal.vue";
import CartDrawer from "./components/CartDrawer.vue";
import ConfirmModal from "./components/ConfirmModal.vue";
import CartToast from "./components/CartToast.vue";
import ToastNotification from "./components/ToastNotification.vue";
import { useConfirm } from "./composables/useConfirm.js";
import { useToast } from "./composables/useToast.js";

const router = useRouter();
const route = useRoute();

// Confirm Modal
const { state: confirmState, handleConfirm, handleCancel } = useConfirm();

// Toast Notification
const { toast } = useToast();

// App Data State
const products = ref([]);
const categories = ref([]);
const inquiries = ref([]);
const allOrders = ref([]);

// Loading State
const isLoading = ref(true);
const loadError = ref(null);

// UI State
const isAuthOpen = ref(false);
const isCartOpen = ref(false);
const isScrolled = ref(false);
const showCartToast = ref(false);
const postLoginRedirect = ref(null);

// User Data State
const cart = ref([]);
const user = ref(null);

// New Arrivals (Featured) Products
const featuredProductIds = ref([]);
const newArrivalsProducts = ref([]);

// 設定 router guard 可以取得用戶資料
setGetUserFunction(() => user.value);

// Scroll Detection
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

const reloadProductsForCurrentRole = async () => {
  if (user.value?.role === "ADMIN") {
    products.value = await api.products.getAllAdmin();
  } else {
    products.value = await api.products.getAll();
  }
};

onMounted(async () => {
  window.addEventListener("scroll", handleScroll);
  window.addEventListener("auth:logout", handleForceLogout);

  try {
    isLoading.value = true;
    loadError.value = null;

    try {
      const currentUser = await api.auth.getMe();
      if (currentUser) {
        user.value = currentUser;
      }
    } catch (error) {
      console.log("Auto-login failed, token may be expired");
    } finally {
      // \u6a19\u8a18\u8a8d\u8b49\u521d\u59cb\u5316\u5b8c\u6210\uff0c\u8b93\u8def\u7531\u5b88\u885e\u53ef\u4ee5\u9032\u884c\u6b0a\u9650\u9a57\u8b49
      setAuthReady();
    }

    const categoriesData = await api.categories.getAll();
    categories.value = categoriesData;

    await reloadProductsForCurrentRole();

    if (user.value) {
      try {
        if (user.value.role === "ADMIN") {
          const [ordersData, inquiriesData] = await Promise.all([
            api.orders.getAll(),
            api.inquiries.getAll(),
          ]);
          allOrders.value = ordersData;
          inquiries.value = inquiriesData;
        } else {
          const ordersData = await api.orders.getMy();
          allOrders.value = ordersData;
          // 確保會員訂單資料也設置到 user.orders
          user.value = { ...user.value, orders: ordersData };
          // 載入會員購物車
          const cartData = await api.cart.getAll();
          cart.value = cartData;
        }
      } catch (error) {
        console.error("Failed to load user data:", error);
      }
    }

    // 只調用一次 API，從結果中提取 ID（優化：避免雙重請求）
    const featuredProducts = await api.featured.getProducts();
    featuredProductIds.value = featuredProducts.map((p) => p.id);
    newArrivalsProducts.value = featuredProducts;

    // 處理 URL query 參數 (例如 showLogin)
    if (route.query.showLogin === "true") {
      isAuthOpen.value = true;
      if (route.query.redirect) {
        postLoginRedirect.value = route.query.redirect;
      }
    }
  } catch (error) {
    console.error("Failed to load data:", error);
    loadError.value = "載入資料失敗，請重新整理頁面";
  } finally {
    isLoading.value = false;
  }
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
  window.removeEventListener("auth:logout", handleForceLogout);
});

// ============================================
// Navigation Handlers (使用 router)
// ============================================
const handleProductClick = (product) => {
  const slug = encodeProductSlug(product.id, product.name);
  router.push({ name: "product", params: { slug } });
};

const handleHomeClick = () => {
  router.push("/");
};

const handleCollectionClick = () => {
  router.push("/collection");
};

const handleSearch = (keyword) => {
  router.push({ path: "/collection", query: { search: keyword } });
};

const handleAdminDashboardClick = () => {
  router.push("/admin");
};

const handleContactClick = () => {
  router.push("/contact");
};

// ============================================
// Cart Logic (以 variantId 為單位)
// 登入會員：同步到資料庫；未登入：本地操作
// ============================================

// 載入會員購物車（並補充完整商品資訊）
const loadUserCart = async () => {
  if (!user.value) return;
  try {
    const cartItems = await api.cart.getAll();

    // 用 products 資料補充購物車項目的商品資訊
    const enrichedCartItems = cartItems.map((item) => {
      // 從 products 中找到對應的商品
      const realProduct = products.value.find((p) => p.id === item.product.id);
      if (realProduct) {
        // 用完整商品資訊替換
        const realVariant = realProduct.variants?.find(
          (v) => v.id === item.variant.id
        );
        return {
          ...item,
          cartItemId: item.cartItemId,
          product: realProduct,
          variant: realVariant || item.variant,
        };
      }
      return item;
    });

    cart.value = enrichedCartItems;
  } catch (error) {
    console.error("Failed to load cart:", error);
  }
};

// 合併本地購物車到資料庫（登入時使用）
const mergeLocalCartToDatabase = async () => {
  if (!user.value || cart.value.length === 0) return;

  try {
    for (const item of cart.value) {
      await api.cart.add(item.variant.id, item.quantity);
    }
    // 重新載入合併後的購物車
    await loadUserCart();
  } catch (error) {
    console.error("Failed to merge cart:", error);
  }
};

const handleAddToCart = async (product, variant) => {
  const stock = variant.stock || 0;
  if (stock <= 0) return;

  const existing = cart.value.find((item) => item.variant.id === variant.id);

  if (existing && existing.quantity + 1 > stock) {
    return;
  }

  // 已登入會員：同步到資料庫
  if (user.value) {
    try {
      await api.cart.add(variant.id, 1);
      await loadUserCart();
    } catch (error) {
      console.error("Failed to add to cart:", error);
    }
  } else {
    // 未登入：本地操作
    if (existing) {
      existing.quantity++;
    } else {
      cart.value.push({
        product,
        variant,
        quantity: 1,
      });
    }
  }
  toast.success("已加入購物車");
};

const handleRemoveFromCart = async (variantId) => {
  // 已登入會員：從資料庫刪除
  if (user.value) {
    const item = cart.value.find((item) => item.variant.id === variantId);
    if (item && item.cartItemId) {
      try {
        await api.cart.remove(item.cartItemId);
        await loadUserCart();
      } catch (error) {
        console.error("Failed to remove from cart:", error);
      }
    } else {
      // 備用：本地過濾
      cart.value = cart.value.filter((item) => item.variant.id !== variantId);
    }
  } else {
    // 未登入：本地操作
    cart.value = cart.value.filter((item) => item.variant.id !== variantId);
  }
};

const handleUpdateQuantity = async (variantId, delta) => {
  const item = cart.value.find((item) => item.variant.id === variantId);
  if (!item) return;

  const realTimeProduct = products.value.find((p) => p.id === item.product.id);
  const realTimeVariant = realTimeProduct?.variants?.find(
    (v) => v.id === variantId
  );
  const stock = realTimeVariant?.stock || 0;

  if (delta > 0 && item.quantity + delta > stock) return;

  const newQty = item.quantity + delta;
  if (newQty <= 0) {
    handleRemoveFromCart(variantId);
    return;
  }

  // 已登入會員：更新資料庫
  if (user.value && item.cartItemId) {
    try {
      await api.cart.updateQuantity(item.cartItemId, newQty);
      await loadUserCart();
    } catch (error) {
      console.error("Failed to update quantity:", error);
      item.quantity = newQty; // 備用：本地更新
    }
  } else {
    item.quantity = newQty;
  }
};

const handleSetQuantity = async (variantId, quantity) => {
  const item = cart.value.find((item) => item.variant.id === variantId);
  if (!item) return;

  const realTimeProduct = products.value.find((p) => p.id === item.product.id);
  const realTimeVariant = realTimeProduct?.variants?.find(
    (v) => v.id === variantId
  );
  const stock = realTimeVariant?.stock || 0;

  let newQty = Math.floor(quantity);
  if (isNaN(newQty) || newQty < 1) newQty = 1;
  if (stock > 0 && newQty > stock) newQty = stock;

  // 已登入會員：更新資料庫
  if (user.value && item.cartItemId) {
    try {
      await api.cart.updateQuantity(item.cartItemId, newQty);
      await loadUserCart();
    } catch (error) {
      console.error("Failed to set quantity:", error);
      item.quantity = newQty;
    }
  } else {
    item.quantity = newQty;
  }
};

const getCartQuantityForVariant = (variantId) => {
  const item = cart.value.find((i) => i.variant.id === variantId);
  return item ? item.quantity : 0;
};

const handleCheckoutStart = () => {
  if (!user.value) {
    postLoginRedirect.value = "/checkout";
    isCartOpen.value = false;
    isAuthOpen.value = true;
    return;
  }
  isCartOpen.value = false;
  router.push("/checkout");
};

const handlePlaceOrder = async (shippingDetails) => {
  try {
    const newOrder = await api.orders.create({
      items: cart.value,
      shippingDetails,
    });

    await reloadProductsForCurrentRole();
    if (user.value?.role === "ADMIN") {
      allOrders.value = await api.orders.getAll();
    } else {
      allOrders.value = await api.orders.getMy();
    }

    if (user.value) {
      user.value.orders.unshift(newOrder);
    }

    cart.value = [];

    if (user.value?.role === "ADMIN") {
      router.push("/admin?tab=orders");
    } else {
      // 導向會員中心
      router.push("/account");

      toast.success("訂單已成功建立！");

      if (shippingDetails.method === "BANK_TRANSFER") {
        setTimeout(() => {
          toast.info(
            "請於匯款後，在訂單備註中補充匯款帳號末五碼、匯款金額及匯款時間"
          );
        }, 500);
      }
    }
  } catch (error) {
    console.error("Failed to place order:", error);
  }
};

// ============================================
// Auth Logic
// ============================================
const handleAuthClose = () => {
  isAuthOpen.value = false;
  postLoginRedirect.value = null;
  // 清除 URL query
  if (route.query.showLogin) {
    router.replace({ query: {} });
  }
};

const handleLogin = async (loggedInUser) => {
  // 先保存跳轉路徑（因為 AuthModal close 事件會清除 postLoginRedirect）
  const savedRedirect = postLoginRedirect.value;

  // 保存登入前的本地購物車
  const localCart = [...cart.value];

  user.value = loggedInUser;
  isAuthOpen.value = false;

  await reloadProductsForCurrentRole();

  if (loggedInUser.role === "ADMIN") {
    // Admin 資料需要重新載入
    const [ordersData, inquiriesData] = await Promise.all([
      api.orders.getAll(),
      api.inquiries.getAll(),
    ]);
    allOrders.value = ordersData;
    inquiries.value = inquiriesData;

    if (savedRedirect) {
      router.push(savedRedirect);
    } else {
      router.push("/admin");
    }
    return;
  }

  // 一般會員：載入會員訂單與購物車
  try {
    const ordersData = await api.orders.getMy();
    allOrders.value = ordersData;
    user.value = { ...user.value, orders: ordersData };
  } catch (error) {
    console.error("Failed to load member orders:", error);
  }

  // 先載入資料庫購物車
  await loadUserCart();

  // 智慧合併：將本地購物車中「資料庫沒有的商品」加入資料庫
  if (localCart.length > 0) {
    try {
      // 取得資料庫購物車中已存在的 variant IDs
      const existingVariantIds = new Set(
        cart.value.map((item) => item.variant.id)
      );

      // 只加入資料庫中不存在的商品
      for (const item of localCart) {
        if (!existingVariantIds.has(item.variant.id)) {
          await api.cart.add(item.variant.id, item.quantity);
        }
      }

      // 重新載入合併後的購物車
      await loadUserCart();
    } catch (error) {
      console.error("Failed to merge local cart:", error);
    }
  }

  // 優先處理跳轉，避免資料重載導致的畫面問題
  if (savedRedirect) {
    toast.success("登入成功");
    router.push(savedRedirect);
    return; // 跳轉後不再執行後續邏輯
  }

  toast.success("登入成功");
};

const handleLogout = async () => {
  try {
    await api.auth.logout();
  } catch (error) {
    console.error("Logout error:", error);
  } finally {
    user.value = null;
    reloadProductsForCurrentRole();
    router.push("/");
    toast.success("登出成功");
  }
};

// 處理 401 強制登出（不顯示 Toast）
const handleForceLogout = () => {
  user.value = null;
  reloadProductsForCurrentRole();
  // 不顯示 Toast，因為這是認證失敗而不是主動登出
};

// ============================================
// Inquiry Logic
// ============================================
const handleContactSubmit = async (name, email, message) => {
  try {
    const newInquiry = await api.inquiries.create({ name, email, message });
    inquiries.value.unshift(newInquiry);
  } catch (error) {
    console.error("Failed to submit inquiry:", error);
  }
};

// ============================================
// Admin: Order & Inquiry
// ============================================
const handleUpdateOrderStatus = async (id, status) => {
  try {
    await api.orders.updateStatus(id, status);
    const order = allOrders.value.find((o) => o.id === id);
    if (order) order.status = status;

    if (user.value) {
      const userOrder = user.value.orders.find((o) => o.id === id);
      if (userOrder) userOrder.status = status;
    }
  } catch (error) {
    console.error("Failed to update order status:", error);
  }
};

const handleReplyInquiry = async (id) => {
  try {
    await api.inquiries.markAsReplied(id);
    const inquiry = inquiries.value.find((i) => i.id === id);
    if (inquiry) inquiry.status = "REPLIED";
  } catch (error) {
    console.error("Failed to reply inquiry:", error);
  }
};

const handleUnreplyInquiry = async (id) => {
  try {
    await api.inquiries.markAsUnreplied(id);
    const inquiry = inquiries.value.find((i) => i.id === id);
    if (inquiry) inquiry.status = "UNREAD";
  } catch (error) {
    console.error("Failed to unreply inquiry:", error);
  }
};

// ============================================
// Admin: Product CRUD
// ============================================
const handleCreateProduct = async (productData) => {
  try {
    const created = await api.products.create(productData);
    await reloadProductsForCurrentRole();
    return created;
  } catch (error) {
    console.error("Failed to create product:", error);
    throw error;
  }
};

const handleUpdateProduct = async (id, productData) => {
  try {
    const updated = await api.products.update(id, productData);
    if (updated) {
      await reloadProductsForCurrentRole();
    }
    return updated;
  } catch (error) {
    console.error("Failed to update product:", error);
    throw error;
  }
};

const handleDeleteProduct = async (id) => {
  try {
    const success = await api.products.delete(id);
    if (success) {
      await reloadProductsForCurrentRole();
    }
    return success;
  } catch (error) {
    console.error("Failed to delete product:", error);
    throw error;
  }
};

const handleUploadImage = async (file) => {
  try {
    const result = await api.products.uploadImage(file);
    return result.url;
  } catch (error) {
    console.error("Failed to upload image:", error);
    throw error;
  }
};

// ============================================
// Admin: Variant CRUD
// ============================================
const refreshSingleProduct = async (productId) => {
  try {
    const refreshed = await api.products.getById(productId);
    if (!refreshed) return;
    const idx = products.value.findIndex((p) => p.id === productId);
    if (idx !== -1) {
      products.value[idx] = refreshed;
    } else {
      products.value.push(refreshed);
    }
  } catch (error) {
    console.error("Failed to refresh product", productId, error);
  }
};

const handleCreateVariant = async (productId, data) => {
  try {
    const newVariant = await api.variants.create(productId, data);
    await refreshSingleProduct(productId);
    return newVariant;
  } catch (error) {
    console.error("Failed to create variant:", error);
    throw error;
  }
};

const handleUpdateVariant = async (id, data) => {
  try {
    const updated = await api.variants.update(id, data);
    if (updated) {
      await refreshSingleProduct(updated.productId);
    } else {
      await reloadProductsForCurrentRole();
    }
    return updated;
  } catch (error) {
    console.error("Failed to update variant:", error);
    throw error;
  }
};

const handleDeleteVariant = async (id) => {
  try {
    const success = await api.variants.delete(id);
    if (success) {
      await reloadProductsForCurrentRole();
    }
    return success;
  } catch (error) {
    console.error("Failed to delete variant:", error);
    throw error;
  }
};

// ============================================
// User: Payment Note
// ============================================
const handleUpdatePaymentNote = async (orderId, note) => {
  try {
    await api.orders.updatePaymentNote(orderId, note);

    // 根據用戶角色呼叫正確的 API
    if (user.value?.role === "ADMIN") {
      allOrders.value = await api.orders.getAll();
    } else {
      allOrders.value = await api.orders.getMy();
    }

    if (user.value) {
      const updatedOrders = user.value.orders.map((o) =>
        o.id === orderId ? { ...o, paymentNote: note } : o
      );
      user.value = { ...user.value, orders: updatedOrders };
    }
  } catch (error) {
    console.error("Failed to update payment note:", error);
    throw error;
  }
};

// ============================================
// Admin: Category CRUD
// ============================================
const handleCreateCategory = async (categoryData) => {
  try {
    const newCategory = await api.categories.create(categoryData);
    categories.value.push(newCategory);
  } catch (error) {
    console.error("Failed to create category:", error);
    throw error;
  }
};

const handleUpdateCategory = async (id, categoryData) => {
  try {
    const updated = await api.categories.update(id, categoryData);
    if (updated) {
      const index = categories.value.findIndex((c) => c.id === id);
      if (index !== -1) {
        categories.value[index] = updated;
      }
    }
  } catch (error) {
    console.error("Failed to update category:", error);
    throw error;
  }
};

const handleDeleteCategory = async (id) => {
  try {
    const success = await api.categories.delete(id);
    if (success) {
      categories.value = categories.value.filter((c) => c.id !== id);
    }
  } catch (error) {
    console.error("Failed to delete category:", error);
    throw error;
  }
};

// ============================================
// Admin: Featured Products
// ============================================
const handleToggleFeatured = async (productId) => {
  try {
    await api.featured.toggle(productId);

    // 重新載入產品列表以更新 isFeatured 狀態
    await reloadProductsForCurrentRole();

    // 重新載入新品上架列表（優化：單一 API 調用）
    const prods = await api.featured.getProducts();
    featuredProductIds.value = prods.map((p) => p.id);
    newArrivalsProducts.value = prods;
  } catch (error) {
    console.error("Failed to toggle featured:", error);
    toast.error("操作失敗：" + (error.message || "未知錯誤"));
  }
};

// ============================================
// Provide data and methods for child views
// ============================================
provide("products", products);
provide("categories", categories);
provide("allOrders", allOrders);
provide("inquiries", inquiries);
provide("featuredProductIds", featuredProductIds);
provide("newArrivalsProducts", newArrivalsProducts);
provide("cart", cart);
provide("user", user);

provide("handleProductClick", handleProductClick);
provide("handleAddToCart", handleAddToCart);
provide("handleContactSubmit", handleContactSubmit);
provide("handlePlaceOrder", handlePlaceOrder);
provide("handleUpdateOrderStatus", handleUpdateOrderStatus);
provide("handleReplyInquiry", handleReplyInquiry);
provide("handleUnreplyInquiry", handleUnreplyInquiry);
provide("handleCreateProduct", handleCreateProduct);
provide("handleUpdateProduct", handleUpdateProduct);
provide("handleDeleteProduct", handleDeleteProduct);
provide("handleUploadImage", handleUploadImage);
provide("handleCreateCategory", handleCreateCategory);
provide("handleUpdateCategory", handleUpdateCategory);
provide("handleDeleteCategory", handleDeleteCategory);
provide("handleCreateVariant", handleCreateVariant);
provide("handleUpdateVariant", handleUpdateVariant);
provide("handleDeleteVariant", handleDeleteVariant);
provide("handleToggleFeatured", handleToggleFeatured);
provide("handleLogout", handleLogout);
provide("handleUpdatePaymentNote", handleUpdatePaymentNote);
provide("handleUpdateQuantity", handleUpdateQuantity);
provide("toast", toast);

// 判斷是否隱藏 Footer (在 Admin 頁面)
const hideFooter = computed(() => route.path.startsWith("/admin"));
</script>

<template>
  <div
    class="min-h-screen flex flex-col bg-washi text-stone-900 font-sans selection:bg-stone-200 selection:text-sumi"
  >
    <Navbar
      :isScrolled="isScrolled"
      :user="user"
      :cartItems="cart"
      :isOnCollectionPage="route.path === '/collection'"
      @open-auth="isAuthOpen = true"
      @open-cart="isCartOpen = true"
      @open-account="router.push('/account')"
      @open-admin="handleAdminDashboardClick"
      @home="handleHomeClick"
      @collection="handleCollectionClick"
      @logout="handleLogout"
      @search="handleSearch"
    />

    <main class="flex-grow">
      <router-view />
    </main>

    <Footer v-if="!hideFooter" />

    <!-- Overlays -->
    <AuthModal
      :isOpen="isAuthOpen"
      @close="handleAuthClose"
      @login="handleLogin"
    />

    <CartDrawer
      :isOpen="isCartOpen"
      :cartItems="cart"
      :products="products"
      @close="isCartOpen = false"
      @remove-item="handleRemoveFromCart"
      @update-quantity="handleUpdateQuantity"
      @set-quantity="handleSetQuantity"
      @checkout="handleCheckoutStart"
    />

    <!-- Global Confirm Modal -->
    <ConfirmModal
      :isOpen="confirmState.isOpen"
      :title="confirmState.title"
      :message="confirmState.message"
      :confirmText="confirmState.confirmText"
      :cancelText="confirmState.cancelText"
      :variant="confirmState.variant"
      @confirm="handleConfirm"
      @cancel="handleCancel"
    />

    <!-- Cart Toast -->
    <CartToast :show="showCartToast" @close="showCartToast = false" />

    <!-- Global Toast Notification -->
    <ToastNotification />
  </div>
</template>
