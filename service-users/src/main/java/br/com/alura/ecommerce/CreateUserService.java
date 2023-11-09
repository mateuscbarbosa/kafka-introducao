package br.com.alura.ecommerce;

import java.sql.SQLException;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import br.com.alura.ecommerce.consumer.ConsumerService;
import br.com.alura.ecommerce.consumer.ServiceRunner;
import br.com.alura.ecommerce.database.LocalDatabase;

public class CreateUserService implements ConsumerService<Order>{
	
	private final LocalDatabase database;

	CreateUserService() throws SQLException{
		this.database = new LocalDatabase("ecommerce_users");
		this.database.crateIfNotExists("CREATE TABLE IF NOT EXISTS users("
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
		var uuid = UUID.randomUUID().toString();
		database.update("INSERT INTO users (uuid, email) VALUES (?,?)", uuid, email);
		
		System.out.println("Usuário " + uuid + " e " + email + " adicionado.");
	}

	private boolean isNewUser(String email) throws SQLException {
		var results = database.query("SELECT uuid FROM users "
				+ "WHERE email = ?", email);
				
		return !results.next();
	}

}
