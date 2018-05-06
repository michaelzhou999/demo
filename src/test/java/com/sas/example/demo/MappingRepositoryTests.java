package com.sas.example.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@EnableJpaRepositories(basePackages = {"com.sas.example.demo"})
public class MappingRepositoryTests {
    @Autowired
    private MappingRepository repository;

    @Test
    public void testAddSearchAndDeleteAll() {
        assertEquals(repository.count(), 0);

        repository.save(new Mapping("one", "1"));
        repository.save(new Mapping("two", "2"));
        repository.save(new Mapping("three", "3"));

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
        repository.save(new Mapping("one", "1"));
        repository.save(new Mapping("two", "2"));
        repository.save(new Mapping("three", "3"));

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

    @Test(expected = DataIntegrityViolationException.class)
    public void testUpdate() {
        repository.save(new Mapping("one", "1"));
        repository.save(new Mapping("two", "2"));
        repository.save(new Mapping("three", "3"));

        Mapping mapping = repository.findByKey("one");
        assertNotNull(mapping);

        // attempt to update a record and expect an exception
        repository.save(new Mapping("two", "22"));
        assertEquals(repository.count(), 3);

        mapping = repository.findByKey("two");
        assertNotNull(mapping);
        assertThat(mapping.getKey()).isEqualTo("two");
        assertThat(mapping.getValue()).isEqualTo("2");
    }

}
