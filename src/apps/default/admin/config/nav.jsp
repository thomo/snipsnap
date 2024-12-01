<%@ page import="java.util.List"%> <%--
  ** Navigation
  ** @author Matthias L. Jugel
  ** 
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="i18n.setup" scope="page" />

<input type="hidden" name="step" value="<c:out value='${step}'/>"/>
<input type="hidden" name="prefix" value="<c:out value="${prefix}"/>"/>
<c:choose>
  <c:when test="${not empty configuser && configuser.admin}">
    <c:if test="${not(step == 'import' || step == 'export' || step == 'users' || step == 'search' || step == 'maintenance')}">
      <input type="submit" name="save" value="<fmt:message key="config.nav.save"/>"/>
    </c:if>
  </c:when>
  <c:otherwise>
    <c:if test="${step != 'database' && step != 'login'}">
      <input type="submit" name="previous" value="<fmt:message key="config.nav.previous"/>">
    </c:if>
    <c:choose>
      <c:when test="${step == 'finish'}">
        <c:if test="${empty advanced}">
          <input type="hidden" name="advanced" value="true">
          <input disabled="disabled" id="submit.advanced" type="submit" name="next" value="<fmt:message key="config.nav.advanced"/>">
        </c:if>
      </c:when>
      <c:otherwise>
        <input type="submit" name="next" value="<fmt:message key="config.nav.next"/>">
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
