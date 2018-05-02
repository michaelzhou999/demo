package com.sas.example.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RestInterfaceTests {

    private static final String BASE_URL = "/mappings";

    private MockMvc mvc;

    @Autowired
    WebApplicationContext webContext;

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
                .andExpect(jsonPath("_links.self.href").value("http://localhost/mappings{?page,size,sort}"))
                .andExpect(jsonPath("_links.profile.href").value("http://localhost/profile/mappings"))
                .andExpect(jsonPath("_links.search.href").value("http://localhost/mappings/search"))
                .andExpect(jsonPath("page.totalElements").value(0))
                .andExpect(jsonPath("page.totalPages").value(0))
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