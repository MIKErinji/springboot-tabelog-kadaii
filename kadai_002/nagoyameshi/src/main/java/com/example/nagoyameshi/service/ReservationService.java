package com.example.nagoyameshi.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {

	private final ReservationRepository reservationRepository;

	public ReservationService(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	public Optional<Reservation> findReservationById(Integer id) {
		return reservationRepository.findById(id);
	}

	public Page<Reservation> findReservationsByUserOrderByReservedDatetimeDesc(User user, Pageable pageable) {
		return reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable);
	}

	public long countReservations() {
		return reservationRepository.count();
	}

	public Reservation findFirstReservationByOrderByIdDesc() {
		return reservationRepository.findFirstByOrderByIdDesc();
	}

	@Transactional
	public void createReservation(ReservationRegisterForm reservationRegisterForm, Restaurant restaurant, User user) {

		Reservation reservation = new Reservation();
		LocalDateTime reservedDatetime = LocalDateTime.of(reservationRegisterForm.getReservationDate(),
				reservationRegisterForm.getReservationTime());

		reservation.setReservedDatetime(reservedDatetime);
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
		reservation.setRestaurant(restaurant);
		reservation.setUser(user);

		reservationRepository.save(reservation);
	}

	@Transactional
	public void deleteReservation(Reservation reservation) {
		reservationRepository.delete(reservation);
	}

	public boolean isAtLeastTwoHoursInFuture(LocalDateTime localDateTime) {
		return Duration.between(LocalDateTime.now(), localDateTime).toHours() >= 2;
	}
}
