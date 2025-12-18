<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useConfirm } from "../composables/useConfirm.js";
import { getThumbnailUrl } from "../utils/imageHelper.js";

const props = defineProps({
  cartItems: Array,
  userEmail: String,
  userName: String,
  userPhone: String,
});

const emit = defineEmits(["place-order", "back", "update-quantity"]);

const { confirm } = useConfirm();

const step = ref("INFO");
const paymentMethod = ref("BANK_TRANSFER");
const isPlacingOrder = ref(false);

// 使用會員資料勾選框（預設打勾）
const useUserInfo = ref(true);

const formData = ref({
  fullName: "",
  email: "",
  phone: "",
  method: "BANK_TRANSFER",
  city: "",
  address: "",
  storeCode: "",
  storeName: "",
});

// 初始化時填入會員資料
onMounted(() => {
  if (useUserInfo.value) {
    fillUserInfo();
  }
});

// 監聽勾選框變化
watch(useUserInfo, (checked) => {
  if (checked) {
    fillUserInfo();
  } else {
    clearUserInfo();
  }
});

// 填入會員資料
const fillUserInfo = () => {
  formData.value.fullName = props.userName || "";
  formData.value.email = props.userEmail || "";
  formData.value.phone = props.userPhone || "";
};

// 清空使用者資料（但保留地址等其他欄位）
const clearUserInfo = () => {
  formData.value.fullName = "";
  formData.value.email = "";
  formData.value.phone = "";
};

// 計算總價 (使用新 CartItem 結構)
const total = computed(() =>
  props.cartItems.reduce(
    (acc, item) => acc + item.product.price * item.quantity,
    0
  )
);
const shippingCost = computed(() => (total.value > 1000 ? 0 : 60));
const finalTotal = computed(() => total.value + shippingCost.value);

// 驗證狀態
const validationErrors = ref({});

// 驗證表單
const validateContactInfo = () => {
  const errors = {};

  // Email 驗證
  if (!formData.value.email?.trim()) {
    errors.email = "請輸入電子郵件";
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.email = "請輸入有效的電子郵件格式";
  }

  // 姓名驗證
  if (!formData.value.fullName?.trim()) {
    errors.fullName = "請輸入收件人姓名";
  }

  // 電話驗證
  if (!formData.value.phone?.trim()) {
    errors.phone = "請輸入聯絡電話";
  } else if (!/^09\d{8}$/.test(formData.value.phone)) {
    errors.phone = "請輸入有效的手機號碼（例：0912345678）";
  }

  validationErrors.value = errors;
  return Object.keys(errors).length === 0;
};

// 驗證地址/店家資訊
const validateShippingInfo = () => {
  const errors = { ...validationErrors.value };

  if (paymentMethod.value === "BANK_TRANSFER") {
    // 驗證宅配地址
    if (!formData.value.city?.trim()) {
      errors.city = "請輸入縣市";
    } else {
      delete errors.city;
    }
    if (!formData.value.address?.trim()) {
      errors.address = "請輸入詳細地址";
    } else {
      delete errors.address;
    }
  } else {
    // 驗證店到店資訊
    if (!formData.value.storeCode?.trim()) {
      errors.storeCode = "請輸入店號";
    } else {
      delete errors.storeCode;
    }
    if (!formData.value.storeName?.trim()) {
      errors.storeName = "請輸入店名";
    } else {
      delete errors.storeName;
    }
  }

  validationErrors.value = errors;
  const shippingErrors = ["city", "address", "storeCode", "storeName"];
  return !shippingErrors.some((key) => errors[key]);
};

// 即時檢查配送資訊是否填寫完整
const isShippingValid = computed(() => {
  if (paymentMethod.value === "BANK_TRANSFER") {
    return formData.value.city?.trim() && formData.value.address?.trim();
  } else {
    return formData.value.storeCode?.trim() && formData.value.storeName?.trim();
  }
});

// 追蹤用戶是否嘗試過提交
const hasAttemptedSubmit = ref(false);

// 結帳按鈕是否可點選（只有嘗試過且無效時才禁用）
const canPlaceOrder = computed(() => {
  // 如果正在下單中，禁用按鈕
  if (isPlacingOrder.value) return false;
  // 如果還沒嘗試過提交，按鈕可點
  if (!hasAttemptedSubmit.value) return true;
  // 嘗試過後，根據資料有效性決定
  return isShippingValid.value;
});

