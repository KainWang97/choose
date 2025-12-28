<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from "vue";
import { getLargeImageUrl } from "../utils/imageHelper.js";

const props = defineProps({
  product: Object,
  currentQuantityForVariant: Function,
});

const emit = defineEmits(["add-to-cart"]);

const addedAnimation = ref(false);
const isImageLoaded = ref(false);
const imageLoadError = ref(false);
const imageLoadTimeout = ref(null);
const currentImageIndex = ref(0); // 當前顯示的圖片索引

// 圖片快取：追蹤已載入的圖片 URL
const loadedImageCache = ref(new Set());

// 選中的規格
const selectedVariant = ref(null);

// 取得所有商品圖片（主圖 + 所有顏色的補充圖片）
const currentImages = computed(() => {
  const mainImage = props.product.image; // API 回傳欄位名稱是 'image'
  const colorImages = props.product.colorImages;
  const allImages = [mainImage];

  // 合併所有顏色的圖片
  if (colorImages) {
    Object.values(colorImages).forEach((urls) => {
      if (Array.isArray(urls)) {
        allImages.push(...urls);
      }
    });
  }

  // 去除重複的圖片 URL
  return [...new Set(allImages)].filter(Boolean);
});

// 當前顯示的圖片 URL
const currentImageUrl = computed(() => {
  return currentImages.value[currentImageIndex.value] || props.product.imageUrl;
});

// 檢查圖片是否已快取
const isImageCached = (url) => {
  return loadedImageCache.value.has(getLargeImageUrl(url));
};

// 預載入所有商品圖片
const preloadAllImages = () => {
  // 收集所有可能的圖片 URL
  const allUrls = new Set();
  allUrls.add(props.product.image);

  if (props.product.colorImages) {
    Object.values(props.product.colorImages).forEach((urls) => {
      urls.forEach((url) => allUrls.add(url));
    });
  }

  // 背景預載入
  allUrls.forEach((url) => {
    if (url && !isImageCached(url)) {
      const img = new Image();
      img.onload = () => {
        loadedImageCache.value.add(getLargeImageUrl(url));
      };
      img.src = getLargeImageUrl(url);
    }
  });
};

// 切換圖片（使用快取判斷是否需要 loading）
const selectImage = (index) => {
  currentImageIndex.value = index;
  const targetUrl = currentImages.value[index];

  // 如果圖片已快取，直接顯示
  if (isImageCached(targetUrl)) {
    isImageLoaded.value = true;
  } else {
    isImageLoaded.value = false;
    startImageLoadTimeout();
  }
};

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

// 設定圖片載入超時（10秒後強制顯示）
const startImageLoadTimeout = () => {
  clearImageLoadTimeout();
  imageLoadTimeout.value = setTimeout(() => {
    if (!isImageLoaded.value) {
      isImageLoaded.value = true; // 超時後強制顯示
      console.warn("圖片載入超時，強制顯示");
    }
  }, 10000);
};

const clearImageLoadTimeout = () => {
  if (imageLoadTimeout.value) {
    clearTimeout(imageLoadTimeout.value);
    imageLoadTimeout.value = null;
  }
};

// 圖片載入成功
const handleImageLoad = () => {
  clearImageLoadTimeout();
  isImageLoaded.value = true;
  imageLoadError.value = false;
  // 加入快取
  loadedImageCache.value.add(getLargeImageUrl(currentImageUrl.value));
};

// 圖片載入失敗
const handleImageError = () => {
  clearImageLoadTimeout();
  isImageLoaded.value = true; // 允許顯示錯誤狀態
  imageLoadError.value = true;
  console.error("圖片載入失敗:", currentImageUrl.value);
};

// 初始化選擇第一個規格
onMounted(() => {
  window.scrollTo(0, 0);
  if (props.product.variants?.length) {
    selectedVariant.value = props.product.variants[0];
  }
  startImageLoadTimeout();
  // 預載入所有商品圖片
  preloadAllImages();
});

// 清理計時器
onUnmounted(() => {
  clearImageLoadTimeout();
});

