package com.insured.assurance;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/"})
public class HelloServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    try (PrintWriter out = resp.getWriter()) {
      out.println("<html><head><title>Insured Assurance</title></head><body>");
      out.println("<h1>Hello from Insured Assurance App</h1>");
      out.println("<p>Deployed via Jenkins to Tomcat</p>");
      out.println("</body></html>");
    }
  }
}
