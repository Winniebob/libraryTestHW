package com.example.library.api;

import com.example.library.aspect.Timer;
import com.example.library.model.Issue;
import com.example.library.model.Reader;
import com.example.library.service.IssueService;
import com.example.library.service.ReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/reader")
@Tag(name = "Reader")
public class ReaderController {

    @Autowired
    private ReaderService readerService;
    @Autowired
    private IssueService issueService;

    // GET  /reader
    @Timer
    @GetMapping()
    @Operation(summary = "get all readers", description = "Загружает список читателей, зарегистрированных в системе")
    public ResponseEntity<List<Reader>> getAllReaders() {
        log.info("Получен запрос актуального списка читателей");

        return new ResponseEntity<>(readerService.showAllReaders(), HttpStatus.OK);
    }

    //  GET /reader/{id}
    @Timer
    @GetMapping("/{id}")
    @Operation(summary = "get info about reader", description = "Загружает информацию о запрашиваемом читателе")
    public ResponseEntity<Reader> getReaderInfo(@PathVariable long id) {
        log.info("Получен запрос информации о читателе: Id = {}", id);

        final Reader reader;
        try {
            reader = readerService.showReaderInfo(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(reader);
    }

    //  DELETE /reader/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "delete reader", description = "Удаляет читателя из системы по Id")
    public ResponseEntity<Reader> deleteReader(@PathVariable long id) {
        log.info("Получен запрос на удаление читателя: Id = {}", id);

        final Reader reader;
        try {
            reader = readerService.deleteReader(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(reader);
    }

    //  POST /reader
    @PostMapping()
    @Operation(summary = "add new reader", description = "Добавляет нового читателя в систему")
    public ResponseEntity<Reader> addNewReader(@RequestBody ReaderRequest request) {
        log.info("Получен запрос на добавление читателя: name = {}", request.getName());

        final Reader reader;
        try {
            reader = readerService.addNewReader(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(reader);
    }

    //  GET /reader/{id}/issue
    @Timer
    @GetMapping("/{id}/issue")
    @Operation(summary = "get all issuance by reader", description = "Загружает список выдач книг читателя")
    public ResponseEntity<List<Issue>> getReaderIssues(@PathVariable long id) {
        log.info("Получен запрос информации о выдачах читателя с id = {}", id);

        final List<Issue> readersIssues;
        try {
            readersIssues = issueService.getAllIssuesByReader(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(readersIssues);
    }
}