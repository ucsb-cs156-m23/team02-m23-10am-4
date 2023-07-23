package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.lang.reflect.Array;
import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase{

    @MockBean
    ArticlesRepository articlesRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/articles?id=1"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().is(200));
    }
    
    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        Articles article1 = Articles.builder()
        .title("test article 1")
        .url("url1.test")
        .explanation("test explanation 1")
        .email("tesmail1@ucsb.edu")
        .dateAdded(ldt1)
        .build();

        when(articlesRepository.findById(eq(1L))).thenReturn(Optional.of(article1));

        MvcResult response = mockMvc.perform(get("/api/articles?id=1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(articlesRepository, times(1)).findById(eq(1L));
        String expectedJSON = mapper.writeValueAsString(article1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        when(articlesRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(get("/api/articles?id=1"))
            .andExpect(status().isNotFound())
            .andReturn();

        verify(articlesRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Articles with id 1 not found", json.get("message"));
    }


    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {
       LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

       Articles article1 = Articles.builder()
       .title("test article 1")
       .url("url1.test")
       .explanation("test explanation 1")
       .email("tesmail1@ucsb.edu")
       .dateAdded(ldt1)
       .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        Articles article2 = Articles.builder()
         .title("test article 2")
         .url("url2.test")
         .explanation("test explanation 2")
         .email("tesmail2@ucsb.edu")
         .dateAdded(ldt2)
         .build();

        ArrayList<Articles> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));

        when(articlesRepository.findAll()).thenReturn(expectedArticles);

        MvcResult response = mockMvc.perform(get("/api/articles/all"))
            .andExpect(status().isOk())
            .andReturn();

        verify(articlesRepository, times(1)).findAll();
        String expectedJSON = mapper.writeValueAsString(expectedArticles);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_article() throws Exception {
        
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        Articles article1 = Articles.builder()
        .title("testarticle1")
        .url("url1.test")
        .explanation("testexplanation1")
        .email("tesmail1@ucsb.edu")
        .dateAdded(ldt1)
        .build();

        when(articlesRepository.save(any(Articles.class))).thenReturn(article1);

        MvcResult response = mockMvc.perform(post("/api/articles/post?title=testarticle1&url=url1.test&explanation=testexplanation1&email=tesmail1@ucsb.edu&date=2022-01-03T00:00:00").with(csrf())).andExpect(status().isOk()).andReturn();

        verify(articlesRepository, times(1)).save(article1);
        String expectedJSON = mapper.writeValueAsString(article1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }
}