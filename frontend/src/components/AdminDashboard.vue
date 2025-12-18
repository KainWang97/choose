<script setup>
import { ref, computed, watch, inject } from "vue";
import { useConfirm } from "../composables/useConfirm.js";

const { confirm } = useConfirm();

// 注入圖片上傳方法
const uploadImageHandler = inject("handleUploadImage");

const props = defineProps({
  products: Array,
  categories: Array,
  orders: Array,
  inquiries: Array,
  members: Array,
  featuredProductIds: Array,
  initialTab: String,
});

const emit = defineEmits([
  "update-order-status",
  "reply-inquiry",
  "create-product",
  "update-product",
  "delete-product",
  "upload-image",
  "create-category",
  "update-category",
  "delete-category",
  "create-variant",
  "update-variant",
  "delete-variant",
  "toggle-featured",
]);

// ============================================
// 未讀/待處理計數
// ============================================
const unreadInquiriesCount = computed(() => {
  if (!props.inquiries) return 0;
  return props.inquiries.filter((i) => i.status !== "REPLIED").length;
});

const pendingOrdersCount = computed(() => {
  if (!props.orders) return 0;
  return props.orders.filter(
    (o) => o.status === "PENDING" || o.status === "PAID"
  ).length;
});

// 從產品的 isFeatured 屬性計算，確保與後端同步
const computedFeaturedIds = computed(() => {
  if (!props.products) return [];
  return props.products.filter((p) => p.isFeatured === true).map((p) => p.id);
});

// 使用 computedFeaturedIds 而非 prop，確保即時同步
const featuredProductIds = computed(() => {
  return computedFeaturedIds.value;
});

const activeTab = ref(props.initialTab ?? "INVENTORY");

watch(
  () => props.initialTab,
  (next) => {
    if (next) activeTab.value = next;
  }
);

// ============================================
// Product Form State
// ============================================
const isFormOpen = ref(false);
const editingProductId = ref(null);
const formData = ref({
  name: "",
  price: 0,
  categoryId: "",
  description: "",
  imageUrl: "",
  isListed: false,
});

// Image Upload State
const isUploading = ref(false);
const uploadError = ref(false);
const pendingFile = ref(null);
const previewUrl = ref(null);

// ============================================
// Variant Form State
// ============================================
const isVariantFormOpen = ref(false);
const editingVariantId = ref(null);
const currentProductForVariant = ref(null);
const isVariantSubmitting = ref(false);

const activeVariantProduct = computed(() => {
  if (!currentProductForVariant.value) return null;
  return (
    props.products.find((p) => p.id === currentProductForVariant.value?.id) ||
    currentProductForVariant.value
  );
});

const variantFormData = ref({
  color: "",
  size: "",
  stock: 0,
});

// ============================================
// Category Form State
// ============================================
const isCategoryFormOpen = ref(false);
const editingCategoryId = ref(null);
const categoryFormData = ref({
  name: "",
  description: "",
});
const categoryError = ref("");

// ============================================
// Category CRUD
// ============================================
const openNewCategoryForm = () => {
  editingCategoryId.value = null;
  categoryFormData.value = { name: "", description: "" };
  categoryError.value = "";
  isCategoryFormOpen.value = true;
};

const openEditCategoryForm = (category) => {
  editingCategoryId.value = category.id;
  categoryFormData.value = {
    name: category.name,
    description: category.description || "",
  };
  categoryError.value = "";
  isCategoryFormOpen.value = true;
};

const closeCategoryForm = () => {
  isCategoryFormOpen.value = false;
  editingCategoryId.value = null;
  categoryError.value = "";
};

const validateCategoryName = (name) => {
  return /^[A-Za-z\s]+$/.test(name);
};

const submitCategoryForm = async () => {
  if (!validateCategoryName(categoryFormData.value.name)) {
    categoryError.value = "分類名稱只能使用英文字母";
    return;
  }
  try {
    if (editingCategoryId.value) {
      await emit(
        "update-category",
        editingCategoryId.value,
        categoryFormData.value
      );
    } else {
      await emit("create-category", categoryFormData.value);
    }
    closeCategoryForm();
  } catch (error) {
    console.error("Failed to save category:", error);
    categoryError.value = "儲存失敗，請重試";
  }
};

const confirmDeleteCategory = async (category) => {
  const confirmed = await confirm({
    title: "刪除分類",
    message: `確定要刪除分類 "${category.name}" 嗎？`,
    confirmText: "刪除",
    cancelText: "取消",
    variant: "danger",
  });
  if (confirmed) {
    emit("delete-category", category.id);
  }
};

// ============================================
// Product CRUD
// ============================================
const getCategoryName = (categoryId) => {
  const cat = props.categories.find((c) => c.id === categoryId);
  return cat?.name || categoryId;
};

const openNewProductForm = () => {
  editingProductId.value = null;
  formData.value = {
    name: "",
    price: 0,
    categoryId: props.categories[0]?.id || "",
    description: "",
    imageUrl: "",
    isListed: false,
  };
  previewUrl.value = null;
  pendingFile.value = null;
  uploadError.value = false;
  isFormOpen.value = true;
};

const openEditForm = (product) => {
  editingProductId.value = product.id;
  formData.value = {
    name: product.name,
    price: product.price,
    categoryId: product.categoryId,
    description: product.description,
    imageUrl: product.imageUrl,
    isListed: product.isListed,
  };
  previewUrl.value = product.imageUrl || null;
  pendingFile.value = null;
  uploadError.value = false;
  isFormOpen.value = true;
};

const closeForm = () => {
  isFormOpen.value = false;
  editingProductId.value = null;
  pendingFile.value = null;
  previewUrl.value = null;
  uploadError.value = false;
};

const handleFileSelect = (event) => {
  const input = event.target;
  if (input.files && input.files[0]) {
    pendingFile.value = input.files[0];
    previewUrl.value = URL.createObjectURL(input.files[0]);
    uploadError.value = false;
  }
};

