
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
import jakarta.servlet.http.HttpSession;

@WebServlet("/AdminPassServlet")
public class AdminPassServlet extends HttpServlet {
	
	 private static final long serialVersionUID = 1L;
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html><html><head>");
            out.println("<title>Manage Transport Passes</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("</head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>Admin - Manage Transport Passes</p></header>");

            out.println("<div class='container'><div class='card'>");
            out.println("<h2>Student Pass Applications</h2>");

            out.println("<table border='1' cellpadding='8'>");
            out.println("<tr><th>Pass ID</th><th>Student</th>"
                    + "<th>Roll Number</th><th>Boarding Point</th>"
                    + "<th>Start Date</th><th>End Date</th>"
                    + "<th>Status</th><th>Action</th></tr>");

            String sql = "SELECT tp.pass_id, s.name, s.roll_no, "
                    + "bp.point_name, tp.start_date, tp.end_date, tp.status "
                    + "FROM transport_pass tp "
                    + "JOIN students s ON tp.student_id = s.student_id "
                    + "JOIN boarding_points bp ON tp.point_id = bp.point_id "
                    + "ORDER BY tp.pass_id DESC";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int passId = rs.getInt("pass_id");
                    String status = rs.getString("status");

                    out.println("<tr>");
                    out.println("<td>" + passId + "</td>");
                    out.println("<td>" + rs.getString("name") + "</td>");
                    out.println("<td>" + rs.getString("roll_no") + "</td>");
                    out.println("<td>" + rs.getString("point_name") + "</td>");
                    out.println("<td>" + rs.getDate("start_date") + "</td>");
                    out.println("<td>" + rs.getDate("end_date") + "</td>");
                    out.println("<td>" + status + "</td>");
                    out.println("<td>");

                    if ("Pending".equalsIgnoreCase(status)) {
                        out.println("<form action='UpdatePassStatusServlet' "
                                + "method='post' style='display:inline'>");
                        out.println("<input type='hidden' name='pass_id' "
                                + "value='" + passId + "'>");
                        out.println("<button type='submit' name='status' "
                                + "value='Approved'>Approve</button>");
                        out.println("<button type='submit' name='status' "
                                + "value='Rejected'>Reject</button>");
                        out.println("</form>");
                    } else {
                        out.println("No action");
                    }

                    out.println("</td></tr>");
                }
            }

            out.println("</table><br>");
            out.println("<a class='btn secondary' "
                    + "href='admin-dashboard.jsp'>Back to Dashboard</a>");
            out.println("</div></div></body></html>");

        } catch (Exception e) {
            throw new ServletException(
                    "Unable to load transport pass applications.", e);
        }
    }
}

