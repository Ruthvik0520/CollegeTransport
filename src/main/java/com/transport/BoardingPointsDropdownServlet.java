
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

@WebServlet("/BoardingPointsDropdownServlet")
public class BoardingPointsDropdownServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("student-login.html");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                 "SELECT bp.point_id, bp.point_name, bp.pickup_time, r.route_name " +
                 "FROM boarding_points bp JOIN routes r ON bp.route_id = r.route_id " +
                 "ORDER BY r.route_name, bp.pickup_time");
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html><html><head>");
            out.println("<title>Apply for Transport Pass</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");

            out.println("<style>");
            out.println("body{font-family:Arial,sans-serif;background:#f1f5f9;padding:20px;}");
            out.println(".container{max-width:520px;margin:35px auto;background:white;padding:28px;border-radius:12px;box-shadow:0 4px 15px #0001;}");
            out.println("label{display:block;margin-top:15px;font-weight:bold;}");
            out.println("input,select{width:100%;padding:11px;margin-top:7px;border:1px solid #cbd5e1;border-radius:6px;box-sizing:border-box;}");
            out.println("button{margin-top:22px;width:100%;padding:12px;background:#172554;color:white;border:0;border-radius:6px;cursor:pointer;}");
            out.println("</style></head><body>");

            out.println("<div class='container'>");
            out.println("<h2>Apply for Transport Pass</h2>");
            out.println("<form action='ApplyPassServlet' method='post'>");

            out.println("<label for='pointId'>Select Boarding Point</label>");
            out.println("<select id='pointId' name='pointId' required>");
            out.println("<option value=''>-- Select Boarding Point --</option>");

            int count = 0;

            while (rs.next()) {
                count++;
                int pointId = rs.getInt("point_id");
                String pointName = escapeHtml(rs.getString("point_name"));
                String routeName = escapeHtml(rs.getString("route_name"));
                String pickupTime = escapeHtml(rs.getString("pickup_time"));

                out.println("<option value='" + pointId + "'>" +
                    pointName + " - " + routeName + " (Pickup: " +
                    pickupTime + ")</option>");
            }

            out.println("</select>");

            if (count == 0) {
                out.println("<p>No boarding points available. Please contact the administrator.</p>");
            } else {
            	
            	out.println("<label for='startDate'>Pass Start Date</label>");
            	out.println("<input type='date' id='startDate' name='startDate' required>");

            	out.println("<label for='endDate'>Pass End Date</label>");
            	out.println("<input type='date' id='endDate' name='endDate' required>");

            	out.println("<script>");
            	out.println("const start = document.getElementById('startDate');");
            	out.println("const end = document.getElementById('endDate');");
            	out.println("const today = new Date();");
            	out.println("const todayString = today.getFullYear() + '-' +");
            	out.println("String(today.getMonth()+1).padStart(2,'0') + '-' +");
            	out.println("String(today.getDate()).padStart(2,'0');");
            	out.println("start.min = todayString;");
            	out.println("end.min = todayString;");
            	out.println("start.addEventListener('change', function(){");
            	out.println("  end.min = start.value || todayString;");
            	out.println("  if(end.value && end.value <= start.value) end.value = '';");
            	out.println("});");
            	out.println("</script>");

            	out.println("<button type='submit'>Apply for Pass</button>");
            	

            }

            out.println("</form>");
            out.println("<p><a href='student-dashboard.jsp'>Back to Dashboard</a></p>");
            out.println("</div></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to load boarding points.", e);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}

