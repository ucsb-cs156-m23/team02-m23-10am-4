package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;

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

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController{

    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    @Operation(summary= "List all UCSB Dining Commons Menu Items Reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allUCSBDiningCommonsMenuItems() {
        Iterable<MenuItemReview> menuItemReview = ucsbDiningCommonsMenuItemRepository.findAll();
        return menuItemReview;
    }

    @Operation(summary= "Get a single UCSB Dining Commons Menu Item Review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
            @Parameter(name="id") @RequestParam Long id) {
        MenuItemReview menuItemReview = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    @Operation(summary= "Create a new UCSB Dining Commons Menu Item Review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMenuItemReview(
        @Parameter(name="itemId") @RequestParam long itemId,
        @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
        @Parameter(name="stars") @RequestParam int stars,
        @Parameter(name="dateReviewed") @RequestParam("dateReviewed") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
        @Parameter(name="comments") @RequestParam String comments) 
        throws JsonProcessingException {

            MenuItemReview menuItemReview = new MenuItemReview();

            menuItemReview.setItemId(itemId);
            menuItemReview.setReviewerEmail(reviewerEmail);
            menuItemReview.setStars(stars);
            menuItemReview.setDateReviewed(dateReviewed);
            menuItemReview.setComments(comments);

            MenuItemReview savedItem = ucsbDiningCommonsMenuItemRepository.save(menuItemReview);

            return savedItem;
        }
    
    @Operation(summary= "Update a UCSB Dining Commons Menu Item Review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updateMenuItemReview(
        @Parameter(name="id") @RequestParam Long id,
        @RequestBody @Valid MenuItemReview incoming){
            MenuItemReview menuItemReview = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

            menuItemReview.setItemId(incoming.getItemId());
            menuItemReview.setReviewerEmail(incoming.getReviewerEmail());
            menuItemReview.setStars(incoming.getStars());
            menuItemReview.setDateReviewed(incoming.getDateReviewed());
            menuItemReview.setComments(incoming.getComments());

            ucsbDiningCommonsMenuItemRepository.save(menuItemReview);
            

            return menuItemReview;
        }

    @Operation(summary= "Delete a UCSB Dining Commons Menu Item Review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMenuItemReview(
        @Parameter(name="id") @RequestParam Long id){
        
        MenuItemReview menuItemReview = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        ucsbDiningCommonsMenuItemRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
        }

}

