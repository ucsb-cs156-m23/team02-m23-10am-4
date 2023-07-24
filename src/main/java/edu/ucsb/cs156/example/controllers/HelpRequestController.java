package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;

import javax.validation.Valid;

@Tag(name = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController{
    
    @Autowired
    HelpRequestRepository requestRepository;

    @Operation(summary= "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequest() {
        Iterable<HelpRequest> helpRequest = requestRepository.findAll();
        return helpRequest;
    }

    @Operation(summary= "Get a single help request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public HelpRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        HelpRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        return request;
    }


    @Operation(summary= "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
        @Parameter(name="title") @RequestParam String title,
        @Parameter(name="requester") @RequestParam String requester,
        @Parameter(name="requestBody") @RequestParam String requestBody,

        @Parameter(name="requestDateTime") @RequestParam("requestDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime requestDateTime) 
        
        throws JsonProcessingException{
            log.info("requestDateTime={} ", requestDateTime);
            
            HelpRequest request = new HelpRequest();
            request.setTitle(title);
            request.setRequester(requester);
            request.setRequestBody(requestBody);
            request.setRequestDateTime(requestDateTime);

            HelpRequest savedArticle = requestRepository.save(request);

            return savedArticle;
        }

    @Operation(summary= "Delete a help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCommons(
            @Parameter(name="id") @RequestParam Long id) {
        HelpRequest commons = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

        requestRepository.delete(commons);
        return genericMessage("HelpRequest with id %d deleted".formatted(id));
    }

    @Operation(summary= "Update an existing help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public HelpRequest updateHelpRequest(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid HelpRequest incoming) {
        log.info("incoming={}", incoming);
        log.info("id={}", id);  
        HelpRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
        
        request.setTitle(incoming.getTitle());
        request.setRequester(incoming.getRequester());
        request.setRequestBody(incoming.getRequestBody());
        request.setRequestDateTime(incoming.getRequestDateTime());

        requestRepository.save(request);

        return request;
    }
    
}
