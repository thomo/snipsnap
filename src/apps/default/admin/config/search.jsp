 <%--
  ** Search Engine Parameter settings
  ** @author Matthias L. Jugel
  ** 
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="i18n.setup" scope="page" />

<table>
  <tr>
    <td>
      <fmt:message key="config.search.reset.text"/>
    </td>
    <td>
      <input <c:if test="${indexerThread.alive}">disabled="disabled"</c:if>
        type="submit" name="reset" value="<fmt:message key="config.search.reset"/>"/>
      <c:if test="${indexerThread.alive}">
        <br/><span class="hint"><fmt:message key="config.search.running"/></span>
      </c:if>
    </td>
  </tr>
</table>
