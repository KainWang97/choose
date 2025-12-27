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
    image: backend.image || "", // 保留原始欄位名稱供 ProductDetail 使用
    colorImages: backend.colorImages || null, // 顏色對應圖片 Map
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
    caseNumber: backend.caseNumber,
    userId: backend.userId ? String(backend.userId) : undefined,
    name: backend.name,
    email: backend.email,
    subject: backend.subject,
    message: backend.message,
    status: backend.status,
    adminReply: backend.adminReply,
    adminReplyBy: backend.adminReplyBy,
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
    emailVerified: backend.emailVerified ?? false,
    passwordSet: backend.passwordSet ?? false,
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
      colorImages: data.colorImages || null,
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
      if (data.colorImages !== undefined)
        requestBody.colorImages = data.colorImages;

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
   * @param {{ name: string; email: string; subject?: string; message: string }} data
   * @returns {Promise<import('../types.js').Inquiry>}
   */
  async create(data) {
    const backend = await apiPost("/inquiries", data);
    return transformInquiry(backend);
  },

  /**
   * 回覆客服訊息（含發送 Email）
   * POST /api/inquiries/:id/reply
   * @param {string} id
   * @param {string} replyContent
   * @returns {Promise<import('../types.js').Inquiry | null>}
   */
  async reply(id, replyContent) {
    try {
      const backend = await apiPost(`/inquiries/${id}/reply`, { replyContent });
      return transformInquiry(backend);
    } catch {
      return null;
    }
  },

  /**
   * 結案客服訊息
   * PATCH /api/inquiries/:id/close
   * @param {string} id
   * @returns {Promise<import('../types.js').Inquiry | null>}
   */
  async close(id) {
    try {
      const backend = await apiPatch(`/inquiries/${id}/close`);
      return transformInquiry(backend);
    } catch {
      return null;
    }
  },

  /**
   * 重開案件（從已結案退回處理中）
   * PATCH /api/inquiries/:id/reopen
   * @param {string} id
   * @returns {Promise<import('../types.js').Inquiry | null>}
   */
  async reopen(id) {
    try {
      const backend = await apiPatch(`/inquiries/${id}/reopen`);
      return transformInquiry(backend);
    } catch {
      return null;
    }
  },
};

// ============================================
// Reply Template API
// ============================================
export const replyTemplateApi = {
  /**
   * 取得所有回覆模板 (Admin)
   * GET /api/reply-templates
   */
  async getAll() {
    return await apiGet("/reply-templates");
  },

  /**
   * 新增回覆模板 (Admin)
   * POST /api/reply-templates
   */
  async create(name, content) {
    return await apiPost("/reply-templates", { name, content });
  },

  /**
   * 更新回覆模板 (Admin)
   * PUT /api/reply-templates/:id
   */
  async update(id, name, content) {
    return await apiPut(`/reply-templates/${id}`, { name, content });
  },

  /**
   * 刪除回覆模板 (Admin)
   * DELETE /api/reply-templates/:id
   */
  async delete(id) {
    return await apiDelete(`/reply-templates/${id}`);
  },
};

// ============================================
// Auth API
// ============================================
import { tokenManager } from "../utils/apiClient.js";

export const authApi = {
  /**
   * 使用者登入
   * POST /api/auth/login
   * @param {{ email: string; password: string }} data
   * @returns {Promise<import('../types.js').User>}
   */
  async login(data) {
    const backend = await apiPost("/auth/login", data);
    // 儲存 Token 到 localStorage
    if (backend.token) {
      tokenManager.setToken(backend.token);
    }
    return transformUser(backend);
  },

  /**
   * 註冊新會員（無密碼，需驗證信箱）
   * POST /api/auth/register
   * @param {{ name: string; email: string }} data
   * @returns {Promise<void>}
   */
  async register(data) {
    await apiPost("/auth/register", data);
    // 不再返回 user，需透過信箱驗證後自動登入
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
    // 清除本地 Token
    tokenManager.removeToken();
    // 後端會清除 HttpOnly Cookie
    await apiPost("/auth/logout");
  },

  /**
   * 驗證信箱（返回用戶資訊以自動登入）
   * POST /api/auth/verify-email
   * @param {string} token
   * @returns {Promise<{user: import('../types.js').User, token: string} | null>}
   */
  async verifyEmail(token) {
    const response = await apiPost("/auth/verify-email", { token });
    if (response && response.token) {
      tokenManager.setToken(response.token);
      return {
        user: transformUser(response),
        token: response.token,
      };
    }
    return null;
  },

  /**
   * 重新發送驗證信
   * POST /api/auth/resend-verification
   * @returns {Promise<void>}
   */
  async resendVerification() {
    await apiPost("/auth/resend-verification");
  },

  /**
   * 忘記密碼
   * POST /api/auth/forgot-password
   * @param {string} email
   * @returns {Promise<void>}
   */
  async forgotPassword(email) {
    await apiPost("/auth/forgot-password", { email });
  },

  /**
   * 重設密碼
   * POST /api/auth/reset-password
   * @param {string} token
   * @param {string} newPassword
   * @returns {Promise<void>}
   */
  async resetPassword(token, newPassword) {
    await apiPost("/auth/reset-password", { token, newPassword });
  },

  /**
   * Magic Link 登入（發送登入驗證信）
   * POST /api/auth/login-magic
   * @param {string} email
   * @returns {Promise<void>}
   */
  async loginMagic(email) {
    await apiPost("/auth/login-magic", { email });
  },

  /**
   * Magic Link 登入驗證
   * POST /api/auth/login-verify
   * @param {string} token
   * @returns {Promise<{user: import('../types.js').User, token: string} | null>}
   */
  async loginVerify(token) {
    const response = await apiPost("/auth/login-verify", { token });
    if (response && response.token) {
      tokenManager.setToken(response.token);
      return {
        user: transformUser(response),
        token: response.token,
      };
    }
    return null;
  },

  /**
   * 初次設定密碼（已登入用戶，token 為可選）
   * POST /api/auth/set-password
   * @param {string} newPassword - 新密碼
   * @param {string} [token] - 可選的驗證 token
   * @returns {Promise<void>}
   */
  async setPassword(newPassword, token = null) {
    const body = { newPassword };
    if (token) {
      body.token = token;
    }
    await apiPost("/auth/set-password", body);
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
export const MAX_FEATURED = 5;

export const featuredApi = {
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

  /**
   * 取得會員消費統計 (Admin)
   * GET /api/users/:userId/statistics
   * @param {string} userId
   * @returns {Promise<Object | null>}
   */
  async getStatistics(userId) {
    try {
      return await apiGet(`/users/${userId}/statistics`);
    } catch {
      return null;
    }
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
  replyTemplates: replyTemplateApi,
};

export default api;
