package com.choose.service;

import com.choose.dto.response.MemberStatisticsDTO;
import com.choose.model.User;
import com.choose.repository.OrderRepository;
import com.choose.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberStatisticsService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    /**
     * 取得單一會員的消費統計
     */
    public MemberStatisticsDTO getMemberStatistics(Long userId) {
        log.info("Getting statistics for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Integer orderCount = orderRepository.countByUserId(userId);
        Integer completedOrderCount = orderRepository.countCompletedByUserId(userId);
        BigDecimal totalSpent = orderRepository.sumCompletedAmountByUserId(userId);
        
        // 計算平均客單價
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (completedOrderCount != null && completedOrderCount > 0 && totalSpent != null) {
            averageOrderValue = totalSpent.divide(
                BigDecimal.valueOf(completedOrderCount), 2, RoundingMode.HALF_UP);
        }
        
        return MemberStatisticsDTO.builder()
                .userId(userId)
                .name(user.getName())
                .email(user.getEmail())
                .totalSpent(totalSpent != null ? totalSpent : BigDecimal.ZERO)
                .averageOrderValue(averageOrderValue)
                .orderCount(orderCount != null ? orderCount : 0)
                .completedOrderCount(completedOrderCount != null ? completedOrderCount : 0)
                .lastOrderDate(orderRepository.findLastOrderDateByUserId(userId))
                .firstOrderDate(orderRepository.findFirstOrderDateByUserId(userId))
                .registeredAt(user.getCreatedAt())
                .build();
    }

    /**
     * 取得所有會員的消費統計（用於列表顯示）
     */
    public List<MemberStatisticsDTO> getAllMemberStatistics() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .filter(u -> u.getRole() == User.Role.MEMBER && !u.getIsDeleted())
                .map(u -> getMemberStatistics(u.getUserId()))
                .toList();
    }
}
