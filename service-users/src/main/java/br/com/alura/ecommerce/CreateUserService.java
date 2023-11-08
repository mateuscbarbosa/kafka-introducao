package br.com.alura.ecommerce;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.ConsumerService;
import br.com.alura.ecommerce.consumer.ServiceRunner;

public class CreateUserService implements ConsumerService<Order>{
	
	private final Connection connection;

	public CreateUserService() throws SQLException {
		String url = "jdbc:postgresql://localhost/ecommerce";
		String user = "postgres";
		String password = "postgres";
		
		connection = DriverManager.getConnection(url, user, password);
		connection.createStatement().execute("CREATE TABLE IF NOT EXISTS users("
				+ "uuid VARCHAR(255) PRIMARY KEY,"
				+ "email VARCHAR(255))");
	}
	
	public static void main(String[] args) {
		new ServiceRunner<>(CreateUserService::new).start(1);
	}
	
	@Override
	public String getTopic() {
		return "ECOMMERCE_NEW_ORDER";
	}
	
	@Override
	public String getConsumerGroup() {
		return CreateUserService.class.getSimpleName();
	}
	
	@Override
	public void parse(ConsumerRecord<String, Message<Order>> record) throws Exception {
		System.out.println("------------------------------------------");
		System.out.println("Processing new order, checking for new user.");
		System.out.println(record.value());
		var order = record.value().getPayload();
		
		if(isNewUser(order.getEmail())) {
			insertNewUser(order.getEmail());
		}
		
	}

	private void insertNewUser(String email) throws SQLException {
		var insert = connection.prepareStatement("INSERT INTO users (uuid, email) VALUES (?,?)");
		var uuid = UUID.randomUUID().toString();
		
		insert.setString(1, uuid);
		insert.setString(2, email);
		insert.execute();
		System.out.println("Usuário " + uuid + " e " + email + " adicionado.");
	}

	private boolean isNewUser(String email) throws SQLException {
		var exists = connection.prepareStatement("SELECT uuid FROM users "
				+ "WHERE email = ?");
		exists.setString(1, email);
		var results = exists.executeQuery();
		
		return !results.next();
	}

}
