package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.time.LocalDateTime;


@Tag(name = "RecommendationRequest")
@RequestMapping("/api/recommendationrequest")
@RestController
public class RecommendationRequestController extends ApiController{
    
    // Create the Repository Instance
    @Autowired
    RecommendationRequestRepository recommendationRequestRepository;

    // GET /api/recommendationrequest/all
    @Operation(summary= "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecommendationRequest> allRecommendationRequests() {
        Iterable<RecommendationRequest> recommendationRequests = recommendationRequestRepository.findAll();
        return recommendationRequests;
    }

    // GET /api/recommendationrequest?id
    @Operation(summary= "Get a single recommendation request")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecommendationRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecommendationRequest recommendationRequest = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

        return recommendationRequest;
    }

    // POST /api/recommendationrequest/post
    @Operation(summary= "Create a new recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecommendationRequest createRecommendationRequest(
            @Parameter(name= "requestorEmail") @RequestParam String requestorEmail,
            @Parameter(name= "professorEmail") @RequestParam String professorEmail,
            @Parameter(name= "explanation") @RequestParam String explanation,
            @Parameter(name= "dateRequested", description = "in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateRequested") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateRequested,
            @Parameter(name= "dateNeeded", description = "in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateNeeded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateNeeded,
            @Parameter(name= "done") @RequestParam boolean done) 
            throws JsonProcessingException {
        
        // Create a new RecommendationRequest object
        RecommendationRequest request = RecommendationRequest.builder()
                                        .requesterEmail(requestorEmail)
                                        .professorEmail(professorEmail)
                                        .explanation(explanation)
                                        .dateRequested(dateRequested)
                                        .dateNeeded(dateNeeded)
                                        .done(done)
                                        .build();
        
        RecommendationRequest savedRequest = recommendationRequestRepository.save(request);

        return savedRequest;    
    }


    // PUT /api/recommendationrequest/put
    @Operation(summary= "Update an existing recommendation request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public RecommendationRequest updateRecommendationRequest(
            @Parameter(name= "id") @RequestParam Long id,
            @RequestBody @Valid RecommendationRequest nReq) {
        
        
        // Get the existing recommendation request
        RecommendationRequest request = recommendationRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));


        // Update the fields
        request.setRequesterEmail(nReq.getRequesterEmail());
        request.setProfessorEmail(nReq.getProfessorEmail());
        request.setExplanation(nReq.getExplanation());
        request.setDateRequested(nReq.getDateRequested());
        request.setDateNeeded(nReq.getDateNeeded());
        request.setDone(nReq.getDone());

        // Save the updated recommendation request
        recommendationRequestRepository.save(request);

        return request;    
    }



}
