package com.livebarn.sushishop.repositories;

import java.util.Optional;

import com.livebarn.sushishop.entities.SushiOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SushiOrderRepository extends JpaRepository<SushiOrder, Integer> {

    Optional<SushiOrder> findFirstByStatusIdOrderByCreatedAt(int statusId);

}
