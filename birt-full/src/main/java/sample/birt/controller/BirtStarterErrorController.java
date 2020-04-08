package sample.birt.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;

// could not get this to work
// @Controller
public class BirtStarterErrorController implements ErrorController {

	@GetMapping(value = "/error")
	public String error() {
		try {
			/*
			 * final PrintStream ps = new PrintStream(response.getOutputStream()); final
			 * BirtStarterException e = (BirtStarterException)
			 * request.getAttribute("BirtStarterException"); if (e != null) {
			 * ps.println(e.getMessage()); } else { final Object status =
			 * request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE); if (status !=
			 * null) { ps.println(status.toString()); } else { ps.println("unknown error");
			 * } }
			 */
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{\"message\": \"test\"}";
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
