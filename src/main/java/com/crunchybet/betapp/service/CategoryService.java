package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.CategoryOnlyDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;




    public List<CategoryDTO> getAllCategoriesWithNominees() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    categoryDTO.setNominees(category.getNominees().stream()
                            .map(nominee -> {
                                NomineeDTO nomineeDTO = new NomineeDTO();
                                nomineeDTO.setId(nominee.getId());
                                nomineeDTO.setName(nominee.getName());
                                nomineeDTO.setImageUrl(nominee.getImageUrl());
                                nomineeDTO.setMultiplier(nominee.getMultiplier());
                                return nomineeDTO;
                            })
                            .collect(Collectors.toList()));
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

    //just get categories no nominees
    public List<CategoryOnlyDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> {
                    CategoryOnlyDTO categoryDTO = new CategoryOnlyDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

    public List<NomineeDTO> getNomineesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return category.getNominees().stream()
                .map(nominee -> {
                    NomineeDTO nomineeDTO = new NomineeDTO();
                    nomineeDTO.setId(nominee.getId());
                    nomineeDTO.setName(nominee.getName());
                    nomineeDTO.setImageUrl(nominee.getImageUrl());
                    nomineeDTO.setMultiplier(nominee.getMultiplier());
                    nomineeDTO.setCategoryId(category.getId());
                    return nomineeDTO;
                })
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> findTopNWithNominees(int limit) {
        List<Category> categories = categoryRepository.findTopN(PageRequest.of(0, limit));
        return categories.stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    categoryDTO.setDescription(category.getDescription());
                    categoryDTO.setNominees(category.getNominees().stream()
                            .map(nominee -> {
                                NomineeDTO nomineeDTO = new NomineeDTO();
                                nomineeDTO.setId(nominee.getId());
                                nomineeDTO.setName(nominee.getName());
                                nomineeDTO.setImageUrl(nominee.getImageUrl());
                                nomineeDTO.setMultiplier(nominee.getMultiplier());
                                return nomineeDTO;
                            })
                            .collect(Collectors.toList()));
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

}
