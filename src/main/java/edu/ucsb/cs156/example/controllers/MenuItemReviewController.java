package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    @Operation(summary = "List all menu item reviews")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allMenuItemReviews() {
        Iterable<MenuItemReview> menuItemReviews = menuItemReviewRepository.findAll();
        return menuItemReviews;
    }

    @Operation(summary = "Create a new MenuItemReview")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postItemReview(
        @Parameter(name="itemId") @RequestParam Long itemId,
        @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
        @Parameter(name="stars") @RequestParam int stars,
        @Parameter(name="dateReviewed") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateReviewed,
        @Parameter(name="comments") @RequestParam String comments
    ) throws JsonProcessingException {

        log.info("postItemReview: itemId={}, reviewerEmail={}, stars={}, dateReviewed={}, comments={}", itemId, reviewerEmail, stars, dateReviewed, comments);
        MenuItemReview menuItemReview = MenuItemReview.builder()
            .itemId(itemId)
            .reviewerEmail(reviewerEmail)
            .stars(stars)
            .dateReviewed(dateReviewed)
            .comments(comments)
            .build();
        
        return menuItemReviewRepository.save(menuItemReview);
    }

    @Operation(summary = "Get a single menu item review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public MenuItemReview getById(
        @Parameter(name="id") @RequestParam Long id
    ) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        return menuItemReview;
    }

    @Operation(summary = "Update a single menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public MenuItemReview updatMenuItemReview(
        @Parameter(name="id") @RequestParam Long id,
        @RequestBody @Valid MenuItemReview menuItemReview
    ) {
        MenuItemReview existingMenuItemReview = menuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        existingMenuItemReview.setItemId(menuItemReview.getItemId());
        existingMenuItemReview.setReviewerEmail(menuItemReview.getReviewerEmail());
        existingMenuItemReview.setStars(menuItemReview.getStars());
        existingMenuItemReview.setDateReviewed(menuItemReview.getDateReviewed());
        existingMenuItemReview.setComments(menuItemReview.getComments());

        menuItemReviewRepository.save(existingMenuItemReview);

        return existingMenuItemReview;
    }

    @Operation(summary = "Delete a menu item review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMenuItemReview(
        @Parameter(name="id") @RequestParam Long id
    ) {
        MenuItemReview menuItemReview = menuItemReviewRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

        menuItemReviewRepository.delete(menuItemReview);
        return genericMessage("MenuItemReview with id %s deleted".formatted(id));
    }
}