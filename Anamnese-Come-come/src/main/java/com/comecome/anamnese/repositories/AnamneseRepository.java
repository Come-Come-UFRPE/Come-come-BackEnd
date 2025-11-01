package com.comecome.anamnese.repositories;

import com.comecome.anamnese.models.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, UUID> {
    Optional<Anamnese> findByAnamneseID(UUID anamneseID);
}
