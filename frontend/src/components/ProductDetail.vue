<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { getLargeImageUrl } from "../utils/imageHelper.js";

const props = defineProps({
  product: Object,
  currentQuantityForVariant: Function,
});

const emit = defineEmits(["add-to-cart"]);

const addedAnimation = ref(false);
const isImageLoaded = ref(false);

// 選中的規格
const selectedVariant = ref(null);

// 取得所有可選的顏色
const availableColors = computed(() => {
  const colors = new Set(props.product.variants?.map((v) => v.color) || []);
  return Array.from(colors);
});

// 尺寸排序順序 (F → S → M → L → XL)
const sizeOrder = ["F", "S", "M", "L", "XL", "XXL", "XXXL"];

// 取得目前顏色下可選的尺寸（已排序）
const availableSizes = computed(() => {
  if (!selectedVariant.value) return [];
  const sizes =
    props.product.variants
      ?.filter((v) => v.color === selectedVariant.value?.color)
      .map((v) => v.size) || [];
  const uniqueSizes = Array.from(new Set(sizes));

  // 根據預定義順序排序
  return uniqueSizes.sort((a, b) => {
    const indexA = sizeOrder.indexOf(a.toUpperCase());
    const indexB = sizeOrder.indexOf(b.toUpperCase());
    // 如果尺寸不在預定義列表中，放到最後
    const orderA = indexA === -1 ? 999 : indexA;
    const orderB = indexB === -1 ? 999 : indexB;
    return orderA - orderB;
  });
});

// 是否只有單一規格
const isSingleVariant = computed(
  () => (props.product.variants?.length || 0) <= 1
);

// 初始化選擇第一個規格
onMounted(() => {
  window.scrollTo(0, 0);
  if (props.product.variants?.length) {
    selectedVariant.value = props.product.variants[0];
  }
});

// 監聽商品變化，重新選擇規格
watch(
  () => props.product,
  (newProduct) => {
    isImageLoaded.value = false; // 重置圖片載入狀態
    if (newProduct.variants?.length) {
      selectedVariant.value = newProduct.variants[0];
    } else {
      selectedVariant.value = null;
    }
  }
);

// 選擇顏色
const selectColor = (color) => {
  const variant = props.product.variants?.find((v) => v.color === color);
  if (variant) {
    selectedVariant.value = variant;
  }
};

// 選擇尺寸
const selectSize = (size) => {
  const variant = props.product.variants?.find(
    (v) => v.color === selectedVariant.value?.color && v.size === size
  );
  if (variant) {
    selectedVariant.value = variant;
  }
};

// 庫存計算
const stock = computed(() => selectedVariant.value?.stock || 0);
const isSoldOut = computed(() => stock.value <= 0);
const currentQty = computed(() =>
  selectedVariant.value
    ? props.currentQuantityForVariant(selectedVariant.value.id)
    : 0
);
const isMaxReached = computed(() => currentQty.value >= stock.value);
const canAdd = computed(
  () => selectedVariant.value && !isSoldOut.value && !isMaxReached.value
);

const handleAddToCart = () => {
  if (!selectedVariant.value || !canAdd.value) return;
  emit("add-to-cart", props.product, selectedVariant.value);
  addedAnimation.value = true;
  setTimeout(() => {
    addedAnimation.value = false;
  }, 2000);
};
</script>