const handleSubmitInfo = () => {
  if (!validateContactInfo()) {
    return;
  }
  step.value = "PAYMENT";
  window.scrollTo({ top: 0, behavior: "smooth" });
};

const handleFinalSubmit = async () => {
  // 標記用戶已嘗試提交
  hasAttemptedSubmit.value = true;

  // 驗證地址/店家資訊
  if (!validateShippingInfo()) {
    return;
  }

  // 下訂前確認
  const confirmed = await confirm({
    title: "確認下訂",
    message: `確定要送出此訂單嗎？\n\n訂單總金額：$${finalTotal.value}`,
    confirmText: "確認下訂",
    cancelText: "取消",
    variant: "default",
  });

  if (!confirmed) return;

  isPlacingOrder.value = true;
  emit("place-order", { ...formData.value, method: paymentMethod.value });
};
</script>

<template>
  <div class="min-h-screen bg-washi pt-32 pb-24 animate-fade-in">
    <div class="max-w-6xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-2 gap-16">
      <!-- Left Column: Forms -->
      <div>
        <button
          @click="emit('back')"
          class="text-xs uppercase tracking-widest text-stone-400 hover:text-sumi mb-8 flex items-center gap-2"
        >
          ← Back to Cart
        </button>

        <h1 class="text-3xl font-serif text-sumi mb-12">Checkout</h1>

        <form
          v-if="step === 'INFO'"
          @submit.prevent="handleSubmitInfo"
          class="space-y-8 animate-slide-up"
        >
          <h2
            class="text-sm uppercase tracking-widest text-stone-500 border-b border-stone-200 pb-2"
          >
            Contact Information
          </h2>

          <!-- 使用會員資料勾選框 -->
          <label class="flex items-center gap-3 cursor-pointer py-2">
            <input
              type="checkbox"
              v-model="useUserInfo"
              class="w-4 h-4 accent-sumi cursor-pointer"
            />
            <span class="text-sm text-stone-600"
              >使用會員資料作為收件人資訊</span
            >
          </label>

          <div class="space-y-6">
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                >Email</label
              >
              <input
                required
                type="email"
                v-model="formData.email"
                placeholder="您的電子郵件地址"
                class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                :class="
                  validationErrors.email
                    ? 'border-red-500'
                    : 'border-stone-300 focus:border-sumi'
                "
              />
              <p
                v-if="validationErrors.email"
                class="text-xs text-red-600 mt-1"
              >
                {{ validationErrors.email }}
              </p>
            </div>
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                >Full Name</label
              >
              <input
                required
                type="text"
                v-model="formData.fullName"
                placeholder="收件人姓名"
                class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                :class="
                  validationErrors.fullName
                    ? 'border-red-500'
                    : 'border-stone-300 focus:border-sumi'
                "
              />
              <p
                v-if="validationErrors.fullName"
                class="text-xs text-red-600 mt-1"
              >
                {{ validationErrors.fullName }}
              </p>
            </div>
            <div>
              <label
                class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                >Phone</label
              >
              <input
                required
                type="tel"
                v-model="formData.phone"
                placeholder="聯絡電話（例：0912345678）"
                class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                :class="
                  validationErrors.phone
                    ? 'border-red-500'
                    : 'border-stone-300 focus:border-sumi'
                "
              />
              <p
                v-if="validationErrors.phone"
                class="text-xs text-red-600 mt-1"
              >
                {{ validationErrors.phone }}
              </p>
            </div>
          </div>

          <div class="pt-8">
            <button
              type="submit"
              class="bg-sumi text-washi px-8 py-3 uppercase tracking-[0.2em] text-xs hover:bg-stone-800 transition-colors w-full md:w-auto cursor-pointer"
            >
              Continue to Payment
            </button>
          </div>
        </form>

        <form
          v-else
          @submit.prevent="handleFinalSubmit"
          class="space-y-10 animate-slide-up"
        >
          <!-- Payment Method Selection -->
          <div>
            <h2
              class="text-sm uppercase tracking-widest text-stone-500 border-b border-stone-200 pb-2 mb-6"
            >
              Payment & Delivery Method
            </h2>

            <div class="space-y-4">
              <label
                class="block border p-6 cursor-pointer transition-all duration-300"
                :class="
                  paymentMethod === 'BANK_TRANSFER'
                    ? 'border-sumi bg-stone-50'
                    : 'border-stone-200 opacity-60 hover:opacity-100'
                "
              >
                <div class="flex items-center gap-4">
                  <input
                    type="radio"
                    value="BANK_TRANSFER"
                    v-model="paymentMethod"
                    class="accent-sumi"
                  />
                  <div>
                    <span class="block font-serif text-lg"
                      >Bank Transfer (郵局宅配/匯款)</span
                    >
                    <span class="text-xs text-stone-500 font-light"
                      >Direct transfer. Delivery to home address.</span
                    >
                  </div>
                </div>
                <p
                  class="text-sm text-red-500 mt-3 leading-relaxed font-medium"
                >
                  請於匯款後，在訂單頁面提供匯款資訊，
                  <br />
                  我們將在收到匯款後進行訂單確認，煩請耐心等候。
                </p>
              </label>

              <label
                class="block border p-6 cursor-pointer transition-all duration-300"
                :class="
                  paymentMethod === 'STORE_PICKUP'
                    ? 'border-sumi bg-stone-50'
                    : 'border-stone-200 opacity-60 hover:opacity-100'
                "
              >
                <div class="flex items-center gap-4">
                  <input
                    type="radio"
                    value="STORE_PICKUP"
                    v-model="paymentMethod"
                    class="accent-sumi"
                  />
                  <div>
                    <span class="block font-serif text-lg"
                      >Store Pickup / COD (店到店貨到付款)</span
                    >
                    <span class="text-xs text-stone-500 font-light"
                      >Pay when you pick up at a convenience store.</span
                    >
                  </div>
                </div>
              </label>
            </div>
          </div>

          <!-- Dynamic Shipping Fields -->
          <div class="bg-white p-6 border border-stone-200">
            <div
              v-if="paymentMethod === 'BANK_TRANSFER'"
              class="space-y-6 animate-fade-in"
            >
              <h3 class="font-serif text-sumi">Shipping Address</h3>
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label
                    class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                    >City</label
                  >
                  <input
                    v-model="formData.city"
                    placeholder="縣市（例：台北市）"
                    class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                    :class="
                      validationErrors.city
                        ? 'border-red-500'
                        : 'border-stone-300 focus:border-sumi'
                    "
                  />
                  <p
                    v-if="validationErrors.city"
                    class="text-xs text-red-600 mt-1"
                  >
                    {{ validationErrors.city }}
                  </p>
                </div>
                <div></div>
              </div>
              <div>
                <label
                  class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                  >Address</label
                >
                <input
                  v-model="formData.address"
                  placeholder="詳細地址（例：信義區松高路1號10樓）"
                  class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                  :class="
                    validationErrors.address
                      ? 'border-red-500'
                      : 'border-stone-300 focus:border-sumi'
                  "
                />
                <p
                  v-if="validationErrors.address"
                  class="text-xs text-red-600 mt-1"
                >
                  {{ validationErrors.address }}
                </p>
              </div>

              <div
                class="bg-stone-100 border p-4 text-xs space-y-1 font-mono mt-4"
              >
                <p
                  class="uppercase tracking-widest text-stone-500 mb-2 font-semibold"
                >
                  請將訂單總金額匯入以下帳戶，若有任何問題，請聯繫我們。
                </p>
                <p class="text-sm">Bank: Choose Bank (808)</p>
                <p class="text-sm">Account: 1234-5678-9012</p>
                <p class="text-sm">Name: Choose Select Ltd.</p>
              </div>
            </div>

            <div v-else class="space-y-6 animate-fade-in">
              <h3 class="font-serif text-sumi">Store Details</h3>
              <p class="text-xs text-stone-500">
                請提供便利商店的店號以及店名。
              </p>

              <div>
                <label
                  class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                  >Store Code (店號)</label
                >
                <input
                  v-model="formData.storeCode"
                  placeholder="e.g. 123456"
                  class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                  :class="
                    validationErrors.storeCode
                      ? 'border-red-500'
                      : 'border-stone-300 focus:border-sumi'
                  "
                />
                <p
                  v-if="validationErrors.storeCode"
                  class="text-xs text-red-600 mt-1"
                >
                  {{ validationErrors.storeCode }}
                </p>
              </div>
              <div>
                <label
                  class="block text-xs uppercase tracking-widest text-stone-400 mb-2"
                  >Store Name (店名)</label
                >
                <input
                  v-model="formData.storeName"
                  placeholder="e.g. 7-11 Shinjuku Branch"
                  class="w-full bg-transparent border-b py-2 focus:outline-none transition-colors rounded-none"
                  :class="
                    validationErrors.storeName
                      ? 'border-red-500'
                      : 'border-stone-300 focus:border-sumi'
                  "
                />
                <p
                  v-if="validationErrors.storeName"
                  class="text-xs text-red-600 mt-1"
                >
                  {{ validationErrors.storeName }}
                </p>
              </div>
            </div>
          </div>

          <!-- 提示訊息 -->
          <p
            v-if="hasAttemptedSubmit && !isShippingValid"
            class="text-sm text-red-500 text-center animate-fade-in"
          >
            請完整填寫以上配送資訊
          </p>

          <div class="flex gap-4 pt-4">
            <button
              type="button"
              @click="step = 'INFO'"
              :disabled="isPlacingOrder"
              class="px-6 py-3 border border-stone-300 text-stone-500 uppercase tracking-widest text-xs hover:border-sumi hover:text-sumi transition-colors disabled:opacity-50"
            >
              Back
            </button>
            <button
              type="submit"
              :disabled="!canPlaceOrder"
              class="flex-1 px-8 py-3 uppercase tracking-[0.2em] text-xs flex items-center justify-center gap-2 transition-all duration-300"
              :class="[
                canPlaceOrder
                  ? 'bg-sumi text-washi hover:bg-stone-700 hover:scale-[1.02] hover:shadow-lg active:scale-[0.98] cursor-pointer'
                  : 'bg-stone-300 text-stone-500 cursor-not-allowed',
              ]"
            >
              <span
                v-if="isPlacingOrder"
                class="inline-block w-4 h-4 border-2 border-washi border-t-transparent rounded-full animate-spin"
              ></span>
              {{
                isPlacingOrder ? "結帳中..." : `Place Order — $${finalTotal}`
              }}
            </button>
          </div>
        </form>
      </div>

      <!-- Right Column: Order Summary -->
      <div class="bg-stone-50 p-8 h-fit border border-stone-200">
        <h2 class="font-serif text-xl text-sumi mb-6">訂單總覽</h2>

        <div class="space-y-4 max-h-[400px] overflow-y-auto pr-2 mb-6">
          <div
            v-for="item in cartItems"
            :key="item.variant.id"
            class="flex gap-4"
          >
            <div class="w-16 h-20 bg-stone-200 flex-shrink-0">
              <img
                :src="getThumbnailUrl(item.product.imageUrl, 200)"
                :alt="item.product.name"
                loading="lazy"
                class="w-full h-full object-cover"
              />
            </div>
            <div class="flex-1 py-1">
              <div class="flex justify-between items-start">
                <h3 class="font-serif text-sm text-sumi">
                  {{ item.product.name }}
                </h3>
                <p class="text-sm font-light text-stone-600">
                  ${{ item.product.price * item.quantity }}
                </p>
              </div>
              <!-- 顯示規格資訊 -->
              <p class="text-xs text-stone-400 mt-1">
                {{ item.variant.color }} / {{ item.variant.size }}
              </p>
              <!-- 數量控制 -->
              <div class="flex items-center gap-2 mt-2">
                <button
                  type="button"
                  @click="emit('update-quantity', item.variant.id, -1)"
                  class="w-6 h-6 flex items-center justify-center border border-stone-300 text-stone-500 hover:border-sumi hover:text-sumi transition-colors"
                >
                  −
                </button>
                <span class="text-sm text-stone-600 w-6 text-center">{{
                  item.quantity
                }}</span>
                <button
                  type="button"
                  @click="emit('update-quantity', item.variant.id, 1)"
                  class="w-6 h-6 flex items-center justify-center border border-stone-300 text-stone-500 hover:border-sumi hover:text-sumi transition-colors"
                >
                  +
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="border-t border-stone-200 pt-6 space-y-2">
          <div class="flex justify-between text-sm text-stone-600">
            <span>商品總計</span>
            <span>${{ total }}</span>
          </div>
          <div class="flex justify-between text-sm text-stone-600">
            <span>運送費用</span>
            <span>{{ shippingCost === 0 ? "Free" : `$${shippingCost}` }}</span>
          </div>
          <div
            class="flex justify-between items-end pt-4 border-t border-stone-200 mt-4"
          >
            <span class="text-xs uppercase tracking-widest text-stone-500"
              >總金額</span
            >
            <span class="font-serif text-2xl text-sumi">${{ finalTotal }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
