
package com.transport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/ApplyPassServlet")
public class ApplyPassServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("studentId") == null) {
            response.sendRedirect("student-login.html");
            return;
        }

        try {
            int studentId = (Integer) session.getAttribute("studentId");

            String pointValue = request.getParameter("pointId");
            String startValue = request.getParameter("startDate");
            String endValue = request.getParameter("endDate");

            if (pointValue == null || pointValue.trim().isEmpty()
                    || startValue == null || startValue.trim().isEmpty()
                    || endValue == null || endValue.trim().isEmpty()) {
                response.sendError(400, "Please complete all fields.");
                return;
            }

            int pointId;

            try {
                pointId = Integer.parseInt(pointValue);
            } catch (NumberFormatException e) {
                response.sendError(400, "Invalid boarding point.");
                return;
            }

            LocalDate startDate;
            LocalDate endDate;

            try {
                startDate = LocalDate.parse(startValue);
                endDate = LocalDate.parse(endValue);
            } catch (DateTimeParseException e) {
                response.sendError(400, "Please enter valid dates.");
                return;
            }

            if (startDate.isBefore(LocalDate.now())
                    || !endDate.isAfter(startDate)) {
                response.sendError(
                    400,
                    "Start date cannot be in the past, and end date must be after start date."
                );
                return;
            }

            try (Connection con = DBConnection.getConnection()) {

                String checkSql =
                    "SELECT COUNT(*) FROM transport_pass " +
                    "WHERE student_id = ? AND point_id = ? " +
                    "AND status IN ('Pending', 'Approved') " +
                    "AND start_date <= ? AND end_date >= ?";

                try (PreparedStatement check =
                         con.prepareStatement(checkSql)) {

                    check.setInt(1, studentId);
                    check.setInt(2, pointId);
                    check.setDate(3,
                        java.sql.Date.valueOf(endDate));
                    check.setDate(4,
                        java.sql.Date.valueOf(startDate));

                    try (ResultSet rs = check.executeQuery()) {
                        rs.next();

                        if (rs.getInt(1) > 0) {
                            response.sendRedirect(
                                "PassStatusServlet?error=duplicate"
                            );
                            return;
                        }
                    }
                }

                String insertSql =
                    "INSERT INTO transport_pass " +
                    "(student_id, point_id, start_date, end_date, status) " +
                    "VALUES (?, ?, ?, ?, 'Pending')";

                try (PreparedStatement ps =
                         con.prepareStatement(insertSql)) {

                    ps.setInt(1, studentId);
                    ps.setInt(2, pointId);
                    ps.setDate(3,
                        java.sql.Date.valueOf(startDate));
                    ps.setDate(4,
                        java.sql.Date.valueOf(endDate));

                    ps.executeUpdate();
                }
            }

            response.sendRedirect("PassStatusServlet?success=applied");

        } catch (Exception e) {
            throw new ServletException(
                "Unable to submit transport pass application.", e
            );
        }
    }
}

