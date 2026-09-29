package org.dev.tequilacluster.repositories.distillation;

import org.dev.tequilacluster.models.distillation.DistillationBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** FR-19/RB-205,206: control de maduración y disponibilidad para envasado. */
public interface DistillationBatchRepository extends JpaRepository<DistillationBatch, UUID> {

    /** Ready-for-bottling: batch de destilación cuya fecha de listo ya se alcanzó y aún no tiene envasado ligado. */
    List<DistillationBatch> findByReadyForBottlingAtIsNotNullAndReadyForBottlingAtLessThanEqual(OffsetDateTime asOf);

    /** Maturing: requiere maduración y todavía no llega su fecha lista-para-envasar. */
    List<DistillationBatch> findByMaturationRequiredTrueAndReadyForBottlingAtIsNull();

    List<DistillationBatch> findByMaturationRequiredTrueAndMaturationStartDateIsNotNull();
}
