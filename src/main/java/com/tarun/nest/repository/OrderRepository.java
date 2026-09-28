package com.tarun.nest.repository;

import com.tarun.nest.entity.Order;
import com.tarun.nest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}