package br.com.alura.ecommerce;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.KafkaService;

public class EmailServiceOld {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		var emailService = new EmailServiceOld();
		try(var service = new KafkaService(
				EmailServiceOld.class.getSimpleName(),
				"ECOMMERCE_SEND_EMAIL",
				emailService::parse,
				new HashMap<>())){
			service.run();
		}
	}
	
	private void parse(ConsumerRecord<String, String> record) {
		System.out.println("------------------------------------------");
		System.out.println("Send email.");
		System.out.println(record.key());
		System.out.println(record.value());
		System.out.println(record.partition());
		System.out.println(record.offset());
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Email sent.");
	}

}
