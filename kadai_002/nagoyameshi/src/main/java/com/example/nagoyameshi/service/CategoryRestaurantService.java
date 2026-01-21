package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {

	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;

	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository,
			CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;
	}

	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}

	@Transactional
	public void createCategoriesRestaurants(List<Integer> List, Restaurant restaurant) {

		for (Integer categoryId : List) {
			if (categoryId != null) {
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
				if (optionalCategory.isPresent()) {
					Category category = optionalCategory.get();

					Optional<CategoryRestaurant> optionalCategoryRestaurant = categoryRestaurantRepository
							.findByCategoryAndRestaurant(category, restaurant);
					if (optionalCategoryRestaurant.isEmpty()) {

						CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
						categoryRestaurant.setCategory(category);
						categoryRestaurant.setRestaurant(restaurant);

						categoryRestaurantRepository.save(categoryRestaurant);
					}
				}
			}
		}
	}

	@Transactional
	public void syncCategoriesRestaurants(List<Integer> List, Restaurant restaurant) {

		List<CategoryRestaurant> currentCategoriesRestaurants = categoryRestaurantRepository
				.findByRestaurantOrderByIdAsc(restaurant);

		if (List == null) {
			for (CategoryRestaurant currentCategoryRestaurants : currentCategoriesRestaurants) {
				categoryRestaurantRepository.delete(currentCategoryRestaurants);
			}
		} else {
			for (CategoryRestaurant currentCategoryRestaurants : currentCategoriesRestaurants) {
				if (!List.contains(currentCategoryRestaurants.getCategory().getId())) {
					categoryRestaurantRepository.delete(currentCategoryRestaurants);
				}

			}

		}

		for (Integer categoryId : List) {
			if (categoryId != null) {
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
				if (optionalCategory.isPresent()) {
					Category category = optionalCategory.get();

					Optional<CategoryRestaurant> optionalCategoryRestaurant = categoryRestaurantRepository
							.findByCategoryAndRestaurant(category, restaurant);
					if (optionalCategoryRestaurant.isEmpty()) {

						CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
						categoryRestaurant.setCategory(category);
						categoryRestaurant.setRestaurant(restaurant);

						categoryRestaurantRepository.save(categoryRestaurant);
					}
				}
			}
		}
	}

}
