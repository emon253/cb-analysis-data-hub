package org.carbarn.scrapify.autotrader.repositories;

import org.carbarn.scrapify.autotrader.domain.Flag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlagRepository extends JpaRepository<Flag, Integer> {
    Optional<Flag> findByName(String name);
}
