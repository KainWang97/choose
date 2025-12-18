/**
 * API Service Layer
 * 與後端 API 通訊的統一介面
 */

import {
  apiGet,
  apiPost,
  apiPut,
  apiPatch,
  apiDelete,
} from "../utils/apiClient.js";

// ============================================
// 資料轉換函數：後端格式 -> 前端格式
// ============================================

/**
 * 轉換後端 ProductDTO 為前端格式
 * 注意：DTO 不包含 variants，需要額外 API 呼叫
 * @param {Object} backend
 * @returns {import('../types.js').Product}
 */
function transformProduct(backend) {
  return {
    id: String(backend.id),
    categoryId: String(backend.categoryId || ""),
    name: backend.name,
    description: backend.description || "",
    price: Number(backend.price),
    imageUrl: backend.image || "", // DTO 使用 image
    isListed: backend.isListed ?? true,
    isFeatured: backend.isFeatured ?? false,
    createdAt: backend.createdAt,
    variants: [], // 需要額外呼叫 getVariants
    totalStock: backend.stock || 0, // DTO 提供總庫存
    category: backend.category,
  };
}

/**
 * 轉換後端 ProductVariant 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').ProductVariant}
 */
function transformVariant(backend) {
  // 後端可能使用 id 或 variantId
  const variantId = backend.id ?? backend.variantId;
  return {
    id: String(variantId),
    productId: String(backend.productId || ""),
    skuCode: backend.skuCode,
    color: backend.color,
    size: backend.size,
    stock: backend.stock,
    createdAt: backend.createdAt,
  };
}

/**
 * 轉換後端 CategoryDTO 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').Category}
 */
function transformCategory(backend) {
  return {
    id: String(backend.id),
    name: backend.name,
    description: backend.description,
    createdAt: backend.createdAt,
  };
}

/**
 * 轉換後端 OrderDTO 為前端格式
 * @param {Object} backend
 * @returns {Promise<import('../types.js').Order>}
 */
async function transformOrder(backend) {
  // 轉換 orderItems
  /** @type {import('../types.js').OrderItem[]} */
  const items = [];
  if (backend.items) {
    for (const item of backend.items) {
      /** @type {import('../types.js').ProductVariant} */
      const variant = {
        id: String(item.variantId),
        productId: "",
        skuCode: item.skuCode || "",
        color: item.color || "",
        size: item.size || "",
        stock: 0,
      };

      items.push({
        variant,
        product: {
          id: "",
          categoryId: "",
          name: item.productName || "",
          description: "",
          price: Number(item.price),
          imageUrl: "",
          isListed: true,
        },
        price: Number(item.price),
        quantity: item.quantity,
      });
    }
  }

  return {
    id: String(backend.id),
    userId: backend.userId ? String(backend.userId) : undefined,
    userName: backend.userName || "",
    userEmail: backend.userEmail || "",
    items,
    total: Number(backend.total),
    status: backend.status,
    paymentMethod: backend.paymentMethod,
    shippingMethod: backend.shippingMethod || "HOME_DELIVERY",
    recipientName: backend.recipientName || "",
    recipientPhone: backend.recipientPhone || "",
    shippingAddress: backend.shippingAddress || "",
    shippingDetails: backend.recipientName
      ? {
          fullName: backend.recipientName,
          phone: backend.recipientPhone || "",
          email: backend.userEmail || "",
          method: backend.paymentMethod || "BANK_TRANSFER",
          address: backend.shippingAddress,
        }
      : undefined,
    paymentNote: backend.paymentNote,
    createdAt: backend.createdAt,
    date: backend.createdAt
      ? new Date(backend.createdAt).toLocaleDateString()
      : undefined,
  };
}

/**
 * 轉換後端 InquiryDTO 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').Inquiry}
 */
function transformInquiry(backend) {
  return {
    id: String(backend.id),
    userId: backend.userId ? String(backend.userId) : undefined,
    name: backend.name,
    email: backend.email,
    message: backend.message,
    status: backend.status,
    createdAt: backend.createdAt,
    repliedAt: backend.repliedAt,
    date: backend.createdAt
      ? new Date(backend.createdAt).toLocaleDateString()
      : undefined,
  };
}

/**
 * 轉換後端 User 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').User}
 */
