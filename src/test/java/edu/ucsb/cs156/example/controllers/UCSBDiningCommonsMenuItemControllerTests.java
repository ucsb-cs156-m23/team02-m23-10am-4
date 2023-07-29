package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.entities.MenuItemReview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

    @MockBean
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @MockBean
    UserRepository UserRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/?id=1"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().is(200));
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBDiningCommonsMenuItem"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBDiningCommonsMenuItem"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBDiningCommonsMenuItem"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBDiningCommonsMenuItem"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("This is a test comment")
            .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(menuItemReview1));

        MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/?id=1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        String expectedJSON = mapper.writeValueAsString(menuItemReview1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }


    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/?id=1"))
            .andExpect(status().isNotFound())
            .andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("MenuItemReview with id 1 not found", json.get("message"));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("This is a test comment 1")
            .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-06T00:00:00");

        MenuItemReview menuItemReview2 = MenuItemReview.builder()
            .itemId(2L)
            .reviewerEmail("test2@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("This is a test comment 2")
            .build();
        
        ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
        expectedReviews.addAll(Arrays.asList(menuItemReview1, menuItemReview2));

        when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedReviews);

        MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
        String expectedJSON = mapper.writeValueAsString(expectedReviews);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_review() throws Exception {
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("Thisisatestcomment1")
            .build();
        
        when(ucsbDiningCommonsMenuItemRepository.save(any(MenuItemReview.class))).thenReturn(menuItemReview1);

        MvcResult response = mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post?itemId=1&reviewerEmail=test1@ucsb.edu&stars=5&dateReviewed=2022-01-03T00:00:00&comments=Thisisatestcomment1").with(csrf())).andExpect(status().isOk()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItemReview1);
        String expectedJSON = mapper.writeValueAsString(menuItemReview1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_review() throws Exception{
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReviewOri = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("Thisisatestcomment1")
            .build();
        
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-06T00:00:00");

        MenuItemReview menuItemReviewEdited = MenuItemReview.builder()
            .itemId(2L)
            .reviewerEmail("test2@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt2)
            .comments("This is a test comment 2")
            .build();

        String requestBody = mapper.writeValueAsString(menuItemReviewEdited);

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(menuItemReviewOri));

        MvcResult response = mockMvc.perform(
            put("/api/UCSBDiningCommonsMenuItem?id=1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();
        
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(menuItemReviewEdited);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_review_that_does_not_exist() throws Exception {
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReviewEdited = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("Thisisatestcomment1")
            .build();

        String requestBody = mapper.writeValueAsString(menuItemReviewEdited);

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
            put("/api/UCSBDiningCommonsMenuItem?id=1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReview with id 1 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_review() throws Exception{
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        MenuItemReview menuItemReview1 = MenuItemReview.builder()
            .itemId(1L)
            .reviewerEmail("test1@ucsb.edu")
            .stars(5)
            .dateReviewed(ldt1)
            .comments("Thisisatestcomment1")
            .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(menuItemReview1));

        MvcResult response = mockMvc.perform(
            delete("/api/UCSBDiningCommonsMenuItem?id=1")
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReview with id 1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_review_and_gets_right_error_message() throws Exception{

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
            delete("/api/UCSBDiningCommonsMenuItem?id=1")
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("MenuItemReview with id 1 not found", json.get("message"));
    }
}
