package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
}
