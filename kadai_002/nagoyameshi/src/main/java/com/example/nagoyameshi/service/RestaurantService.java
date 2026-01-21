package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantService {

	private final RestaurantRepository restaurantRepository;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;

	public RestaurantService(RestaurantRepository restaurantRepository,
			CategoryRestaurantService categoryRestaurantService,
			RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}

	public Page<Restaurant> findAllRestaurants(Pageable pageable) {

		return restaurantRepository.findAll(pageable);
	}

	public Page<Restaurant> findRestaurantsByNameLike(String namekeyword, Pageable pageable) {

		return restaurantRepository.findByNameLike("%" + namekeyword + "%", pageable);
	}

	public Optional<Restaurant> findRestaurantById(Integer id) {

		return restaurantRepository.findById(id);
	}

	public long countRestaurants() {

		return restaurantRepository.count();
	}

	public Restaurant findFirstRestaurantByOrderByIdDesc() {

		return restaurantRepository.findFirstByOrderByIdDesc();
	}

	@Transactional
	public void createRestaurant(RestaurantRegisterForm restaurantRegisterForm) {

		Restaurant restaurant = new Restaurant();
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		List<Integer> categoryIds = restaurantRegisterForm.getCategoryIds();
		List<Integer> regularHolidayIds = restaurantRegisterForm.getRegularHolidayIds();

		if (!imageFile.isEmpty()) {

			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);

		}

		restaurant.setName(restaurantRegisterForm.getName());
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setBusinessHours(
				restaurantRegisterForm.getOpeningTime() + "~" + restaurantRegisterForm.getClosingTime());
		restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
		restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
		restaurant.setAddress(restaurantRegisterForm.getAddress());
		restaurant.setPhoneNumber(restaurantRegisterForm.getPhoneNumber());

		restaurantRepository.save(restaurant);

		if (categoryIds != null) {

			categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);

		}

		if (regularHolidayIds != null) {

			regularHolidayRestaurantService.createRegularHolidaysRestaurants(regularHolidayIds, restaurant);

		}

	}

	@Transactional
	public void updateRestaurant(RestaurantEditForm restaurantEditForm, Restaurant restaurant) {
		MultipartFile imageFile = restaurantEditForm.getImageFile();
		List<Integer> categoryIds = restaurantEditForm.getCategoryIds();
		List<Integer> regularHolidayIds = restaurantEditForm.getRegularHolidayIds();

		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);
		}

		restaurant.setName(restaurantEditForm.getName());
		restaurant.setDescription(restaurantEditForm.getDescription());
		restaurant.setLowestPrice(restaurantEditForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantEditForm.getHighestPrice());
		restaurant.setBusinessHours(restaurantEditForm.getOpeningTime() + "~" + restaurantEditForm.getClosingTime());
		restaurant.setPostalCode(restaurantEditForm.getPostalCode());
		restaurant.setAddress(restaurantEditForm.getAddress());
		restaurant.setPhoneNumber(restaurantEditForm.getPhoneNumber());

		restaurantRepository.save(restaurant);

		categoryRestaurantService.syncCategoriesRestaurants(categoryIds, restaurant);
		regularHolidayRestaurantService.syncRegularHolidaysRestaurants(regularHolidayIds, restaurant);
	}

	@Transactional
	public void deleteRestaurant(Restaurant restaurant) {
		restaurantRepository.delete(restaurant);
	}

	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");

		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}

		String hashedFileName = String.join(".", fileNames);

		return hashedFileName;

	}

	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isValidPrices(Integer lowestPrice, Integer highestPrice) {

		return highestPrice >= lowestPrice;
	}

	public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {

		return closingTime.isAfter(openingTime);
	}

	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);
	}

	public Page<Restaurant> findAllRestaurantsByOrderByLowestPriceAsc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(nameKeyword,
				addressKeyword, categoryNameKeyword, pageable);
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(nameKeyword,
				addressKeyword, categoryNameKeyword, pageable);
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByCreatedAtDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByLowestPriceAsc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
	}

	public Page<Restaurant> findAllRestaurantsByOrderByAverageScoreDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByAverageScoreDesc(pageable);
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(nameKeyword,
				addressKeyword, categoryNameKeyword, pageable);
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByAverageScoreDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(categoryId, pageable);
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByAverageScoreDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(price, pageable);
	}

	public Page<Restaurant> findAllRestaurantsByOrderByReservationCountDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByReservationCountDesc(pageable);
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
				nameKeyword, addressKeyword, categoryNameKeyword, pageable);
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByReservationCountDesc(Integer categoryId,
			Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByReservationCountDesc(categoryId, pageable);
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByReservationCountDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByReservationCountDesc(price, pageable);
	}

	public List<Integer> findDayIndexesByRestaurantId(Integer restaurantId) {
		return restaurantRepository.findDayIndexesByRestaurantId(restaurantId);
	}

}
