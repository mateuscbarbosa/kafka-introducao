package br.com.alura.ecommerce.consumer;

import org.apache.kafka.common.serialization.Deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.com.alura.ecommerce.Message;
import br.com.alura.ecommerce.MessageAdapter;

public class GsonDeserializer implements Deserializer<Message> {

	//public static final String TYPE_CONFIG = "br.com.alura.ecommerce.type_config";
	private final Gson gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageAdapter()).create();
	/*Desserialização de tipos de dado específicos
	 * private Class<T> type;

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		String typeName = String.valueOf(configs.get(TYPE_CONFIG));
		try {
			this.type = (Class<T>) Class.forName(typeName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Type for deserialization does not exists in the classpath", e);
		}
	}*/
	
	@Override
	//public T deserialize(String s, byte[] bytes) {
	public Message deserialize(String s, byte[] bytes) {
		return gson.fromJson(new String(bytes), Message.class);
	}
	
}
