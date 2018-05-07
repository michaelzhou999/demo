package com.sas.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RestInterfaceTests {

    private static final String BASE_URL = "/mappings";
    private static final String HTTP_HOST = "http://localhost";

    private MockMvc mvc;

    @Autowired
    WebApplicationContext webContext;

    /** Convert an object to JSON string */
    public static String asJson(final Object o) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String json = mapper.writeValueAsString(o);
            return json;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void init() {
        mvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Test
    public void emptyRepositoryTest() throws Exception {
        final ResultActions result = mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isEmpty())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.totalElements").value(0))
                .andExpect(jsonPath("page.totalPages").value(0))
                .andReturn();
    }

    @Test
    public void createTest() throws Exception {
        final ResultActions result = mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("one", "1")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("1"))
                .andReturn();
    }

    @Test
    public void updateTest() throws Exception {
        // Create 2 mappings
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("one", "1")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("1"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/1"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/1"))
                .andReturn();
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("two", "2")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("two"))
                .andExpect(jsonPath("value").value("2"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/2"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/2"))
                .andReturn();

        // Check all mappings - should have 2 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(2))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Updates an existing record with different value
        ResultActions result = mvc.perform(put(BASE_URL + "/1")
                .content(asJson(new Mapping("one", "11")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("11"))
                .andReturn();

        // Updates an existing record with same value
        result = mvc.perform(put(BASE_URL + "/1")
                .content(asJson(new Mapping("one", "11")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("11"))
                .andReturn();

        // Check all mappings - should still have 2 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(2))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Updates a non-existing record, which will create a new one
        result = mvc.perform(put(BASE_URL + "/3")
                .content(asJson(new Mapping("three", "3")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("three"))
                .andExpect(jsonPath("value").value("3"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/3"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/3"))
                .andExpect(jsonPath("_links.search").doesNotExist())
                .andExpect(jsonPath("_links.profile").doesNotExist())
                .andExpect(jsonPath("page").doesNotExist())
                .andReturn();

        // Check all mappings - should have 3 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(3))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();
    }

    @Test
    public void deleteTest() throws Exception {
        // Create 2 mappings
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("one", "1")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("1"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/1"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/1"))
                .andReturn();
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("two", "2")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("two"))
                .andExpect(jsonPath("value").value("2"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/2"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/2"))
                .andReturn();

        // Check all mappings - should have 2 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(2))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Delete a mapping
        ResultActions result = mvc.perform(delete(BASE_URL + "/1")
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andReturn();

        // Check all mappings - should have 1 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(1))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Delete a non-existing record
        result = mvc.perform(delete(BASE_URL + "/3")
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andReturn();

        // Check all mappings - should have 3 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(1))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();
    }

    @Test
    public void findTest() throws Exception {
        // Create 2 mappings
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("one", "1")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("1"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/1"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/1"))
                .andReturn();
        mvc.perform(post(BASE_URL)
                .content(asJson(new Mapping("two", "2")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("key").value("two"))
                .andExpect(jsonPath("value").value("2"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/2"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/2"))
                .andReturn();

        // Check all mappings - should have 2 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(2))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Find a mapping by key
        ResultActions result = mvc.perform(get(BASE_URL + "/search/findByKey")
                .param("key", "one")
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("key").value("one"))
                .andExpect(jsonPath("value").value("1"))
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings/1"))
                .andExpect(jsonPath("_links.mapping.href").value(HTTP_HOST + "/mappings/1"))
                .andExpect(jsonPath("_links.profile").doesNotExist())
                .andExpect(jsonPath("_links.search").doesNotExist())
                .andExpect(jsonPath("page").doesNotExist())
                .andReturn();

        // Find a non-existing mapping by key
        result = mvc.perform(get(BASE_URL + "/search/findByKey")
                .param("key", "does not exist")
                .accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andReturn();

        // Check all mappings - should still have 2 elements
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(2))
                .andExpect(jsonPath("page.totalPages").value(1))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();
    }

    private void addMapping(String key, String value) {
        try {
            mvc.perform(post(BASE_URL)
                    .content(asJson(new Mapping(key, value)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void pagingTest() throws Exception {
        // Create 65 mappings
        IntStream.rangeClosed(1, 65).forEach(
                i -> addMapping("key" + i, "value" + i)
        );

        // Check all mappings w/o any query params
        mvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=20"))
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=1&size=20"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=3&size=20"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(4))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Check first page explicitly, with default size
        mvc.perform(get(BASE_URL)
                .param("page", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=20"))
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=1&size=20"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=3&size=20"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(4))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Check second page explicitly, with default size
        mvc.perform(get(BASE_URL)
                .param("page", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{&sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=20"))
                .andExpect(jsonPath("_links.prev.href").value(HTTP_HOST + "/mappings?page=0&size=20"))
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=2&size=20"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=3&size=20"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(4))
                .andExpect(jsonPath("page.number").value(1))
                .andReturn();

        // Check last page explicitly, with default size
        mvc.perform(get(BASE_URL)
                .param("page", "3")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{&sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=20"))
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.prev.href").value(HTTP_HOST + "/mappings?page=2&size=20"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(4))
                .andExpect(jsonPath("page.number").value(3))
                .andReturn();

        // Check all mappings with custom page size of 10
        mvc.perform(get(BASE_URL)
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_embedded.mappings[0].key").value("key1"))
                .andExpect(jsonPath("_embedded.mappings[0].value").value("value1"))
                .andExpect(jsonPath("_embedded.mappings[9].key").value("key10"))
                .andExpect(jsonPath("_embedded.mappings[9].value").value("value10"))
                .andExpect(jsonPath("_embedded.mappings[1]").exists())
                .andExpect(jsonPath("_embedded.mappings[2]").exists())
                .andExpect(jsonPath("_embedded.mappings[3]").exists())
                .andExpect(jsonPath("_embedded.mappings[4]").exists())
                .andExpect(jsonPath("_embedded.mappings[5]").exists())
                .andExpect(jsonPath("_embedded.mappings[6]").exists())
                .andExpect(jsonPath("_embedded.mappings[7]").exists())
                .andExpect(jsonPath("_embedded.mappings[8]").exists())
                .andExpect(jsonPath("_embedded.mappings[10]").doesNotExist())
                .andExpect(jsonPath("_embedded.mappings[11]").doesNotExist())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{&sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=10"))
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=1&size=10"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=6&size=10"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(7))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Check third page with custom page size of 10
        mvc.perform(get(BASE_URL)
                .param("page", "2")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_embedded.mappings[0].key").value("key21"))
                .andExpect(jsonPath("_embedded.mappings[0].value").value("value21"))
                .andExpect(jsonPath("_embedded.mappings[9].key").value("key30"))
                .andExpect(jsonPath("_embedded.mappings[9].value").value("value30"))
                .andExpect(jsonPath("_embedded.mappings[1]").exists())
                .andExpect(jsonPath("_embedded.mappings[2]").exists())
                .andExpect(jsonPath("_embedded.mappings[3]").exists())
                .andExpect(jsonPath("_embedded.mappings[4]").exists())
                .andExpect(jsonPath("_embedded.mappings[5]").exists())
                .andExpect(jsonPath("_embedded.mappings[6]").exists())
                .andExpect(jsonPath("_embedded.mappings[7]").exists())
                .andExpect(jsonPath("_embedded.mappings[8]").exists())
                .andExpect(jsonPath("_embedded.mappings[10]").doesNotExist())
                .andExpect(jsonPath("_embedded.mappings[11]").doesNotExist())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{&sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=10"))
                .andExpect(jsonPath("_links.prev.href").value(HTTP_HOST + "/mappings?page=1&size=10"))
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=3&size=10"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=6&size=10"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(7))
                .andExpect(jsonPath("page.number").value(2))
                .andReturn();

        // Check last page with custom page size of 10
        mvc.perform(get(BASE_URL)
                .param("page", "6")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_embedded.mappings[0]").exists())
                .andExpect(jsonPath("_embedded.mappings[0].key").value("key61"))
                .andExpect(jsonPath("_embedded.mappings[0].value").value("value61"))
                .andExpect(jsonPath("_embedded.mappings[1]").exists())
                .andExpect(jsonPath("_embedded.mappings[2]").exists())
                .andExpect(jsonPath("_embedded.mappings[3]").exists())
                .andExpect(jsonPath("_embedded.mappings[4]").exists())
                .andExpect(jsonPath("_embedded.mappings[4].key").value("key65"))
                .andExpect(jsonPath("_embedded.mappings[4].value").value("value65"))
                .andExpect(jsonPath("_embedded.mappings[5]").doesNotExist())
                .andExpect(jsonPath("_embedded.mappings[6]").doesNotExist())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings{&sort}"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=10"))
                .andExpect(jsonPath("_links.prev.href").value(HTTP_HOST + "/mappings?page=5&size=10"))
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=6&size=10"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(7))
                .andExpect(jsonPath("page.number").value(6))
                .andReturn();

        // Sort by key, ascending
        mvc.perform(get(BASE_URL)
                .param("sort", "key,asc")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_embedded.mappings[0].key").value("key1"))
                .andExpect(jsonPath("_embedded.mappings[0].value").value("value1"))
                .andExpect(jsonPath("_embedded.mappings[1].key").value("key10"))
                .andExpect(jsonPath("_embedded.mappings[1].value").value("value10"))
                .andExpect(jsonPath("_embedded.mappings[2].key").value("key11"))
                .andExpect(jsonPath("_embedded.mappings[2].value").value("value11"))
                .andExpect(jsonPath("_embedded.mappings[3].key").value("key12"))
                .andExpect(jsonPath("_embedded.mappings[3].value").value("value12"))
                .andExpect(jsonPath("_embedded.mappings[4].key").value("key13"))
                .andExpect(jsonPath("_embedded.mappings[4].value").value("value13"))
                .andExpect(jsonPath("_embedded.mappings[5].key").value("key14"))
                .andExpect(jsonPath("_embedded.mappings[5].value").value("value14"))
                .andExpect(jsonPath("_embedded.mappings[6].key").value("key15"))
                .andExpect(jsonPath("_embedded.mappings[6].value").value("value15"))
                .andExpect(jsonPath("_embedded.mappings[7].key").value("key16"))
                .andExpect(jsonPath("_embedded.mappings[7].value").value("value16"))
                .andExpect(jsonPath("_embedded.mappings[8].key").value("key17"))
                .andExpect(jsonPath("_embedded.mappings[8].value").value("value17"))
                .andExpect(jsonPath("_embedded.mappings[9].key").value("key18"))
                .andExpect(jsonPath("_embedded.mappings[9].value").value("value18"))
                .andExpect(jsonPath("_embedded.mappings[10].key").value("key19"))
                .andExpect(jsonPath("_embedded.mappings[10].value").value("value19"))
                .andExpect(jsonPath("_embedded.mappings[11].key").value("key2"))
                .andExpect(jsonPath("_embedded.mappings[11].value").value("value2"))
                .andExpect(jsonPath("_embedded.mappings[12].key").value("key20"))
                .andExpect(jsonPath("_embedded.mappings[12].value").value("value20"))
                .andExpect(jsonPath("_embedded.mappings[13].key").value("key21"))
                .andExpect(jsonPath("_embedded.mappings[13].value").value("value21"))
                .andExpect(jsonPath("_embedded.mappings[14].key").value("key22"))
                .andExpect(jsonPath("_embedded.mappings[14].value").value("value22"))
                .andExpect(jsonPath("_embedded.mappings[15].key").value("key23"))
                .andExpect(jsonPath("_embedded.mappings[15].value").value("value23"))
                .andExpect(jsonPath("_embedded.mappings[16].key").value("key24"))
                .andExpect(jsonPath("_embedded.mappings[16].value").value("value24"))
                .andExpect(jsonPath("_embedded.mappings[17].key").value("key25"))
                .andExpect(jsonPath("_embedded.mappings[17].value").value("value25"))
                .andExpect(jsonPath("_embedded.mappings[18].key").value("key26"))
                .andExpect(jsonPath("_embedded.mappings[18].value").value("value26"))
                .andExpect(jsonPath("_embedded.mappings[19].key").value("key27"))
                .andExpect(jsonPath("_embedded.mappings[19].value").value("value27"))
                .andExpect(jsonPath("_embedded.mappings[20]").doesNotExist())
                .andExpect(jsonPath("_embedded.mappings[21]").doesNotExist())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=20&sort=key,asc"))
                .andExpect(jsonPath("_links.prev.href").doesNotExist())
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=1&size=20&sort=key,asc"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=3&size=20&sort=key,asc"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(20))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(4))
                .andExpect(jsonPath("page.number").value(0))
                .andReturn();

        // Sort by value, descending, 2nd page with size of 10
        mvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "key,desc")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mappings").isArray())
                .andExpect(jsonPath("_embedded.mappings[0].key").value("key59"))
                .andExpect(jsonPath("_embedded.mappings[0].value").value("value59"))
                .andExpect(jsonPath("_embedded.mappings[1].key").value("key58"))
                .andExpect(jsonPath("_embedded.mappings[1].value").value("value58"))
                .andExpect(jsonPath("_embedded.mappings[2].key").value("key57"))
                .andExpect(jsonPath("_embedded.mappings[2].value").value("value57"))
                .andExpect(jsonPath("_embedded.mappings[3].key").value("key56"))
                .andExpect(jsonPath("_embedded.mappings[3].value").value("value56"))
                .andExpect(jsonPath("_embedded.mappings[4].key").value("key55"))
                .andExpect(jsonPath("_embedded.mappings[4].value").value("value55"))
                .andExpect(jsonPath("_embedded.mappings[5].key").value("key54"))
                .andExpect(jsonPath("_embedded.mappings[5].value").value("value54"))
                .andExpect(jsonPath("_embedded.mappings[6].key").value("key53"))
                .andExpect(jsonPath("_embedded.mappings[6].value").value("value53"))
                .andExpect(jsonPath("_embedded.mappings[7].key").value("key52"))
                .andExpect(jsonPath("_embedded.mappings[7].value").value("value52"))
                .andExpect(jsonPath("_embedded.mappings[8].key").value("key51"))
                .andExpect(jsonPath("_embedded.mappings[8].value").value("value51"))
                .andExpect(jsonPath("_embedded.mappings[9].key").value("key50"))
                .andExpect(jsonPath("_embedded.mappings[9].value").value("value50"))
                .andExpect(jsonPath("_embedded.mappings[10]").doesNotExist())
                .andExpect(jsonPath("_embedded.mappings[11]").doesNotExist())
                .andExpect(jsonPath("_links.self.href").value(HTTP_HOST + "/mappings"))
                .andExpect(jsonPath("_links.first.href").value(HTTP_HOST + "/mappings?page=0&size=10&sort=key,desc"))
                .andExpect(jsonPath("_links.prev.href").value(HTTP_HOST + "/mappings?page=0&size=10&sort=key,desc"))
                .andExpect(jsonPath("_links.next.href").value(HTTP_HOST + "/mappings?page=2&size=10&sort=key,desc"))
                .andExpect(jsonPath("_links.last.href").value(HTTP_HOST + "/mappings?page=6&size=10&sort=key,desc"))
                .andExpect(jsonPath("_links.profile.href").value(HTTP_HOST + "/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value(HTTP_HOST + "/mappings/search"))
                .andExpect(jsonPath("page.size").value(10))
                .andExpect(jsonPath("page.totalElements").value(65))
                .andExpect(jsonPath("page.totalPages").value(7))
                .andExpect(jsonPath("page.number").value(1))
                .andReturn();
    }

}