package com.tarun.nest.repository;

import com.tarun.nest.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    Page<UserNotification> findByRecipient_IdOrderByCreatedAtDescIdDesc(Long recipientId, Pageable pageable);

    Page<UserNotification> findByRecipient_IdAndReadStatusFalseOrderByCreatedAtDescIdDesc(
            Long recipientId,
            Pageable pageable
    );

    long countByRecipient_IdAndReadStatusFalse(Long recipientId);

    Optional<UserNotification> findByRecipient_IdAndEventKey(Long recipientId, String eventKey);

    Optional<UserNotification> findByIdAndRecipient_Id(Long id, Long recipientId);

    @Modifying
    @Query(value = "INSERT INTO notifications " +
            "(recipient_id, notification_type, title, message, related_vendor_id, event_key, is_read, created_at, href) " +
            "VALUES (:recipientId, :type, :title, :message, :vendorId, :eventKey, false, CURRENT_TIMESTAMP, :href) " +
            "ON CONFLICT (recipient_id, event_key) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(
            @Param("recipientId") Long recipientId,
            @Param("type") String type,
            @Param("title") String title,
            @Param("message") String message,
            @Param("vendorId") Long relatedVendorId,
            @Param("eventKey") String eventKey,
            @Param("href") String href
    );

    @Modifying
    @Query("update UserNotification n set n.readStatus = true, n.readAt = :readAt " +
            "where n.recipient.id = :recipientId and n.readStatus = false")
    int markAllRead(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);
}