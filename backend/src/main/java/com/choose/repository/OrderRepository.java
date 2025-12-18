package com.choose.repository;

import com.choose.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserUserId(Long userId);
    List<Order> findByUserUserIdAndStatus(Long userId, Order.OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByStatus(Order.OrderStatus status);
    boolean existsByUserUserIdAndStatusIn(Long userId, java.util.Collection<Order.OrderStatus> statuses);
}

