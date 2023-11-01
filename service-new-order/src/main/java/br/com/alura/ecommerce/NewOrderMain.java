package br.com.alura.ecommerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;

public class NewOrderMain {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		try (var orderDispatcher = new KafkaDispatcher<Order>()) {
			var email = Math.random() + "@email.com";
			for (int i = 0; i < 10; i++) {
				var orderId = UUID.randomUUID().toString();
				var amount = new BigDecimal(Math.random() * 5000 + 1).setScale(2, RoundingMode.HALF_UP);
			
				var order = new Order( orderId, amount, email);
				
				var correlationId = new CorrelationId(NewOrderMain.class.getSimpleName());
				orderDispatcher.send(
						"ECOMMERCE_NEW_ORDER",
						email,
						correlationId,
						order);
			}
		}
	}

}