function transformUser(backend) {
  return {
    id: String(backend.id),
    email: backend.email,
    name: backend.name,
    phone: backend.phone,
    role: backend.role === "ADMIN" ? "ADMIN" : "MEMBER",
    orders: [],
  };
}

// ============================================
// Product API
// ============================================
export const productApi = {
  /**
   * 取得所有商品
   * GET /api/products
   * @returns {Promise<import('../types.js').Product[]>}
   */
  async getAll() {
    const backendProducts = await apiGet("/products");
    const products = backendProducts.map(transformProduct);

    // 為每個商品取得 variants
    const productsWithVariants = await Promise.all(
      products.map(async (product) => {
        try {
          const variants = await this.getVariants(product.id);
          return {
            ...product,
            variants,
            totalStock: variants.reduce((sum, v) => sum + v.stock, 0),
          };
        } catch {
          return product;
        }
      })
    );

    return productsWithVariants;
  },

  /**
   * 取得所有商品 (Admin) - 包含未上架
   * GET /api/products/admin/all
   * @returns {Promise<import('../types.js').Product[]>}
   */
  async getAllAdmin() {
    const backendProducts = await apiGet("/products/admin/all");
    const products = backendProducts.map(transformProduct);

    const productsWithVariants = await Promise.all(
      products.map(async (product) => {
        try {
          const variants = await this.getVariants(product.id);
          return {
            ...product,
            variants,
            totalStock: variants.reduce((sum, v) => sum + v.stock, 0),
          };
        } catch {
          return product;
        }
      })
    );

    return productsWithVariants;
  },

  /**
   * 取得單一商品
   * GET /api/products/:id
   * @param {string} id
   * @returns {Promise<import('../types.js').Product | undefined>}
   */
  async getById(id) {
    try {
      const backend = await apiGet(`/products/${id}`);
      const product = transformProduct(backend);

      try {
        const variants = await this.getVariants(id);
        product.variants = variants;
        product.totalStock = variants.reduce((sum, v) => sum + v.stock, 0);
      } catch {
        // Variants 取得失敗，使用預設值
      }

      return product;
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return undefined;
      }
      throw error;
    }
  },

  /**
   * 依分類取得商品
   * GET /api/products/category/:categoryId
   * @param {string} categoryId
   * @returns {Promise<import('../types.js').Product[]>}
   */
  async getByCategory(categoryId) {
    const backendProducts = await apiGet(`/products/category/${categoryId}`);
    const products = backendProducts.map(transformProduct);

    const productsWithVariants = await Promise.all(
      products.map(async (product) => {
        try {
          const variants = await this.getVariants(product.id);
          return {
            ...product,
            variants,
            totalStock: variants.reduce((sum, v) => sum + v.stock, 0),
          };
        } catch {
          return product;
        }
      })
    );

    return productsWithVariants;
  },

  /**
   * 搜尋商品
   * GET /api/products/search?keyword=...
   * @param {string} keyword
   * @returns {Promise<import('../types.js').Product[]>}
   */
  async search(keyword) {
    const backendProducts = await apiGet(
      `/products/search?keyword=${encodeURIComponent(keyword)}`
    );
    const products = backendProducts.map(transformProduct);

    const productsWithVariants = await Promise.all(
      products.map(async (product) => {
        try {
          const variants = await this.getVariants(product.id);
          return {
            ...product,
            variants,
            totalStock: variants.reduce((sum, v) => sum + v.stock, 0),
          };
        } catch {
          return product;
        }
      })
    );

    return productsWithVariants;
  },

  /**
   * 取得商品規格
   * GET /api/products/:productId/variants
   * @param {string} productId
   * @returns {Promise<import('../types.js').ProductVariant[]>}
   */
  async getVariants(productId) {
    const backendVariants = await apiGet(`/products/${productId}/variants`);
    return backendVariants.map(transformVariant);
  },

  /**
   * 新增商品 (Admin)
   * POST /api/products
   * @param {Object} data
   * @returns {Promise<import('../types.js').Product>}
   */
  async create(data) {
    const requestBody = {
      categoryId: Number(data.categoryId),
      name: data.name,
      description: data.description,
      price: data.price,
      imageUrl: data.imageUrl,
      isListed: data.isListed ?? true,
    };
    const backend = await apiPost("/products", requestBody);
    const product = transformProduct(backend);

    try {
      const variants = await this.getVariants(product.id);
      product.variants = variants;
      product.totalStock = variants.reduce((sum, v) => sum + v.stock, 0);
    } catch {
      // Variants 取得失敗
    }

    return product;
  },

  /**
   * 更新商品 (Admin)
   * PUT /api/products/:id
   * @param {string} id
   * @param {Object} data
   * @returns {Promise<import('../types.js').Product | null>}
   */
  async update(id, data) {
    try {
      const requestBody = {};
      if (data.categoryId) requestBody.categoryId = Number(data.categoryId);
      if (data.name) requestBody.name = data.name;
      if (data.description !== undefined)
        requestBody.description = data.description;
      if (data.price !== undefined) requestBody.price = data.price;
      if (data.imageUrl !== undefined) requestBody.imageUrl = data.imageUrl;
      if (data.isListed !== undefined) requestBody.isListed = data.isListed;

      const backend = await apiPut(`/products/${id}`, requestBody);
      const product = transformProduct(backend);

      try {
        const variants = await this.getVariants(id);
        product.variants = variants;
        product.totalStock = variants.reduce((sum, v) => sum + v.stock, 0);
      } catch {
        // Variants 取得失敗
      }

      return product;
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return null;
      }
      throw error;
    }
  },

  /**
   * 刪除商品 (Admin)
   * DELETE /api/products/:id
   * @param {string} id
   * @returns {Promise<boolean>}
   */
  async delete(id) {
    try {
      await apiDelete(`/products/${id}`);
      return true;
    } catch {
      return false;
    }
  },

  /**
   * 上傳商品圖片
   * POST /api/upload/image
   * @param {File} file
   * @returns {Promise<{ url: string }>}
   */
  async uploadImage(file) {
    const formData = new FormData();
    formData.append("file", file);

    // 使用 apiClient 發送 multipart/form-data 請求
    const { apiClient } = await import("../utils/apiClient.js");
    const response = await apiClient.post("/upload/image", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return { url: response.url };
  },
};

