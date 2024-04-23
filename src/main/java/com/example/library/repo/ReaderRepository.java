package com.example.library.repo;

import com.example.library.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {

    Reader findReaderByName(String name);

}