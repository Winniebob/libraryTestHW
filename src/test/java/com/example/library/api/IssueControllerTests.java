package com.example.library.api;


import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import com.example.library.JUnitSpringBootBase;
import com.example.library.model.Book;
import com.example.library.model.Issue;
import com.example.library.model.Reader;
import com.example.library.repo.BookRepository;
import com.example.library.repo.IssueRepository;
import com.example.library.repo.ReaderRepository;


class IssueControllerTests extends JUnitSpringBootBase {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    IssueRepository issueRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    ReaderRepository readerRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Data
    static class JUnitIssueResponse {
        private Long id;
        private Long bookId;
        private Long readerId;
        private LocalDateTime issued_at;
        private LocalDateTime returned_at;
    }

    @BeforeEach
    void clean() {
        bookRepository.deleteAll();
        readerRepository.deleteAll();
        issueRepository.deleteAll();
    }

    @Test
    void testGetAllIssues() {
        issueRepository.saveAll(List.of(
                new Issue(1L, 1L, 1L, LocalDateTime.now()),
                new Issue(2L, 2L, 2L, LocalDateTime.now())
        ));

        List<Issue> expected = issueRepository.findAll();

        List<JUnitIssueResponse> responseBody = webTestClient.get()
                .uri("/issue")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<JUnitIssueResponse>>() {})
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(expected.size(), responseBody.size());
        for (JUnitIssueResponse issueResponse : responseBody) {
            boolean found = expected.stream()
                    .filter(it -> Objects.equals(it.getId(), issueResponse.getId()))
                    .anyMatch(it -> Objects.equals(it.getBookId(), issueResponse.getBookId()));
            Assertions.assertTrue(found);
        }
    }

    @Test
    void testFindByIdSuccess() {
        Issue expected = issueRepository.save(new Issue(1L, 1L, 1L, LocalDateTime.now()));

        JUnitIssueResponse responseBody = webTestClient.get()
                .uri("/issue/" + expected.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(JUnitIssueResponse.class)
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(expected.getId(), responseBody.getId());
        Assertions.assertEquals(expected.getBookId(), responseBody.getBookId());
        Assertions.assertEquals(expected.getReaderId(), responseBody.getReaderId());
        Assertions.assertEquals(expected.getIssued_at().toLocalDate(), responseBody.getIssued_at().toLocalDate());
        Assertions.assertEquals(expected.getReturned_at(), responseBody.getReturned_at());
    }

    @Test
    void testFindByIdNotFound() {
        issueRepository.save(new Issue(1L, 1L, 1L, LocalDateTime.now()));

        Long nonExisting = jdbcTemplate.queryForObject("select max(id) from issues", Long.class);
        nonExisting++;

        webTestClient.get()
                .uri("/issue/" + nonExisting)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testSaveIssueSuccess() {
        bookRepository.save(new Book(1L, "Book_1"));
        readerRepository.save(new Reader(1L, "Reader_1"));
        JUnitIssueResponse request = new JUnitIssueResponse();
        request.setId(1L);
        request.setBookId(1L);
        request.setReaderId(1L);
        request.setIssued_at(LocalDateTime.now());

        JUnitIssueResponse responseBody = webTestClient.post()
                .uri("/issue")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(JUnitIssueResponse.class)
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertNotNull(responseBody.getId());
        Assertions.assertTrue(issueRepository.findById(request.getId()).isPresent());
    }

    @Test
    void testSaveIssueNotFound() {
        JUnitIssueResponse request = new JUnitIssueResponse();
        request.setId(1L);
        request.setBookId(1L);
        request.setReaderId(1L);
        request.setIssued_at(LocalDateTime.now());

        webTestClient.post()
                .uri("/issue")
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testSaveIssueNotConflict() {
        bookRepository.saveAll(List.of(
                new Book(1L, "Book_1"),
                new Book(2L, "Book_2")
        ));
        readerRepository.save(new Reader(1L, "Reader_1"));
        issueRepository.saveAll(List.of(
                new Issue(1L, 1L, 1L, LocalDateTime.now()),
                new Issue(2L, 2L, 1L, LocalDateTime.now())
        ));

        JUnitIssueResponse request = new JUnitIssueResponse();
        request.setId(1L);
        request.setBookId(1L);
        request.setReaderId(1L);
        request.setIssued_at(LocalDateTime.now());

        webTestClient.post()
                .uri("/issue")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void testReturnBookSuccess() {
        Issue expected = issueRepository.save(new Issue(1L, 1L, 1L, LocalDateTime.now()));

        JUnitIssueResponse responseBody = webTestClient.put()
                .uri("/issue/" + expected.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(JUnitIssueResponse.class)
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(expected.getId(), responseBody.getId());
        Assertions.assertEquals(expected.getBookId(), responseBody.getBookId());
        Assertions.assertEquals(expected.getReaderId(), responseBody.getReaderId());
        Assertions.assertEquals(expected.getIssued_at().toLocalDate(), responseBody.getIssued_at().toLocalDate());
        Assertions.assertNotNull(responseBody.getReturned_at());
    }

    @Test
    void testReturnBookNotFound() {
        issueRepository.save(new Issue(1L, 1L, 1L, LocalDateTime.now()));

        Long nonExisting = jdbcTemplate.queryForObject("select max(id) from issues", Long.class);
        nonExisting++;

        webTestClient.put()
                .uri("/issue/" + nonExisting)
                .exchange()
                .expectStatus().isNotFound();
    }
}