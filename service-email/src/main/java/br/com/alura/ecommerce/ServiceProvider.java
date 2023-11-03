package br.com.alura.ecommerce;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import br.com.alura.ecommerce.consumer.KafkaService;

public class ServiceProvider<T> implements Callable<Void>{

	private final ServiceFactory<T> factory;

	public ServiceProvider(ServiceFactory<T> factory) {
		this.factory = factory;
	}

	@Override
	public Void call() throws InterruptedException, ExecutionException {
		var myService = factory.create();
		try(var service = new KafkaService(
				myService.getConsumerGroup(),
				myService.getTopic(),
				myService::parse,
				new HashMap<>())){
			service.run();
		}
		
		return null;
	}

}
