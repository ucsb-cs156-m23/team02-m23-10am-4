package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
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
import java.util.ResourceBundle.Control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase { 
    
        @MockBean
        MenuItemReviewRepository menuItemReviewRepository;
    
        @MockBean
        UserRepository userRepository;

        @Test
        public void logged_out_users_cannot_get_all_menu_item_reviews() throws Exception {
            mockMvc.perform(get("/api/MenuItemReview/all"))
                .andExpect(status().isForbidden());
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_users_can_get_all_menu_item_reviews() throws Exception {
            mockMvc.perform(get("/api/MenuItemReview/all"))
                .andExpect(status().isOk());
        }

        @Test
        public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/MenuItemReview/post"))
                .andExpect(status().isForbidden());
        }

        @WithMockUser(roles = {"USER"})
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/MenuItemReview/post"))
                .andExpect(status().isForbidden());
        }

        @WithMockUser(roles = {"ADMIN", "USER"})
        @Test
        public void an_admin_user_can_post_a_new_menu_item_review() throws Exception {
            // arrange
            MenuItemReview menuItemReview = MenuItemReview.builder()
                .itemId(1L)
                .reviewerEmail("test@ucsb.edu")
                .stars(5)
                .dateReviewed(LocalDateTime.of(2021, 5, 1, 12, 0, 0))
                .comments("This is a test")
                .build();
            
            when(menuItemReviewRepository.save(eq(menuItemReview))).thenReturn(menuItemReview);
        
            // act
            MvcResult response = mockMvc.perform(
                post("/api/MenuItemReview/post?itemId=1&reviewerEmail=test@ucsb.edu&stars=5&dateReviewed=2021-05-01T12:00:00&comments=This is a test")
                    .with(csrf()))
                .andExpect(status().isOk()).andReturn();

            // assert
            verify(menuItemReviewRepository, times(1)).save(menuItemReview);
            String expectedJson = mapper.writeValueAsString(menuItemReview);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

}