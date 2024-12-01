<%--
  ** Snip diff display template.
  ** @author Stephan J. Schmidt
  ** 
  --%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://snipsnap.com/snipsnap" prefix="s" %>

<div class="snip-wrapper">
  <div class="snip-title">
   <h1 class="snip-name">
     <fmt:message key="snip.diff.title"/> <c:out value="${snip}" escapeXml="false"/>
     <fmt:message key="snip.diff.changes" >
       <fmt:param>
         <a href="exec/version?name=<c:out value='${snip.nameEncoded}'/>&amp;version=<c:out value="${oldVersion}"/>">#<c:out value="${oldVersion}"/></a>
       </fmt:param>
       <fmt:param>
         <a href="exec/version?name=<c:out value='${snip.nameEncoded}'/>&amp;version=<c:out value="${newVersion}"/>">#<c:out value="${newVersion}"/></a>
       </fmt:param>
     </fmt:message>
   </h1>
  </div>
  <div class="diff">
    <c:forEach items="${diff}" var="changeInfo">
      <c:choose>
        <c:when test="${changeInfo.type=='DELETE'}">
          <fmt:message key="snip.diff.deleted" >
            <fmt:param value="${changeInfo.from}"/>
          </fmt:message>
          <div class="diff-delete">
            <c:forEach items="${changeInfo.lines}" var="line" varStatus="stat">
              <div class="line"><c:out value="${stat.index+changeInfo.from}"/>: <c:out value="${line}"/></div>
            </c:forEach>
          </div>
        </c:when>
        <c:when test="${changeInfo.type=='INSERT'}">
          <fmt:message key="snip.diff.inserted" >
            <fmt:param value="${changeInfo.from}"/>
          </fmt:message>
          <div class="diff-insert">
            <c:forEach items="${changeInfo.lines}" var="line" varStatus="stat">
              <div class="line"><c:out value="${stat.index+changeInfo.from}"/>: <c:out value="${line}"/></div>
            </c:forEach>
          </div>
        </c:when>
        <c:when test="${changeInfo.type=='CHANGE'}">
          <fmt:message key="snip.diff.changed" >
            <fmt:param value="${changeInfo.from}"/>
          </fmt:message>
          <div class="diff-change">
            <c:forEach items="${changeInfo.lines}" var="line" varStatus="stat">
              <div class="line"><c:out value="${stat.index+changeInfo.from}"/>: <c:out value="${line}"/></div>
            </c:forEach>
          </div>
        </c:when>
        <c:when test="${changeInfo.type=='MOVE'}">
          <fmt:message key="snip.diff.moved" >
            <fmt:param value="${changeInfo.from}"/>
            <fmt:param value="${changeInfo.to}"/>
          </fmt:message>
          <div class="diff-move">
            <c:forEach items="${changeInfo.lines}" var="line" varStatus="stat">
              <div class="line"><c:out value="${stat.index+changeInfo.from}"/>: <c:out value="${line}"/></div>
            </c:forEach>
          </div>
        </c:when>
      </c:choose>
    </c:forEach>
<%--    <pre>--%>
<%--      <c:out value="${diff}"/>--%>
<%--    </pre>--%>
  </div>
  <form class="form" name="f" method="get" action="space/<c:out value='${snip.nameEncoded}'/>">
    <table class="wiki-table">
      <tr>
       <td class="form-buttons">
         <input value="<fmt:message key="dialog.back.to"><fmt:param value="${snip.name}"/></fmt:message>" type="submit"/>
       </td>
     </tr>
   </table>
 </form>
</div>
