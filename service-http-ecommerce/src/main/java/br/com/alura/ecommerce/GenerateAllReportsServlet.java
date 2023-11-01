package br.com.alura.ecommerce;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.http.HttpStatus;

import br.com.alura.ecommerce.dispatcher.KafkaDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class GenerateAllReportsServlet extends HttpServlet {
	
	private final KafkaDispatcher<String> batchDispatcher = new KafkaDispatcher<>();

	@Override
	public void destroy() {
		super.destroy();
		batchDispatcher.close();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			
			batchDispatcher.send("ECOMMERCE_SEND_MESSAGE_TO_ALL_USERS",
					"ECOMMERCE_USER_GENERATE_READING_REPORT",
					new CorrelationId(GenerateAllReportsServlet.class.getSimpleName()),
					"ECOMMERCE_USER_GENERATE_READING_REPORT");
			
			System.out.println("Sent generated reports to all users.");
			resp.setStatus(HttpStatus.OK_200);
			resp.getWriter().println("Report requets generated.");
		} catch (InterruptedException e) {
			throw new ServletException();
		} catch (ExecutionException e) {
			throw new ServletException();
		}
	}
}