<template>
  <div class="bg-washi">
    <div class="container mx-auto px-4 py-20">
      <div class="flex flex-col md:flex-row gap-8 items-start">
        <!-- Image Section -->
        <div
          class="w-full md:w-1/2 aspect-[3/4] md:aspect-auto md:h-auto relative overflow-hidden"
          :class="isImageLoaded ? 'bg-stone-100' : 'bg-stone-200'"
        >
          <!-- Loading Spinner -->
          <div
            v-if="!isImageLoaded"
            class="absolute inset-0 flex items-center justify-center"
          >
            <div
              class="w-10 h-10 border-2 border-stone-400 border-t-stone-600 rounded-full animate-spin"
            ></div>
          </div>

          <img
            :src="getLargeImageUrl(product.imageUrl)"
            :alt="product.name"
            @load="isImageLoaded = true"
            class="w-full h-full object-cover transition-opacity duration-500"
            :class="isImageLoaded ? 'opacity-100' : 'opacity-0'"
          />
          <div
            v-if="isSoldOut && isSingleVariant"
            class="absolute inset-0 bg-stone-900/10 flex items-center justify-center"
          >
            <span
              class="bg-sumi text-washi px-6 py-2 text-sm uppercase tracking-widest border border-washi transform -rotate-12"
            >
              Sold Out
            </span>
          </div>
        </div>

        <!-- Content Section -->
        <div class="w-full md:w-1/2 py-4 flex flex-col justify-center bg-washi">
          <div class="max-w-md mx-auto w-full space-y-8 animate-slide-up">
            <div class="space-y-2 border-l-2 border-stone-800 pl-6">
              <p class="text-xs tracking-[0.2em] uppercase text-stone-500">
                {{ product.category }}
              </p>
              <h2 class="text-4xl md:text-5xl font-serif text-sumi">
                {{ product.name }}
              </h2>
              <p class="text-xl font-light text-stone-800 pt-2">
                ${{ product.price }}
              </p>
            </div>

            <div
              class="py-8 space-y-6 text-stone-600 font-light leading-relaxed"
            >
              <p>{{ product.description }}</p>
            </div>

            <!-- Variant Selection -->
            <div
              v-if="product.variants?.length"
              class="space-y-4 border-t border-stone-200 pt-6"
            >
              <!-- 單一規格顯示 -->
              <div v-if="isSingleVariant && selectedVariant" class="space-y-2">
                <span
                  class="block text-xs uppercase tracking-wider text-stone-400"
                  >規格</span
                >
                <button
                  class="px-4 py-2 border-2 border-sumi bg-sumi text-washi text-sm"
                >
                  {{ selectedVariant.color }} / {{ selectedVariant.size }}
                </button>
                <p class="text-xs text-stone-500">
                  庫存：{{ selectedVariant.stock }}
                </p>
              </div>

              <!-- 多規格選擇 -->
              <template v-else>
                <!-- Color Selection -->
                <div class="space-y-2">
                  <span
                    class="block text-xs uppercase tracking-wider text-stone-400"
                    >顏色</span
                  >
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="color in availableColors"
                      :key="color"
                      @click="selectColor(color)"
                      class="px-4 py-2 border text-sm transition-all"
                      :class="
                        selectedVariant?.color === color
                          ? 'border-sumi bg-sumi text-washi'
                          : 'border-stone-300 hover:border-stone-500'
                      "
                    >
                      {{ color }}
                    </button>
                  </div>
                </div>

                <!-- Size Selection -->
                <div class="space-y-2">
                  <span
                    class="block text-xs uppercase tracking-wider text-stone-400"
                    >尺寸</span
                  >
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="size in availableSizes"
                      :key="size"
                      @click="selectSize(size)"
                      class="px-4 py-2 border text-sm transition-all min-w-[50px]"
                      :class="
                        selectedVariant?.size === size
                          ? 'border-sumi bg-sumi text-washi'
                          : 'border-stone-300 hover:border-stone-500'
                      "
                    >
                      {{ size }}
                    </button>
                  </div>
                </div>

                <!-- Selected Variant Info -->
                <div v-if="selectedVariant" class="text-xs text-stone-500 pt-2">
                  <span :class="stock < 5 ? 'text-red-500' : 'hidden'">
                    僅剩{{ stock }}件
                  </span>
                </div>
              </template>
            </div>

            <!-- Add to Cart -->
            <div class="space-y-2">
              <button
                @click="handleAddToCart"
                :disabled="!canAdd || addedAnimation"
                class="w-full py-4 uppercase tracking-[0.2em] text-xs transition-all duration-500 relative overflow-hidden"
                :class="
                  !canAdd
                    ? 'bg-stone-300 text-stone-500 cursor-not-allowed'
                    : addedAnimation
                    ? 'bg-stone-200 text-sumi'
                    : 'bg-sumi text-washi hover:bg-stone-800'
                "
              >
                <span
                  class="relative z-10"
                  :class="addedAnimation ? 'opacity-0' : 'opacity-100'"
                >
                  {{
                    !selectedVariant
                      ? "Select Size"
                      : isSoldOut
                      ? "Sold Out"
                      : isMaxReached
                      ? "庫存不足"
                      : "Add to Cart"
                  }}
                </span>
                <span
                  class="absolute inset-0 flex items-center justify-center z-10 transition-opacity duration-300"
                  :class="addedAnimation ? 'opacity-100' : 'opacity-0'"
                >
                  Added to Collection
                </span>
              </button>

              <!-- <p
                v-if="isMaxReached && !isSoldOut && selectedVariant"
                class="text-center text-[10px] text-red-800 uppercase tracking-widest animate-fade-in"
              >
                庫存不足 ({{ currentQty }}/{{ stock }})
              </p> -->
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
