package com.backend.favorite.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_FAVORITE")
public class Favorite {

    @Id
    @GeneratedValue
    private UUID id;

    private String productId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("favorites")
    private Category category;

}