// ============================================
// Variant API
// ============================================
export const variantApi = {
  /**
   * 取得商品所有規格
   * GET /api/variants/product/:productId
   * @param {string} productId
   * @returns {Promise<import('../types.js').ProductVariant[]>}
   */
  async getByProductId(productId) {
    const backendVariants = await apiGet(`/variants/product/${productId}`);
    return backendVariants.map(transformVariant);
  },

  /**
   * 新增規格
   * POST /api/variants
   * @param {string} productId
   * @param {{ color: string; size: string; stock: number }} data
   * @returns {Promise<import('../types.js').ProductVariant>}
   */
  async create(productId, data) {
    const requestBody = {
      productId: Number(productId),
      skuCode: "",
      color: data.color,
      size: data.size,
      stock: data.stock,
    };
    const backend = await apiPost("/variants", requestBody);
    return transformVariant(backend);
  },

  /**
   * 更新規格
   * PUT /api/variants/:id
   * @param {string} id
   * @param {Object} data
   * @returns {Promise<import('../types.js').ProductVariant | null>}
   */
  async update(id, data) {
    try {
      const requestBody = {};
      if (data.color) requestBody.color = data.color;
      if (data.size) requestBody.size = data.size;
      if (data.stock !== undefined) requestBody.stock = data.stock;
      if (data.skuCode) requestBody.skuCode = data.skuCode;

      const backend = await apiPut(`/variants/${id}`, requestBody);
      return transformVariant(backend);
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return null;
      }
      throw error;
    }
  },

  /**
   * 刪除規格
   * DELETE /api/variants/:id
   * @param {string} id
   * @returns {Promise<boolean>}
   */
  async delete(id) {
    try {
      await apiDelete(`/variants/${id}`);
      return true;
    } catch {
      return false;
    }
  },
};

