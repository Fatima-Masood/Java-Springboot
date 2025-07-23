package com.redmath.training.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class NewsAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testNewsGetSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/123"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.newsId", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("title 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details", Matchers.is("details 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reportedBy", Matchers.is("reporter 123")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reportedAt", Matchers.notNullValue()));
    }

    @Test
    public void testNewsGetNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/news/1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testNewsUpdateSuccess() throws Exception {
        News updatedNews = new News();
        updatedNews.setTitle("newtitle 123");

        ObjectMapper objectMapper = new ObjectMapper();
        String updatedNewsJson = objectMapper.writeValueAsString(updatedNews);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedNewsJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.newsId", Matchers.is(123)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("newtitle 123")));
    }

    @Test
    void testNewsUpdateGetNotFound() throws Exception {
        News updatedNews = new News();
        updatedNews.setTitle("newtitle 123");

        ObjectMapper objectMapper = new ObjectMapper();
        String updatedNewsJson = objectMapper.writeValueAsString(updatedNews);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/news/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedNewsJson))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}