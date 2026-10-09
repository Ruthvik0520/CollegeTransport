package com.transport;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/StudentRegisterServlet")
public class StudentRegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        String name = request.getParameter("name");
        String rollNo = request.getParameter("roll_no");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        int year = Integer.parseInt(request.getParameter("year"));
        String password = request.getParameter("password");

        try {

            Connection con = DBConnection.getConnection();

            String sql = "INSERT INTO students " +
                    "(name, roll_no, email, phone, department, year, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, rollNo);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, department);
            ps.setInt(6, year);
            ps.setString(7, password);

            ps.executeUpdate();

            ps.close();
            con.close();

            response.sendRedirect("login.html");

        } catch (Exception e) {

            e.printStackTrace();

            response.getWriter().println(
                    "Registration Failed: " + e.getMessage()
            );
        }
    }
}