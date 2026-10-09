
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

@WebServlet("/RouteManagementServlet")
public class RouteManagementServlet extends HttpServlet {
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

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
            out.println("<title>Manage Routes</title>");
            out.println("<link rel='stylesheet' href='css/style.css'>");
            out.println("<style>");
            out.println("table{width:100%;border-collapse:collapse;margin-top:20px}");
            out.println("th,td{padding:10px;border:1px solid #ddd;text-align:left}");
            out.println("input,select{max-width:100%;box-sizing:border-box;margin:5px 0}");
            out.println(".actions{display:flex;gap:6px;flex-wrap:wrap}");
            out.println("</style></head><body>");

            out.println("<header><h1>College Transport Management System</h1>");
            out.println("<p>Admin - Route Management</p></header>");
            out.println("<div class='container'><div class='card'>");

            out.println("<h2>Add New Route</h2>");
            out.println("<form method='post' action='RouteManagementServlet'>");
            out.println("<input type='hidden' name='action' value='add'>");
            out.println("<label>Route Name</label>");
            out.println("<input type='text' name='route_name' maxlength='100' required>");
            out.println("<label>Assign Bus</label><select name='bus_id' required>");
            out.println("<option value=''>Select Bus</option>");

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT bus_id, bus_number FROM buses ORDER BY bus_number");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    out.println("<option value='" + rs.getInt("bus_id") + "'>"
                            + escape(rs.getString("bus_number")) + "</option>");
                }
            }

            out.println("</select><br><button class='btn' type='submit'>Add Route</button>");
            out.println("</form><h2>Existing Routes</h2>");
            out.println("<table><tr><th>ID</th><th>Route Name</th>"
                    + "<th>Assigned Bus</th><th>Actions</th></tr>");

            String sql = "SELECT r.route_id, r.route_name, r.bus_id, b.bus_number "
                    + "FROM routes r LEFT JOIN buses b ON r.bus_id=b.bus_id "
                    + "ORDER BY r.route_id";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("route_id");
                    int busId = rs.getInt("bus_id");
                    String routeName = rs.getString("route_name");

                    out.println("<tr><td>" + id + "</td><td>");
                    out.println("<form method='post' action='RouteManagementServlet'>");
                    out.println("<input type='hidden' name='action' value='update'>");
                    out.println("<input type='hidden' name='route_id' value='" + id + "'>");
                    out.println("<input name='route_name' maxlength='100' required value='"
                            + escape(routeName) + "'>");
                    out.println("</td><td><select name='bus_id' required>");

                    try (PreparedStatement bp = con.prepareStatement(
                            "SELECT bus_id, bus_number FROM buses ORDER BY bus_number");
                         ResultSet br = bp.executeQuery()) {

                        while (br.next()) {
                            int optionId = br.getInt("bus_id");
                            String selected = optionId == busId ? " selected" : "";
                            out.println("<option value='" + optionId + "'" + selected + ">"
                                    + escape(br.getString("bus_number")) + "</option>");
                        }
                    }

                    out.println("</select></td><td><div class='actions'>");
                    out.println("<button class='btn' type='submit'>Update</button></form>");
                    out.println("<form method='post' action='RouteManagementServlet' "
                            + "onsubmit=\"return confirm('Delete this route?');\">");
                    out.println("<input type='hidden' name='action' value='delete'>");
                    out.println("<input type='hidden' name='route_id' value='" + id + "'>");
                    out.println("<button class='btn' type='submit'>Delete</button>");
                    out.println("</form></div></td></tr>");
                }
            }

            out.println("</table><br><a class='btn secondary' href='admin-dashboard.jsp'>"
                    + "Back to Admin Dashboard</a>");
            out.println("</div></div></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to load routes.", e);
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
                String name = request.getParameter("route_name");
                int busId = Integer.parseInt(request.getParameter("bus_id"));

                if (name == null || name.isBlank()) {
                    response.sendError(400, "Route name is required.");
                    return;
                }

                String sql;
                if ("add".equals(action)) {
                    sql = "INSERT INTO routes (route_name, bus_id) VALUES (?, ?)";
                } else {
                    int routeId = Integer.parseInt(request.getParameter("route_id"));
                    sql = "UPDATE routes SET route_name=?, bus_id=? WHERE route_id=?";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, name.trim());
                        ps.setInt(2, busId);
                        ps.setInt(3, routeId);
                        ps.executeUpdate();
                    }
                    response.sendRedirect("RouteManagementServlet");
                    return;
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name.trim());
                    ps.setInt(2, busId);
                    ps.executeUpdate();
                }

            } else if ("delete".equals(action)) {
                int routeId = Integer.parseInt(request.getParameter("route_id"));

                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM routes WHERE route_id=?")) {
                    ps.setInt(1, routeId);
                    ps.executeUpdate();
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("RouteManagementServlet");

        } catch (NumberFormatException e) {
            response.sendError(400, "Enter valid route and bus IDs.");
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to manage routes. Check whether the route has boarding points.",
                    e);
        }
    }
}