// ============================================
// Order API
// ============================================
export const orderApi = {
  /**
   * 取得所有訂單 (Admin)
   * GET /api/orders
   * @returns {Promise<import('../types.js').Order[]>}
   */
  async getAll() {
    const backendOrders = await apiGet("/orders");
    return Promise.all(backendOrders.map(transformOrder));
  },

  /**
   * 取得自己的訂單
   * GET /api/orders/my
   * @returns {Promise<import('../types.js').Order[]>}
   */
  async getMy() {
    const backendOrders = await apiGet("/orders/my");
    return Promise.all(backendOrders.map(transformOrder));
  },

  /**
   * 取得單一訂單
   * GET /api/orders/:id
   * @param {string} id
   * @returns {Promise<import('../types.js').Order | undefined>}
   */
  async getById(id) {
    try {
      const backend = await apiGet(`/orders/${id}`);
      return transformOrder(backend);
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return undefined;
      }
      throw error;
    }
  },

  /**
   * 建立新訂單
   * POST /api/orders
   * @param {{ items: import('../types.js').CartItem[]; shippingDetails: import('../types.js').ShippingDetails }} orderData
   * @returns {Promise<import('../types.js').Order>}
   */
  async create(orderData) {
    const items = orderData.items.map((item) => ({
      variantId: Number(item.variant.id),
      quantity: item.quantity,
    }));

    const requestBody = {
      shippingMethod:
        orderData.shippingDetails.method === "STORE_PICKUP"
          ? "STORE_PICKUP"
          : "HOME_DELIVERY",
      paymentMethod: orderData.shippingDetails.method,
      recipientName: orderData.shippingDetails.fullName,
      recipientPhone: orderData.shippingDetails.phone,
      shippingAddress:
        orderData.shippingDetails.address ||
        orderData.shippingDetails.storeName ||
        "",
      items,
    };

    const backend = await apiPost("/orders", requestBody);
    return transformOrder(backend);
  },

  /**
   * 更新訂單狀態 (Admin)
   * PATCH /api/orders/:id/status
   * @param {string} id
   * @param {import('../types.js').OrderStatus} status
   * @returns {Promise<boolean>}
   */
  async updateStatus(id, status) {
    try {
      await apiPatch(`/orders/${id}/status`, { status });
      return true;
    } catch {
      return false;
    }
  },

  /**
   * 更新訂單付款備註
   * PATCH /api/orders/:id/payment-note
   * @param {string} id
   * @param {string} note
   * @returns {Promise<boolean>}
   */
  async updatePaymentNote(id, note) {
    try {
      await apiPatch(`/orders/${id}/payment-note`, { paymentNote: note });
      return true;
    } catch {
      return false;
    }
  },
};

// ============================================
// Inquiry API
// ============================================
export const inquiryApi = {
  /**
   * 取得所有詢問 (Admin)
   * GET /api/inquiries
   * @returns {Promise<import('../types.js').Inquiry[]>}
   */
  async getAll() {
    const backendInquiries = await apiGet("/inquiries");
    return backendInquiries.map(transformInquiry);
  },

  /**
   * 送出聯絡詢問
   * POST /api/inquiries
   * @param {{ name: string; email: string; message: string }} data
   * @returns {Promise<import('../types.js').Inquiry>}
   */
  async create(data) {
    const backend = await apiPost("/inquiries", data);
    return transformInquiry(backend);
  },

  /**
   * 標記已回覆 (Admin)
   * PATCH /api/inquiries/:id/reply
   * @param {string} id
   * @returns {Promise<boolean>}
   */
  async markAsReplied(id) {
    try {
      await apiPatch(`/inquiries/${id}/reply`);
      return true;
    } catch {
      return false;
    }
  },
};

// ============================================
// Auth API
// ============================================
export const authApi = {
  /**
   * 使用者登入
   * POST /api/auth/login
   * @param {{ email: string; password: string }} data
   * @returns {Promise<import('../types.js').User>}
   */
  async login(data) {
    const backend = await apiPost("/auth/login", data);
    // Token 透過 HttpOnly Cookie 自動設定
    return transformUser(backend);
  },

  /**
   * 註冊新會員
   * POST /api/auth/register
   * @param {{ name: string; email: string; password: string; phone?: string }} data
   * @returns {Promise<import('../types.js').User>}
   */
  async register(data) {
    const backend = await apiPost("/auth/register", data);
    // Token 透過 HttpOnly Cookie 自動設定
    return transformUser(backend);
  },

  /**
   * 取得目前使用者
   * GET /api/auth/me
   * @returns {Promise<import('../types.js').User | null>}
   */
  async getMe() {
    try {
      const backend = await apiGet("/auth/me");
      return transformUser(backend);
    } catch {
      return null;
    }
  },

  /**
   * 登出
   * POST /api/auth/logout
   * @returns {Promise<void>}
   */
  async logout() {
    // 後端會清除 HttpOnly Cookie
    await apiPost("/auth/logout");
  },
};

