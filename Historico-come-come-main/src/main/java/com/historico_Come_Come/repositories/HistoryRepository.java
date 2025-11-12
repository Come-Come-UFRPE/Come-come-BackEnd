package com.historico_Come_Come.repositories; // Verifique seu pacote

import com.historico_Come_Come.models.HistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryModel, Long> {

    List<HistoryModel> findByUserId(UUID userId);

    void deleteAllByUserId(UUID id);

    boolean existsByUserId(UUID id);
}
