
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

@WebServlet("/BusManagementServlet")
public class BusManagementServlet extends HttpServlet {
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
        response.sendRedirect(
            request.getContextPath() + "/admin-login.html");
        return;
    }

    response.setContentType("text/html;charset=UTF-8");

    String context = request.getContextPath();

    try (PrintWriter out = response.getWriter()) {

        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>Manage Buses | College Transport</title>");
        out.println("<link rel='stylesheet' href='" + context + "/css/style.css'>");
        out.println("</head><body>");

        out.println("<header>");
        out.println("<h1>College Transport Management System</h1>");
        out.println("<p>Administrator / Manage Buses</p>");
        out.println("</header>");

        out.println("<main class='container'>");

        out.println("<h2>Manage Buses</h2>");
        out.println("<p>Add new buses, update existing details, or remove buses from the system.</p>");

        out.println("<h2>Add New Bus</h2>");

        out.println("<form class='bus-form' method='post' action='"
                + context + "/BusManagementServlet'>");

        out.println("<input type='hidden' name='action' value='add'>");

        out.println("<div>");
        out.println("<label for='newBusNumber'>Bus Number</label>");
        out.println("<input id='newBusNumber' type='text' name='bus_number' "
                + "required maxlength='30' placeholder='Enter bus number'>");
        out.println("</div>");

        out.println("<div>");
        out.println("<label for='newDriverName'>Driver Name</label>");
        out.println("<input id='newDriverName' type='text' name='driver_name' "
                + "required maxlength='100' placeholder='Enter driver name'>");
        out.println("</div>");

        out.println("<div>");
        out.println("<label for='newCapacity'>Bus Capacity</label>");
        out.println("<input id='newCapacity' type='number' name='capacity' "
                + "min='1' required placeholder='Enter capacity'>");
        out.println("</div>");

        out.println("<button type='submit'>+ Add Bus</button>");
        out.println("</form>");

        out.println("<h2>Existing Buses</h2>");
        out.println("<div class='table-wrapper'>");
        out.println("<table>");
        out.println("<thead><tr>");
        out.println("<th>ID</th>");
        out.println("<th>Bus Number</th>");
        out.println("<th>Driver Name</th>");
        out.println("<th>Capacity</th>");
        out.println("<th>Actions</th>");
        out.println("</tr></thead><tbody>");

        String sql = "SELECT bus_id, bus_number, driver_name, capacity "
                   + "FROM buses ORDER BY bus_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("bus_id");
                String number = rs.getString("bus_number");
                String driver = rs.getString("driver_name");
                int capacity = rs.getInt("capacity");

                String formId = "updateBus" + id;

                out.println("<tr>");

                out.println("<td>" + id + "</td>");

                out.println("<td><input form='" + formId
                        + "' name='bus_number' required maxlength='30' value='"
                        + escape(number) + "'></td>");

                out.println("<td><input form='" + formId
                        + "' name='driver_name' required maxlength='100' value='"
                        + escape(driver) + "'></td>");

                out.println("<td><input form='" + formId
                        + "' type='number' name='capacity' min='1' required value='"
                        + capacity + "'></td>");

                out.println("<td><div class='actions'>");

                out.println("<form id='" + formId
                        + "' method='post' action='" + context
                        + "/BusManagementServlet'>");
                out.println("<input type='hidden' name='action' value='update'>");
                out.println("<input type='hidden' name='bus_id' value='"
                        + id + "'>");
                out.println("<button type='submit'>Update</button>");
                out.println("</form>");

                out.println("<form method='post' action='" + context
                        + "/BusManagementServlet' "
                        + "onsubmit=\"return confirm('Delete this bus?');\">");
                out.println("<input type='hidden' name='action' value='delete'>");
                out.println("<input type='hidden' name='bus_id' value='"
                        + id + "'>");
                out.println("<button type='submit' class='btn-danger'>Delete</button>");
                out.println("</form>");

                out.println("</div></td>");
                out.println("</tr>");
            }

        } catch (Exception e) {
            throw new ServletException("Unable to load buses.", e);
        }

        out.println("</tbody></table></div>");

        out.println("<p style='margin-top:24px'>");
        out.println("<a class='btn secondary' href='" + context
                + "/admin-dashboard.jsp'>Back to Admin Dashboard</a>");
        out.println("</p>");

        out.println("</main>");
        out.println("<footer>College Transport Pass &amp; Boarding Management System</footer>");
        out.println("</body></html>");
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

                String number = request.getParameter("bus_number");
                String driver = request.getParameter("driver_name");
                int capacity = Integer.parseInt(request.getParameter("capacity"));

                if (number == null || number.isBlank()
                        || driver == null || driver.isBlank()
                        || capacity < 1) {
                    response.sendError(400, "Enter valid bus details.");
                    return;
                }

                if ("add".equals(action)) {
                    String sql = "INSERT INTO buses "
                               + "(bus_number, driver_name, capacity) "
                               + "VALUES (?, ?, ?)";

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, number.trim());
                        ps.setString(2, driver.trim());
                        ps.setInt(3, capacity);
                        ps.executeUpdate();
                    }

                } else {
                    int id = Integer.parseInt(request.getParameter("bus_id"));

                    String sql = "UPDATE buses SET bus_number=?, "
                               + "driver_name=?, capacity=? WHERE bus_id=?";

                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setString(1, number.trim());
                        ps.setString(2, driver.trim());
                        ps.setInt(3, capacity);
                        ps.setInt(4, id);
                        ps.executeUpdate();
                    }
                }

            } else if ("delete".equals(action)) {

                int id = Integer.parseInt(request.getParameter("bus_id"));

                String sql = "DELETE FROM buses WHERE bus_id=?";

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

            } else {
                response.sendError(400, "Invalid action.");
                return;
            }

            response.sendRedirect("BusManagementServlet");

        } catch (NumberFormatException e) {
            response.sendError(400, "Enter a valid capacity or bus ID.");
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to manage buses. Check whether the bus is used by a route.",
                    e);
        }
    }
}

