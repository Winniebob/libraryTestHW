package com.example.library.api;


import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.List;
import java.util.Objects;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import com.example.library.JUnitSpringBootBase;
import com.example.library.model.Issue;
import com.example.library.model.Reader;
import com.example.library.repo.ReaderRepository;
import com.example.library.service.IssueService;


class ReaderControllerTests extends JUnitSpringBootBase {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    ReaderRepository readerRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @MockBean
    private IssueService issueService;

    @Data
    static class JUnitReaderResponse {
        private Long id;
        private String name;
    }

    @BeforeEach
    void clean() {
        readerRepository.deleteAll();
    }

    @Test
    void testGetAllReaders() {
        readerRepository.saveAll(List.of(
                new Reader(1L, "Reader_1"),
                new Reader(2L, "Reader_2")
        ));

        List<Reader> expected = readerRepository.findAll();

        List<JUnitReaderResponse> responseBody = webTestClient.get()
                .uri("/reader")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<JUnitReaderResponse>>() {})
                .returnResult()
                .getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(expected.size(), responseBody.size());
        for (JUnitReaderResponse readerResponse : responseBody) {
            boolean found = expected.stream()
                    .filter(it -> Objects.equals(it.getId(), readerResponse.getId()))
                    .anyMatch(it -> Objects.equals(it.getName(), readerResponse.getName()));
            Assertions.assertTrue(found);
        }
    }

    @Test
    void testFindByIdSuccess() {
        Reader expected = readerRepository.save(new Reader(1L, "Reader_1"));

        JUnitReaderResponse responseBody = webTestClient.get()
                .uri("/reader/" + expected.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(JUnitReaderResponse.class)
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(expected.getId(), responseBody.getId());
        Assertions.assertEquals(expected.getName(), responseBody.getName());
    }

    @Test
    void testFindByIdNotFound() {
        readerRepository.save(new Reader(1L, "Reader_1"));

        Long nonExisting = jdbcTemplate.queryForObject("select max(id) from readers", Long.class);
        nonExisting++;

        webTestClient.get()
                .uri("/reader/" + nonExisting)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testSaveReaderSuccess() {
        JUnitReaderResponse request = new JUnitReaderResponse();
        request.setId(1L);
        request.setName("Reader_1");

        JUnitReaderResponse responseBody = webTestClient.post()
                .uri("/reader")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(JUnitReaderResponse.class)
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertNotNull(responseBody.getId());
        Assertions.assertTrue(readerRepository.findById(request.getId()).isPresent());
    }

    @Test
    void testSaveReaderFail() {
        webTestClient.post()
                .uri("reader")
                .bodyValue(readerRepository.save(new Reader()))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(IllegalArgumentException.class);
    }

    @Test
    void testDeleteReaderSuccess() {
        readerRepository.saveAll(List.of(
                new Reader(1L, "Reader_1"),
                new Reader(2L, "Reader_2")
        ));

        Long deletedId = jdbcTemplate.queryForObject("select max(id) from readers", Long.class);

        webTestClient.delete()
                .uri("/reader/" + deletedId)
                .exchange()
                .expectStatus().isOk();

        Assertions.assertFalse(readerRepository.existsById(deletedId));
    }

    @Test
    void testDeleteReaderNotFound() {
        readerRepository.saveAll(List.of(
                new Reader(1L, "Reader_1"),
                new Reader(2L, "Reader_2")
        ));

        Long nonExisting = jdbcTemplate.queryForObject("select max(id) from readers", Long.class);
        nonExisting++;

        webTestClient.delete()
                .uri("/reader/" + nonExisting)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testGetReaderIssuesSuccess() {
        Reader reader = readerRepository.save(new Reader(1L, "Reader_1"));
        List<Issue> issueList = List.of(
                new Issue(1L, 1L, 1L, LocalDateTime.now()),
                new Issue(2L, 2L, 1L, LocalDateTime.now())
        );

        Mockito.when(issueService.getAllIssuesByReader(reader.getId())).thenReturn(issueList);

        List<Issue> responseBody = webTestClient.get()
                .uri("/reader/" + reader.getId() + "/issue")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Issue>>() {})
                .returnResult().getResponseBody();

        Assertions.assertNotNull(responseBody);
        Assertions.assertEquals(issueList.size(), responseBody.size());
        for (Issue issue : issueList) {
            Assertions.assertEquals(reader.getId(), issue.getReaderId());
        }
    }

    @Test
    void testGetReaderIssuesNotFound() {
        readerRepository.saveAll(List.of(
                new Reader(1L, "Reader_1"),
                new Reader(2L, "Reader_2")
        ));

        Long nonExisting = jdbcTemplate.queryForObject("select max(id) from readers", Long.class);
        nonExisting++;

        Mockito.when(issueService.getAllIssuesByReader(nonExisting)).thenThrow(NoSuchElementException.class);

        webTestClient.get()
                .uri("/reader/" + nonExisting + "/issue")
                .exchange()
                .expectStatus().isNotFound();
    }
}