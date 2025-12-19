/**
 * API Client Utility
 * 使用 axios 處理統一的 API 請求、錯誤處理
 * JWT token 透過 Bearer Token 和 Cookie 雙重機制
 */
import axios from "axios";

// API Base URL - 從環境變數讀取，預設為開發環境
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/chooseMVP/api";

// Token 儲存 key
const TOKEN_KEY = "auth_token";

// Token 管理函數
export const tokenManager = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  removeToken: () => localStorage.removeItem(TOKEN_KEY),
};

// 建立 axios instance
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Cookie 自動附帶（作為 fallback）
});

// Request 攔截器 - 加入 Authorization header
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenManager.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response 攔截器 - 處理錯誤和解析 ApiResponse
apiClient.interceptors.response.use(
  (response) => {
    // 解析 ApiResponse 格式
    const result = response.data;
    if (!result.success) {
      return Promise.reject(new Error(result.message || "API request failed"));
    }
    return result.data;
  },
  (error) => {
    let errorMessage = "Network error";

    if (error.response) {
      const { status, data } = error.response;
      errorMessage =
        data?.message || `HTTP ${status}: ${error.response.statusText}`;

      // 401 Unauthorized - token 過期或無效
      if (status === 401) {
        tokenManager.removeToken();
        if (typeof window !== "undefined") {
          window.dispatchEvent(new CustomEvent("auth:logout"));
        }
      }
    }

    return Promise.reject(new Error(errorMessage));
  }
);

/**
 * GET 請求
 * @template T
 * @param {string} endpoint
 * @returns {Promise<T>}
 */
export async function apiGet(endpoint) {
  return apiClient.get(endpoint);
}

/**
 * POST 請求
 * @template T
 * @param {string} endpoint
 * @param {unknown} [body]
 * @returns {Promise<T>}
 */
export async function apiPost(endpoint, body) {
  return apiClient.post(endpoint, body);
}

/**
 * PUT 請求
 * @template T
 * @param {string} endpoint
 * @param {unknown} [body]
 * @returns {Promise<T>}
 */
export async function apiPut(endpoint, body) {
  return apiClient.put(endpoint, body);
}

/**
 * PATCH 請求
 * @template T
 * @param {string} endpoint
 * @param {unknown} [body]
 * @returns {Promise<T>}
 */
export async function apiPatch(endpoint, body) {
  return apiClient.patch(endpoint, body);
}

/**
 * DELETE 請求
 * @template T
 * @param {string} endpoint
 * @returns {Promise<T>}
 */
export async function apiDelete(endpoint) {
  return apiClient.delete(endpoint);
}

// 導出 axios instance 供特殊用途
export { apiClient };
