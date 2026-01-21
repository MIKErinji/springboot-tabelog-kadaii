package com.example.nagoyameshi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	//	ユーザー情報の仮登録
	@Transactional
	public User createUser(SignupForm signupForm) {

		User user = new User();
		Role role = roleRepository.findByName("ROLE_GENERAL");

		user.setRole(role);
		user.setName(signupForm.getName());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setEnabled(false);

		return userRepository.save(user);

	}

	//	メールアドレスが既に登録されていないかチェック
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		return user != null;
	}

	//	パスワード入力と確認用パスワード入力が一致しているかチェック
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}

	//	全てのユーザーを表示する
	public Page<User> findAllUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	//	キーワード検索した結果を表示する
	public Page<User> findUsersByNameLike(String keyword, Pageable pageable) {
		return userRepository.findByNameLike("%" + keyword + "%", pageable);
	}

	//	ユーザーをIDで検索する
	public Optional<User> findUserById(Integer id) {
		return userRepository.findById(id);
	}

	@Transactional
	public User updateUser(User user, UserEditForm userEditForm) {

		user.setName(userEditForm.getName());
		user.setEmail(userEditForm.getEmail());

		return userRepository.save(user);

	}

	public boolean isEmailChanged(User user, UserEditForm userEditForm) {
		return !userEditForm.getEmail().equals(user.getEmail());
	}

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional
	public void saveStripeCustomerId(User user, String customerId) {
		user.setStripeCustomerId(customerId);
		userRepository.save(user);
	}

	@Transactional
	public void updateRole(User user, String roleName) {
		Role role = roleRepository.findByName(roleName);
		user.setRole(role);
		userRepository.save(user);
	}

	public long countUsersByRole_Name(String roleName) {
		return userRepository.countByRole_Name(roleName);
	}

	// 認証情報のロールを更新する
	public void refreshAuthenticationByRole(String newRole) {
		// 現在の認証情報を取得する
		Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

		// 新しい認証情報を作成する
		List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
		simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
		Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(),
				currentAuthentication.getCredentials(), simpleGrantedAuthorities);

		// 認証情報を更新する
		SecurityContextHolder.getContext().setAuthentication(newAuthentication);
	}

}
