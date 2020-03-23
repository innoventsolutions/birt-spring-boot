package com.innoventsolutions.birt.controller;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.innoventsolutions.birt.exception.BirtStarterException;

// could not get this to work
@Controller
public class BirtStarterErrorController implements ErrorController {

	@GetMapping(value = "/error")
	public void handleError(final HttpServletRequest request, final HttpServletResponse response) {
		try {
			final PrintStream ps = new PrintStream(response.getOutputStream());
			final BirtStarterException e = (BirtStarterException) request.getAttribute("BirtStarterException");
			if (e != null) {
				ps.println(e.getMessage());
			} else {
				final Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
				if (status != null) {
					ps.println(status.toString());
				} else {
					ps.println("unknown error");
				}
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

}
