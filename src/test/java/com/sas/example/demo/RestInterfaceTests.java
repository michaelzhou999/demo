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
    public void searchNotFoundTest() throws Exception {
        final ResultActions result = mvc.perform(get(BASE_URL + "/mappings/100").accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void deleteNotFoundTest() throws Exception {
        final ResultActions result = mvc.perform(delete(BASE_URL + "/mappings/100").accept(MediaType.APPLICATION_JSON));
        result.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

}