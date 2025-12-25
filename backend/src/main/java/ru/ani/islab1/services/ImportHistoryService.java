package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.ani.islab1.exceptions.ResourceNotFoundException;
import ru.ani.islab1.models.ImportOperation;
import ru.ani.islab1.models.enums.ImportStatus;
import ru.ani.islab1.models.enums.ImportPhase;
import ru.ani.islab1.repositories.ImportOperationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {

    private final ImportOperationRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(String userName, String errorMessage) {
        String safeError = (errorMessage != null && errorMessage.length() > 2048)
                ? errorMessage.substring(0, 2045) + "..."
                : errorMessage;

        ImportOperation log = ImportOperation.builder()
                .userName(userName)
                .status(ImportStatus.FAILED)
                .phase(ImportPhase.ROLLED_BACK)
                .errorMessage(safeError)
                .build();
        repository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportOperation logPrepared(String txId, String userName, String pendingFileKey) {
        ImportOperation log = ImportOperation.builder()
                .txId(txId)
                .userName(userName)
                .status(ImportStatus.SUCCESS)
                .phase(ImportPhase.PREPARED)
                .minioFileKey(pendingFileKey)
                .build();
        return repository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCommitted(String txId, String userName, int count, String finalFileKey) {
        ImportOperation op = repository.findByTxId(txId);
        if (op == null) {
            throw new ResourceNotFoundException("Import transaction not found by txId: " + txId);
        }
        op.setStatus(ImportStatus.SUCCESS);
        op.setPhase(ImportPhase.COMMITTED);
        op.setImportedCount(count);
        op.setUserName(userName);
        op.setMinioFileKey(finalFileKey);
        repository.save(op);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRolledBack(String txId, String errorMessage) {
        ImportOperation op = repository.findByTxId(txId);
        if (op == null) {
            throw new ResourceNotFoundException("Import transaction not found by txId: " + txId);
        }

        String safeError = (errorMessage != null && errorMessage.length() > 2048)
                ? errorMessage.substring(0, 2045) + "..."
                : errorMessage;

        op.setStatus(ImportStatus.FAILED);
        op.setPhase(ImportPhase.ROLLED_BACK);
        op.setErrorMessage(safeError);
        repository.save(op);
    }

    @Transactional(readOnly = true)
    public List<ImportOperation> getHistory() {
        return repository.findAllByOrderByOperationTimeDesc();
    }

    @Transactional(readOnly = true)
    public ImportOperation getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Import operation not found by id: " + id));
    }
}