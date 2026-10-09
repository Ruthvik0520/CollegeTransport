
package com.transport;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/BoardingPointManagementServlet")
public class BoardingPointManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

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

        try (PrintWriter out = response.getWriter();
             Connection con = DBConnection.getConnection()) {

            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>Manage Boarding Points</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("<style>");
            out.println("table{width:100%;border-collapse:collapse;margin-top:20px}");
            out.println("th,td{padding:10px;border:1px solid #ddd;text-align:left}");
            out.println("input,select{max-width:100%;box-sizing:border-box;margin:5px 0}");
            out.println(".actions{display:flex;gap:6px;flex-wrap:wrap}");
            out.println("</style></head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>Admin - Boarding Point Management</p></header>");
            out.println("<div class='container'><div class='card'>");

            out.println("<h2>Add Boarding Point</h2>");
            out.println("<form method='post' action='BoardingPointManagementServlet'>");
            out.println("<input type='hidden' name='action' value='add'>");
            out.println("<label>Boarding Point Name</label>");
            out.println("<input type='text' name='point_name' maxlength='100' required>");
            out.println("<label>Route</label><select name='route_id' required>");
            out.println("<option value=''>Select Route</option>");

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT route_id, route_name FROM routes ORDER BY route_name");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.println("<option value='" + rs.getInt("route_id") + "'>"
                            + escape(rs.getString("route_name")) + "</option>");
                }
            }

            out.println("</select><br><label>Pickup Time</label>");
            out.println("<input type='time' name='pickup_time' required>");
            out.println("<button class='btn' type='submit'>Add Boarding Point</button>");
            out.println("</form>");

            out.println("<h2>Existing Boarding Points</h2>");
            out.println("<table><tr><th>ID</th><th>Point Name</th>");
            out.println("<th>Route</th><th>Pickup Time</th><th>Actions</th></tr>");

            String sql = "SELECT p.point_id, p.point_name, p.route_id, "
                    + "p.pickup_time, r.route_name FROM boarding_points p "
                    + "JOIN routes r ON p.route_id=r.route_id "
                    + "ORDER BY p.point_id";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int pointId = rs.getInt("point_id");
                    int routeId = rs.getInt("route_id");
                    String pointName = rs.getString("point_name");
                    Time pickupTime = rs.getTime("pickup_time");

                    out.println("<tr><td>" + pointId + "</td><td>");
                    out.println("<form method='post' action='BoardingPointManagementServlet'>");
                    out.println("<input type='hidden' name='action' value='update'>");
                    out.println("<input type='hidden' name='point_id' value='" + pointId + "'>");
                    out.println("<input name='point_name' maxlength='100' required value='"
                            + escape(pointName) + "'>");
                    out.println("</td><td><select name='route_id' required>");

                    try (PreparedStatement rp = con.prepareStatement(
                            "SELECT route_id, route_name FROM routes ORDER BY route_name");
                         ResultSet rr = rp.executeQuery()) {

                        while (rr.next()) {
                            int optionId = rr.getInt("route_id");
                            String selected = optionId == routeId ? " selected" : "";
                            out.println("<option value='" + optionId + "'" + selected + ">"
                                    + escape(rr.getString("route_name")) + "</option>");
                        }
                    }

                    out.println("</select></td><td>");
                    out.println("<input type='time' name='pickup_time' required value='"
                            + pickupTime.toString().substring(0, 5) + "'>");
                    out.println("</td><td><div class='actions'>");
                    out.println("<button class='btn' type='submit'>Update</button></form>");

                    out.println("<form method='post' action='BoardingPointManagementServlet' "
                            + "onsubmit=\"return confirm('Delete this boarding point?');\">");
                    out.println("<input type='hidden' name='action' value='delete'>");
                    out.println("<input type='hidden' name='point_id' value='" + pointId + "'>");
                    out.println("<button class='btn' type='submit'>Delete</button>");
                    out.println("</form></div></td></tr>");
                }
            }

            out.println("</table><br>");
            out.println("<a class='btn secondary' href='admin-dashboard.jsp'>"
                    + "Back to Admin Dashboard</a>");
            out.println("</div></div></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to load boarding points.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        try (Connection con = DBConnection.getConnection()) {

            if ("add".equals(action) || "update".equals(action)) {
                String name = request.getParameter("point_name");
                int routeId = Integer.parseInt(request.getParameter("route_id"));
                Time time = Time.valueOf(request.getParameter("pickup_time") + ":00");

                if (name == null || name.isBlank()) {
                    response.sendError(400, "Boarding point name is required.");
                    return;
                }

                if ("add".equals(action)) {
                    String sql = "INSERT INTO boarding_points "
                            + "(point_name, route_id, pickup_time) VALUES (?, ?, ?)";

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name.trim());
                        ps.setInt(2, routeId);
                        ps.setTime(3, time);
                        ps.executeUpdate();
                    }
                } else {
                    int pointId = Integer.parseInt(request.getParameter("point_id"));

                    String sql = "UPDATE boarding_points SET point_name=?, "
                            + "route_id=?, pickup_time=? WHERE point_id=?";

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name.trim());
                        ps.setInt(2, routeId);
                        ps.setTime(3, time);
                        ps.setInt(4, pointId);
                        ps.executeUpdate();
                    }
                }

            } else if ("delete".equals(action)) {
                int pointId = Integer.parseInt(request.getParameter("point_id"));

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM boarding_points WHERE point_id=?")) {
                    ps.setInt(1, pointId);
                    ps.executeUpdate();
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("BoardingPointManagementServlet");

        } catch (IllegalArgumentException e) {
            response.sendError(400, "Enter valid boarding point details.");
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to manage boarding points. Check whether this point is used by a pass.",
                    e);
        }
    }
}

