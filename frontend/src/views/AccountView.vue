<script setup>
/**
 * AccountView - 會員中心頁面
 * 包含訂單管理和個人資料
 */
import { ref, inject, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { userApi } from "../services/api.js";

const router = useRouter();

// 從 App.vue 注入資料和方法
const user = inject("user");
const handleUpdatePaymentNote = inject("handleUpdatePaymentNote");
const handleLogout = inject("handleLogout");

// Tab State
const activeTab = ref("orders");

// 訂單搜尋和篩選
const orderSearchQuery = ref("");
const orderStatusFilter = ref("ALL");

// 篩選後的訂單
const filteredOrders = computed(() => {
  if (!user.value?.orders) return [];

  return user.value.orders.filter((order) => {
    // 搜尋：依訂單編號
    const matchesSearch =
      orderSearchQuery.value === "" ||
      String(order.id).includes(orderSearchQuery.value);

    // 篩選：依處理進度
    const matchesStatus =
      orderStatusFilter.value === "ALL" ||
      order.status === orderStatusFilter.value;

    return matchesSearch && matchesStatus;
  });
});

// Payment Note Editing State
const editingNoteOrderId = ref(null);
const noteContent = ref("");
const isSavingNote = ref(false);

// 更改密碼 State
const passwordForm = ref({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});
const isChangingPassword = ref(false);
const passwordError = ref("");
const passwordSuccess = ref("");

// 刪除帳號 State
const showDeleteConfirm = ref(false);
const deleteForm = ref({
  password: "",
  confirmText: "",
});
const isDeletingAccount = ref(false);
const deleteError = ref("");

// 處理刪除帳號
const handleDeleteAccount = async () => {
  deleteError.value = "";

  // 驗證確認輸入
  if (deleteForm.value.confirmText !== "DELETE") {
    deleteError.value = "請輸入 DELETE 以確認刪除";
    return;
  }

  if (!deleteForm.value.password) {
    deleteError.value = "請輸入密碼";
    return;
  }

  isDeletingAccount.value = true;
  try {
    await userApi.deleteAccount(deleteForm.value.password);
    // 刪除成功，登出並跳轉首頁
    await handleLogout();
    router.push("/");
  } catch (error) {
    deleteError.value = error.message || "刪除帳號失敗，請稍後再試";
  } finally {
    isDeletingAccount.value = false;
  }
};

const cancelDeleteAccount = () => {
  showDeleteConfirm.value = false;
  deleteForm.value = { password: "", confirmText: "" };
  deleteError.value = "";
};

// 未登入則重導向首頁
onMounted(() => {
  if (!user.value) {
    router.push("/?showLogin=true&redirect=/account");
  }
});

const startEditingNote = (orderId, currentNote) => {
  editingNoteOrderId.value = orderId;
  noteContent.value = currentNote || "";
};

const cancelEditingNote = () => {
  editingNoteOrderId.value = null;
  noteContent.value = "";
};

const savePaymentNote = async (orderId) => {
  isSavingNote.value = true;
  await handleUpdatePaymentNote(orderId, noteContent.value);
  isSavingNote.value = false;
  editingNoteOrderId.value = null;
};

const doLogout = async () => {
  await handleLogout();
  router.push("/");
};

// 更改密碼處理
const handleChangePassword = async () => {
  passwordError.value = "";
  passwordSuccess.value = "";

  // 前端驗證
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = "新密碼與確認密碼不符";
    return;
  }

  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = "新密碼長度至少需要 6 個字元";
    return;
  }

  isChangingPassword.value = true;
  try {
    await userApi.changePassword(
      passwordForm.value.currentPassword,
      passwordForm.value.newPassword
    );
    passwordSuccess.value = "密碼更改成功";
    // 清空表單
    passwordForm.value = {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    };
  } catch (error) {
    passwordError.value = error.message || "密碼更改失敗，請稍後再試";
  } finally {
    isChangingPassword.value = false;
  }
};
</script>

