package com.decisionhub.backend.repository;

import com.decisionhub.backend.entity.OptionProsCons;
import com.decisionhub.backend.entity.ProConType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionProsConsRepository extends JpaRepository<OptionProsCons, Long> {

    List<OptionProsCons> findByOptionIdOrderByDisplayOrderAsc(Long optionId);

    List<OptionProsCons> findByOptionDecisionId(Long decisionId);

    Optional<OptionProsCons> findByIdAndOptionId(Long id, Long optionId);

    List<OptionProsCons> findByOptionIdAndTypeOrderByDisplayOrderAsc(Long optionId, ProConType type);
}
