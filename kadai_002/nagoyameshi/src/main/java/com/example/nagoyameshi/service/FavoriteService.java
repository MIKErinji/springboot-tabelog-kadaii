package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@Service
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;

	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}

	public Optional<Favorite> findFavoriteById(Integer id) {
		return favoriteRepository.findById(id);
	}

	public Favorite findFavoriteByRestaurantAndUser(Restaurant restaurant, User user) {
		return favoriteRepository.findByRestaurantAndUser(restaurant, user);
	}

	public Page<Favorite> findFavoritesByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
		return favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);
	}

	public long countFavorites() {
		return favoriteRepository.count();
	}

	@Transactional
	public void createFavorite(Restaurant restaurant, User user) {
		Favorite favorite = new Favorite();

		favorite.setUser(user);
		favorite.setRestaurant(restaurant);

		favoriteRepository.save(favorite);
	}

	@Transactional
	public void deleteFavorite(Favorite favorite) {
		favoriteRepository.delete(favorite);
	}

	public boolean isFavorite(Restaurant restaurant, User user) {
		return favoriteRepository.findByRestaurantAndUser(restaurant, user) != null;
	}

}
