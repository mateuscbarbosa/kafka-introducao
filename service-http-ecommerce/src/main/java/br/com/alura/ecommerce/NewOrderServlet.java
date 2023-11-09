package br.com.alura.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.http.HttpStatus;

import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NewOrderServlet extends HttpServlet {
	
	private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();

	@Override
	public void destroy() {
		super.destroy();
		orderDispatcher.close();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			var email = req.getParameter("email");
			var amount = new BigDecimal(req.getParameter("amount"));
			
			//var orderId = UUID.randomUUID().toString();
			var orderId = req.getParameter("uuid");
			var order = new Order(orderId, amount, email);

			try(var database = new OrdersDatabase()){
				if(database.saveNew(order)) {
					orderDispatcher.send(
							"ECOMMERCE_NEW_ORDER",
							email,
							new CorrelationId(NewOrderServlet.class.getSimpleName()),
							order);
					
					System.out.println("New order "+ orderId +" sent sucessfully.");
					resp.setStatus(HttpStatus.OK_200);
					resp.getWriter().println("New order "+ orderId +" sent sucessfully.");
				} else {
					System.out.println("Old order "+ orderId +" was already received");
					resp.setStatus(HttpStatus.OK_200);
					resp.getWriter().println("Old order "+ orderId +" was already received");
				}
			}
			
		} catch (InterruptedException | ExecutionException | SQLException e) {
			throw new ServletException(e);
		}
	}
}
