package org.dev.tequilacluster.repositories.bottling;

import org.dev.tequilacluster.models.bottling.BottlingLabelValue;
import org.dev.tequilacluster.models.bottling.BottlingLabelValueId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** FR-24/RB-305,307: valores capturados por requisito de etiqueta para un lote de envasado. */
public interface BottlingLabelValueRepository extends JpaRepository<BottlingLabelValue, BottlingLabelValueId> {

    List<BottlingLabelValue> findByBottlingBatchId(UUID bottlingBatchId);

    List<BottlingLabelValue> findByRequirementId(UUID requirementId);
}
