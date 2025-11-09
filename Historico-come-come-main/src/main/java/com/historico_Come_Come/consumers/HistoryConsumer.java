package com.historico_Come_Come.consumers;

import com.historico_Come_Come.dtos.HistoryRecord;
import com.historico_Come_Come.models.HistoryModel;
import com.historico_Come_Come.repositories.HistoryRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class HistoryConsumer {

    final HistoryRepository historyRepository;

    public HistoryConsumer(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @RabbitListener(queues = "fila-historico")
    public void listenHistoryQueue(@Payload HistoryRecord historyRecord) {

        HistoryModel model = new HistoryModel();
        model.setNomeProduto(historyRecord.query());
        model.setData(historyRecord.dataDaBusca());
        model.setUserId(historyRecord.userID());

        try {
            historyRepository.save(model);
            System.out.println("Histórico salvo no banco com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao salvar no banco: " + e.getMessage());

        }
    }
}
