package br.com.alura.ecommerce;

import java.util.UUID;

public class CorrelationId {
	private final String id;

	public CorrelationId() {
		id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "CorrelationId {id=" + id + "}";
	}

}
