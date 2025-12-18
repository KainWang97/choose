<script setup>
import { computed } from "vue";
import { getThumbnailUrl } from "../utils/imageHelper.js";

const props = defineProps({
  isOpen: Boolean,
  cartItems: Array,
  products: Array,
});

const emit = defineEmits([
  "close",
  "remove-item",
  "update-quantity",
  "set-quantity",
  "checkout",
]);

// Helper: 取得規格的即時庫存狀態
const getStockInfo = (item) => {
  // 從即時商品資料中找到對應的規格
  const realTimeProduct = props.products.find((p) => p.id === item.product.id);
  const realTimeVariant = realTimeProduct?.variants?.find(
    (v) => v.id === item.variant.id
  );
  const stock = realTimeVariant?.stock || 0;
  return {
    stock,
    isSoldOut: stock <= 0,
    isOverStock: stock > 0 && item.quantity > stock,
    isAtMax: stock > 0 && item.quantity === stock,
  };
};

// 全域驗證
const validation = computed(() => {
  let hasSoldOut = false;
  let hasInsufficient = false;
  props.cartItems.forEach((item) => {
    const { isSoldOut, isOverStock } = getStockInfo(item);
    if (isSoldOut) hasSoldOut = true;
    if (isOverStock) hasInsufficient = true;
  });
  return { hasSoldOut, hasInsufficient };
});

// 計算總價 (排除售罄商品)
const total = computed(() => {
  return props.cartItems.reduce((acc, item) => {
    const { isSoldOut } = getStockInfo(item);
    if (isSoldOut) return acc;
    return acc + item.product.price * item.quantity;
  }, 0);
});

const disableCheckout = computed(
  () =>
    props.cartItems.length === 0 ||
    validation.value.hasSoldOut ||
    validation.value.hasInsufficient
);

const handleInput = (variantId, event) => {
  const val = parseInt(event.target.value, 10);
  if (!isNaN(val) && val > 0) {
    emit("set-quantity", variantId, val);
  } else {
    emit("set-quantity", variantId, 1);
  }
};
</script>

