package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.RegularHolidayRestaurantRepository;

@Service
public class RegularHolidayRestaurantService {

	private final RegularHolidayRestaurantRepository regularHolidayRestaurantRepository;
	private final RegularHolidayService regularHolidayService;

	public RegularHolidayRestaurantService(RegularHolidayRestaurantRepository regularHolidayRestaurantRepository,
			RegularHolidayService regularHolidayService) {
		this.regularHolidayRestaurantRepository = regularHolidayRestaurantRepository;
		this.regularHolidayService = regularHolidayService;
	}

	public List<Integer> findRegularHolidayIdsByRestaurant(Restaurant restaurant) {
		return regularHolidayRestaurantRepository.findRegularHolidayIdsByRestaurant(restaurant);
	}

	@Transactional
	public void createRegularHolidaysRestaurants(List<Integer> List, Restaurant restaurant) {

		for (Integer regularHolidayId : List) {
			if (regularHolidayId != null) {
				Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService
						.findRegularHolidayById(regularHolidayId);
				if (optionalRegularHoliday.isPresent()) {
					RegularHoliday regularHoliday = optionalRegularHoliday.get();

					Optional<RegularHolidayRestaurant> optionalRegularHolidayRestaurant = regularHolidayRestaurantRepository
							.findByRegularHolidayAndRestaurant(regularHoliday, restaurant);
					if (optionalRegularHolidayRestaurant.isEmpty()) {

						RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
						regularHolidayRestaurant.setRegularHoliday(regularHoliday);
						regularHolidayRestaurant.setRestaurant(restaurant);

						regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
					}
				}
			}
		}
	}

	@Transactional
	public void syncRegularHolidaysRestaurants(List<Integer> List, Restaurant restaurant) {

		List<RegularHolidayRestaurant> currentRegularHolidayRestaurants = regularHolidayRestaurantRepository
				.findByRestaurant(restaurant);

		if (List == null) {
			for (RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidayRestaurants) {
				regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
			}
		} else {
			for (RegularHolidayRestaurant currentRegularHolidayRestaurant : currentRegularHolidayRestaurants) {
				if (!List.contains(currentRegularHolidayRestaurant.getRegularHoliday().getId())) {
					regularHolidayRestaurantRepository.delete(currentRegularHolidayRestaurant);
				}

			}

		}

		for (Integer newRegularHolidayId : List) {
			if (newRegularHolidayId != null) {
				Optional<RegularHoliday> optionalRegularHoliday = regularHolidayService
						.findRegularHolidayById(newRegularHolidayId);
				if (optionalRegularHoliday.isPresent()) {
					RegularHoliday regularHoliday = optionalRegularHoliday.get();

					Optional<RegularHolidayRestaurant> optionalRegularHolidayRestaurant = regularHolidayRestaurantRepository
							.findByRegularHolidayAndRestaurant(regularHoliday, restaurant);
					if (optionalRegularHolidayRestaurant.isEmpty()) {

						RegularHolidayRestaurant regularHolidayRestaurant = new RegularHolidayRestaurant();
						regularHolidayRestaurant.setRegularHoliday(regularHoliday);
						regularHolidayRestaurant.setRestaurant(restaurant);

						regularHolidayRestaurantRepository.save(regularHolidayRestaurant);
					}
				}
			}
		}
	}

}