<template>
  <div class="pt-24 min-h-screen bg-stone-50">
    <div class="max-w-4xl mx-auto px-6 py-12">
      <!-- Header -->
      <div class="mb-8 border-b border-stone-200 pb-8">
        <h1 class="text-3xl font-serif text-sumi mb-2">
          Welcome, {{ user?.name }}
        </h1>
        <p class="text-xs tracking-widest text-stone-500 uppercase">
          Member Dashboard
        </p>
      </div>

      <!-- Tabs -->
      <div class="flex border-b border-stone-200 mb-8">
        <button
          class="flex-1 py-4 text-xs uppercase tracking-widest transition-colors"
          :class="
            activeTab === 'orders'
              ? 'bg-stone-100 text-sumi border-b-2 border-sumi'
              : 'text-stone-400 hover:text-stone-600'
          "
          @click="activeTab = 'orders'"
        >
          Order History
        </button>
        <button
          class="flex-1 py-4 text-xs uppercase tracking-widest transition-colors"
          :class="
            activeTab === 'profile'
              ? 'bg-stone-100 text-sumi border-b-2 border-sumi'
              : 'text-stone-400 hover:text-stone-600'
          "
          @click="activeTab = 'profile'"
        >
          Profile
        </button>
      </div>

      <!-- Orders Tab -->
      <div v-if="activeTab === 'orders'" class="space-y-6">
        <!-- 匯款說明區塊 -->
        <div class="bg-amber-50 border border-amber-200 p-6 rounded-sm">
          <div class="flex items-start gap-3">
            <div
              class="flex-shrink-0 w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center"
            >
              <svg
                class="w-4 h-4 text-amber-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="text-sm font-medium text-amber-800 mb-2">匯款說明</h3>
              <p class="text-xs text-amber-700 mb-3">
                如選擇「郵局宅配/匯款」付款方式，請於下訂後將訂單金額匯至以下帳戶：
              </p>
              <div
                class="bg-white/60 p-3 rounded text-xs font-mono text-amber-900 space-y-1"
              >
                <p>
                  <span class="text-amber-600">銀行：</span>Choose Bank (808)
                </p>
                <p><span class="text-amber-600">帳號：</span>1234-5678-9012</p>
                <p>
                  <span class="text-amber-600">戶名：</span>Choose Select Ltd.
                </p>
              </div>
              <p class="text-xs text-amber-600 mt-3">
                ※
                匯款後請點擊下方訂單的「新增付款備註」填寫匯款資訊（帳號末五碼、金額、時間）
              </p>
            </div>
          </div>
        </div>

        <!-- 搜尋和篩選 -->
        <div class="flex flex-col sm:flex-row gap-4 mb-6">
          <div class="flex-1">
            <input
              v-model="orderSearchQuery"
              type="text"
              placeholder="搜尋訂單編號..."
              class="w-full px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi"
            />
          </div>
          <div>
            <select
              v-model="orderStatusFilter"
              class="px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi bg-white"
            >
              <option value="ALL">全部進度</option>
              <option value="PENDING">待付款</option>
              <option value="PAID">已付款</option>
              <option value="SHIPPED">已出貨</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>
          </div>
        </div>

        <p
          v-if="!user?.orders || user.orders.length === 0"
          class="text-stone-500 font-light italic"
        >
          No orders placed yet.
        </p>
        <p
          v-else-if="filteredOrders.length === 0"
          class="text-stone-500 font-light italic"
        >
          找不到符合條件的訂單
        </p>
        <div
          v-else
          v-for="order in filteredOrders"
          :key="order.id"
          class="bg-white border border-stone-200 p-6 shadow-sm"
        >
          <!-- 頂部：配送方式 + 訂單編號 + 日期 -->
          <div
            class="flex justify-between items-center mb-4 border-b border-stone-100 pb-4"
          >
            <div class="flex items-center gap-3">
              <!-- 配送方式（置頂） -->
              <span
                class="px-3 py-1.5 text-sm font-medium rounded border"
                :class="
                  order.shippingMethod === 'STORE_PICKUP' ||
                  order.shippingMethod === 'STORE'
                    ? 'bg-slate-100 text-slate-600 border-slate-300'
                    : 'bg-teal-50 text-teal-700 border-teal-200'
                "
              >
                {{
                  order.shippingMethod === "STORE_PICKUP" ||
                  order.shippingMethod === "STORE"
                    ? "店到店"
                    : "郵局宅配"
                }}
              </span>
              <span class="font-medium text-lg text-sumi"
                >訂單編號 #{{ order.id }}</span
              >
            </div>
            <span class="text-sm text-stone-400">{{
              order.date || order.createdAt
            }}</span>
          </div>

          <!-- 商品明細 -->
          <div class="space-y-3 mb-4">
            <div
              v-for="(item, idx) in order.items"
              :key="idx"
              class="flex justify-between text-sm font-light text-stone-600"
            >
              <span
                >{{ item.product?.name || item.productName }} ({{
                  item.variant?.color || item.color
                }}/{{ item.variant?.size || item.size }})
                <span class="text-stone-400">x{{ item.quantity }}</span></span
              >
              <span
                >${{
                  (item.price || item.variant?.price || 0) * item.quantity
                }}</span
              >
            </div>
          </div>

          <!-- 底部：訂單進度 + 金額 -->
          <div
            class="flex justify-between items-center pt-4 border-t border-stone-100"
          >
            <div class="flex items-center gap-2">
              <span class="text-xs text-stone-500">訂單進度：</span>
              <span
                class="text-sm px-2 py-1 font-medium rounded border border-stone-300 bg-stone-50 text-stone-700"
              >
                {{
                  order.status === "PENDING"
                    ? "待付款"
                    : order.status === "PAID"
                    ? "已付款"
                    : order.status === "SHIPPED"
                    ? "已出貨"
                    : order.status === "COMPLETED"
                    ? "已完成"
                    : order.status === "CANCELLED"
                    ? "已取消"
                    : order.status
                }}
              </span>
            </div>
            <span class="font-serif text-lg text-sumi"
              >總金額：${{ order.total }}</span
            >
          </div>

          <!-- Payment Note Section -->
          <div class="mt-6 pt-4 border-t border-stone-200">
            <div v-if="editingNoteOrderId === order.id" class="space-y-3">
              <label
                class="block text-xs uppercase tracking-widest text-stone-500"
                >訂單備註</label
              >
              <textarea
                v-model="noteContent"
                rows="4"
                placeholder="匯款範例：&#10;匯款帳號末五碼：12345&#10;匯款金額：$XXX&#10;匯款時間：2025/12/12 12:00"
                class="w-full border border-stone-300 p-3 text-sm focus:outline-none focus:border-sumi resize-none"
              ></textarea>
              <div class="flex gap-2 justify-end">
                <button
                  @click="cancelEditingNote"
                  :disabled="isSavingNote"
                  class="px-4 py-2 text-xs uppercase tracking-widest border border-stone-300 text-stone-600 hover:bg-stone-50 disabled:opacity-50"
                >
                  取消
                </button>
                <button
                  @click="savePaymentNote(order.id)"
                  :disabled="isSavingNote"
                  class="px-4 py-2 text-xs uppercase tracking-widest bg-sumi text-washi hover:bg-stone-800 disabled:opacity-70 flex items-center gap-2"
                >
                  <span
                    v-if="isSavingNote"
                    class="inline-block w-4 h-4 border-2 border-washi border-t-transparent rounded-full animate-spin"
                  ></span>
                  {{ isSavingNote ? "儲存中..." : "儲存" }}
                </button>
              </div>
            </div>

            <div v-else>
              <div v-if="order.paymentNote" class="space-y-2">
                <div class="flex justify-between items-start">
                  <span class="text-xs uppercase tracking-widest text-stone-500"
                    >付款備註</span
                  >
                  <button
                    @click="startEditingNote(order.id, order.paymentNote)"
                    class="text-xs text-stone-500 hover:text-sumi underline"
                  >
                    編輯
                  </button>
                </div>
                <div
                  class="bg-stone-50 p-3 text-sm text-stone-700 whitespace-pre-wrap font-light"
                >
                  {{ order.paymentNote }}
                </div>
              </div>

              <button
                v-else
                @click="startEditingNote(order.id)"
                class="text-xs text-stone-500 hover:text-sumi underline"
              >
                + 新增付款備註
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Tab -->
      <div v-else class="space-y-8 max-w-md">
        <!-- 基本資料 -->
        <div class="space-y-4">
          <h2
            class="text-sm uppercase tracking-widest text-stone-500 border-b border-stone-200 pb-2"
          >
            基本資料
          </h2>
          <div>
            <label
              class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
              >Name</label
            >
            <div
              class="w-full bg-white border border-stone-200 p-3 text-sm text-sumi"
            >
              {{ user?.name }}
            </div>
          </div>
          <div>
            <label
              class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
              >Email</label
            >
            <div
              class="w-full bg-white border border-stone-200 p-3 text-sm text-sumi"
            >
              {{ user?.email }}
            </div>
          </div>
        </div>

        <!-- 更改密碼 -->
        <div class="space-y-4">
          <h2
            class="text-sm uppercase tracking-widest text-stone-500 border-b border-stone-200 pb-2"
          >
            更改密碼
          </h2>
          <form @submit.prevent="handleChangePassword" class="space-y-4">
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
              >
                目前密碼
              </label>
              <input
                type="password"
                v-model="passwordForm.currentPassword"
                required
                placeholder="請輸入目前密碼"
                class="w-full bg-white border border-stone-300 p-3 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
              >
                新密碼
              </label>
              <input
                type="password"
                v-model="passwordForm.newPassword"
                required
                minlength="6"
                placeholder="請輸入新密碼（至少 6 個字元）"
                class="w-full bg-white border border-stone-300 p-3 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-500 mb-2"
              >
                確認新密碼
              </label>
              <input
                type="password"
                v-model="passwordForm.confirmPassword"
                required
                placeholder="請再次輸入新密碼"
                class="w-full bg-white border border-stone-300 p-3 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <p v-if="passwordError" class="text-sm text-red-600">
              {{ passwordError }}
            </p>
            <p v-if="passwordSuccess" class="text-sm text-green-600">
              {{ passwordSuccess }}
            </p>
            <button
              type="submit"
              :disabled="isChangingPassword"
              class="px-6 py-2 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800 transition-colors disabled:opacity-70 flex items-center gap-2"
            >
              <span
                v-if="isChangingPassword"
                class="inline-block w-4 h-4 border-2 border-washi border-t-transparent rounded-full animate-spin"
              ></span>
              {{ isChangingPassword ? "更新中..." : "更改密碼" }}
            </button>
          </form>
        </div>

        <!-- 刪除帳號 - 危險操作區 -->
        <div class="space-y-4 mt-8 pt-8 border-t-2 border-red-200">
          <h2
            class="text-sm uppercase tracking-widest text-red-600 border-b border-red-100 pb-2"
          >
            危險操作
          </h2>
          <div class="bg-red-50 border border-red-200 p-4 rounded">
            <p class="text-sm text-red-700 mb-4">
              刪除帳號後，您的登入資訊將被移除，個人資料將進行去識別化處理。<br />
              <strong>依《商業會計法》規定，訂單紀錄將保留 5 年。</strong>
            </p>
            <button
              v-if="!showDeleteConfirm"
              @click="showDeleteConfirm = true"
              class="px-4 py-2 border border-red-300 text-red-600 text-xs uppercase tracking-widest hover:bg-red-100 transition-colors"
            >
              刪除帳號
            </button>

            <!-- 刪除確認表單 -->
            <div
              v-else
              class="space-y-4 mt-4 p-4 bg-white border border-red-300 rounded"
            >
              <p class="text-sm text-red-800 font-medium">確認刪除帳號</p>
              <div>
                <label class="block text-xs text-stone-500 mb-1">
                  請輸入密碼以驗證身份
                </label>
                <input
                  type="password"
                  v-model="deleteForm.password"
                  placeholder="請輸入密碼"
                  class="w-full bg-white border border-stone-300 p-2 text-sm focus:outline-none focus:border-red-400"
                />
              </div>
              <div>
                <label class="block text-xs text-stone-500 mb-1">
                  請輸入
                  <span class="font-mono font-bold text-red-600">DELETE</span>
                  以確認
                </label>
                <input
                  type="text"
                  v-model="deleteForm.confirmText"
                  placeholder="DELETE"
                  class="w-full bg-white border border-stone-300 p-2 text-sm focus:outline-none focus:border-red-400 font-mono"
                />
              </div>
              <p v-if="deleteError" class="text-sm text-red-600">
                {{ deleteError }}
              </p>
              <div class="flex gap-2">
                <button
                  @click="cancelDeleteAccount"
                  :disabled="isDeletingAccount"
                  class="px-4 py-2 border border-stone-300 text-stone-600 text-xs uppercase tracking-widest hover:bg-stone-50 disabled:opacity-50"
                >
                  取消
                </button>
                <button
                  @click="handleDeleteAccount"
                  :disabled="
                    isDeletingAccount || deleteForm.confirmText !== 'DELETE'
                  "
                  class="px-4 py-2 bg-red-600 text-white text-xs uppercase tracking-widest hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  <span
                    v-if="isDeletingAccount"
                    class="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
                  ></span>
                  {{ isDeletingAccount ? "刪除中..." : "確認刪除帳號" }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 登出 -->
        <div class="pt-4 border-t border-stone-200">
          <button
            @click="doLogout"
            class="px-6 py-2 border border-stone-300 text-stone-500 text-xs uppercase tracking-widest hover:border-red-900 hover:text-red-900 transition-colors"
          >
            Log Out
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
