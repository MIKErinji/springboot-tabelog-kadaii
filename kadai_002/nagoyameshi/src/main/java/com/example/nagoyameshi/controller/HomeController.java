package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class HomeController {

	private final CategoryService categoryService;
	private final RestaurantService restaurantService;

	public HomeController(CategoryService categoryService, RestaurantService restaurantService) {
		this.categoryService = categoryService;
		this.restaurantService = restaurantService;
	}

	@GetMapping("/")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {

		if (userDetailsImpl != null && userDetailsImpl.getUser().getRole().getName().equals("ROLE_ADMIN")) {
			return "redirect:/admin";
		}

		Page<Restaurant> highlyRatedRestaurants = restaurantService
				.findAllRestaurantsByOrderByAverageScoreDesc(PageRequest.of(0, 6));
		Page<Restaurant> newRestaurants = restaurantService
				.findAllRestaurantsByOrderByCreatedAtDesc(PageRequest.of(0, 6));

		Category washoku = categoryService.findFirstCategoryByName("和風");
		Category noodles = categoryService.findFirstCategoryByName("麺類");
		Category don = categoryService.findFirstCategoryByName("丼物");
		Category fried = categoryService.findFirstCategoryByName("揚げ物");
		Category standard = categoryService.findFirstCategoryByName("定番");
		Category western = categoryService.findFirstCategoryByName("洋食");

		List<Category> categories = categoryService.findAllCategories();

		model.addAttribute("highlyRatedRestaurants", highlyRatedRestaurants);
		model.addAttribute("newRestaurants", newRestaurants);

		model.addAttribute("washoku", washoku);
		model.addAttribute("noodles", noodles);
		model.addAttribute("don", don);
		model.addAttribute("fried", fried);
		model.addAttribute("standard", standard);
		model.addAttribute("western", western);

		model.addAttribute("categories", categories);

		return "index";
	}

}