// ============================================
// Category API
// ============================================
export const categoryApi = {
  /**
   * 取得所有分類
   * GET /api/categories
   * @returns {Promise<import('../types.js').Category[]>}
   */
  async getAll() {
    const backendCategories = await apiGet("/categories");
    return backendCategories.map(transformCategory);
  },

  /**
   * 取得單一分類
   * GET /api/categories/:id
   * @param {string} id
   * @returns {Promise<import('../types.js').Category | undefined>}
   */
  async getById(id) {
    try {
      const backend = await apiGet(`/categories/${id}`);
      return transformCategory(backend);
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return undefined;
      }
      throw error;
    }
  },

  /**
   * 新增分類 (Admin)
   * POST /api/categories
   * @param {Object} data
   * @returns {Promise<import('../types.js').Category>}
   */
  async create(data) {
    const backend = await apiPost("/categories", data);
    return transformCategory(backend);
  },

  /**
   * 更新分類 (Admin)
   * PUT /api/categories/:id
   * @param {string} id
   * @param {Object} data
   * @returns {Promise<import('../types.js').Category | null>}
   */
  async update(id, data) {
    try {
      const backend = await apiPut(`/categories/${id}`, data);
      return transformCategory(backend);
    } catch (error) {
      if (error instanceof Error && error.message.includes("404")) {
        return null;
      }
      throw error;
    }
  },

  /**
   * 刪除分類 (Admin)
   * DELETE /api/categories/:id
   * @param {string} id
   * @returns {Promise<boolean>}
   */
  async delete(id) {
    try {
      await apiDelete(`/categories/${id}`);
      return true;
    } catch {
      return false;
    }
  },
};

// ============================================
// Featured Products API (已實作後端 API)
// ============================================
const MAX_FEATURED = 5;

export const featuredApi = {
  /**
   * 取得所有新品上架商品 IDs
   * @returns {Promise<string[]>}
   */
  async getAll() {
    const products = await this.getProducts();
    return products.map((p) => p.id);
  },

  /**
   * 取得新品上架商品列表
   * @returns {Promise<import('../types.js').Product[]>}
   */
  async getProducts() {
    const data = await apiGet("/products/featured");
    return (data || []).map(transformProduct);
  },

  /**
   * 切換商品新品上架狀態
   * @param {string} productId
   * @returns {Promise<{ isFeatured: boolean; message: string }>}
   */
  async toggle(productId) {
    const numericId =
      typeof productId === "string" ? parseInt(productId, 10) : productId;
    const data = await apiPatch(`/products/${numericId}/featured`);
    return {
      isFeatured: data.isFeatured,
      message: data.isFeatured ? "已加入新品上架" : "已從新品上架移除",
    };
  },

  /**
   * 新增商品到新品上架 (透過 toggle 實作)
   * @param {string} productId
   * @returns {Promise<{ success: boolean; message: string }>}
   */
  async add(productId) {
    const result = await this.toggle(productId);
    if (result.isFeatured) {
      return { success: true, message: "已加入新品上架" };
    }
    return { success: false, message: "操作失敗" };
  },

  /**
   * 從新品上架移除商品 (透過 toggle 實作)
   * @param {string} productId
   * @returns {Promise<{ success: boolean; message: string }>}
   */
  async remove(productId) {
    const result = await this.toggle(productId);
    if (!result.isFeatured) {
      return { success: true, message: "已從新品上架移除" };
    }
    return { success: false, message: "操作失敗" };
  },
};

/**
 * 轉換後端 CartItemDTO 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').CartItem}
 */
function transformCartItem(backend) {
  if (!backend.variant) {
    throw new Error("CartItem missing variant information");
  }

  const backendVariant = backend.variant;
  const backendProduct = backendVariant.product;

  // 轉換 variant
  const variant = {
    id: String(backendVariant.id),
    productId: String(backendVariant.productId || ""),
    color: backendVariant.color || "",
    size: backendVariant.size || "",
    stock: backendVariant.stock || 0,
    skuCode: "",
  };

  // 轉換 product（從 variant.product 取得）
  const product = backendProduct
    ? {
        id: String(backendProduct.id),
        categoryId: "",
        name: backendProduct.name || "",
        description: "",
        price: Number(backendProduct.price) || 0,
        imageUrl: backendProduct.image || "",
        isListed: true,
      }
    : {
        id: variant.productId,
        categoryId: "",
        name: "",
        description: "",
        price: 0,
        imageUrl: "",
        isListed: true,
      };

  return {
    cartItemId: backend.cartItemId ? String(backend.cartItemId) : undefined,
    product,
    variant,
    quantity: backend.quantity,
  };
}