// 監聽商品變化，重新選擇規格
watch(
  () => props.product,
  (newProduct) => {
    isImageLoaded.value = false; // 重置圖片載入狀態
    imageLoadError.value = false;
    currentImageIndex.value = 0; // 重置圖片索引
    startImageLoadTimeout(); // 重新開始超時計時
    if (newProduct.variants?.length) {
      selectedVariant.value = newProduct.variants[0];
    } else {
      selectedVariant.value = null;
    }

    // 檢查新商品的初始圖片是否已快取
    const initialImageUrl = currentImages.value[0];
    if (isImageCached(initialImageUrl)) {
      isImageLoaded.value = true;
    } else {
      isImageLoaded.value = false;
      startImageLoadTimeout(); // 重新開始超時計時
    }
    // 預載入新商品的所有圖片
    preloadAllImages();
  }
);

// 監聽顏色變化（現在所有圖片都顯示，不需要重置圖片索引）
// 保留此 watcher 以便未來可能的擴展
watch(
  () => selectedVariant.value?.color,
  () => {
    // 顏色切換不再需要重置圖片，因為所有圖片都已顯示在縮圖列表中
  }
);

// 選擇顏色（並自動跳到該顏色的第一張圖片）
const selectColor = (color) => {
  const variant = props.product.variants?.find((v) => v.color === color);
  if (variant) {
    selectedVariant.value = variant;

    // 找到該顏色的第一張圖片在 currentImages 中的索引
    const colorImages = props.product.colorImages;
    if (colorImages && colorImages[color]?.length) {
      const firstColorImage = colorImages[color][0];
      const imageIndex = currentImages.value.findIndex(
        (url) => url === firstColorImage
      );
      if (imageIndex !== -1) {
        // 使用快取判斷是否顯示 loading
        if (isImageCached(firstColorImage)) {
          currentImageIndex.value = imageIndex;
          isImageLoaded.value = true;
        } else {
          currentImageIndex.value = imageIndex;
          isImageLoaded.value = false;
          startImageLoadTimeout();
        }
      }
    }
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
        <div class="w-full md:w-1/2 space-y-3">
          <!-- 主圖區域 -->
          <div
            class="aspect-[3/4] relative overflow-hidden"
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
              :src="getLargeImageUrl(currentImageUrl)"
              :alt="product.name"
              @load="handleImageLoad"
              @error="handleImageError"
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

          <!-- 縮圖列表 -->
          <div
            v-if="currentImages.length > 1"
            class="flex justify-center gap-2 flex-wrap pt-2"
          >
            <button
              v-for="(img, index) in currentImages"
              :key="index"
              @click="selectImage(index)"
              class="flex-shrink-0 w-16 h-20 border-2 transition-all overflow-hidden"
              :class="
                currentImageIndex === index
                  ? 'border-sumi'
                  : 'border-stone-200 hover:border-stone-400'
              "
            >
              <img
                :src="getLargeImageUrl(img)"
                :alt="`${product.name} - ${index + 1}`"
                class="w-full h-full object-cover"
              />
            </button>
          </div>
        </div>

        <!-- Content Section -->
        <div class="w-full md:w-1/2 py-4 flex flex-col justify-center bg-washi">
          <div class="max-w-md mx-auto w-full flex flex-col animate-slide-up">
            <!-- 標題/價格 (order-1) -->
            <div class="space-y-2 border-l-2 border-stone-800 pl-6 order-1">
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

            <!-- Variant Selection (行動版 order-2，桌面版 order-3) -->
            <div
              v-if="product.variants?.length"
              class="space-y-4 border-t border-stone-200 pt-6 mt-6 order-2 md:order-3"
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

            <!-- 描述 (行動版 order-3，桌面版 order-2) -->
            <div
              class="py-6 md:py-8 space-y-6 text-stone-600 font-light leading-relaxed order-3 md:order-2"
            >
              <p>{{ product.description }}</p>
            </div>

            <!-- Add to Cart (order-4) -->
            <div class="space-y-2 order-4">
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
