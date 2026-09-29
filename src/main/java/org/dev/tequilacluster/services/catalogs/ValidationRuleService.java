package org.dev.tequilacluster.services.catalogs;

import org.dev.tequilacluster.dtos.catalogs.ValidationRuleRequest;
import org.dev.tequilacluster.dtos.catalogs.ValidationRuleResponse;
import org.dev.tequilacluster.exceptions.NotFoundException;
import org.dev.tequilacluster.models.catalogs.ValidationRule;
import org.dev.tequilacluster.models.shared.ProcessStage;
import org.dev.tequilacluster.repositories.catalogs.ValidationRuleRepository;
import org.dev.tequilacluster.repositories.shared.ProcessStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * FR-04/RB-103/203/204/207: Administrator CRUD for the configurable range/tolerance rules used
 * by every stage's alerts.
 */
@Service
public class ValidationRuleService {

    private final ValidationRuleRepository repository;
    private final ProcessStageRepository processStageRepository;

    public ValidationRuleService(ValidationRuleRepository repository, ProcessStageRepository processStageRepository) {
        this.repository = repository;
        this.processStageRepository = processStageRepository;
    }

    /** @Transactional needed: ValidationRule.processStage is a lazy association read in toResponse(). */
    @Transactional(readOnly = true)
    public List<ValidationRuleResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ValidationRuleResponse get(UUID id) {
        return toResponse(findOrThrow(id));
    }

    public ValidationRuleResponse create(ValidationRuleRequest request) {
        ValidationRule rule = new ValidationRule();
        applyRequest(rule, request);
        rule.setActive(true);
        return toResponse(repository.save(rule));
    }

    public ValidationRuleResponse update(UUID id, ValidationRuleRequest request) {
        ValidationRule rule = findOrThrow(id);
        applyRequest(rule, request);
        return toResponse(repository.save(rule));
    }

    /** Catalogs are never hard-deleted (NFR-09 spirit) — deactivate instead. */
    public void deactivate(UUID id) {
        ValidationRule rule = findOrThrow(id);
        rule.setActive(false);
        repository.save(rule);
    }

    private ValidationRule findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("ValidationRule", id));
    }

    private void applyRequest(ValidationRule rule, ValidationRuleRequest request) {
        ProcessStage stage = processStageRepository.findById(request.stageCode())
                .orElseThrow(() -> NotFoundException.of("ProcessStage", request.stageCode()));
        rule.setProcessStage(stage);
        rule.setParameterCode(request.parameterCode());
        rule.setDisplayName(request.displayName());
        rule.setUnit(request.unit());
        rule.setMinValue(request.minValue());
        rule.setMaxValue(request.maxValue());
        rule.setAllowedDeviation(request.allowedDeviation());
        rule.setValidFrom(request.validFrom());
        rule.setValidTo(request.validTo());
    }

    private ValidationRuleResponse toResponse(ValidationRule rule) {
        return new ValidationRuleResponse(
                rule.getId(),
                rule.getProcessStage() != null ? rule.getProcessStage().getCode() : null,
                rule.getParameterCode(),
                rule.getDisplayName(),
                rule.getUnit(),
                rule.getMinValue(),
                rule.getMaxValue(),
                rule.getAllowedDeviation(),
                Boolean.TRUE.equals(rule.getActive()),
                rule.getValidFrom(),
                rule.getValidTo()
        );
    }
}
