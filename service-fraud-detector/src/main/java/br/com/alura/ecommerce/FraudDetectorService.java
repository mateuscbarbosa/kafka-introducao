package br.com.alura.ecommerce;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.ConsumerService;
import br.com.alura.ecommerce.consumer.ServiceRunner;
import br.com.alura.ecommerce.database.LocalDatabase;
import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;

public class FraudDetectorService implements ConsumerService<Order>{

	private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();
	private final LocalDatabase database;

	FraudDetectorService() throws SQLException{
		this.database = new LocalDatabase("ecommerce_frauds");
		this.database.crateIfNotExists("CREATE TABLE IF NOT EXISTS orders("
			+ "uuid VARCHAR(255) PRIMARY KEY,"
			+ "is_fraud BOOLEAN)");
	}
	public static void main(String[] args) {
		new ServiceRunner<>(FraudDetectorService::new).start(1);
	}
	
	@Override
	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}
	
	@Override
	public String getConsumerGroup() {
		return FraudDetectorService.class.getSimpleName();
	}
	
	@Override
	public void parse(ConsumerRecord<String, Message<Order>> record) throws InterruptedException, ExecutionException, SQLException {
		System.out.println("------------------------------------------");
		System.out.println("Processing new order, checking for frauds.");
		System.out.println(record.key());
		System.out.println(record.value());
		System.out.println(record.partition());
		System.out.println(record.offset());
		
		var message = record.value();
		var order = message.getPayload();
		
		if(wasProcessed(order)) {
			System.out.printf("""
					Order %s was alredy processed.
					""", order.getOrderId());
			return;
		}
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(isFraud(order)) {
			//pretending that the fraud happens when the amount is > 4000
			database.update("INSERT INTO orders (uuid, is_fraud) VALUES(?, true)", order.getOrderId());
			System.out.println("Order is a fraud. " + order);
			orderDispatcher.send(
					"ECOMMERCE_ORDER_REJECTED",
					order.getEmail(),
					message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
					order);
		}
		else {
			database.update("INSERT INTO orders (uuid, is_fraud) VALUES(?, false)", order.getOrderId());
			System.out.println("Approved: " + order);
			orderDispatcher.send(
					"ECOMMERCE_ORDER_APPROVED",
					order.getEmail(),
					message.getId().continueWith(FraudDetectorService.class.getSimpleName()),
					order);
		}
	}

	private boolean wasProcessed(Order order) throws SQLException {
		var results = database.query("SELECT uuid FROM orders "
				+ "WHERE uuid = ? "
				+ "LIMIT 1", order.getOrderId());
		
		return results.next();
	}
	private boolean isFraud(Order order) {
		return order.getAmount().compareTo(new BigDecimal("4000")) >= 0; 
	}

}
