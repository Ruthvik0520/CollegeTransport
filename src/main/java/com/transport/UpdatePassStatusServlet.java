
package com.transport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UpdatePassStatusServlet")
public class UpdatePassStatusServlet extends HttpServlet {
	 private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("admin-login.html");
            return;
        }

        try {
            int passId = Integer.parseInt(request.getParameter("pass_id"));
            String status = request.getParameter("status");

            if (!"Approved".equals(status) && !"Rejected".equals(status)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid status.");
                return;
            }

            String sql = "UPDATE transport_pass SET status = ? "
                    + "WHERE pass_id = ? AND status = 'Pending'";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, status);
                ps.setInt(2, passId);
                ps.executeUpdate();
            }

            response.sendRedirect("AdminPassServlet");

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid pass ID.");
        } catch (Exception e) {
            throw new ServletException(
                    "Unable to update transport pass status.", e);
        }
    }
}

