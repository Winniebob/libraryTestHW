package com.example.library.myMetrics;

import com.example.library.api.IssueRequest;
import com.example.library.model.Issue;
import com.example.library.service.IssueService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.NoSuchElementException;


@Slf4j
@Component
public class MyMetrics {

    @Autowired
    private IssueService service;
    private final Counter issuedBooksCount ;
    private final Counter deniedRequestsCount ;
    public MyMetrics(MeterRegistry meterRegistry) {
        issuedBooksCount = meterRegistry.counter("issued_books_count");
        deniedRequestsCount = meterRegistry.counter("denied_requests_count");
    }

    // POST /issue
    @PostMapping
    @Operation(summary = "issue book", description = "Регистрирует выдачу книги читателю")
    public ResponseEntity<Issue> issueBook(@RequestBody IssueRequest request) {
        log.info("Получен запрос на выдачу: readerId = {}, bookId = {}", request.getReaderId(), request.getBookId());

        final Issue issue;
        try {
            issue = service.issue(request);
            // Успешная выдача книги
            issuedBooksCount.increment();
        } catch (NoSuchElementException e) {
            // Ошибка - ресурс не найден
            deniedRequestsCount.increment();
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            // Ошибка - превышен лимит книг у читателя или другая причина
            deniedRequestsCount.increment();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(issue);
    }
}
