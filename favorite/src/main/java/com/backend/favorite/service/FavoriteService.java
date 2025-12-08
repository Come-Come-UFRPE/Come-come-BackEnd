package com.backend.favorite.service;

import com.backend.favorite.dtos.CategoryPatchDto;
import com.backend.favorite.dtos.DeleteFavoriteDTO;
import com.backend.favorite.dtos.FavoriteDTO;
import com.backend.favorite.exceptions.CategoryNotFoundException;
import com.backend.favorite.exceptions.FavoriteAlreadyAddedException;
import com.backend.favorite.exceptions.FavoriteNotFoundException;
import com.backend.favorite.models.Category;
import com.backend.favorite.models.Favorite;
import com.backend.favorite.repositories.CategoryRepository;
import com.backend.favorite.repositories.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {


    private final FavoriteRepository favoriteRepository;


    CategoryRepository categoryRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, CategoryRepository categoryRepository) {
        this.favoriteRepository = favoriteRepository;
        this.categoryRepository = categoryRepository;
    }

    //lista todos os favoritos em uma categoria
    public List<Favorite> getAllFavoritesByCategoryId(UUID categoryId){
        return favoriteRepository.findByCategory_CategoryId(categoryId);
    }

    //deleta um favorito em uma categoria
    public void deleteFavoriteByProductIdAndCategory(DeleteFavoriteDTO favoriteDTO) {
        //Procurar e
        Favorite fav = favoriteRepository.findByProductIdAndCategory_CategoryId(favoriteDTO.productId(), favoriteDTO.categoryId())
                                            .orElseThrow(FavoriteNotFoundException::new);

        //Deletar na categoria criada
        Category deleteFavorite = categoryRepository.findById(favoriteDTO.categoryId()).orElseThrow(CategoryNotFoundException::new);

        deleteFavorite.getFavorite().remove(fav);

        categoryRepository.save(deleteFavorite);
    }

    //adiciona um item a uma categoria
    public Favorite addFavoriteByIdAndCategory(FavoriteDTO favoriteDto){

        if(favoriteRepository.findByProductIdAndCategory_CategoryId(favoriteDto.productId(),favoriteDto.categoryId()).isPresent()){
            throw new FavoriteAlreadyAddedException();
        }

        Category category = categoryRepository.findById(favoriteDto.categoryId()).orElseThrow(CategoryNotFoundException::new);
        Favorite favorite = new Favorite();
        favorite.setCategory(category);
        favorite.setProductId(favoriteDto.productId());
        favorite.setProductName(favoriteDto.productName());
        favorite.setUrl(favoriteDto.url());
        favoriteRepository.save(favorite);
        return favorite;
    }
}
