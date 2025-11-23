package com.backend.favorite.controllers;

import com.backend.favorite.dtos.FavoriteDTO;
import com.backend.favorite.models.Favorite;
import com.backend.favorite.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Autowired
    FavoriteService favoriteService;

    @GetMapping("/favorites/{categoryId}")
    public ResponseEntity<List<Favorite>> getAllFavoritesByCategoryId(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(favoriteService.getAllFavoritesByCategoryId(categoryId));
    }

    @DeleteMapping
    public ResponseEntity<Favorite> deleteFavoriteByIdAndCategory(FavoriteDTO favoriteDto) {

        favoriteService.deleteFavoriteByProductIdAndCategory(favoriteDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Favorite> addFavorite(@RequestBody FavoriteDTO favoriteDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.addFavoriteByIdAndCategory(favoriteDto));
    }
}
