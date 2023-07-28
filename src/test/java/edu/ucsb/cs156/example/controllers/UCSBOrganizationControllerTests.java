package edu.ucsb.cs156.example.controllers;

// import edu.ucsb.cs156.example.entities.UCSBDate;
// import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
// import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
// import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

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

// import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)

public class UCSBOrganizationControllerTests extends ControllerTestCase{

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;


    // Authorization tests for /api/UCSBOrganization/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().is(200));
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/UCSBOrganization?orgCode=1"))
                .andExpect(status().is(403));
    }

    // Authorization tests for /api/UCSBOrganization/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBOrganization/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/UCSBOrganization/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    @Test
    public void logged_out_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBOrganization"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_put() throws Exception {
        mockMvc.perform(put("/api/UCSBOrganization"))
                .andExpect(status().is(403)); // only admins can post
    }

    @Test
    public void logged_out_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBOrganization?orgCode=255"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_delete() throws Exception {
        mockMvc.perform(delete("/api/UCSBOrganization?orgCode=255"))
                .andExpect(status().is(403)); // only admins can del
    }


    // // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        UCSBOrganization obj = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("A")
                .orgTranslation("AA")
                .inactive(true)
                .build();

        when(ucsbOrganizationRepository.findById(eq("1"))).thenReturn(Optional.of(obj));

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=1"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq("1"));
        String expectedJson = mapper.writeValueAsString(obj);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }






    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange
        when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization?orgCode=munger-hall"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq("munger-hall"));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
    }



    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

        // arrange

        UCSBOrganization obj1 = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("A")
                .orgTranslation("AA")
                .inactive(true)
                .build();

        UCSBOrganization obj2 = UCSBOrganization.builder()
                .orgCode("2")
                .orgTranslationShort("B")
                .orgTranslation("BB")
                .inactive(false)
                .build();


        ArrayList<UCSBOrganization> expectedOrg = new ArrayList<>();
        expectedOrg.addAll(Arrays.asList(obj1, obj2));

        when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrg);

        // act
        MvcResult response = mockMvc.perform(get("/api/UCSBOrganization/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrg);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_org() throws Exception {
        // arrange

        UCSBOrganization obj3 = UCSBOrganization.builder()
                .orgCode("3")
                .orgTranslationShort("C")
                .orgTranslation("CC")
                .inactive(true)
                .build();


        when(ucsbOrganizationRepository.save(eq(obj3))).thenReturn(obj3);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/UCSBOrganization/post?orgCode=3&orgTranslationShort=C&orgTranslation=CC&inactive=true")
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(obj3);
        String expectedJson = mapper.writeValueAsString(obj3);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }




    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_org() throws Exception {
        // arrange

        UCSBOrganization obj1 = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("A")
                .orgTranslation("AA")
                .inactive(true)
                .build();

        when(ucsbOrganizationRepository.findById(eq("1"))).thenReturn(Optional.of(obj1));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/UCSBOrganization?orgCode=1")
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("1");
        verify(ucsbOrganizationRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("record 1 deleted", json.get("message"));
    }



    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_orgs_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(ucsbOrganizationRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/UCSBOrganization?orgCode=munger-hall")
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("munger-hall");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id munger-hall not found", json.get("message"));
    }



    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_org() throws Exception {
        // arrange

        UCSBOrganization obj1 = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("A")
                .orgTranslation("AA")
                .inactive(true)
                .build();

        UCSBOrganization obj2 = UCSBOrganization.builder()
                .orgCode("1")
                .orgTranslationShort("B")
                .orgTranslation("BB")
                .inactive(false)
                .build();

        String requestBody = mapper.writeValueAsString(obj2);

        when(ucsbOrganizationRepository.findById(eq("1"))).thenReturn(Optional.of(obj1));

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/UCSBOrganization?orgCode=1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("1");
        verify(ucsbOrganizationRepository, times(1)).save(obj2); // should be saved with updated info
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }




    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_orgs_that_does_not_exist() throws Exception {
        // arrange

        UCSBOrganization obj = UCSBOrganization.builder()
                .orgCode("5")
                .orgTranslationShort("S")
                .orgTranslation("SS")
                .inactive(false)
                .build();


        String requestBody = mapper.writeValueAsString(obj);

        when(ucsbOrganizationRepository.findById(eq("5"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        put("/api/UCSBOrganization?orgCode=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8")
                                .content(requestBody)
                                .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById("5");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id 5 not found", json.get("message"));

    }

}
