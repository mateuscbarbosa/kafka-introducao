package br.com.alura.ecommerce;

import java.io.Closeable;
import java.sql.SQLException;

import br.com.alura.ecommerce.database.LocalDatabase;

public class OrdersDatabase implements Closeable{

	private final LocalDatabase database;

	OrdersDatabase() throws SQLException{
		this.database = new LocalDatabase("ecommerce_orders");
		this.database.crateIfNotExists("CREATE TABLE IF NOT EXISTS orders("
			+ "uuid VARCHAR(255) PRIMARY KEY)");
	}

	public boolean saveNew(Order order) throws SQLException {
		if(wasProcessed(order)) {
			return false;
		}
		database.update("INSERT INTO orders (uuid) VALUES(?)", order.getOrderId());
		return true;
	}
	
	private boolean wasProcessed(Order order) throws SQLException {
		var results = database.query("SELECT uuid FROM orders "
				+ "WHERE uuid = ? "
				+ "LIMIT 1", order.getOrderId());
		
		return results.next();
	}

	@Override
	public void close() {
		try {
			this.database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
