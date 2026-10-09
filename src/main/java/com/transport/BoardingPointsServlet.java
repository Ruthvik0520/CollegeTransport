
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

@WebServlet("/BoardingPointsServlet")
public class BoardingPointsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<html><head><title>Boarding Points</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("</head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>Boarding Points and Pickup Times</p></header>");

            out.println("<div class='container'><div class='card'>");
            out.println("<h2>Available Boarding Points</h2>");

            out.println("<table border='1' cellpadding='10'>");
            out.println("<tr><th>Point ID</th><th>Boarding Point</th>"
                    + "<th>Route</th><th>Pickup Time</th></tr>");

            String sql = "SELECT p.point_id, p.point_name, "
                    + "r.route_name, p.pickup_time "
                    + "FROM boarding_points p "
                    + "JOIN routes r ON p.route_id = r.route_id "
                    + "ORDER BY p.pickup_time";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rs.getInt("point_id") + "</td>");
                    out.println("<td>" + rs.getString("point_name") + "</td>");
                    out.println("<td>" + rs.getString("route_name") + "</td>");
                    out.println("<td>" + rs.getTime("pickup_time") + "</td>");
                    out.println("</tr>");
                }

            } catch (Exception e) {
                throw new ServletException(
                        "Unable to load boarding points.", e);
            }

            out.println("</table>");
            out.println("<br><a class='btn' href='student-dashboard.jsp'>"
                    + "Back to Dashboard</a>");
            out.println("</div></div></body></html>");
        }
    }
}

