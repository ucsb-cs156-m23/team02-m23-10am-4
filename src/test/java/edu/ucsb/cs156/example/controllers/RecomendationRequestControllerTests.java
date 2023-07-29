package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import lombok.With;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
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

    @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recomendationrequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-02-03T00:00:00");

                RecommendationRequest req1 = RecommendationRequest.builder()
                                .requesterEmail("test1@")
                                .professorEmail("test2@")
                                .explanation("testexplanation")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();
                RecommendationRequest req2 = RecommendationRequest.builder()
                                .requesterEmail("test3@")
                                .professorEmail("test4@")
                                .explanation("testexplanation2")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt2)
                                .done(false)
                                .build();



                ArrayList<RecommendationRequest> reqs = new ArrayList<>();
                reqs.addAll(Arrays.asList(req1, req2));

                when(recommendationRequestRepository.findAll()).thenReturn(reqs);

                // act
                MvcResult response = mockMvc.perform(get("/api/recommendationrequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(recommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(reqs);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest?id=1"))
                        .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

            // arrange
            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
            LocalDateTime ldt2 = LocalDateTime.parse("2022-02-03T00:00:00");

            RecommendationRequest req = RecommendationRequest.builder()
                                        .requesterEmail("test1@")
                                        .professorEmail("test2@")
                                        .explanation("testexplanation")
                                        .dateRequested(ldt1)
                                        .dateNeeded(ldt2)
                                        .done(false)
                                        .build();

                                        
            when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.of(req));

            // act
            MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=123"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(recommendationRequestRepository, times(1)).findById(eq(123L));
            String expectedJson = mapper.writeValueAsString(req);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
        }

    
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
        
        when(recommendationRequestRepository.findById(eq(123L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=123"))
                                .andExpect(status().isNotFound()).andReturn();

        verify(recommendationRequestRepository, times(1)).findById(eq(123L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequest with id 123 not found", json.get("message"));
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
    public void an_admin_user_can_post_a_new_recomendationrequest() throws Exception {
        
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

    // Tests for PUT

    @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbdate() throws Exception {

            LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
            LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

            RecommendationRequest Oreq = RecommendationRequest.builder()
                            .requesterEmail("test1@")
                            .professorEmail("test2@")
                            .explanation("testexplanation")
                            .dateRequested(ldt1)
                            .dateNeeded(ldt2)
                            .done(false)
                            .build();
            RecommendationRequest Ereq = RecommendationRequest.builder()
                            .requesterEmail("Etest1@")
                            .professorEmail("Etest2@")
                            .explanation("Etestexplanation")
                            .dateRequested(ldt2)
                            .dateNeeded(ldt1)
                            .done(true)
                            .build();

            String requestBody = mapper.writeValueAsString(Ereq);

            when(recommendationRequestRepository.findById(eq(57L))).thenReturn(Optional.of(Oreq));

            
            MvcResult response = mockMvc.perform(
                            put("/api/recommendationrequest?id=57")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .characterEncoding("utf-8")
                                            .content(requestBody)
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

        
            verify(recommendationRequestRepository, times(1)).findById(57L);
            verify(recommendationRequestRepository, times(1)).save(Ereq); // should be saved with correct user
            String responseString = response.getResponse().getContentAsString();
            assertEquals(requestBody, responseString);

        }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsbdate_that_does_not_exist() throws Exception {
        
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        RecommendationRequest Ereq = RecommendationRequest.builder()
                        .requesterEmail("test1@")
                        .professorEmail("test2@")
                        .explanation("testexplanation")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt1)
                        .done(false)
                        .build();

    

        String requestBody = mapper.writeValueAsString(Ereq);

        when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
                        put("/api/recommendationrequest?id=67")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .characterEncoding("utf-8")
                                        .content(requestBody)
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
    }


    // Tests for DELETE

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_date() throws Exception {

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2022-02-03T00:00:00");

        RecommendationRequest req = RecommendationRequest.builder()
                        .requesterEmail("test1@")
                        .professorEmail("test2@")
                        .explanation("testexplanation")
                        .dateRequested(ldt1)
                        .dateNeeded(ldt2)
                        .done(false)
                        .build();

        when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(req));

            
        MvcResult response = mockMvc.perform(delete("/api/recommendationrequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

        verify(recommendationRequestRepository, times(1)).findById(15L);
        verify(recommendationRequestRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));

    }

    @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbdate_and_gets_right_error_message() throws Exception {

            when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

            
            MvcResult response = mockMvc.perform(
                            delete("/api/recommendationrequest?id=15")
                                            .with(csrf()))
                            .andExpect(status().isNotFound()).andReturn();

            
            verify(recommendationRequestRepository, times(1)).findById(15L);
            Map<String, Object> json = responseToJson(response);
            assertEquals("RecommendationRequest with id 15 not found", json.get("message"));




        }

}
