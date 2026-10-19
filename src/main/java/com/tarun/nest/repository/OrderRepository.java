package com.tarun.nest.repository;

import com.tarun.nest.entity.Order;
import com.tarun.nest.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Optional<Order> findByUserIdAndIdempotencyKey(
            Long userId, String idempotencyKey
    );

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :orderId and o.user.id = :userId")
    Optional<Order> findByIdAndUserIdForUpdate(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId
    );


    @EntityGraph(attributePaths = {
            "user", "items", "items.product", "items.product.vendor"
    })
    @Query("select distinct o from Order o join o.items i "
            + "where i.vendorUserIdSnapshot = :vendorUserId "
            + "or (i.vendorUserIdSnapshot is null and i.product.vendor.id = :vendorUserId) "
            + "order by o.createdAt desc")
    List<Order> findByVendorUserId(@Param("vendorUserId") Long vendorUserId);

    @EntityGraph(attributePaths = {
            "user", "items", "items.product", "items.product.vendor"
    })
    @Query("select distinct o from Order o join o.items i "
            + "where o.id = :orderId and (i.vendorUserIdSnapshot = :vendorUserId "
            + "or (i.vendorUserIdSnapshot is null and i.product.vendor.id = :vendorUserId))")
    Optional<Order> findByIdAndVendorUserId(
            @Param("orderId") Long orderId,
            @Param("vendorUserId") Long vendorUserId
    );
}