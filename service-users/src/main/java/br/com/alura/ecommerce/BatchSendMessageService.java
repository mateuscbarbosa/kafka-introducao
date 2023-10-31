package br.com.alura.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class BatchSendMessageService {
	
	private final Connection connection;
	private final KafkaDispatcher<User> userDispatcher = new KafkaDispatcher<>();

	public BatchSendMessageService() throws SQLException {
		String url = "jdbc:postgresql://localhost/ecommerce";
		String user = "postgres";
		String password = "postgres";
		
		connection = DriverManager.getConnection(url, user, password);
		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users("
				+ "uuid VARCHAR(255) PRIMARY KEY,"
				+ "email VARCHAR(255))");
	}
	
	public static void main(String[] args) throws SQLException {
		var batchService = new BatchSendMessageService();
		try (var service = new KafkaService(
				BatchSendMessageService.class.getSimpleName(),
				"ECOMMERCE_SEND_MESSAGE_TO_ALL_USERS",
				batchService::parse,
				Map.of())){
			service.run();
		}
	}
	
	private void parse(ConsumerRecord<String, Message<String>> record) throws Exception {
		System.out.println("------------------------------------------");
		System.out.println("Processing new batch.");
		var message = record.value();
		System.out.println("Topic: " + message.getPayload());
		
		for(User user: getAllUsers()) {
			userDispatcher.send(
					message.getPayload(),
					user.getUuid(),
					message.getId().continueWith(BatchSendMessageService.class.getSimpleName()),
					user);
		}
	}

	private List<User> getAllUsers() throws SQLException {
		var results = connection.prepareStatement("SELECT uuid FROM users").executeQuery();
		List<User> users = new ArrayList<>();
		while(results.next()) {
			users.add(new User(results.getString(1)));
		}
		return users;
	}
}