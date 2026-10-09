
package com.transport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String rollNo = request.getParameter("roll_no");
        String password = request.getParameter("password");

        String sql = "SELECT student_id, name FROM students "
                   + "WHERE roll_no = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNo);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    HttpSession session = request.getSession();
                    session.setAttribute("studentId",
                                         rs.getInt("student_id"));
                    session.setAttribute("studentName",
                                         rs.getString("name"));

                    response.sendRedirect(
                        "student-dashboard.jsp"
                    );

                } else {

                    response.sendRedirect(
                        "login.html?error=invalid"
                    );
                }
            }

        } catch (Exception e) {
            throw new ServletException(
                "Unable to log in. Check the server log.", e
            );
        }
    }
}
