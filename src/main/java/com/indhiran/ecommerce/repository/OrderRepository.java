package com.indhiran.ecommerce.repository;

import com.indhiran.ecommerce.entity.Order;
import com.indhiran.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByPlacedAtDesc(User user);

    Page<Order> findByUserOrderByPlacedAtDesc(
            User user, Pageable pageable);

}