<template>
  <!-- Backdrop -->
  <div
    @click="emit('close')"
    class="fixed inset-0 z-[70] bg-stone-900/30 backdrop-blur-sm transition-opacity duration-500"
    :class="
      isOpen
        ? 'opacity-100 pointer-events-auto'
        : 'opacity-0 pointer-events-none'
    "
  />

  <!-- Drawer -->
  <div
    class="fixed inset-y-0 right-0 z-[80] w-full max-w-md bg-washi shadow-2xl transform transition-transform duration-500 ease-in-out border-l border-stone-200 flex flex-col"
    :class="isOpen ? 'translate-x-0' : 'translate-x-full'"
  >
    <div
      class="p-6 md:p-8 flex justify-between items-center border-b border-stone-200"
    >
      <h2 class="font-serif text-2xl text-sumi">Your Selection</h2>
      <button
        @click="emit('close')"
        class="text-stone-400 hover:text-sumi transition-colors"
      >
        ✕
      </button>
    </div>

    <div class="flex-1 overflow-y-auto p-6 md:p-8 space-y-8">
      <div
        v-if="cartItems.length === 0"
        class="h-full flex flex-col items-center justify-center text-stone-500 space-y-4"
      >
        <p class="font-serif italic text-lg">Your collection is empty.</p>
        <button
          @click="emit('close')"
          class="text-xs uppercase tracking-widest border-b border-stone-400 pb-1"
        >
          Continue Browsing
        </button>
      </div>

      <div v-else v-for="item in cartItems" :key="item.variant.id">
        <div
          class="flex gap-4 animate-fade-in relative transition-all duration-300 p-3 -mx-3 rounded-sm"
          :class="
            getStockInfo(item).isSoldOut
              ? 'border-2 border-red-400 bg-red-50'
              : ''
          "
        >
          <!-- Sold Out Overlay -->
          <div
            v-if="getStockInfo(item).isSoldOut"
            class="absolute inset-0 z-10 flex items-center justify-center pointer-events-none"
          >
            <span
              class="bg-stone-800 text-washi px-3 py-1 text-[10px] uppercase tracking-widest font-bold transform -rotate-12 border border-washi"
            >
              Sold Out
            </span>
          </div>

          <div class="w-20 h-24 bg-stone-200 flex-shrink-0">
            <img
              :src="getThumbnailUrl(item.product.imageUrl, 200)"
              :alt="item.product.name"
              loading="lazy"
              class="w-full h-full object-cover"
            />
          </div>
          <div class="flex-1 flex flex-col justify-between py-1">
            <div class="flex justify-between items-start">
              <div>
                <h3 class="font-serif text-sumi">{{ item.product.name }}</h3>
                <!-- 顯示規格：顏色 / 尺寸 -->
                <p class="text-xs text-stone-500 uppercase tracking-wider mt-1">
                  {{ item.variant.color }} / {{ item.variant.size }}
                </p>
                <p
                  v-if="getStockInfo(item).isOverStock"
                  class="text-[10px] text-red-700 font-medium mt-1"
                >
                  Only {{ getStockInfo(item).stock }} available
                </p>
                <p
                  v-if="getStockInfo(item).isAtMax"
                  class="text-[10px] text-red-800 font-medium mt-1 animate-pulse"
                >
                  Max limit reached
                </p>
              </div>
              <p
                class="text-sm font-light"
                :class="
                  getStockInfo(item).isSoldOut
                    ? 'text-stone-400 line-through'
                    : 'text-sumi'
                "
              >
                ${{ item.product.price * item.quantity }}
              </p>
            </div>

            <div class="flex justify-between items-end">
              <div
                v-if="getStockInfo(item).isSoldOut"
                class="text-[10px] text-red-800 uppercase tracking-widest font-medium"
              >
                Item Unavailable
              </div>
              <div v-else class="flex items-center border border-stone-300">
                <button
                  @click="emit('update-quantity', item.variant.id, -1)"
                  class="px-2 py-1 text-stone-500 hover:bg-stone-100 transition-colors"
                >
                  -
                </button>

                <input
                  type="number"
                  min="1"
                  :max="getStockInfo(item).stock"
                  :value="item.quantity"
                  @input="handleInput(item.variant.id, $event)"
                  class="w-12 text-center bg-transparent text-xs font-mono focus:outline-none appearance-none m-0 [&::-webkit-inner-spin-button]:appearance-none"
                  :class="
                    getStockInfo(item).isOverStock || getStockInfo(item).isAtMax
                      ? 'text-red-800 font-bold'
                      : 'text-stone-800'
                  "
                />

                <button
                  @click="emit('update-quantity', item.variant.id, 1)"
                  class="px-2 py-1 text-stone-500 hover:bg-stone-100 transition-colors"
                  :class="{
                    'opacity-30 cursor-not-allowed':
                      item.quantity >= getStockInfo(item).stock,
                  }"
                  :disabled="item.quantity >= getStockInfo(item).stock"
                >
                  +
                </button>
              </div>

              <button
                @click="emit('remove-item', item.variant.id)"
                class="text-[10px] uppercase tracking-widest transition-colors"
                :class="
                  getStockInfo(item).isSoldOut
                    ? 'pointer-events-auto z-20 text-red-600 font-bold bg-red-100 px-2 py-1 rounded border border-red-300 hover:bg-red-200 animate-pulse'
                    : 'text-stone-400 hover:text-red-900'
                "
              >
                Remove
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="cartItems.length > 0"
      class="p-6 md:p-8 bg-stone-50 border-t border-stone-200 space-y-6"
    >
      <div class="flex justify-between items-end">
        <span class="text-xs uppercase tracking-widest text-stone-500"
          >Subtotal</span
        >
        <span class="font-serif text-2xl text-sumi">${{ total }}</span>
      </div>

      <div
        v-if="validation.hasSoldOut || validation.hasInsufficient"
        class="text-center bg-red-50 border border-red-100 p-2"
      >
        <p class="text-[10px] uppercase tracking-widest text-red-800">
          {{
            validation.hasSoldOut
              ? "Please remove sold out items"
              : "Quantity exceeds available stock"
          }}
        </p>
      </div>

      <button
        @click="emit('checkout')"
        :disabled="disableCheckout"
        class="w-full py-4 uppercase tracking-[0.2em] text-xs transition-colors"
        :class="
          disableCheckout
            ? 'bg-stone-300 text-stone-500 cursor-not-allowed'
            : 'bg-sumi text-washi hover:bg-stone-800'
        "
      >
        Proceed to Checkout
      </button>
    </div>
  </div>
</template>
