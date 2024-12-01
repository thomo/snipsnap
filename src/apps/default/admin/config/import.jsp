 <%--
  ** Mail Settings
  ** @author Matthias L. Jugel
  ** 
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="i18n.setup" scope="page" />

<table>
  <c:choose>
    <c:when test="${not empty running && not empty running.import}">
      <tr>
        <td>
          <fmt:message key="config.import.running"/>
          <fmt:message key="config.refresh.text"/>
        </td>
        <td>
          <fmt:message key="config.import.status">
            <fmt:param value="${running.current}" />
          </fmt:message>
          <br/>
          <a href="configure?step=import"><fmt:message key="config.refresh"/></a>
        </td>
      </tr>
    </c:when>
    <c:otherwise>
      <tr>
        <td><fmt:message key="config.import.file.text"/></td>
        <td>
          <fmt:message key="config.import.file"/><br/>
          <input type="file" name="import.file" accept="text/xml" value="<c:out value="${importFile}"/>">
          <c:if test="${!empty errors['import.file']}"><img src="images/attention.jpg"></c:if>
        </td>
      </tr>
      <tr>
        <td><fmt:message key="config.import.types.text"/></td>
        <td>
          <c:if test="${!empty errors['import.types']}"><img src="images/attention.jpg"><br/></c:if>
          <input type="checkbox" name="import.types" value="snips"
            <c:if test="${empty importTypes || importTypeSnips == 'true'}">checked="checked"</c:if>>
          <fmt:message key="config.import.types.snips"/><br/>
          <input type="checkbox" name="import.types" value="users"
            <c:if test="${empty importTypes || importTypeUsers == 'true'}">checked="checked"</c:if>>
           <fmt:message key="config.import.types.users"/>
        </td>
      </tr>
      <tr>
        <td><fmt:message key="config.import.overwrite.text"/></td>
        <td>
          <input type="checkbox" name="import.overwrite"
            <c:if test="${empty importOverwrite || importOverwrite == 'true'}">checked="checked"</c:if>>
          <c:if test="${!empty errors['config.import.overwrite']}"><img src="images/attention.jpg"></c:if>
          <fmt:message key="config.import.overwrite"/><br/>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
         <input type="submit" name="import" value="<fmt:message key="config.import"/>">
        </td>
      </tr>
    </c:otherwise>
  </c:choose>
</table>