const uploadImage = async () => {
  if (!pendingFile.value) return;
  isUploading.value = true;
  uploadError.value = false;
  try {
    // 使用 inject 的處理器直接調用，確保取得返回值
    const url = await uploadImageHandler(pendingFile.value);
    formData.value.imageUrl = url;
    previewUrl.value = url;
    pendingFile.value = null;
  } catch (error) {
    console.error("Upload failed:", error);
    uploadError.value = true;
  } finally {
    isUploading.value = false;
  }
};

const submitForm = async () => {
  if (pendingFile.value && !uploadError.value) {
    await uploadImage();
    if (uploadError.value) return;
  }

  const category = props.categories.find(
    (c) => c.id === formData.value.categoryId
  );
  const productData = {
    name: formData.value.name,
    price: formData.value.price,
    categoryId: formData.value.categoryId,
    category: category?.name || "",
    description: formData.value.description,
    imageUrl: formData.value.imageUrl,
    isListed: formData.value.isListed,
  };

  try {
    if (editingProductId.value) {
      await emit("update-product", editingProductId.value, productData);
    } else {
      await emit("create-product", productData);
    }
    closeForm();
  } catch (error) {
    console.error("Failed to save product:", error);
  }
};

const confirmDelete = async (product) => {
  const confirmed = await confirm({
    title: "刪除商品",
    message: `確定要刪除 "${product.name}" 嗎？此操作無法復原。`,
    confirmText: "刪除",
    cancelText: "取消",
    variant: "danger",
  });
  if (confirmed) {
    emit("delete-product", product.id);
  }
};

// ============================================
// Variant CRUD
// ============================================
const openVariantManager = (product) => {
  currentProductForVariant.value = product;
  editingVariantId.value = null;
  variantFormData.value = { color: "", size: "", stock: 0 };
  isVariantFormOpen.value = true;
};

const closeVariantForm = () => {
  isVariantFormOpen.value = false;
  currentProductForVariant.value = null;
  editingVariantId.value = null;
};

const editVariant = (variant) => {
  editingVariantId.value = variant.id;
  variantFormData.value = {
    color: variant.color,
    size: variant.size,
    stock: variant.stock,
  };
};

const cancelEditVariant = () => {
  editingVariantId.value = null;
  variantFormData.value = { color: "", size: "", stock: 0 };
};

const submitVariantForm = async () => {
  if (!currentProductForVariant.value) return;
  isVariantSubmitting.value = true;

  // 記錄開始時間，確保載入動畫至少顯示 800ms
  const startTime = Date.now();
  const MIN_LOADING_TIME = 800;

  try {
    if (editingVariantId.value) {
      emit("update-variant", editingVariantId.value, variantFormData.value);
    } else {
      emit(
        "create-variant",
        currentProductForVariant.value.id,
        variantFormData.value
      );
    }

    // 等待最小載入時間
    const elapsed = Date.now() - startTime;
    if (elapsed < MIN_LOADING_TIME) {
      await new Promise((resolve) =>
        setTimeout(resolve, MIN_LOADING_TIME - elapsed)
      );
    }

    cancelEditVariant();
  } catch (error) {
    console.error("Failed to save variant:", error);
  } finally {
    isVariantSubmitting.value = false;
  }
};

const confirmDeleteVariant = async (variant) => {
  const confirmed = await confirm({
    title: "刪除規格",
    message: `確定要刪除規格 "${variant.color} / ${variant.size}" 嗎？`,
    confirmText: "刪除",
    cancelText: "取消",
    variant: "danger",
  });
  if (confirmed) {
    emit("delete-variant", variant.id);
  }
};

// Helper: 取得訂單項目顯示文字
const getOrderItemsText = (order) => {
  return order.items
    .map(
      (item) =>
        `${item.product.name} (${item.variant.color}/${item.variant.size}) x${item.quantity}`
    )
    .join(", ");
};

// ============================================
// 訂單搜尋和篩選
// ============================================
const orderSearchQuery = ref("");
const orderStatusFilter = ref("ALL");
const orderSortOrder = ref("DESC"); // DESC=新到舊, ASC=舊到新

