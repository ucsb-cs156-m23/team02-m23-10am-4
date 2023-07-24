package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;

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
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase{

    @MockBean
    HelpRequestRepository requestRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequest/all"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/helprequest?id=1"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/helprequest/all"))
            .andExpect(status().is(200));
    }
    
    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/helprequest/post"))
            .andExpect(status().is(403));
    }

    @Test
    public void logged_out_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/helprequest"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/helprequest"))
            .andExpect(status().is(403));
    }


    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/helprequest/post"))
            .andExpect(status().is(403));
    }

    @WithMockUser(roles = {"USER"})
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        HelpRequest request1 = HelpRequest.builder()
        .title("test request 1")
        .requester("someuser")
        .requestBody("test request body 1")
        .requestDateTime(ldt1)
        .build();

        when(requestRepository.findById(eq(1L))).thenReturn(Optional.of(request1));

        MvcResult response = mockMvc.perform(get("/api/helprequest?id=1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(requestRepository, times(1)).findById(eq(1L));
        String expectedJSON = mapper.writeValueAsString(request1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_cannot_get_by_id_when_the_id_does_not_exist() throws Exception {
        when(requestRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(get("/api/helprequest?id=1"))
            .andExpect(status().isNotFound())
            .andReturn();

        verify(requestRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("HelpRequest with id 1 not found", json.get("message"));
    }


    @WithMockUser(roles = {"USER"})
    @Test
    public void logged_in_user_can_get_all_request() throws Exception {
       LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

       HelpRequest request1 = HelpRequest.builder()
       .title("test request 1")
       .requester("usera")
       .requestBody("test body 1")
       .requestDateTime(ldt1)
       .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        HelpRequest request2 = HelpRequest.builder()
        .title("test request 2")
        .requester("userb")
        .requestBody("test body 2")
        .requestDateTime(ldt2)
        .build();

        ArrayList<HelpRequest> expectedHelpRequest = new ArrayList<>();
        expectedHelpRequest.addAll(Arrays.asList(request1, request2));

        when(requestRepository.findAll()).thenReturn(expectedHelpRequest);

        MvcResult response = mockMvc.perform(get("/api/helprequest/all"))
            .andExpect(status().isOk())
            .andReturn();

        verify(requestRepository, times(1)).findAll();
        String expectedJSON = mapper.writeValueAsString(expectedHelpRequest);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_request() throws Exception {
        
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        HelpRequest request1 = HelpRequest.builder()
        .title("testrequest1")
        .requester("someuser")
        .requestBody("testbody1")
        .requestDateTime(ldt1)
        .build();

        when(requestRepository.save(any(HelpRequest.class))).thenReturn(request1);

        MvcResult response = mockMvc.perform(post("/api/helprequest/post?title=testrequest1&requester=someuser&requestBody=testbody1&requestDateTime=2022-01-03T00:00:00").with(csrf())).andExpect(status().isOk()).andReturn();

        verify(requestRepository, times(1)).save(request1);
        String expectedJSON = mapper.writeValueAsString(request1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_request() throws Exception {
    
        //arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        HelpRequest request1 = HelpRequest.builder()
        .title("test request 1")
        .requester("someuser")
        .requestBody("test body 1")
        .requestDateTime(ldt1)
        .build();

        when(requestRepository.findById(eq(1L))).thenReturn(Optional.of(request1));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/helprequest?id=1")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(requestRepository, times(1)).findById(1L);
        verify(requestRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("HelpRequest with id 1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_request_and_gets_right_error_message()
                    throws Exception {
        // arrange

        when(requestRepository.findById(eq(123L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/helprequest?id=123")
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(requestRepository, times(1)).findById(123L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("HelpRequest with id 123 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_request() throws Exception{
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

        HelpRequest requestOri = HelpRequest.builder()
        .title("test request 1")
        .requester("usera")
        .requestBody("test body 1")
        .requestDateTime(ldt1)
        .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        HelpRequest requestEdited = HelpRequest.builder()
        .title("test request 2")
        .requester("userb")
        .requestBody("test body 2")
        .requestDateTime(ldt2)
        .build();

        String requestBody = mapper.writeValueAsString(requestEdited);

        when(requestRepository.findById(eq(1L))).thenReturn(Optional.of(requestOri));
        
        MvcResult response = mockMvc.perform(
            put("/api/helprequest?id=1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();
        
        verify(requestRepository, times(1)).findById(eq(1L));
        verify(requestRepository, times(1)).save(requestEdited);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_request_that_does_not_exist() throws Exception {
        LocalDateTime ldt2 = LocalDateTime.parse("2022-01-04T00:00:00");

        HelpRequest requestEdited = HelpRequest.builder()
        .title("test request 2")
        .requester("userb")
        .requestBody("test body 2")
        .requestDateTime(ldt2)
        .build();

        String requestBody = mapper.writeValueAsString(requestEdited);

        when(requestRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
            put("/api/helprequest?id=1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(requestRepository, times(1)).findById(eq(1L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("HelpRequest with id 1 not found", json.get("message"));
    }
}