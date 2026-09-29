package org.dev.tequilacluster.services.traceability;

import org.dev.tequilacluster.dtos.traceability.BackwardTraceabilityResponse;
import org.dev.tequilacluster.dtos.traceability.BatchHistoryResponse;
import org.dev.tequilacluster.dtos.traceability.ForwardTraceabilityResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.bottling.BottledUnit;
import org.dev.tequilacluster.models.bottling.BottlingBatch;
import org.dev.tequilacluster.models.catalogs.AgaveField;
import org.dev.tequilacluster.models.catalogs.AuthorizedProductionArea;
import org.dev.tequilacluster.models.catalogs.Brand;
import org.dev.tequilacluster.models.catalogs.Supplier;
import org.dev.tequilacluster.models.catalogs.TequilaCategory;
import org.dev.tequilacluster.models.distillation.DistillationBatch;
import org.dev.tequilacluster.models.harvest.JimaBatch;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.BatchLineage;
import org.dev.tequilacluster.models.shared.BatchLineageId;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.models.shared.enums.BatchStatus;
import org.dev.tequilacluster.repositories.bottling.BottledUnitRepository;
import org.dev.tequilacluster.repositories.bottling.BottlingBatchRepository;
import org.dev.tequilacluster.repositories.catalogs.AgaveFieldRepository;
import org.dev.tequilacluster.repositories.catalogs.SupplierRepository;
import org.dev.tequilacluster.repositories.distillation.DistillationBatchRepository;
import org.dev.tequilacluster.repositories.harvest.JimaBatchRepository;
import org.dev.tequilacluster.repositories.logistics.ShipmentItemRepository;
import org.dev.tequilacluster.repositories.quality.NonConformityRepository;
import org.dev.tequilacluster.repositories.shared.BatchLineageRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.BatchTransitionHistoryRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.services.shared.AlertService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceabilityQueryServiceTest {

    @Mock
    private BatchRepository batchRepository;
    @Mock
    private BatchLineageRepository batchLineageRepository;
    @Mock
    private BottledUnitRepository bottledUnitRepository;
    @Mock
    private JimaBatchRepository jimaBatchRepository;
    @Mock
    private DistillationBatchRepository distillationBatchRepository;
    @Mock
    private BottlingBatchRepository bottlingBatchRepository;
    @Mock
    private ShipmentItemRepository shipmentItemRepository;
    @Mock
    private BatchTransitionHistoryRepository batchTransitionHistoryRepository;
    @Mock
    private ProcessAlertRepository processAlertRepository;
    @Mock
    private NonConformityRepository nonConformityRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private AgaveFieldRepository agaveFieldRepository;
    @Mock
    private StagePermissionService stagePermissionService;
    @Mock
    private AlertService alertService;

    @InjectMocks
    private TraceabilityQueryService service;

    private ProcessStage harvestStage;
    private ProcessStage distillationStage;
    private ProcessStage bottlingStage;

    @BeforeEach
    void setUp() {
        harvestStage = new ProcessStage(ProcessStageCodes.HARVEST, "Harvest Stage", (short) 1);
        distillationStage = new ProcessStage(ProcessStageCodes.DISTILLATION, "Distillation Stage", (short) 2);
        bottlingStage = new ProcessStage(ProcessStageCodes.BOTTLING, "Bottling Stage", (short) 3);
    }

    @Test
    void backward_blankCode_throwsBusinessRuleViolation() {
        assertThrows(BusinessRuleViolationException.class, () -> service.backward("   ", List.of("ADMIN")));
    }

    @Test
    void backward_notFound_throwsNotFoundException() {
        when(bottledUnitRepository.findByUnitCodeWithBatch("UNKNOWN")).thenReturn(Optional.empty());
        when(batchRepository.findByTraceabilityCodeWithStage("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.backward("UNKNOWN", List.of("ADMIN")));
    }

    @Test
    void backward_byBatchCode_success() {
        UUID harvestBatchId = UUID.randomUUID();
        Batch harvestBatch = Batch.builder()
                .id(harvestBatchId)
                .traceabilityCode("BATCH-HARVEST-01")
                .processStage(harvestStage)
                .status(BatchStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        UUID distBatchId = UUID.randomUUID();
        Batch distBatch = Batch.builder()
                .id(distBatchId)
                .traceabilityCode("BATCH-DIST-01")
                .processStage(distillationStage)
                .status(BatchStatus.COMPLETED)
                .createdAt(Instant.now())
                .build();

        when(bottledUnitRepository.findByUnitCodeWithBatch("BATCH-DIST-01")).thenReturn(Optional.empty());
        when(batchRepository.findByTraceabilityCodeWithStage("BATCH-DIST-01")).thenReturn(Optional.of(distBatch));

        BatchLineage lineage = BatchLineage.builder()
                .id(new BatchLineageId(harvestBatchId, distBatchId))
                .parentBatch(harvestBatch)
                .childBatch(distBatch)
                .quantityUsed(new BigDecimal("100.00"))
                .unit("L")
                .linkedAt(Instant.now())
                .build();

        when(batchLineageRepository.findByChildBatch_IdIn(List.of(distBatchId))).thenReturn(List.of(lineage));
        when(batchLineageRepository.findByChildBatch_IdIn(List.of(harvestBatchId))).thenReturn(List.of());

        AuthorizedProductionArea area = AuthorizedProductionArea.builder()
                .id(UUID.randomUUID())
                .code("DO-01")
                .name("Area Valles")
                .stateName("Jalisco")
                .municipality("Tequila")
                .active(true)
                .build();

        AgaveField field = AgaveField.builder()
                .id(UUID.randomUUID())
                .fieldCode("FIELD-01")
                .name("Rancho Los Agaves")
                .authorizedArea(area)
                .active(true)
                .build();

        Supplier supplier = Supplier.builder()
                .id(UUID.randomUUID())
                .supplierCode("SUP-01")
                .legalName("Agave Azul S.A.")
                .taxId("AAZ123456789")
                .active(true)
                .createdAt(Instant.now())
                .build();

        JimaBatch jimaBatch = JimaBatch.builder()
                .batchId(harvestBatchId)
                .batch(harvestBatch)
                .field(field)
                .supplier(supplier)
                .harvestDate(LocalDate.now().minusDays(10))
                .totalWeightKg(new BigDecimal("5000.00"))
                .agaveHeartsCount(500)
                .estimatedYieldL(new BigDecimal("1000.00"))
                .estimatedYieldFactor(new BigDecimal("0.200000"))
                .build();

        when(jimaBatchRepository.findWithDetailsByBatchIdIn(any())).thenReturn(List.of(jimaBatch));

        BackwardTraceabilityResponse response = service.backward("BATCH-DIST-01", List.of("ADMIN"));

        assertNotNull(response);
        assertEquals("BATCH-DIST-01", response.queryCode());
        assertEquals("BATCH", response.searchType());
        assertEquals(2, response.nodes().size());
        assertEquals(1, response.links().size());
        assertEquals(1, response.harvestOrigins().size());
        assertEquals("Agave Azul S.A.", response.harvestOrigins().getFirst().supplierName());
        verify(stagePermissionService).assertAllowed(anyList(), eq(ProcessStageCodes.DISTILLATION), eq(StageAction.VIEW));
    }

    @Test
    void forward_noOriginOrMultipleOrigins_throwsBusinessRuleViolation() {
        assertThrows(BusinessRuleViolationException.class,
                () -> service.forward(null, null, null, List.of("ADMIN")));

        assertThrows(BusinessRuleViolationException.class,
                () -> service.forward(UUID.randomUUID(), UUID.randomUUID(), null, List.of("ADMIN")));
    }

    @Test
    void getHistory_success() {
        UUID batchId = UUID.randomUUID();
        Batch batch = Batch.builder()
                .id(batchId)
                .traceabilityCode("BATCH-BOT-01")
                .processStage(bottlingStage)
                .status(BatchStatus.IN_PROGRESS)
                .createdAt(Instant.now())
                .build();

        when(batchRepository.findByTraceabilityCodeWithStage("BATCH-BOT-01")).thenReturn(Optional.of(batch));

        Brand brand = Brand.builder().id(UUID.randomUUID()).name("Premium Tequila").active(true).build();
        TequilaCategory category = TequilaCategory.builder().id(UUID.randomUUID()).name("Reposado").active(true).build();

        BottlingBatch bottlingBatch = BottlingBatch.builder()
                .id(batchId)
                .batch(batch)
                .brand(brand)
                .category(category)
                .bottlingDate(LocalDate.now())
                .bottleCapacityMl(750)
                .totalVolumeL(new BigDecimal("750.00"))
                .productionLotNumber("LOT-BOT-001")
                .unitsBottled(1000)
                .registeredLossesUnits(5)
                .build();

        when(bottlingBatchRepository.findWithDetailsById(batchId)).thenReturn(Optional.of(bottlingBatch));
        when(batchTransitionHistoryRepository.findByBatchIdOrderByChangedAtDescWithDetails(batchId)).thenReturn(List.of());
        when(processAlertRepository.findByBatchIdOrderByDetectedAtDescWithDetails(batchId)).thenReturn(List.of());
        when(nonConformityRepository.findByBatch_Id(batchId)).thenReturn(List.of());

        BatchHistoryResponse history = service.getHistory("BATCH-BOT-01", List.of("ADMIN"));

        assertNotNull(history);
        assertEquals(batchId, history.batchId());
        assertEquals("BATCH-BOT-01", history.traceabilityCode());
        assertEquals(ProcessStageCodes.BOTTLING, history.stageCode());
        assertNotNull(history.stageDetail());
        verify(stagePermissionService).assertAllowed(anyList(), eq(ProcessStageCodes.BOTTLING), eq(StageAction.VIEW));
    }
}