const filteredOrders = computed(() => {
  if (!props.orders) return [];

  let result = props.orders.filter((order) => {
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

  // 排序：依建立時間
  result.sort((a, b) => {
    const dateA = new Date(a.createdAt || 0);
    const dateB = new Date(b.createdAt || 0);
    return orderSortOrder.value === "DESC" ? dateB - dateA : dateA - dateB;
  });

  return result;
});

// ============================================
// 商品搜尋和篩選
// ============================================
const productSearchQuery = ref("");
const productCategoryFilter = ref("ALL");
const productSortOrder = ref("DESC"); // DESC=新到舊, ASC=舊到新

const filteredProducts = computed(() => {
  if (!props.products) return [];

  let result = props.products.filter((product) => {
    // 搜尋：產品名稱、SKU（使用 variant.skuCode）
    const query = productSearchQuery.value.toLowerCase().trim();
    const matchesSearch =
      query === "" ||
      product.name?.toLowerCase().includes(query) ||
      product.variants?.some((v) => v.skuCode?.toLowerCase().includes(query));

    // 篩選：依種類
    const matchesCategory =
      productCategoryFilter.value === "ALL" ||
      product.categoryId === productCategoryFilter.value;

    return matchesSearch && matchesCategory;
  });

  // 排序：依建立時間
  result.sort((a, b) => {
    const dateA = new Date(a.createdAt || 0);
    const dateB = new Date(b.createdAt || 0);
    return productSortOrder.value === "DESC" ? dateB - dateA : dateA - dateB;
  });

  return result;
});

// ============================================
// 銷售統計
// ============================================
const salesStats = computed(() => {
  if (!props.orders)
    return { totalRevenue: 0, orderCount: 0, avgOrderValue: 0 };

  // 只計算已完成或已出貨的訂單
  const validOrders = props.orders.filter(
    (o) =>
      o.status === "COMPLETED" || o.status === "SHIPPED" || o.status === "PAID"
  );

  // 使用 total 欄位（非 totalAmount）
  const totalRevenue = validOrders.reduce((sum, o) => sum + (o.total || 0), 0);
  const orderCount = validOrders.length;
  const avgOrderValue =
    orderCount > 0 ? Math.round(totalRevenue / orderCount) : 0;

  return { totalRevenue, orderCount, avgOrderValue };
});

// 訂單狀態統計
const orderStatusStats = computed(() => {
  if (!props.orders) return {};
  const stats = {};
  props.orders.forEach((o) => {
    stats[o.status] = (stats[o.status] || 0) + 1;
  });
  return stats;
});

// 熱銷商品 (依訂單中出現次數)
const topSellingProducts = computed(() => {
  if (!props.orders) return [];
  const productCount = {};

  props.orders.forEach((order) => {
    if (order.items) {
      order.items.forEach((item) => {
        const name = item.product?.name || item.productName || "Unknown";
        productCount[name] = (productCount[name] || 0) + (item.quantity || 1);
      });
    }
  });

  return Object.entries(productCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([name, count]) => ({ name, count }));
});

// 月銷量分析
const monthlySales = computed(() => {
  if (!props.orders) return [];

  const monthlyData = {};

  props.orders.forEach((order) => {
    // 只計算有效訂單
    if (order.status === "CANCELLED") return;

    const date = new Date(order.createdAt);
    if (isNaN(date.getTime())) return;

    const monthKey = `${date.getFullYear()}-${String(
      date.getMonth() + 1
    ).padStart(2, "0")}`;

    if (!monthlyData[monthKey]) {
      monthlyData[monthKey] = { revenue: 0, count: 0 };
    }
    monthlyData[monthKey].revenue += order.total || 0;
    monthlyData[monthKey].count += 1;
  });

  // 轉換為陣列並排序（最近的月份在前）
  return Object.entries(monthlyData)
    .map(([month, data]) => ({
      month,
      label: month.replace("-", "年") + "月",
      revenue: data.revenue,
      count: data.count,
    }))
    .sort((a, b) => b.month.localeCompare(a.month))
    .slice(0, 6); // 最近 6 個月
});
</script>

<template>
  <div class="min-h-screen bg-stone-100 pt-32 pb-24 px-6 animate-fade-in">
    <div class="max-w-7xl mx-auto">
      <div class="flex justify-between items-end mb-12">
        <div>
          <h1 class="text-3xl font-serif text-sumi mb-2">Admin Dashboard</h1>
          <p class="text-xs uppercase tracking-widest text-stone-500">
            Store Management System
          </p>
        </div>
        <div class="flex gap-1 bg-white p-1 rounded-sm border border-stone-200">
          <button
            v-for="tab in [
              'INVENTORY',
              'CATEGORIES',
              'ORDERS',
              'MEMBERS',
              'STATS',
              'INQUIRIES',
            ]"
            :key="tab"
            @click="activeTab = tab"
            class="px-6 py-2 text-xs uppercase tracking-widest transition-all relative"
            :class="
              activeTab === tab
                ? 'bg-sumi text-washi'
                : 'text-stone-500 hover:bg-stone-50'
            "
          >
            {{ tab }}
            <!-- 未讀訊息徽章 -->
            <span
              v-if="tab === 'INQUIRIES' && unreadInquiriesCount > 0"
              class="absolute -top-1 -right-1 min-w-[18px] h-[18px] bg-amber-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 shadow-sm"
            >
              {{ unreadInquiriesCount > 99 ? "99+" : unreadInquiriesCount }}
            </span>
            <!-- 待處理訂單徽章 -->
            <span
              v-if="tab === 'ORDERS' && pendingOrdersCount > 0"
              class="absolute -top-1 -right-1 min-w-[18px] h-[18px] bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 shadow-sm"
            >
              {{ pendingOrdersCount > 99 ? "99+" : pendingOrdersCount }}
            </span>
          </button>
        </div>
      </div>

      <div class="bg-white border border-stone-200 shadow-sm min-h-[600px] p-8">
        <!-- Inventory Tab -->
        <div v-if="activeTab === 'INVENTORY'" class="space-y-6">
          <div
            class="flex justify-between items-center border-b border-stone-100 pb-4"
          >
            <h2 class="font-serif text-xl text-sumi">Product Inventory</h2>
            <button
              @click="openNewProductForm"
              class="px-6 py-2 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800 transition-colors"
            >
              + New Product
            </button>
          </div>

          <!-- Featured Products Counter -->
          <div class="flex items-center gap-4 pb-4 border-b border-stone-100">
            <span class="text-sm text-stone-500">
              首頁新品上架：
              <span
                :class="
                  featuredProductIds.length >= 5
                    ? 'text-red-600 font-medium'
                    : 'text-sumi font-medium'
                "
              >
                {{ featuredProductIds.length }}件 / 5件
              </span>
            </span>
          </div>

          <!-- 商品搜尋和篩選 -->
          <div
            class="flex flex-col sm:flex-row gap-4 pb-4 border-b border-stone-100"
          >
            <div class="flex-1">
              <input
                v-model="productSearchQuery"
                type="text"
                placeholder="搜尋商品名稱或 SKU..."
                class="w-full px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <div>
              <select
                v-model="productCategoryFilter"
                class="px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi bg-white"
              >
                <option value="ALL">全部種類</option>
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div>
              <select
                v-model="productSortOrder"
                class="px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi bg-white"
              >
                <option value="DESC">新到舊</option>
                <option value="ASC">舊到新</option>
              </select>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr
                  class="text-xs uppercase tracking-widest text-stone-400 border-b border-stone-200"
                >
                  <th class="pb-4 font-normal">Product</th>
                  <th class="pb-4 font-normal">Category</th>
                  <th class="pb-4 font-normal">Price</th>
                  <th class="pb-4 font-normal">Total Stock</th>
                  <th class="pb-4 font-normal">Status</th>
                  <th class="pb-4 font-normal text-center">新品</th>
                  <th class="pb-4 font-normal">Actions</th>
                </tr>
              </thead>
              <tbody class="text-sm font-light text-stone-600">
                <tr
                  v-for="product in filteredProducts"
                  :key="product.id"
                  class="border-b border-stone-50 hover:bg-stone-50/50"
                >
                  <td class="py-4 pr-4">
                    <div class="flex items-center gap-4">
                      <div
                        class="w-10 h-10 bg-stone-200 flex items-center justify-center overflow-hidden"
                      >
                        <img
                          v-if="product.imageUrl"
                          :src="product.imageUrl"
                          class="w-full h-full object-cover"
                          alt=""
                        />
                        <div
                          v-else
                          class="text-[8px] text-stone-400 text-center p-1"
                        >
                          {{ product.name.substring(0, 8) }}
                        </div>
                      </div>
                      <span class="font-medium text-sumi">{{
                        product.name
                      }}</span>
                    </div>
                  </td>
                  <td class="py-4">
                    {{ getCategoryName(product.categoryId) }}
                  </td>
                  <td class="py-4">${{ product.price }}</td>
                  <td class="py-4">
                    <span
                      class="px-2 py-1 min-w-[40px] text-center inline-block"
                      :class="
                        (product.totalStock || 0) < 5
                          ? 'bg-red-50 text-red-700'
                          : 'bg-stone-100'
                      "
                    >
                      {{ product.totalStock || 0 }}
                    </span>
                  </td>
                  <td class="py-4">
                    <span
                      class="px-2 py-1 text-xs"
                      :class="
                        product.isListed
                          ? 'bg-green-50 text-green-700'
                          : 'bg-stone-100 text-stone-500'
                      "
                    >
                      {{ product.isListed ? "上架中" : "未上架" }}
                    </span>
                  </td>
                  <td class="py-4 text-center">
                    <button
                      @click="emit('toggle-featured', product.id)"
                      :disabled="
                        !product.isFeatured && featuredProductIds.length >= 5
                      "
                      class="p-1.5 rounded transition-all hover:scale-110 hover:bg-amber-50 disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-transparent"
                      :title="
                        product.isFeatured
                          ? '從新品移除'
                          : featuredProductIds.length >= 5
                          ? '已達上限 5 個'
                          : '設為新品'
                      "
                    >
                      <!-- 已選取：打勾方框 + 發光效果 -->
                      <svg
                        v-if="product.isFeatured"
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                        class="w-5 h-5 text-amber-500 drop-shadow-[0_0_4px_rgba(245,158,11,0.6)]"
                      >
                        <path
                          fill-rule="evenodd"
                          d="M2.25 6A2.25 2.25 0 0 1 4.5 3.75h15a2.25 2.25 0 0 1 2.25 2.25v12a2.25 2.25 0 0 1-2.25 2.25h-15A2.25 2.25 0 0 1 2.25 18V6Zm16.28 3.22a.75.75 0 0 0-1.06 0l-5.47 5.47-2.47-2.47a.75.75 0 0 0-1.06 1.06l3 3a.75.75 0 0 0 1.06 0l6-6a.75.75 0 0 0 0-1.06Z"
                          clip-rule="evenodd"
                        />
                      </svg>
                      <!-- 未選取：空心方框 -->
                      <svg
                        v-else
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke-width="1.5"
                        stroke="currentColor"
                        class="w-5 h-5 text-stone-400 hover:text-amber-400"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          d="M5.25 7.5A2.25 2.25 0 0 1 7.5 5.25h9a2.25 2.25 0 0 1 2.25 2.25v9a2.25 2.25 0 0 1-2.25 2.25h-9a2.25 2.25 0 0 1-2.25-2.25v-9Z"
                        />
                      </svg>
                    </button>
                  </td>
                  <td class="py-4">
                    <div class="flex items-center gap-2">
                      <button
                        @click="openVariantManager(product)"
                        class="px-3 py-1 text-xs border border-blue-300 text-blue-600 hover:bg-blue-50 transition-colors"
                      >
                        規格
                      </button>
                      <button
                        @click="openEditForm(product)"
                        class="px-3 py-1 text-xs border border-stone-300 hover:bg-stone-100 transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        @click="confirmDelete(product)"
                        class="px-3 py-1 text-xs border border-red-300 text-red-600 hover:bg-red-50 transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Categories Tab -->
        <div v-if="activeTab === 'CATEGORIES'" class="space-y-6">
          <div
            class="flex justify-between items-center border-b border-stone-100 pb-4"
          >
            <h2 class="font-serif text-xl text-sumi">Categories</h2>
            <button
              @click="openNewCategoryForm"
              class="px-6 py-2 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800 transition-colors"
            >
              + New Category
            </button>
          </div>

          <div class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr
                  class="text-xs uppercase tracking-widest text-stone-400 border-b border-stone-200"
                >
                  <th class="pb-4 font-normal">Name (English Only)</th>
                  <th class="pb-4 font-normal">Description</th>
                  <th class="pb-4 font-normal">Actions</th>
                </tr>
              </thead>
              <tbody class="text-sm font-light text-stone-600">
                <tr
                  v-for="category in categories"
                  :key="category.id"
                  class="border-b border-stone-50 hover:bg-stone-50/50"
                >
                  <td class="py-4 font-medium text-sumi">
                    {{ category.name }}
                  </td>
                  <td class="py-4">{{ category.description }}</td>
                  <td class="py-4">
                    <div class="flex items-center gap-2">
                      <button
                        @click="openEditCategoryForm(category)"
                        class="px-3 py-1 text-xs border border-stone-300 hover:bg-stone-100 transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        @click="confirmDeleteCategory(category)"
                        class="px-3 py-1 text-xs border border-red-300 text-red-600 hover:bg-red-50 transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Orders Tab -->
        <div v-if="activeTab === 'ORDERS'" class="space-y-6">
          <div
            class="flex justify-between items-center border-b border-stone-100 pb-4"
          >
            <h2 class="font-serif text-xl text-sumi">Recent Orders</h2>
          </div>

          <!-- 搜尋和篩選 -->
          <div class="flex flex-col sm:flex-row gap-4">
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
            <div>
              <select
                v-model="orderSortOrder"
                class="px-4 py-2 border border-stone-300 rounded text-sm focus:outline-none focus:border-sumi bg-white"
              >
                <option value="DESC">新到舊</option>
                <option value="ASC">舊到新</option>
              </select>
            </div>
          </div>

          <p v-if="orders.length === 0" class="text-stone-400 italic">
            No orders found.
          </p>
          <p
            v-else-if="filteredOrders.length === 0"
            class="text-stone-400 italic"
          >
            找不到符合條件的訂單
          </p>
          <div v-else class="space-y-4">
            <div
              v-for="order in filteredOrders"
              :key="order.id"
              class="border border-stone-200 p-6 hover:shadow-md transition-shadow"
            >
              <!-- 頂部：配送方式 + 訂單編號 + 日期 -->
              <div
                class="flex items-center justify-between mb-4 pb-4 border-b border-stone-200"
              >
                <div class="flex items-center gap-4">
                  <!-- 配送方式（置頂顯眼） -->
                  <span
                    class="px-4 py-2 text-sm font-bold rounded"
                    :class="
                      order.shippingMethod === 'STORE_PICKUP' ||
                      order.shippingMethod === 'STORE'
                        ? 'bg-blue-100 text-blue-800 border-2 border-blue-300'
                        : 'bg-green-100 text-green-800 border-2 border-green-300'
                    "
                  >
                    {{
                      order.shippingMethod === "STORE_PICKUP" ||
                      order.shippingMethod === "STORE"
                        ? "店到店"
                        : "宅配"
                    }}
                  </span>
                  <span class="font-serif text-lg text-sumi"
                    >Order #{{ order.id }}</span
                  >
                  <span class="text-xs text-stone-400">{{
                    order.createdAt
                  }}</span>
                </div>
                <span class="font-serif text-2xl text-sumi"
                  >${{ order.total }}</span
                >
              </div>

              <div class="flex flex-col md:flex-row justify-between gap-6">
                <!-- 左側：買家 + 收件人資訊 -->
                <div class="space-y-4 flex-1">
                  <!-- 買家資訊 -->
                  <div class="bg-stone-50 p-4 rounded border border-stone-100">
                    <p
                      class="text-xs uppercase tracking-widest text-stone-400 mb-2 font-medium"
                    >
                      買家資訊
                    </p>
                    <div class="space-y-1 text-sm">
                      <p>
                        <span class="text-stone-500">用戶：</span
                        ><span class="font-medium text-sumi">{{
                          order.userName || "未知"
                        }}</span>
                      </p>
                      <p>
                        <span class="text-stone-500">Email：</span
                        ><span class="text-sumi">{{
                          order.userEmail || "-"
                        }}</span>
                      </p>
                    </div>
                  </div>

                  <!-- 收件人資訊 -->
                  <div class="bg-stone-50 p-4 rounded border border-stone-100">
                    <p
                      class="text-xs uppercase tracking-widest text-stone-400 mb-2 font-medium"
                    >
                      收件人資訊
                    </p>
                    <div class="space-y-1 text-sm">
                      <p>
                        <span class="text-stone-500">收件人：</span
                        ><span class="font-medium text-sumi">{{
                          order.recipientName
                        }}</span>
                      </p>
                      <p>
                        <span class="text-stone-500">電話：</span
                        ><span class="text-sumi">{{
                          order.recipientPhone
                        }}</span>
                      </p>
                      <p>
                        <span class="text-stone-500">地址：</span
                        ><span class="text-sumi">{{
                          order.shippingAddress
                        }}</span>
                      </p>
                    </div>
                  </div>
                </div>

                <!-- 右側：商品明細 + 訂單狀態 -->
                <div class="flex-1 space-y-4">
                  <!-- 商品明細（含 SKU） -->
                  <div class="bg-white border border-stone-200 rounded p-4">
                    <p
                      class="text-xs uppercase tracking-widest text-stone-400 mb-3 font-medium"
                    >
                      商品明細
                    </p>
                    <div class="space-y-2">
                      <div
                        v-for="(item, idx) in order.items"
                        :key="idx"
                        class="flex justify-between text-sm border-b border-stone-50 pb-2 last:border-0 last:pb-0"
                      >
                        <div>
                          <p class="font-medium text-sumi">
                            {{ item.product?.name || item.productName }}
                          </p>
                          <p class="text-xs text-stone-500">
                            {{ item.variant?.color || item.color }} /
                            {{ item.variant?.size || item.size }}
                            <span class="ml-2 text-stone-400"
                              >SKU:
                              {{
                                item.variant?.skuCode || item.skuCode || "-"
                              }}</span
                            >
                          </p>
                        </div>
                        <div class="text-right">
                          <p class="text-sumi">x{{ item.quantity }}</p>
                          <p class="text-stone-500">
                            ${{ item.price * item.quantity }}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 訂單狀態 -->
                  <div class="flex items-center justify-end gap-3">
                    <span
                      class="text-xs uppercase tracking-widest text-stone-400"
                      >狀態：</span
                    >
                    <select
                      :value="order.status"
                      @change="
                        emit(
                          'update-order-status',
                          order.id,
                          $event.target.value
                        )
                      "
                      class="bg-stone-50 border border-stone-200 text-sm uppercase px-3 py-2 focus:outline-none focus:border-sumi rounded"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="PAID">Paid</option>
                      <option value="SHIPPED">Shipped</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </div>
                </div>
              </div>

              <!-- 付款備註 -->
              <div
                v-if="order.paymentNote"
                class="mt-4 pt-4 border-t border-stone-200"
              >
                <div class="flex items-start gap-2">
                  <span
                    class="text-m uppercase tracking-widest text-stone-400 shrink-0"
                    >會員付款備註：</span
                  >
                  <p class="text-l text-stone-600 whitespace-pre-wrap">
                    {{ order.paymentNote }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Members Tab -->
        <div v-if="activeTab === 'MEMBERS'" class="space-y-6">
          <div
            class="flex justify-between items-center border-b border-stone-100 pb-4"
          >
            <h2 class="font-serif text-xl text-sumi">Members</h2>
            <span class="text-sm text-stone-500">
              共 {{ members?.length || 0 }} 位會員
            </span>
          </div>

          <p
            v-if="!members || members.length === 0"
            class="text-stone-400 italic"
          >
            No members found.
          </p>

          <div v-else class="overflow-x-auto">
            <table class="w-full text-left border-collapse">
              <thead>
                <tr
                  class="text-xs uppercase tracking-widest text-stone-400 border-b border-stone-200"
                >
                  <th class="pb-4 font-normal">姓名</th>
                  <th class="pb-4 font-normal">Email</th>
                  <th class="pb-4 font-normal">電話</th>
                  <th class="pb-4 font-normal">角色</th>
                  <th class="pb-4 font-normal">訂單數</th>
                </tr>
              </thead>
              <tbody class="text-sm font-light text-stone-600">
                <tr
                  v-for="member in members"
                  :key="member.id"
                  class="border-b border-stone-50 hover:bg-stone-50/50"
                >
                  <td class="py-4 pr-4 font-medium text-sumi">
                    {{ member.name }}
                  </td>
                  <td class="py-4 pr-4">{{ member.email }}</td>
                  <td class="py-4 pr-4">{{ member.phone || "-" }}</td>
                  <td class="py-4 pr-4">
                    <span
                      :class="
                        member.role === 'ADMIN'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-stone-100 text-stone-600'
                      "
                      class="px-2 py-1 rounded text-xs uppercase"
                    >
                      {{ member.role }}
                    </span>
                  </td>
                  <td class="py-4 pr-4 text-center">
                    <span class="bg-stone-100 px-2 py-1 rounded text-xs">
                      {{ member.orderCount || 0 }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Stats Tab -->
        <div v-if="activeTab === 'STATS'" class="space-y-6">
          <h2
            class="font-serif text-xl text-sumi border-b border-stone-100 pb-4"
          >
            Sales Statistics
          </h2>

          <!-- 主要指標 -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="bg-stone-50 border border-stone-200 p-6 rounded">
              <p class="text-xs uppercase tracking-widest text-stone-500 mb-2">
                總銷售額
              </p>
              <p class="text-3xl font-serif text-sumi">
                ${{ salesStats.totalRevenue.toLocaleString() }}
              </p>
            </div>
            <div class="bg-stone-50 border border-stone-200 p-6 rounded">
              <p class="text-xs uppercase tracking-widest text-stone-500 mb-2">
                有效訂單數
              </p>
              <p class="text-3xl font-serif text-sumi">
                {{ salesStats.orderCount }}
              </p>
            </div>
            <div class="bg-stone-50 border border-stone-200 p-6 rounded">
              <p class="text-xs uppercase tracking-widest text-stone-500 mb-2">
                平均訂單金額
              </p>
              <p class="text-3xl font-serif text-sumi">
                ${{ salesStats.avgOrderValue.toLocaleString() }}
              </p>
            </div>
          </div>

          <!-- 訂單狀態統計 -->
          <div class="bg-stone-50 border border-stone-200 p-6 rounded">
            <h3 class="text-sm font-medium text-sumi mb-4">訂單狀態分佈</h3>
            <div class="flex flex-wrap gap-4">
              <div
                v-for="(count, status) in orderStatusStats"
                :key="status"
                class="flex items-center gap-2"
              >
                <span
                  class="px-2 py-1 rounded text-xs uppercase"
                  :class="{
                    'bg-yellow-100 text-yellow-700': status === 'PENDING',
                    'bg-blue-100 text-blue-700': status === 'PAID',
                    'bg-purple-100 text-purple-700': status === 'SHIPPED',
                    'bg-green-100 text-green-700': status === 'COMPLETED',
                    'bg-red-100 text-red-700': status === 'CANCELLED',
                  }"
                >
                  {{ status }}
                </span>
                <span class="text-lg font-medium text-sumi">{{ count }}</span>
              </div>
            </div>
          </div>

          <!-- 熱銷商品 -->
          <div class="bg-stone-50 border border-stone-200 p-6 rounded">
            <h3 class="text-sm font-medium text-sumi mb-4">熱銷商品 TOP 5</h3>
            <div
              v-if="topSellingProducts.length === 0"
              class="text-stone-400 italic text-sm"
            >
              尚無銷售資料
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="(product, index) in topSellingProducts"
                :key="product.name"
                class="flex items-center justify-between"
              >
                <div class="flex items-center gap-3">
                  <span
                    class="w-6 h-6 rounded-full bg-sumi text-washi text-xs flex items-center justify-center"
                  >
                    {{ index + 1 }}
                  </span>
                  <span class="text-sm text-stone-700">{{ product.name }}</span>
                </div>
                <span class="text-sm font-medium text-sumi">
                  {{ product.count }} 件
                </span>
              </div>
            </div>
          </div>

          <!-- 月銷量分析 -->
          <div class="bg-stone-50 border border-stone-200 p-6 rounded">
            <h3 class="text-sm font-medium text-sumi mb-4">
              月銷量分析（最近 6 個月）
            </h3>
            <div
              v-if="monthlySales.length === 0"
              class="text-stone-400 italic text-sm"
            >
              尚無銷售資料
            </div>
            <div v-else class="overflow-x-auto">
              <table class="w-full text-left">
                <thead>
                  <tr
                    class="text-xs uppercase tracking-widest text-stone-400 border-b border-stone-200"
                  >
                    <th class="pb-3 font-normal">月份</th>
                    <th class="pb-3 font-normal text-right">訂單數</th>
                    <th class="pb-3 font-normal text-right">銷售額</th>
                  </tr>
                </thead>
                <tbody class="text-sm text-stone-600">
                  <tr
                    v-for="item in monthlySales"
                    :key="item.month"
                    class="border-b border-stone-100"
                  >
                    <td class="py-3 font-medium text-sumi">{{ item.label }}</td>
                    <td class="py-3 text-right">{{ item.count }} 筆</td>
                    <td class="py-3 text-right font-medium text-sumi">
                      ${{ item.revenue.toLocaleString() }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- Inquiries Tab -->
        <div v-if="activeTab === 'INQUIRIES'" class="space-y-6">
          <h2
            class="font-serif text-xl text-sumi border-b border-stone-100 pb-4"
          >
            Customer Inquiries
          </h2>
          <p v-if="inquiries.length === 0" class="text-stone-400 italic">
            No new messages.
          </p>
          <div v-else class="grid grid-cols-1 gap-4">
            <div
              v-for="inquiry in inquiries"
              :key="inquiry.id"
              class="p-6 border-2 rounded transition-all"
              :class="
                inquiry.status === 'UNREAD'
                  ? 'border-amber-400 bg-amber-50 shadow-md'
                  : inquiry.status === 'REPLIED'
                  ? 'border-stone-200 bg-stone-50 opacity-70'
                  : 'border-orange-300 bg-orange-50'
              "
            >
              <div class="flex justify-between items-start mb-4">
                <div>
                  <h3 class="font-serif text-sumi">{{ inquiry.name }}</h3>
                  <p class="text-xs text-stone-400">{{ inquiry.email }}</p>
                </div>
                <span class="text-xs text-stone-400">{{
                  inquiry.date || inquiry.createdAt
                }}</span>
              </div>
              <p
                class="text-sm text-stone-600 font-light mb-4 leading-relaxed bg-white p-4 border border-stone-100"
              >
                {{ inquiry.message }}
              </p>
              <div class="flex justify-end">
                <button
                  v-if="inquiry.status !== 'REPLIED'"
                  @click="emit('reply-inquiry', inquiry.id)"
                  class="px-4 py-2 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800"
                >
                  Mark as Replied
                </button>
                <span
                  v-else
                  class="text-xs uppercase tracking-widest text-green-700 flex items-center gap-2"
                >
                  ✓ Replied
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Category Form Modal -->
    <div
      v-if="isCategoryFormOpen"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click.self="closeCategoryForm"
    >
      <div class="bg-white max-w-md w-full p-8 shadow-xl">
        <div class="flex justify-between items-center mb-8">
          <h2 class="font-serif text-2xl text-sumi">
            {{ editingCategoryId ? "Edit Category" : "New Category" }}
          </h2>
          <button
            @click="closeCategoryForm"
            class="text-stone-400 hover:text-sumi text-2xl"
          >
            &times;
          </button>
        </div>

        <form @submit.prevent="submitCategoryForm" class="space-y-6">
          <div class="space-y-2">
            <label
              class="block text-xs uppercase tracking-widest text-stone-500"
              >Name * (English Only)</label
            >
            <input
              v-model="categoryFormData.name"
              required
              pattern="^[A-Za-z\s]+$"
              class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi"
              placeholder="e.g. Apparel, Kitchen"
            />
            <p v-if="categoryError" class="text-xs text-red-600">
              {{ categoryError }}
            </p>
          </div>

          <div class="space-y-2">
            <label
              class="block text-xs uppercase tracking-widest text-stone-500"
              >Description</label
            >
            <textarea
              v-model="categoryFormData.description"
              rows="3"
              class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi resize-none"
            ></textarea>
          </div>

          <div class="flex justify-end gap-4 pt-4 border-t border-stone-200">
            <button
              type="button"
              @click="closeCategoryForm"
              class="px-6 py-3 border border-stone-300 text-stone-600 text-xs uppercase tracking-widest hover:bg-stone-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="px-6 py-3 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Product Form Modal -->
    <div
      v-if="isFormOpen"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click.self="closeForm"
    >
      <div
        class="bg-white max-w-2xl w-full max-h-[90vh] overflow-y-auto p-8 shadow-xl"
      >
        <div class="flex justify-between items-center mb-8">
          <h2 class="font-serif text-2xl text-sumi">
            {{ editingProductId ? "Edit Product" : "New Product" }}
          </h2>
          <button
            @click="closeForm"
            class="text-stone-400 hover:text-sumi text-2xl"
          >
            &times;
          </button>
        </div>

        <form @submit.prevent="submitForm" class="space-y-6">
          <!-- Image Upload -->
          <div class="space-y-2">
            <label
              class="block text-xs uppercase tracking-widest text-stone-500"
              >Product Image</label
            >
            <div class="flex gap-4 items-start">
              <div
                class="w-32 h-32 bg-stone-200 flex items-center justify-center overflow-hidden relative"
              >
                <img
                  v-if="previewUrl && !uploadError"
                  :src="previewUrl"
                  class="w-full h-full object-cover"
                  alt="Preview"
                />
                <div v-else-if="!previewUrl" class="text-center p-2">
                  <p class="text-xs text-stone-500 font-medium">
                    {{ formData.name || "Product" }}
                  </p>
                </div>
                <div
                  v-if="isUploading"
                  class="absolute inset-0 bg-white/80 flex items-center justify-center"
                >
                  <div
                    class="w-8 h-8 border-2 border-sumi border-t-transparent rounded-full animate-spin"
                  ></div>
                </div>
              </div>
              <div class="flex-1 space-y-2">
                <input
                  type="file"
                  accept="image/*"
                  @change="handleFileSelect"
                  class="block w-full text-sm text-stone-500 file:mr-4 file:py-2 file:px-4 file:border file:border-stone-300 file:text-xs file:bg-white file:text-stone-700 hover:file:bg-stone-50"
                />
                <div v-if="uploadError" class="flex items-center gap-2">
                  <span class="text-xs text-red-600">上傳失敗</span>
                  <button
                    type="button"
                    @click="uploadImage"
                    class="px-3 py-1 text-xs bg-red-600 text-white hover:bg-red-700"
                  >
                    重新上傳
                  </button>
                </div>
                <p
                  v-if="pendingFile && !uploadError && !isUploading"
                  class="text-xs text-stone-500"
                >
                  Selected: {{ pendingFile.name }}
                </p>
              </div>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <label
                class="block text-xs uppercase tracking-widest text-stone-500"
                >Name *</label
              >
              <input
                v-model="formData.name"
                required
                class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi"
              />
            </div>
            <div class="space-y-2">
              <label
                class="block text-xs uppercase tracking-widest text-stone-500"
                >Price *</label
              >
              <input
                v-model.number="formData.price"
                type="number"
                min="0"
                step="0.01"
                required
                class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <label
                class="block text-xs uppercase tracking-widest text-stone-500"
                >Category *</label
              >
              <select
                v-model="formData.categoryId"
                required
                class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi bg-white"
              >
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>
            <div class="space-y-2">
              <label
                class="block text-xs uppercase tracking-widest text-stone-500"
                >Status</label
              >
              <label
                class="flex items-center gap-3 p-3 border border-stone-300 cursor-pointer hover:bg-stone-50"
              >
                <input
                  type="checkbox"
                  v-model="formData.isListed"
                  class="accent-sumi w-4 h-4"
                />
                <span class="text-sm">上架 (Listed)</span>
              </label>
            </div>
          </div>

          <div class="space-y-2">
            <label
              class="block text-xs uppercase tracking-widest text-stone-500"
              >Description</label
            >
            <textarea
              v-model="formData.description"
              rows="4"
              class="w-full border border-stone-300 p-3 focus:outline-none focus:border-sumi resize-none"
            ></textarea>
          </div>

          <div class="flex justify-end gap-4 pt-4 border-t border-stone-200">
            <button
              type="button"
              @click="closeForm"
              class="px-6 py-3 border border-stone-300 text-stone-600 text-xs uppercase tracking-widest hover:bg-stone-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="isUploading"
              class="px-6 py-3 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800 disabled:opacity-50"
            >
              {{
                isUploading
                  ? "Uploading..."
                  : editingProductId
                  ? "Save Changes"
                  : "Create Product"
              }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Variant Manager Modal -->
    <div
      v-if="isVariantFormOpen && currentProductForVariant"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click.self="closeVariantForm"
    >
      <div
        class="bg-white max-w-3xl w-full max-h-[90vh] overflow-y-auto p-8 shadow-xl"
      >
        <div class="flex justify-between items-center mb-8">
          <div>
            <h2 class="font-serif text-2xl text-sumi">規格管理</h2>
            <p class="text-sm text-stone-500 mt-1">
              {{ activeVariantProduct?.name }}
            </p>
          </div>
          <button
            @click="closeVariantForm"
            class="text-stone-400 hover:text-sumi text-2xl"
          >
            &times;
          </button>
        </div>

        <!-- Existing Variants Table -->
        <div class="mb-8">
          <h3 class="text-sm uppercase tracking-widest text-stone-500 mb-4">
            現有規格
          </h3>
          <table class="w-full text-left border-collapse">
            <thead>
              <tr
                class="text-xs uppercase tracking-widest text-stone-400 border-b border-stone-200"
              >
                <th class="pb-3 font-normal">SKU</th>
                <th class="pb-3 font-normal">Color</th>
                <th class="pb-3 font-normal">Size</th>
                <th class="pb-3 font-normal">Stock</th>
                <th class="pb-3 font-normal">Actions</th>
              </tr>
            </thead>
            <tbody class="text-sm">
              <tr
                v-for="variant in activeVariantProduct?.variants"
                :key="variant.id"
                class="border-b border-stone-50"
              >
                <td class="py-3 font-mono text-xs text-stone-500">
                  {{ variant.skuCode }}
                </td>
                <td class="py-3">{{ variant.color }}</td>
                <td class="py-3">{{ variant.size }}</td>
                <td class="py-3">
                  <span
                    class="px-2 py-1"
                    :class="
                      variant.stock < 5
                        ? 'bg-red-50 text-red-700'
                        : 'bg-stone-100'
                    "
                  >
                    {{ variant.stock }}
                  </span>
                </td>
                <td class="py-3">
                  <div class="flex items-center gap-2">
                    <button
                      @click="editVariant(variant)"
                      class="px-2 py-1 text-xs border border-stone-300 hover:bg-stone-100"
                    >
                      Edit
                    </button>
                    <button
                      @click.stop="confirmDeleteVariant(variant)"
                      class="px-2 py-1 text-xs border border-red-300 text-red-600 hover:bg-red-50"
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
              <!-- Loading Row -->
              <tr
                v-if="isVariantSubmitting"
                class="border-b border-stone-50 bg-stone-50/50"
              >
                <td class="py-3 font-mono text-xs text-stone-400 italic">
                  Generating...
                </td>
                <td class="py-3 text-stone-400">{{ variantFormData.color }}</td>
                <td class="py-3 text-stone-400">{{ variantFormData.size }}</td>
                <td class="py-3">
                  <span class="px-2 py-1 bg-stone-100 text-stone-400">
                    {{ variantFormData.stock }}
                  </span>
                </td>
                <td class="py-3">
                  <div
                    class="w-4 h-4 border-2 border-sumi border-t-transparent rounded-full animate-spin"
                  ></div>
                </td>
              </tr>
              <tr
                v-if="
                  !activeVariantProduct?.variants?.length &&
                  !isVariantSubmitting
                "
              >
                <td colspan="5" class="py-4 text-center text-stone-400 italic">
                  尚無規格，請新增
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Add/Edit Variant Form -->
        <div class="border-t border-stone-200 pt-6">
          <h3 class="text-sm uppercase tracking-widest text-stone-500 mb-4">
            {{ editingVariantId ? "編輯規格" : "新增規格" }}
          </h3>
          <form
            @submit.prevent="submitVariantForm"
            class="flex gap-4 items-end flex-wrap"
          >
            <div class="space-y-1">
              <label class="block text-xs text-stone-500">Color *</label>
              <input
                v-model="variantFormData.color"
                required
                placeholder="e.g. White, Black"
                class="w-32 border border-stone-300 p-2 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <div class="space-y-1">
              <label class="block text-xs text-stone-500">Size *</label>
              <input
                v-model="variantFormData.size"
                required
                placeholder="e.g. S, M, L, Free"
                class="w-24 border border-stone-300 p-2 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <div class="space-y-1">
              <label class="block text-xs text-stone-500">Stock *</label>
              <input
                v-model.number="variantFormData.stock"
                type="number"
                min="0"
                required
                class="w-20 border border-stone-300 p-2 text-sm focus:outline-none focus:border-sumi"
              />
            </div>
            <button
              type="submit"
              :disabled="isVariantSubmitting"
              class="px-4 py-2 bg-sumi text-washi text-xs uppercase tracking-widest hover:bg-stone-800 disabled:opacity-70 flex items-center gap-2"
            >
              <div
                v-if="isVariantSubmitting"
                class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
              ></div>
              <span>
                {{
                  isVariantSubmitting
                    ? "Processing..."
                    : editingVariantId
                    ? "Update"
                    : "+ Add"
                }}
              </span>
            </button>
            <button
              v-if="editingVariantId"
              type="button"
              @click="cancelEditVariant"
              class="px-4 py-2 border border-stone-300 text-stone-600 text-xs uppercase hover:bg-stone-50"
            >
              Cancel
            </button>
          </form>
          <p class="text-xs text-stone-400 mt-3">SKU 編碼將自動產生</p>
        </div>
      </div>
    </div>
  </div>
</template>
