package com.sas.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class MappingRepositoryTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MappingRepository repository;

    @Test
    public void testSearchAndDeleteAll() {
        entityManager.persist(new Mapping("one", "1"));
        entityManager.persist(new Mapping("two", "2"));
        entityManager.persist(new Mapping("three", "3"));

        assertEquals(repository.count(), 3);

        for (Mapping m : repository.findAll()) {
            System.out.println(m);
        }

        Mapping mapping;
        mapping = repository.findByKey("one");
        assertThat(mapping.getKey()).isEqualTo("one");
        assertThat(mapping.getValue()).isEqualTo("1");

        mapping = repository.findByKey("two");
        assertThat(mapping.getKey()).isEqualTo("two");
        assertThat(mapping.getValue()).isEqualTo("2");

        mapping = repository.findByKey("three");
        assertThat(mapping.getKey()).isEqualTo("three");
        assertThat(mapping.getValue()).isEqualTo("3");

        mapping = repository.findByKey("four");
        assertNull(mapping);

        repository.deleteAll();

        assertEquals(repository.count(), 0);

        mapping = repository.findByKey("one");
        assertNull(mapping);
        mapping = repository.findByKey("two");
        assertNull(mapping);
        mapping = repository.findByKey("three");
        assertNull(mapping);
        mapping = repository.findByKey("four");
        assertNull(mapping);
    }

    @Test
    public void testDelete() {
        entityManager.persist(new Mapping("one", "1"));
        entityManager.persist(new Mapping("two", "2"));
        entityManager.persist(new Mapping("three", "3"));

        assertEquals(repository.count(), 3);

        Mapping mapping = repository.findByKey("one");
        assertNotNull(mapping);
        repository.delete(mapping);

        assertEquals(repository.count(), 2);

        mapping = repository.findByKey("one");
        assertNull(mapping);

        mapping = repository.findByKey("three");
        assertNotNull(mapping);
        repository.delete(mapping);

        assertEquals(repository.count(), 1);

        mapping = repository.findByKey("one");
        assertNull(mapping);

        mapping = repository.findByKey("two");
        assertNotNull(mapping);
    }

}
