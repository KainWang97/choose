/**
 * useToast Composable
 * 提供全域的 Toast 通知功能
 */

import { reactive } from "vue";

// 全域狀態
const state = reactive({
  toasts: [],
  nextId: 1,
});

// Toast 類型配置
const TOAST_CONFIG = {
  success: { autoClose: true, duration: 3000 },
  error: { autoClose: false, duration: 0 },
  info: { autoClose: false, duration: 0 },
  warning: { autoClose: false, duration: 0 },
};

/**
 * 新增 Toast
 * @param {string} message 訊息內容
 * @param {'success' | 'error' | 'info' | 'warning'} type Toast 類型
 * @param {Object} options 額外選項
 */
const addToast = (message, type = "info", options = {}) => {
  const config = TOAST_CONFIG[type] || TOAST_CONFIG.info;
  const id = state.nextId++;

  const toast = {
    id,
    message,
    type,
    autoClose: options.autoClose ?? config.autoClose,
    duration: options.duration ?? config.duration,
  };

  state.toasts.push(toast);

  // 如果需要自動關閉
  if (toast.autoClose && toast.duration > 0) {
    setTimeout(() => {
      removeToast(id);
    }, toast.duration);
  }

  return id;
};

/**
 * 移除指定 Toast
 * @param {number} id Toast ID
 */
const removeToast = (id) => {
  const index = state.toasts.findIndex((t) => t.id === id);
  if (index !== -1) {
    state.toasts.splice(index, 1);
  }
};

/**
 * 清除所有 Toast
 */
const clearAllToasts = () => {
  state.toasts.splice(0, state.toasts.length);
};

// Toast 快捷方法
const toast = {
  /**
   * 顯示成功訊息（3 秒後自動關閉）
   * @param {string} message 訊息內容
   */
  success: (message) => addToast(message, "success"),

  /**
   * 顯示錯誤訊息（需手動關閉）
   * @param {string} message 訊息內容
   */
  error: (message) => addToast(message, "error"),

  /**
   * 顯示資訊訊息（需手動關閉）
   * @param {string} message 訊息內容
   */
  info: (message) => addToast(message, "info"),

  /**
   * 顯示警告訊息（需手動關閉）
   * @param {string} message 訊息內容
   */
  warning: (message) => addToast(message, "warning"),
};

/**
 * useToast Hook
 */
export const useToast = () => {
  return {
    state,
    toasts: state.toasts,
    toast,
    addToast,
    removeToast,
    clearAllToasts,
  };
};

export default useToast;