// ============================================
// Cart API
// ============================================
export const cartApi = {
  /**
   * 取得購物車
   * GET /api/cart
   * @returns {Promise<import('../types.js').CartItem[]>}
   */
  async getAll() {
    const backendCartItems = await apiGet("/cart");
    return backendCartItems.map(transformCartItem);
  },

  /**
   * 加入購物車
   * POST /api/cart
   * @param {string} variantId
   * @param {number} quantity
   * @returns {Promise<import('../types.js').CartItem>}
   */
  async add(variantId, quantity) {
    const backend = await apiPost("/cart", {
      variantId: Number(variantId),
      quantity,
    });
    return transformCartItem(backend);
  },

  /**
   * 更新數量
   * PUT /api/cart/:id
   * @param {string} cartItemId
   * @param {number} quantity
   * @returns {Promise<void>}
   */
  async updateQuantity(cartItemId, quantity) {
    await apiPut(`/cart/${cartItemId}`, { quantity });
  },

  /**
   * 移除項目
   * DELETE /api/cart/:id
   * @param {string} cartItemId
   * @returns {Promise<void>}
   */
  async remove(cartItemId) {
    await apiDelete(`/cart/${cartItemId}`);
  },

  /**
   * 清空購物車
   * DELETE /api/cart
   * @returns {Promise<void>}
   */
  async clear() {
    await apiDelete("/cart");
  },
};

/**
 * 轉換後端 UserProfile 為前端格式
 * @param {Object} backend
 * @returns {import('../types.js').User}
 */
function transformUserProfile(backend) {
  return {
    id: String(backend.id),
    email: backend.email,
    name: backend.name,
    phone: backend.phone,
    role: backend.role === "ADMIN" ? "ADMIN" : "MEMBER",
    orders: [],
  };
}

// ============================================
// User API
// ============================================
export const userApi = {
  /**
   * 取得個人資料
   * GET /api/users/me
   * @returns {Promise<import('../types.js').User | null>}
   */
  async getMe() {
    try {
      const backend = await apiGet("/users/me");
      return transformUserProfile(backend);
    } catch {
      return null;
    }
  },

  /**
   * 更新個人資料
   * PUT /api/users/me
   * @param {{ name?: string; phone?: string; newPassword?: string }} data
   * @returns {Promise<import('../types.js').User>}
   */
  async updateMe(data) {
    const backend = await apiPut("/users/me", data);
    return transformUserProfile(backend);
  },

  /**
   * 更改密碼
   * PUT /api/users/password
   * @param {{ currentPassword: string; newPassword: string }} data
   * @returns {Promise<void>}
   */
  async changePassword(currentPassword, newPassword) {
    await apiPut("/users/password", { currentPassword, newPassword });
  },

  /**
   * 取得所有會員 (Admin)
   * GET /api/users
   * @returns {Promise<Array>}
   */
  async getAll() {
    const backend = await apiGet("/users");
    return backend.map((u) => ({
      id: u.id,
      email: u.email,
      name: u.name,
      phone: u.phone,
      role: u.role,
    }));
  },

  /**
   * 取得用戶訂單數量 (Admin)
   * GET /api/orders/user/:userId
   * @param {string} userId
   * @returns {Promise<number>}
   */
  async getUserOrderCount(userId) {
    try {
      const orders = await apiGet(`/orders/user/${userId}`);
      return orders.length;
    } catch {
      return 0;
    }
  },

  /**
   * 刪除帳號（去識別化 + 軟刪除）
   * POST /api/users/me/delete (使用 POST 以支援 request body)
   * @param {string} password - 當前密碼（用於驗證身份）
   * @returns {Promise<void>}
   */
  async deleteAccount(password) {
    await apiPost("/users/me/delete", { password });
  },
};

// ============================================
// 統一匯出
// ============================================
export const api = {
  products: productApi,
  variants: variantApi,
  orders: orderApi,
  inquiries: inquiryApi,
  categories: categoryApi,
  auth: authApi,
  featured: featuredApi,
  cart: cartApi,
  users: userApi,
};

export default api;
