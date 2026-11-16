package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.OptionProsCons;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionProsConsRepository
        extends JpaRepository<OptionProsCons, Long> {

    List<OptionProsCons> findByOptionIdOrderByCreatedAtAsc(Long optionId);
}