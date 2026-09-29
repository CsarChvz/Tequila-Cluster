package org.dev.tequilacluster.services.shared;

import tools.jackson.databind.ObjectMapper;
import org.dev.tequilacluster.dtos.alerts.ProcessAlertResponse;
import org.dev.tequilacluster.exceptions.BusinessRuleViolationException;
import org.dev.tequilacluster.models.security.AppUser;
import org.dev.tequilacluster.models.shared.Batch;
import org.dev.tequilacluster.models.shared.ProcessAlert;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.models.shared.enums.AlertSeverity;
import org.dev.tequilacluster.repositories.logistics.ShipmentRepository;
import org.dev.tequilacluster.repositories.security.AppUserRepository;
import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.dev.tequilacluster.repositories.shared.ProcessAlertRepository;
import org.dev.tequilacluster.services.security.StagePermissionService;
import org.dev.tequilacluster.utils.security.StageAction;
import org.dev.tequilacluster.utils.shared.ProcessStageCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
class AlertServiceTest {

    @Mock
    private ProcessAlertRepository processAlertRepository;
    @Mock
    private BatchRepository batchRepository;
    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private StagePermissionService stagePermissionService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AlertService alertService;

    private ProcessStage harvestStage;

    @BeforeEach
    void setUp() {
        harvestStage = new ProcessStage(ProcessStageCodes.HARVEST, "Harvest Stage", (short) 1);
    }

    @Test
    void list_emptyAllowedStages_returnsEmpty() {
        when(stagePermissionService.getAllowedStages(anyList(), eq(StageAction.VIEW))).thenReturn(Set.of());

        List<ProcessAlertResponse> result = alertService.list("OPEN", null, null, null, List.of("UNKNOWN_ROLE"));
        assertEquals(0, result.size());
    }

    @Test
    void list_withPermission_filtersCorrectly() {
        when(stagePermissionService.getAllowedStages(anyList(), eq(StageAction.VIEW)))
                .thenReturn(Set.of(ProcessStageCodes.HARVEST));

        Batch batch = Batch.builder()
                .id(UUID.randomUUID())
                .traceabilityCode("HARV-01")
                .processStage(harvestStage)
                .build();

        ProcessAlert alert = ProcessAlert.builder()
                .id(UUID.randomUUID())
                .batch(batch)
                .alertType("SUPPLIER_INACTIVE")
                .severity(AlertSeverity.CRITICAL)
                .message("Supplier is inactive")
                .detectedAt(Instant.now())
                .build();

        when(processAlertRepository.findWithDetailsFiltered(null, null, null, "OPEN"))
                .thenReturn(List.of(alert));

        List<ProcessAlertResponse> result = alertService.list("OPEN", null, null, null, List.of("AGRONOMIST"));

        assertEquals(1, result.size());
        assertEquals("SUPPLIER_INACTIVE", result.getFirst().alertType());
    }

    @Test
    void resolve_alreadyResolved_throwsBusinessRuleViolation() {
        UUID alertId = UUID.randomUUID();
        ProcessAlert alert = ProcessAlert.builder()
                .id(alertId)
                .alertType("TEST")
                .severity(AlertSeverity.WARNING)
                .message("Test")
                .detectedAt(Instant.now())
                .resolvedAt(Instant.now())
                .build();

        when(processAlertRepository.findByIdForUpdate(alertId)).thenReturn(Optional.of(alert));

        assertThrows(BusinessRuleViolationException.class,
                () -> alertService.resolve(alertId, UUID.randomUUID(), List.of("ADMIN")));
    }

    @Test
    void resolve_success_setsResolvedAtAndAudits() {
        UUID alertId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Batch batch = Batch.builder()
                .id(UUID.randomUUID())
                .traceabilityCode("HARV-01")
                .processStage(harvestStage)
                .build();

        ProcessAlert alert = ProcessAlert.builder()
                .id(alertId)
                .batch(batch)
                .alertType("SUPPLIER_INACTIVE")
                .severity(AlertSeverity.CRITICAL)
                .message("Supplier is inactive")
                .detectedAt(Instant.now())
                .build();

        AppUser user = AppUser.builder().id(userId).username("admin").build();

        when(processAlertRepository.findByIdForUpdate(alertId)).thenReturn(Optional.of(alert));
        when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(processAlertRepository.save(any(ProcessAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcessAlertResponse response = alertService.resolve(alertId, userId, List.of("ADMIN"));

        assertNotNull(response);
        assertNotNull(response.resolvedAt());
        assertEquals("admin", response.resolvedByUsername());
        verify(stagePermissionService).assertAllowed(anyList(), eq(ProcessStageCodes.HARVEST), eq(StageAction.UPDATE));
        verify(auditLogService).record(eq(userId), eq("RESOLVE_ALERT"), eq("process_alert"), eq(alertId), any(), any());
    }
}
