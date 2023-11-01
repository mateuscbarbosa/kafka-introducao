package br.com.alura.ecommerce;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.KafkaService;
import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;

public class FraudDetectorService {

	private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		var fraudService = new FraudDetectorService();
		try (var service = new KafkaService(
				FraudDetectorService.class.getSimpleName(),
				"ECOMMERCE_NEW_ORDER",
				fraudService::parse,
				Map.of())){
			service.run();
		}
	}
	
	private void parse(ConsumerRecord<String, Message<Order>> record) throws InterruptedException, ExecutionException {
		System.out.println("------------------------------------------");
		System.out.println("Processing new order, checking for frauds.");
		System.out.println(record.key());
		System.out.println(record.value());
		System.out.println(record.partition());
		System.out.println(record.offset());
		
		var message = record.value();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		var order = message.getPayload();
		if(isFraud(order)) {
			//pretending that the fraud happens when the amount is < 1000
			System.out.println("Order is a fraud. " + order);
			orderDispatcher.send(
					"ECOMMERCE_ORDER_REJECTED",
					order.getEmail(),
					message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
					order);
		}
		else {
			System.out.println("Approved: " + order);
			orderDispatcher.send(
					"ECOMMERCE_ORDER_APPROVED",
					order.getEmail(),
					message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
					order);
		}
	}

	private boolean isFraud(Order order) {
		return order.getAmount().compareTo(new BigDecimal("4000")) >= 0; 
	}

}
