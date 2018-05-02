package com.sas.example.demo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Custom repository for string mapping resource that supports paging and sorting
 *
 * TODO - add transaction support
 */
@RepositoryRestResource
public interface MappingRepository extends PagingAndSortingRepository<Mapping, String> {
}
