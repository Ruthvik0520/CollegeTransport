
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%
    if (session.getAttribute("adminId") == null) {
        response.sendRedirect("admin-login.html");
        return;
    }
    String adminUsername = (String) session.getAttribute("adminUsername");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header>
        <h1>College Transport Management System</h1>
        <p>Admin Dashboard</p>
    </header>

    <div class="container">
        <div class="card">
            <h2>Welcome, <%= adminUsername %>!</h2>
            <p>Manage college transport operations.</p>

            <a class="btn" href="AdminPassServlet">
                Manage Transport Passes
            </a>
            
<a class="btn" href="BusManagementServlet">
    Manage Buses
</a>

<a class="btn" href="RouteManagementServlet">
    Manage Routes
</a>

<a class="btn" href="BoardingPointManagementServlet">
    Manage Boarding Points
</a>

<a class="btn" href="ViewStudentsServlet">View Registered Students</a>




            <a class="btn" href="${pageContext.request.contextPath}/LogoutServlet">
    Logout
</a>
        </div>
    </div>
</body>
</html>

