package com.warehouseslot.repository;

import com.warehouseslot.entity.PickListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickListItemRepository extends JpaRepository<PickListItem, Long> {

    // Check whether a bin is already used in a pick list
    boolean existsByBinId(Long binId);

    // Get pick list items in pick sequence order
    List<PickListItem> findByPickListIdOrderByPickSequenceAsc(Long pickListId);
}