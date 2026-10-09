
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%
    if (session.getAttribute("studentId") == null) {
        response.sendRedirect("login.html");
        return;
    }

    String studentName =
        (String) session.getAttribute("studentName");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Student Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

<header>
    <h1>College Transport Management System</h1>
    <p>Student Dashboard</p>
</header>

<div class="container">
    <div class="card">
        <h2>Welcome, <%= studentName %>!</h2>
        <p>Manage your college transport details here.</p>

        <a class="btn" href="routes.html">View Routes</a>
        <a class="btn" href="boarding-points.html">View Boarding Points</a>
<a class="btn" href="BoardingPointsDropdownServlet">Apply for Transport Pass</a>


        <a class="btn" href="LogoutServlet">Logout</a>
    </div>
</div>

</body>
</html>

