# Implementation Plan - Issue #11: Evaluación y separación progresiva del dominio respecto a JPA

Perform a rigorous architectural audit and evaluation of ExplorerSaga Backend's domain models (`Category`, `Source`, `Place`, `PlaceSource`) and their coupling with JPA, documenting clear conclusions and decisions without forcing unnecessary duplication or boilerplate layers.

## User Review Required

> [!IMPORTANT]
> **Audit & Evaluation First**: Issue #11 is primarily an analytical and design issue. The deliverables are `docs/architecture/DOMAIN_MODEL.md` and updates to `HEXAGONAL_ARCHITECTURE.md`. We will avoid creating artificial domain entities and mappers unless a concrete business/architectural benefit is justified.

- **Domain Model Audit**: Evaluate invariants, behavior, boundaries, and JPA coupling for `Category`, `Source`, `Place`, `PlaceSource`, and potential Value Objects (`Location`).
- **Architectural Decision Document**: Create `docs/architecture/DOMAIN_MODEL.md` detailing the evaluation, classification, and final decision (justification for deferred or partial separation vs. current shared model).
- **Architecture Updates**: Update `docs/architecture/HEXAGONAL_ARCHITECTURE.md` to reflect domain evaluation findings.
- **Verification**: Run `.\mvnw.cmd clean verify` to ensure build success and all 33 existing tests pass.

## Open Questions

- None. The evaluation framework is fully specified in Issue #11.

## Proposed Changes

### Architecture & Domain Documentation

#### [NEW] [DOMAIN_MODEL.md](file:///C:/Users/lovei/Documents/XD/ExplorerSaga-Extremadura/backend/docs/architecture/DOMAIN_MODEL.md)
- Comprehensive domain evaluation and architectural decision document for ExplorerSaga Backend.

#### [MODIFY] [HEXAGONAL_ARCHITECTURE.md](file:///C:/Users/lovei/Documents/XD/ExplorerSaga-Extremadura/backend/docs/architecture/HEXAGONAL_ARCHITECTURE.md)
- Update architecture status to incorporate domain model evaluation results.

## Verification Plan

### Automated Tests
- Run `.\mvnw.cmd clean verify` in the backend directory.
- Ensure `BUILD SUCCESS` and all 33 tests pass successfully.

### Manual Verification
- Review `DOMAIN_MODEL.md` against all 17 criteria of Issue #11 Definition of Done.
