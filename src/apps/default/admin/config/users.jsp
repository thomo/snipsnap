<%@ page import="java.util.*,
                 snipsnap.api.config.Configuration,
                 snipsnap.api.app.Application,
                 snipsnap.api.container.Components,
                 org.snipsnap.user.UserManager"%>
 <%--
  ** User management
  ** @author Matthias L. Jugel
  ** 
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="i18n.setup" scope="page" />

<c:choose>
  <c:when test="${not empty edit}">
    <c:import url="config/users.edit.jsp"/>
  </c:when>
  <c:otherwise>
    <div class="users">
      <c:import url="config/users.list.jsp"/>
    </div>
  </c:otherwise>
</c:choose>
