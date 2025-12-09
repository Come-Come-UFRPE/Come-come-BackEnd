package com.backend.favorite.service;

import com.backend.favorite.dtos.CategoryDTO;
import com.backend.favorite.dtos.CategoryPatchDto;
import com.backend.favorite.exceptions.CategoryAlreadyAddedException;
import com.backend.favorite.exceptions.CategoryNotFoundException;
import com.backend.favorite.models.Category;
import com.backend.favorite.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    //lista as categorias de um usuario
    public List<Category> getCategoryByOwnerId(UUID id) {
        return categoryRepository.findCategoryByOwnerId(id);
    }

    //busca uma unica categoria
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
    }

    public Category updateCategoryName(CategoryPatchDto categoryPatchDto) {

       Category categoryOld = categoryRepository.findById(categoryPatchDto.id()).orElseThrow(CategoryNotFoundException::new);

       categoryOld.setCategoryName(categoryPatchDto.name());
       return categoryRepository.save(categoryOld);

    }


    //add uma nova categoria
    public Category addCategory(CategoryDTO categoryDTO) {
        if(categoryRepository.findByCategoryNameAndOwnerId(categoryDTO.categoryName(), categoryDTO.ownerId()).isPresent()){
            throw new CategoryAlreadyAddedException();
        }
        Category category = new Category();
        category.setCategoryName(categoryDTO.categoryName());
        category.setOwnerId(categoryDTO.ownerId());

        return categoryRepository.save(category);
    }

    //deleta uma categoria
    public void  deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }

}
