package com.example.library.api;

import lombok.Data;

/**
 * Запрос на выдачу
 */
@Data
public class IssueRequest {

    private long readerId;

    private long bookId;
}