package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionListParams;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService {

	@Value("${stripe.api-key}")
	public String stripeApikey;

	@PostConstruct
	private void init() {
		Stripe.apiKey = stripeApikey;
	}

	public Customer createCustomer(User user) throws StripeException {

		CustomerCreateParams params = CustomerCreateParams.builder()
				.setName(user.getName())
				.setEmail(user.getEmail())
				.build();

		return Customer.create(params);
	}

	public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
		PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
		PaymentMethodAttachParams params = PaymentMethodAttachParams.builder().setCustomer(customerId).build();

		paymentMethod.attach(params);
	}

	public void setDefaultPaymentMethod(String paymentMethodId, String customerId) throws StripeException {

		Customer customer = Customer.retrieve(customerId);

		CustomerUpdateParams params = CustomerUpdateParams.builder()
				.setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
						.setDefaultPaymentMethod(paymentMethodId)
						.build())
				.build();

		customer.update(params);
	}

	public Subscription createSubscription(String customerId, String priceId) throws StripeException {

		SubscriptionCreateParams params = SubscriptionCreateParams.builder()
				.setCustomer(customerId)
				.addItem(
						SubscriptionCreateParams.Item.builder()
								.setPrice(priceId)
								.build())
				.build();

		return Subscription.create(params);
	}

	public PaymentMethod getDefaultPaymentMethod(String customerId) throws StripeException {

		Customer customer = Customer.retrieve(customerId);

		String defaultPaymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

		return PaymentMethod.retrieve(defaultPaymentMethodId);
	}

	public String getDefaultPaymentMethodId(String customerId) throws StripeException {

		Customer customer = Customer.retrieve(customerId);
		return customer.getInvoiceSettings().getDefaultPaymentMethod();
	}

	public void detachPaymentMethodFromCustomer(String paymentMethodId) throws StripeException {

		PaymentMethod resource = PaymentMethod.retrieve(paymentMethodId);

		resource.detach();
	}

	public List<Subscription> getSubscriptions(String customerId) throws StripeException {

		SubscriptionListParams params = SubscriptionListParams.builder().setCustomer(customerId).build();

		return Subscription.list(params).getData();
	}

	public void cancelSubscriptions(List<Subscription> subscriptions) throws StripeException {

		for (Subscription subscription : subscriptions) {
			subscription.cancel();
		}
	}

}
