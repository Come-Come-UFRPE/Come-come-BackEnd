package com.historico_Come_Come.services;

import com.historico_Come_Come.dtos.HistoryRecord;
import com.historico_Come_Come.models.HistoryModel;
import com.historico_Come_Come.repositories.HistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<HistoryRecord> getHistoryByUserId(UUID userId) {

        List<HistoryModel> models = historyRepository.findByUserId(userId);

        return models.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteByUserId(UUID id) {

        boolean historicoExiste = historyRepository.existsByUserId(id);

        if (!historicoExiste) {
            throw new RuntimeException("Usuário com ID " + id + " não encontrado.");
        }
        else {
            historyRepository.deleteAllByUserId(id);
            System.out.println("Histórico com UserId:" + id + " foi deletado com sucesso!");
        }
    }

    private HistoryRecord convertToDto(HistoryModel model) {

        return new HistoryRecord(
                model.getUserId(),
                model.getNomeProduto(),
                model.getData()
        );
    }
}