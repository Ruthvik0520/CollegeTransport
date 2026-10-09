
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

@WebServlet("/ViewStudentsServlet")
public class ViewStudentsServlet extends HttpServlet {
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

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT student_id, name, roll_no, email, phone, department, year FROM students ORDER BY student_id DESC");
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Registered Students</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");

            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;background:#f2f5fa;padding:20px;}");
            out.println(".container{max-width:1200px;margin:30px auto;background:white;padding:25px;border-radius:10px;overflow-x:auto;}");
            out.println("table{width:100%;border-collapse:collapse;margin-top:20px;}");
            out.println("th,td{border:1px solid #ccc;padding:12px;text-align:left;}");
            out.println("th{background:#172554;color:white;}");
            out.println("tr:nth-child(even){background:#f1f5f9;}");
            out.println("</style></head><body>");

            out.println("<div class='container'>");
            out.println("<h1>Registered Students</h1>");

            out.println("<table>");
            out.println("<thead><tr>");
            out.println("<th>ID</th><th>Name</th><th>Roll No</th>");
            out.println("<th>Email</th><th>Phone</th><th>Department</th><th>Year</th>");
            out.println("</tr></thead><tbody>");

            int count = 0;

            while (rs.next()) {
                count++;
                out.println("<tr>");
                out.println("<td>" + rs.getInt("student_id") + "</td>");
                out.println("<td>" + escapeHtml(rs.getString("name")) + "</td>");
                out.println("<td>" + escapeHtml(rs.getString("roll_no")) + "</td>");
                out.println("<td>" + escapeHtml(rs.getString("email")) + "</td>");
                out.println("<td>" + escapeHtml(rs.getString("phone")) + "</td>");
                out.println("<td>" + escapeHtml(rs.getString("department")) + "</td>");
                out.println("<td>" + rs.getInt("year") + "</td>");
                out.println("</tr>");
            }

            if (count == 0) {
                out.println("<tr><td colspan='7'>No registered students found.</td></tr>");
            }

            out.println("</tbody></table>");
            out.println("<p>Total students: " + count + "</p>");
            out.println("<p><a href='admin-dashboard.jsp'>Back to Dashboard</a></p>");
            out.println("</div></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to load registered students.", e);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}

