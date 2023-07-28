package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "UCSBOrganization")
@RequestMapping("/api/UCSBOrganization")
@RestController
@Slf4j

public class UCSBOrganizationController extends ApiController{
    @Autowired
    UCSBOrganizationRepository uCSBOrganizationRepository;


    @Operation(summary = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allUCSBOrganization() {
        Iterable<UCSBOrganization> UCSBOrganizations = uCSBOrganizationRepository.findAll();
        return UCSBOrganizations;
    }

    @Operation(summary= "Get a UCSB organization by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization ucsborg = uCSBOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return ucsborg;
    }



    @Operation(summary = "Create a new ucsb organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postUCSBOrganization(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
            @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
            @Parameter(name="inactive") @RequestParam boolean inactive
    ) throws JsonProcessingException {

        log.info("Detail: orgCode={}, orgTranslationShort={}, orgTranslation={}, inactive={}", orgCode, orgTranslationShort, orgTranslation, inactive);
        UCSBOrganization UCSBOrg = new UCSBOrganization();
        UCSBOrg.setOrgCode(orgCode);
        UCSBOrg.setOrgTranslationShort(orgTranslationShort);
        UCSBOrg.setOrgTranslation(orgTranslation);
        UCSBOrg.setInactive(inactive);

        UCSBOrganization savedorg = uCSBOrganizationRepository.save(UCSBOrg);
        return savedorg;
    }


    @Operation(summary= "Update an existing ucsb organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateHelpRequest(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {
        log.info("incoming={}", incoming);
        log.info("orgCode={}", orgCode);
        UCSBOrganization ucsborg = uCSBOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        //ucsborg.setOrgCode(incoming.getOrgCode());
        ucsborg.setOrgTranslationShort(incoming.getOrgTranslationShort());
        ucsborg.setOrgTranslation(incoming.getOrgTranslation());
        ucsborg.setInactive(incoming.getInactive());

        uCSBOrganizationRepository.save(ucsborg);

        return ucsborg;
    }



    @Operation(summary= "Delete a ucsb organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteOrg(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization Org = uCSBOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        uCSBOrganizationRepository.delete(Org);
        return genericMessage("record %s deleted".formatted(orgCode));
    }

}
