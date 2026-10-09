
package com.transport;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/RoutesServlet")
public class RoutesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<html><head><title>Bus Routes</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("</head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>Available Bus Routes</p></header>");

            out.println("<div class='container'><div class='card'>");
            out.println("<h2>Bus Routes</h2>");
            out.println("<table border='1' cellpadding='10'>");
            out.println("<tr><th>Route ID</th><th>Route Name</th>"
                    + "<th>Bus Number</th></tr>");

            String sql = "SELECT r.route_id, r.route_name, b.bus_number "
                    + "FROM routes r LEFT JOIN buses b "
                    + "ON r.bus_id = b.bus_id";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("route_id") + "</td>");
                    out.println("<td>" + rs.getString("route_name") + "</td>");
                    out.println("<td>" + rs.getString("bus_number") + "</td>");
                    out.println("</tr>");
                }

            } catch (Exception e) {
                throw new ServletException("Unable to load bus routes.", e);
            }

            out.println("</table>");
            out.println("<br><a class='btn' href='student-dashboard.jsp'>"
                    + "Back to Dashboard</a>");
            out.println("</div></div></body></html>");
        }
    }
}

