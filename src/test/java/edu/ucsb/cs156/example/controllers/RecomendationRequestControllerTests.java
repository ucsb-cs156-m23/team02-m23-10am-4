package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import lombok.With;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

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

@WebMvcTest(RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecomendationRequestControllerTests extends ControllerTestCase{
    
    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest/all"))
                        .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest/all"))
                        .andExpect(status().is(200)); // logged in users can get all
    }

    // Tests for POST

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequest/post"))
                        .andExpect(status().is(403)); // logged out users can't post
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequest/post"))
                        .andExpect(status().is(403)); // logged in users can't post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsbdate() throws Exception {
        
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-02-03T00:00:00");
        
        RecommendationRequest req = RecommendationRequest.builder()
            .requesterEmail("testr@ucsb.edu")
            .professorEmail("testp@ucsb.edu")
            .explanation("testexplanation")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

        when(recommendationRequestRepository.save(eq(req))).thenReturn(req);

        MvcResult response = mockMvc.perform(post("/api/recommendationrequest/post?requestorEmail=testr@ucsb.edu&professorEmail=testp@ucsb.edu&explanation=testexplanation&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-02-03T00:00:00&done=false").with(csrf()))
                        .andExpect(status().isOk())
                        .andReturn();

        verify(recommendationRequestRepository, times(1)).save(req);
        String expectedJson = mapper.writeValueAsString(req);
        String responseString = response.getResponse().getContentAsString();

        assertEquals(expectedJson, responseString);

    }


}
