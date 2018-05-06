package com.sas.example.demo;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.persistence.LockModeType;

/**
 * Custom repository for string mapping resource that supports paging and sorting. Paging may be necessary for
 * reading large number of records.
 */
@RepositoryRestResource
public interface MappingRepository extends PagingAndSortingRepository<Mapping, Long> {

    // Re-declare save methods to delegate record locking to underlying DB

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Mapping save(Mapping entity);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Iterable<Mapping> save(Iterable entities);

    /**
     * Find a mapping by key
     *
     * @param key, query param
     * @return value. Null if key is not found
     */
    Mapping findByKey(@Param("key") String key);

}
