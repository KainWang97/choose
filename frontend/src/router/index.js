/**
 * Vue Router 設定
 * 包含路由定義和權限驗證
 */
import { createRouter, createWebHistory } from "vue-router";

// Views - 懶加載
const HomeView = () => import("../views/HomeView.vue");
const CollectionView = () => import("../views/CollectionView.vue");
const ProductView = () => import("../views/ProductView.vue");
const ContactView = () => import("../views/ContactView.vue");
const CheckoutView = () => import("../views/CheckoutView.vue");
const AdminView = () => import("../views/AdminView.vue");
const AccountView = () => import("../views/AccountView.vue");

// 路由定義
const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
    meta: { title: "Choose - 選擇有品味的生活" },
  },
  {
    path: "/collection",
    name: "collection",
    component: CollectionView,
    meta: { title: "Collection - Choose" },
  },
  {
    path: "/product/:slug",
    name: "product",
    component: ProductView,
    meta: { title: "Product - Choose" },
  },
  {
    path: "/contact",
    name: "contact",
    component: ContactView,
    meta: { title: "Contact - Choose" },
  },
  {
    path: "/checkout",
    name: "checkout",
    component: CheckoutView,
    meta: { title: "Checkout - Choose", requiresAuth: true },
  },
  {
    path: "/account",
    name: "account",
    component: AccountView,
    meta: { title: "My Account - Choose", requiresAuth: true },
  },
  {
    path: "/return-policy",
    name: "return-policy",
    component: () => import("../views/ReturnPolicyView.vue"),
    meta: { title: "退換貨說明 - Choose" },
  },
  {
    path: "/payment-guide",
    name: "payment-guide",
    component: () => import("../views/PaymentGuideView.vue"),
    meta: { title: "匯款說明 - Choose" },
  },
  {
    path: "/privacy",
    name: "privacy",
    component: () => import("../views/PrivacyPage.vue"),
    meta: { title: "隱私權政策 - Choose" },
  },
  {
    path: "/verify-email",
    name: "verify-email",
    component: () => import("../views/EmailVerificationView.vue"),
    meta: { title: "信箱驗證 - Choose" },
  },
  {
    path: "/login-verify",
    name: "login-verify",
    component: () => import("../views/LoginVerifyView.vue"),
    meta: { title: "登入驗證 - Choose" },
  },
  {
    path: "/forgot-password",
    name: "forgot-password",
    component: () => import("../views/ForgotPasswordView.vue"),
    meta: { title: "忘記密碼 - Choose" },
  },
  {
    path: "/reset-password",
    name: "reset-password",
    component: () => import("../views/ResetPasswordView.vue"),
    meta: { title: "重設密碼 - Choose" },
  },
  {
    path: "/admin",
    name: "admin",
    component: AdminView,
    meta: { title: "Admin Dashboard - Choose", requiresAdmin: true },
  },
];

// 建立 Router
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    return { top: 0 };
  },
});

// 用於存儲用戶資料的 getter (由 App.vue 提供)
let getUserFn = null;

// 認證初始化完成 Promise
let authReadyResolve = null;
const authReady = new Promise((resolve) => {
  authReadyResolve = resolve;
});

/**
 * 設定取得用戶的函數
 * @param {Function} fn
 */
export const setGetUserFunction = (fn) => {
  getUserFn = fn;
};

/**
 * 標記認證初始化完成
 * 應在 App.vue onMounted 完成 getMe() 後呼叫
 */
export const setAuthReady = () => {
  if (authReadyResolve) {
    authReadyResolve();
    authReadyResolve = null;
  }
};

// Navigation Guard - 權限驗證
router.beforeEach(async (to, from, next) => {
  // 更新頁面標題
  document.title = to.meta.title || "Choose";

  // 等待認證初始化完成（避免誤判未登入）
  await authReady;

  // 使用 user 物件判斷登入狀態（Token 儲存在 HttpOnly Cookie 中無法直接讀取）
  const user = getUserFn ? getUserFn() : null;
  const isLoggedIn = !!user;

  // 需要 Admin 權限的路由
  if (to.meta.requiresAdmin) {
    if (!isLoggedIn) {
      // 未登入 - 重導向首頁並顯示登入
      next({
        path: "/",
        query: { showLogin: "true", redirect: to.fullPath },
      });
      return;
    }

    if (user.role !== "ADMIN") {
      // 已登入但非 Admin - 重導向首頁
      next({
        path: "/",
        query: { error: "unauthorized" },
      });
      return;
    }
  }

  // 需要登入的路由
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({
      path: "/",
      query: { showLogin: "true", redirect: to.fullPath },
    });
    return;
  }

  next();
});

export default router;
