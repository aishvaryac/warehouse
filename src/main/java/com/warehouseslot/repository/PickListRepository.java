package com.warehouseslot.repository;

import com.warehouseslot.entity.PickList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PickListRepository
        extends JpaRepository<PickList, Long> {

    Optional<PickList> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}