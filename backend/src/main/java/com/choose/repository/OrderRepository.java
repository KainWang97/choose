package com.choose.repository;

import com.choose.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUserId(Long userId);
    List<Order> findByUserUserIdAndStatus(Long userId, Order.OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatus(Order.OrderStatus status);
    boolean existsByUserUserIdAndStatusIn(Long userId, java.util.Collection<Order.OrderStatus> statuses);

    // 消費統計查詢
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Integer countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId AND o.status = 'COMPLETED'")
    Integer countCompletedByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.userId = :userId AND o.status = 'COMPLETED'")
    BigDecimal sumCompletedAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(o.createdAt) FROM Order o WHERE o.user.userId = :userId")
    LocalDateTime findLastOrderDateByUserId(@Param("userId") Long userId);

    @Query("SELECT MIN(o.createdAt) FROM Order o WHERE o.user.userId = :userId")
    LocalDateTime findFirstOrderDateByUserId(@Param("userId") Long userId);
}
