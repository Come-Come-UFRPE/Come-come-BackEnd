package com.backend.favorite.service;

import com.backend.favorite.dtos.FavoriteDTO;
import com.backend.favorite.models.Category;
import com.backend.favorite.models.Favorite;
import com.backend.favorite.repositories.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {


    private final FavoriteRepository favoriteRepository;

    @Autowired
    CategoryService categoryService;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    //lista todos os favoritos em uma categoria
    public List<Favorite> getAllFavoritesByCategoryId(UUID categoryId){
        return favoriteRepository.findByCategory_CategoryId(categoryId);
    }

    //deleta um favorito em uma categoria
    public void deleteFavoriteByProductIdAndCategory(FavoriteDTO favoriteDTO) {
        favoriteRepository.deleteByProductIdAndCategory_CategoryId(favoriteDTO.productId(), favoriteDTO.categoryId());
    }

    //adiciona um item a uma categoria
    public Favorite addFavoriteByIdAndCategory(FavoriteDTO favoriteDto){
        Category category = categoryService.getCategoryById(favoriteDto.categoryId());
        Favorite favorite = new Favorite();
        favorite.setCategory(category);
        favorite.setProductId(favoriteDto.productId());
        favoriteRepository.save(favorite);
        return favorite;
    }
}
