package br.com.alura.ecommerce.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocalDatabase {

	private final Connection connection;

	public LocalDatabase(String name) throws SQLException {
		String url = "jdbc:postgresql://localhost/" + name;
		String user = "postgres";
		String password = "postgres";
		
		connection = DriverManager.getConnection(url, user, password);
	}
	
	//possible sql injecction
	public void crateIfNotExists(String sql){
		try {
			connection.createStatement().execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void update(String statement, String ... params) throws SQLException {
		prepare(statement, params).execute();
	}

	public ResultSet query(String query, String ... params) throws SQLException {
		return prepare(query, params).executeQuery();
	}

	private PreparedStatement prepare(String statement, String... params) throws SQLException {
		var preparedStatement = connection.prepareStatement(statement);
		for(int i = 0; i < params.length; i++) {
			preparedStatement.setString(i + 1, params[i]);
		}
		
		return preparedStatement;
	}

	public void close() throws SQLException {
		this.connection.close();
	}
}
