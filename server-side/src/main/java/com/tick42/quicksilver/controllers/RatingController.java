package com.tick42.quicksilver.controllers;

import com.tick42.quicksilver.exceptions.ExtensionUnavailableException;
import com.tick42.quicksilver.exceptions.UnauthorizedExtensionModificationException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/rating")
public class RatingController {
    private final ExtensionService extensionService;
    private final RatingService ratingService;

    @Autowired
    public RatingController(ExtensionService extensionService, RatingService ratingService) {
        this.extensionService = extensionService;
        this.ratingService = ratingService;
    }

    @PatchMapping(value = "/auth/rate/{id}/{rating}")
    public double rating(@PathVariable("id") int id, @PathVariable("rating") int rating, HttpServletRequest request) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        Extension extension = ratingService.rate(extensionService.findById(id, loggedUser), rating, userId);
        extensionService.reloadExtension(extension);
        return extension.getRating();
    }

    @GetMapping(value = "/auth/userRating/{id}")
    public int userRatingForExtension(@PathVariable("id") int id, HttpServletRequest request) {
        UserDetails loggedUser = (UserDetails)SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        long userId = loggedUser.getId();

        return ratingService.userRatingForExtension(id, userId);
    }

    @ExceptionHandler
    ResponseEntity handleUnauthorizedExtensionModificationException(UnauthorizedExtensionModificationException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleExtensionUnavailable(ExtensionUnavailableException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity handleExtensionNotFoundException(EntityNotFoundException e) {
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
