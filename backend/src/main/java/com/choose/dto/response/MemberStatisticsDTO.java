package com.choose.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 會員消費統計 DTO
 */
@Data
@Builder
public class MemberStatisticsDTO {
    private Long userId;
    private String name;
    private String email;
    
    // 消費統計
    private BigDecimal totalSpent;           // 累計消費金額
    private BigDecimal averageOrderValue;    // 平均客單價
    private Integer orderCount;              // 訂單總數
    private Integer completedOrderCount;     // 已完成訂單數
    private LocalDateTime lastOrderDate;     // 最近消費日期
    private LocalDateTime firstOrderDate;    // 首次消費日期
    
    // 會員資訊
    private LocalDateTime registeredAt;      // 註冊日期
}
