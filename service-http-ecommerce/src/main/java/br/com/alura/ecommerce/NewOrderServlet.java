package br.com.alura.ecommerce;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.http.HttpStatus;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NewOrderServlet extends HttpServlet {
	
	private final KafkaDispatcher<Order> orderDispatcher = new KafkaDispatcher<>();
	private final KafkaDispatcher<String> emailDispatcher = new KafkaDispatcher<>();

	@Override
	public void destroy() {
		super.destroy();
		orderDispatcher.close();
		emailDispatcher.close();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			var email = req.getParameter("email");
			var amount = new BigDecimal(req.getParameter("amount"));
			
			var orderId = UUID.randomUUID().toString();

			var order = new Order(orderId, amount, email);
			orderDispatcher.send("ECOMMERCE_NEW_ORDER", email, order);

			var emailCode = "Thank you for your order. We are processing your order.";
			emailDispatcher.send("ECOMMERCE_SEND_EMAIL", email, emailCode);
			
			System.out.println("New order "+ orderId +" sent sucessfully.");
			resp.setStatus(HttpStatus.OK_200);
			resp.getWriter().println("New order "+ orderId +" sent sucessfully.");
		} catch (InterruptedException e) {
			throw new ServletException();
		} catch (ExecutionException e) {
			throw new ServletException();
		}
	}
}
