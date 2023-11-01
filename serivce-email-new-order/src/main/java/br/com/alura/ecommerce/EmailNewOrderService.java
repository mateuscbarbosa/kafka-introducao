package br.com.alura.ecommerce;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.KafkaService;
import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;

public class EmailNewOrderService {

	private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		var emailService = new EmailNewOrderService();
		try (var service = new KafkaService(
				EmailNewOrderService.class.getSimpleName(),
				"ECOMMERCE_NEW_ORDER",
				emailService::parse,
				Map.of())){
			service.run();
		}
	}
	
	private void parse(ConsumerRecord<String, Message<Order>> record) throws InterruptedException, ExecutionException {
		System.out.println("------------------------------------------");
		System.out.println("Processing new order, preparing e-mail.");
		
		var message = record.value();
		System.out.println(message);
		
		var order = message.getPayload();
		var emailCode = "Thank you for your order. We are processing your order.";
		var correlationId = message.getId().continueWith(EmailNewOrderService.class.getSimpleName());
		emailDispatcher.send(
				"ECOMMERCE_SEND_EMAIL",
				order.getEmail(),
				correlationId,
				emailCode);

	}

}
