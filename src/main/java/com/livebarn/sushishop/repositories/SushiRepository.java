package com.livebarn.sushishop.repositories;

import java.util.Optional;

import com.livebarn.sushishop.entities.Sushi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SushiRepository extends JpaRepository<Sushi, Integer> {

    Optional<Sushi> findByName(String name);
}
