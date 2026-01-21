package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

	@Value("${stripe.premium-plan-price-id}")
	private String premiumPlanPriceId;

	private final UserService userService;
	private final StripeService stripeService;

	public SubscriptionController(UserService userService, StripeService stripeService) {
		this.userService = userService;
		this.stripeService = stripeService;
	}

	@GetMapping("/register")
	public String register() {
		return "subscription/register";
	}

	@PostMapping("/create")
	public String create(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();

		String stripeCustomerId = user.getStripeCustomerId();

		if (stripeCustomerId == null) {
			try {
				Customer customer = stripeService.createCustomer(user);
				userService.saveStripeCustomerId(user, customer.getId());
			} catch (StripeException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
				return "redirect:/";
			}
		}

		try {
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			stripeService.createSubscription(stripeCustomerId, premiumPlanPriceId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。再度お試しください。");
			return "redirect:/";
		}

		userService.updateRole(user, "ROLE_PAID_GENERAL");
		userService.refreshAuthenticationByRole("ROLE_PAID_GENERAL");

		redirectAttributes.addFlashAttribute("successMessage", "有料プランへの登録が完了しました。");
		return "redirect:/";
	}

	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes,
			Model model) {
		User user = userDetailsImpl.getUser();

		try {
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(user.getStripeCustomerId());

			model.addAttribute("card", paymentMethod.getCard());
			model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法を取得できませんでした。再度お試しください。");
			return "redirect:/";
		}

		return "subscription/edit";
	}

	@PostMapping("/update")
	public String update(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String paymentMethodId,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();
		String customerId = user.getStripeCustomerId();

		try {
			String currentDefaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(customerId);

			stripeService.attachPaymentMethodToCustomer(paymentMethodId, customerId);
			stripeService.setDefaultPaymentMethod(paymentMethodId, customerId);
			stripeService.detachPaymentMethodFromCustomer(currentDefaultPaymentMethodId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払い方法の変更に失敗しました。再度お試しください。");

			return "redirect:/";
		}

		redirectAttributes.addFlashAttribute("successMessage", "お支払い方法を変更しました。");

		return "redirect:/";
	}

	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}

	@PostMapping("/delete")
	public String delete(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
			RedirectAttributes redirectAttributes) {
		User user = userDetailsImpl.getUser();

		try {
			List<Subscription> subscriptions = stripeService.getSubscriptions(user.getStripeCustomerId());
			stripeService.cancelSubscriptions(subscriptions);
			String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");
			return "redirect:/";
		}
		userService.updateRole(user, "ROLE_GENERAL");
		userService.refreshAuthenticationByRole("ROLE_GENERAL");

		redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");

		return "redirect:/";
	}
}
