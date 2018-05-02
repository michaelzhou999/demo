package com.sas.example.demo;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.LockModeType;

/**
 * Custom repository for string mapping resource that supports paging and sorting. Paging may be necessary for
 * reading large number of records.
 */
@RepositoryRestResource
public interface MappingRepository extends PagingAndSortingRepository<Mapping, String> {

    // Re-declare save methods to delegate record locking to underlying DB

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Mapping save(Mapping entity);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Iterable<Mapping> save(Iterable entities);

}
