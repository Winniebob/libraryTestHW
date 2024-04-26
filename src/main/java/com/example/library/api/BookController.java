package com.example.library.api;

import com.example.library.aspect.Timer;
import com.example.library.model.Book;
import com.example.library.service.BookService;
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
@RequestMapping("/book")
@Tag(name = "Book")
public class BookController {



    @Autowired
    private BookService bookService;

    // GET  /book
    @Timer
    @GetMapping()
    @Operation(summary = "get all books", description = "Загружает список книг, внесённых в систему")
    public ResponseEntity<List<Book>> getAllBooks() {
        log.info("Получен запрос актуального списка книг");

        return new ResponseEntity<>(bookService.showAllBooks(), HttpStatus.OK);
    }

    //  GET /book/{id}
    @Timer
    @GetMapping("/{id}")
    @Operation(summary = "get info about book", description = "Загружает информацию о запрашиваемой книге")
    public ResponseEntity<Book> getBookInfo(@PathVariable long id) {
        log.info("Получен запрос информации о книге: Id = {}", id);

        final Book book;
        try {
            book = bookService.showBookInfo(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    //  DELETE /book/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "delete book", description = "Удаляет книгу из системы по Id")
    public ResponseEntity<Book> deleteBook(@PathVariable long id) {
        log.info("Получен запрос на удаление книги: Id = {}", id);

        final Book book;
        try {
            book = bookService.deleteBook(id);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(book);
    }

    //  POST /book
    @PostMapping
    @Operation(summary = "add new book", description = "Добавляет новую книгу в систему")
    public ResponseEntity<Book> addNewBook(@RequestBody BookRequest request) {
        log.info("Получен запрос на добавление книги: name = {}", request.getName());

        final Book book;
        try {
            book = bookService.addNewBook(request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }


}