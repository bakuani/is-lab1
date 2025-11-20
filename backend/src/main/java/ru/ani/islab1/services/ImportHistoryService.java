package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ani.islab1.models.ImportOperation;
import ru.ani.islab1.models.enums.ImportStatus;
import ru.ani.islab1.repositories.ImportOperationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {

    private final ImportOperationRepository repository;

    @Transactional
    public void logSuccess(String userName, int count) {
        ImportOperation log = ImportOperation.builder()
                .userName(userName)
                .status(ImportStatus.SUCCESS)
                .importedCount(count)
                .build();
        repository.save(log);
    }

    @Transactional
    public void logFailure(String userName, String errorMessage) {
        
        String safeError = (errorMessage != null && errorMessage.length() > 2048)
                ? errorMessage.substring(0, 2045) + "..."
                : errorMessage;

        ImportOperation log = ImportOperation.builder()
                .userName(userName)
                .status(ImportStatus.FAILED)
                .errorMessage(safeError)
                .build();
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ImportOperation> getHistory() {
        return repository.findAllByOrderByOperationTimeDesc();
    }
}