<%@ page import="java.util.Set"%>
 <!--
  ** User Information display/edit.
  ** @author Matthias L. Jugel
  ** @version $Id$
  -->

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<c:import url="/admin/menu.jsp"/>

<c:forEach items="${errors}" var="error">
  <div class="error"><c:out value="${error.value}"/></div>
</c:forEach>

<form method="POST" action="<c:url value='/exec/admin/user'/>">
 <input type="hidden" name="command" value="update"/>
 <table class="snip-table" border="0" cellspacing="0" cellpadding="2">
  <tr><td class="snip-table-header" colspan="2">User Information</td></tr>
  <tr>
    <td>User Name: </td><td><input name="login" type="hidden" size="20" value="<c:out value='${user.login}'/>"/><c:out value="${user.login}"/></td>
  </tr>
  <tr>
    <td>Email: </td><td><input name="email" type="text" size="20" value="<c:out value='${user.email}'/>" tabindex="0"/></td>
  </tr>
  <tr class="snip-table-header"><td colspan="2">Change Password<br/>
    <c:if test="${user.admin}">
        <i>Attention:</i> Changing the admin password does not change the database password!
    </c:if>
  </td></tr>
  <tr <c:if test="${errors['password'] != null}">class="error-position"</c:if>>
    <td>New Password: </td><td><input name="password.new" type="password" size="20" value="" tabindex="0"/></td></tr>
  <tr <c:if test="${errors['password'] != null}">class="error-position"</c:if>>
    <td>New Password (verification): </td><td><input name="password2.new" type="password" size="20" value="" tabindex="0"/></td></tr>
  <tr class="snip-table-header"><td colspan="2">Roles and Status</td></tr>
  <tr>
    <td>Status: </td><td><input name="status" type="text" size="20" value="<c:out value='${user.status}'/>" tabindex="0"/></td>
  </tr>
  <tr <c:if test="${errors['roles'] != null}">class="error-position"</c:if>>
    <td valign="top">Roles: </td>
    <!-- TODO: use checkboxes here ... -->
    <td>
      <c:set var="userRoles" value="${user.roles.roleSet}"/>
      <% Set userRoles = (Set)pageContext.findAttribute("userRoles"); %>
      <c:forEach items="${user.roles.allRoles}" var="role">
        <input type="checkbox" name="roles" value="<c:out value='${role}'/>" <%= userRoles.contains(pageContext.findAttribute("role")) ? "checked=\"checked\"" : "" %>/>
        <c:out value="${role}"/><br/>
      </c:forEach>
    </td>
  </tr>
  <tr><td colspan="2">
   <input value="Update User" name="ok" type="submit" tabindex="0"/>
   <input value="Cancel" name="cancel" type="submit" tabindex="0"/>
  </td></tr>
 </table>
</form>
