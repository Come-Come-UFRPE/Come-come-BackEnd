package com.historico_Come_Come.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico")
public class HistoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID querryId;

    private UUID userId;

    private String nomeProduto;

    private LocalDateTime data;

    public UUID getQuerryId() {
        return querryId;
    }

    public void setQuerryId(UUID querryId) {
        this.querryId = querryId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }


}
