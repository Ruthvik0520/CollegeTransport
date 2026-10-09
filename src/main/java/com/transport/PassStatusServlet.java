
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

@WebServlet("/PassStatusServlet")
public class PassStatusServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int studentId = (Integer) session.getAttribute("studentId");

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html><html><head>");
            out.println("<title>My Transport Passes</title>");
            
            String success = request.getParameter("success");

            if ("applied".equals(success)) {
                out.println("<p style='color:#166534;background:#dcfce7;padding:12px;border-radius:6px;'>");
                out.println("Transport pass application submitted successfully! Your application is Pending admin approval.");
                out.println("</p>");
            }

            String error = request.getParameter("error");

            if ("duplicate".equals(error)) {
                out.println("<p style='color:#b91c1c;background:#fee2e2;padding:12px;border-radius:6px;'>");
                out.println("You already have a pending or approved pass for overlapping dates at this boarding point.");
                out.println("</p>");
            }
            

            
           

            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("</head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>My Transport Pass Applications</p></header>");

            out.println("<div class='container'><div class='card'>");
            out.println("<h2>Application Status</h2>");
            out.println("<table border='1' cellpadding='10'>");
            out.println("<tr><th>Pass ID</th><th>Boarding Point</th>"
                    + "<th>Start Date</th><th>End Date</th>"
                    + "<th>Status</th></tr>");

            String sql = "SELECT tp.pass_id, bp.point_name, "
                    + "tp.start_date, tp.end_date, tp.status "
                    + "FROM transport_pass tp "
                    + "JOIN boarding_points bp ON tp.point_id = bp.point_id "
                    + "WHERE tp.student_id = ? ORDER BY tp.pass_id DESC";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, studentId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("pass_id") + "</td>");
                        out.println("<td>" + rs.getString("point_name") + "</td>");
                        out.println("<td>" + rs.getDate("start_date") + "</td>");
                        out.println("<td>" + rs.getDate("end_date") + "</td>");
                        out.println("<td>" + rs.getString("status") + "</td>");
                        out.println("</tr>");
                    }
                }
            }

            out.println("</table><br>");
            out.println("<a class='btn' href='apply-pass.html'>Apply Again</a>");
            out.println("<a class='btn secondary' href='student-dashboard.jsp'>"
                    + "Back to Dashboard</a>");
            out.println("</div></div></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to load pass status.", e);
        }
    }
}

