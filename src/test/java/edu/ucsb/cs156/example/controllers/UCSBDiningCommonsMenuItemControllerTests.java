package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;

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

        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(uCSBDiningCommonsMenuItem1));

        MvcResult response = mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/?id=1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        String expectedJSON = mapper.writeValueAsString(uCSBDiningCommonsMenuItem1);
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
        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();

        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem2 = UCSBDiningCommonsMenuItem.builder()
        .dinningCommonsCode("Ortega")
        .name("Lasagna")
        .station("LasagnaStation")
        .build();
        
        ArrayList<UCSBDiningCommonsMenuItem> expectedReviews = new ArrayList<>();
        expectedReviews.addAll(Arrays.asList(uCSBDiningCommonsMenuItem1, uCSBDiningCommonsMenuItem2));

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
    public void an_admin_user_can_post_a_new_item() throws Exception {
        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();
        
        when(ucsbDiningCommonsMenuItemRepository.save(any(UCSBDiningCommonsMenuItem.class))).thenReturn(UCSBDiningCommonsMenuItem1);

        MvcResult response = mockMvc.perform(post("/api/UCSBDiningCommonsMenuItem/post?dinningCommonsCode=Carrillo&name=Pizza&station=PizzaStation").with(csrf())).andExpect(status().isOk()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(uCSBDiningCommonsMenuItem1);
        String expectedJSON = mapper.writeValueAsString(uCSBDiningCommonsMenuItem1);
        String responseJSON = response.getResponse().getContentAsString();
        assertEquals(expectedJSON, responseJSON);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_item() throws Exception{
        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();

        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem2 = UCSBDiningCommonsMenuItem.builder()
        .dinningCommonsCode("Ortega")
        .name("Lasagna")
        .station("LasagnaStation")
        .build();

        String requestBody = mapper.writeValueAsString(uCSBDiningCommonsMenuItem2);

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(uCSBDiningCommonsMenuItem1));

        MvcResult response = mockMvc.perform(
            put("/api/UCSBDiningCommonsMenuItem?id=1")
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("utf-8")
            .content(requestBody)
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();
        
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(uCSBDiningCommonsMenuItem2);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_item_that_does_not_exist() throws Exception {
        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();

        String requestBody = mapper.writeValueAsString(uCSBDiningCommonsMenuItem1);

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
        assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_item() throws Exception{
        UCSBDiningCommonsMenuItem uCSBDiningCommonsMenuItem1 = UCSBDiningCommonsMenuItem.builder()
            .dinningCommonsCode("Carrillo")
            .name("Pizza")
            .station("PizzaStation")
            .build();

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.of(uCSBDiningCommonsMenuItem1));

        MvcResult response = mockMvc.perform(
            delete("/api/UCSBDiningCommonsMenuItem?id=1")
            .with(csrf()))
            .andExpect(status().isOk()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(1L));
        verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBDiningCommonsMenuItem with id 1 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_item_and_gets_right_error_message() throws Exception{

        when(ucsbDiningCommonsMenuItemRepository.findById(eq(1L))).thenReturn(Optional.empty());

        MvcResult response = mockMvc.perform(
            delete("/api/UCSBDiningCommonsMenuItem?id=1")
            .with(csrf()))
            .andExpect(status().isNotFound()).andReturn();

        verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(1L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBDiningCommonsMenuItem with id 1 not found", json.get("message"));
    }
}
