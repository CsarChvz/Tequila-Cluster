package org.dev.tequilacluster.repositories.harvest;

import org.dev.tequilacluster.models.harvest.JimaBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** FR-06/37/38: batch_id es FK 1:1 hacia batch(id); trazabilidad por campo o proveedor. */
public interface JimaBatchRepository extends JpaRepository<JimaBatch, UUID> {

    List<JimaBatch> findByField_Id(UUID fieldId);

    List<JimaBatch> findBySupplier_Id(UUID supplierId);

    List<JimaBatch> findByHarvestDateBetween(LocalDate from, LocalDate to);

    @org.springframework.data.jpa.repository.Query("SELECT jb FROM JimaBatch jb " +
            "JOIN FETCH jb.batch b JOIN FETCH b.processStage " +
            "JOIN FETCH jb.field f JOIN FETCH f.authorizedArea " +
            "JOIN FETCH jb.supplier " +
            "WHERE jb.batchId IN :batchIds")
    List<JimaBatch> findWithDetailsByBatchIdIn(@org.springframework.data.repository.query.Param("batchIds") java.util.Collection<UUID> batchIds);

    @org.springframework.data.jpa.repository.Query("SELECT jb FROM JimaBatch jb " +
            "JOIN FETCH jb.batch b JOIN FETCH b.processStage " +
            "JOIN FETCH jb.field f JOIN FETCH f.authorizedArea " +
            "JOIN FETCH jb.supplier " +
            "WHERE jb.batchId = :batchId")
    java.util.Optional<JimaBatch> findWithDetailsByBatchId(@org.springframework.data.repository.query.Param("batchId") UUID batchId);
}
