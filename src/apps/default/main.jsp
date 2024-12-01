 <%--
  ** Main layout template.
  ** @author Matthias L. Jugel
  ** 
  --%>

<%@ page import="snipsnap.api.snip.SnipSpace,
                 snipsnap.api.app.Application,
                 snipsnap.api.container.Components,
                 snipsnap.api.snip.Snip,
                 java.util.Collection,
                 java.util.Iterator,
                 org.snipsnap.snip.label.TypeLabel"%>
<%@ page pageEncoding="iso-8859-1" %>
<% response.setContentType("text/html; charset="+Application.get().getConfiguration().getEncoding()); %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://snipsnap.com/snipsnap" prefix="s" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="<c:out value='${app.configuration.locale}'/>" xml:lang="<c:out value='${app.configuration.locale}'/>">
 <head>
  <!-- base of this document to make all links relative -->
  <base href="<c:out value='${app.configuration.url}/'/>"/>
  <!-- content type and generator -->
  <meta http-equiv="Content-Type" content="text/html; charset=<c:out value='${app.configuration.encoding}'/>"/>
  <meta http-equiv="Generator" content="SnipSnap/<c:out value="${app.configuration.version}"/>"/>
  <s:geoUrl/>
  <!-- aggregrator related info -->
  <link rel="EditURI" type="application/rsd+xml" title="RSD" href="<c:out value='${app.configuration.url}/exec/rsd'/>"/>
  <c:choose>
   <c:when test="${snip.notWeblog}">
    <link rel="alternate" type="application/rss+xml" title="RSS" href="<c:out value='${app.configuration.url}/exec/rss'/>"/>
   </c:when>
   <c:otherwise>
    <link rel="alternate" type="application/rss+xml" title="RSS" href="<c:out value='${app.configuration.url}/exec/rss?snip=${snip.nameEncoded}'/>"/>
   </c:otherwise>
  </c:choose>
  <link rel="index" href="<c:out value='${app.configuration.url}/space/snipsnap-index'/>"/>
  <!-- icons and stylesheet -->
  <link rel="shortcut icon" href="<c:out value='${app.configuration.url}/favicon.ico'/>"/>
  <link rel="icon" href="<c:out value='${app.configuration.url}/favicon.ico'/>"/>
  <link rel="STYLESHEET" type="text/css" href="<c:out value='${app.configuration.url}/theme/default.css'/>" />
  <link rel="STYLESHEET" type="text/css" href="<c:out value='${app.configuration.url}/theme/print.css'/>" media="print" />
  <!-- title of this document -->
  <title><c:out value="${app.configuration.name}" default="SnipSnap"/> :: <c:out value="${snip.name}"/></title>
 </head>
 <body>
  <div id="page-logo">
   <c:choose>
    <c:when test="${snip.name==app.configuration.startSnip && not(empty app.configuration.logo)}"><s:image root="SnipSnap/config" name="${app.configuration.logo}" alt="${app.configuration.name}"/></c:when>
    <c:when test="${snip.name!=app.configuration.startSnip && not(empty app.configuration.logo)}"><a href="<c:out value='${app.configuration.url}'/>" accesskey="1"><s:image root="SnipSnap/config" name="${app.configuration.logo}" alt="${app.configuration.name}"/></a></c:when>
    <c:when test="${snip.name==app.configuration.startSnip && empty app.configuration.logo}"><c:out value="${app.configuration.name}" default="SnipSnap"/></c:when>
    <c:otherwise><a href="<c:out value='${app.configuration.url}'/>" accesskey="1"><c:out value="${app.configuration.name}" default="SnipSnap"/></a></c:otherwise>
   </c:choose>
  </div>
  <div id="page-title">
   <div id="page-tagline"><c:out value="${app.configuration.tagline}"/></div>
   <div id="page-buttons"><c:import url="util/mainbuttons.jsp"/></div>
  </div>
  <div id="page-wrapper">
   <div id="page-content">
    <c:import url="${page}"/>
    <s:debug/>
   </div>
   <%
     SnipSpace space = (SnipSpace) Components.getComponent(SnipSpace.class);
     for(int i = 1; space.exists("snipsnap-portlet-"+i) || space.exists("SnipSnap/portlet/"+i); i++) {
       Snip snip = space.load("snipsnap-portlet-"+i);
       if(null == snip) {
         snip = space.load("SnipSnap/portlet/" + i);
       }
       pageContext.setAttribute("portlet", snip);
       pageContext.removeAttribute("view_handler");
       pageContext.removeAttribute("mime_type");

           String viewHandler;
           String type;
           Collection mimeTypes = snip.getLabels().getLabels("TypeLabel");
           if (!mimeTypes.isEmpty()) {
             Iterator handlerIt = mimeTypes.iterator();
             while (handlerIt.hasNext()) {
               TypeLabel typeLabel = (TypeLabel) handlerIt.next();
               viewHandler = typeLabel.getViewHandler();
               // search for default handler if non found
               if (null == viewHandler) {
                 viewHandler = TypeLabel.getViewHandler(typeLabel.getTypeValue());
               }

               if (null != viewHandler) {
                 type = typeLabel.getTypeValue();
                 pageContext.setAttribute("view_handler", viewHandler);
                 pageContext.setAttribute("mime_type", type);
                 break;
               }
             }
           }

   %>
    <div id="page-portlet-<%=i%>-wrapper">
     <div id="page-portlet-<%=i%>">
       <%-- if there is a special view handler, use it, else display standard page --%>
       <c:choose>
         <c:when test="${not empty(view_handler)}">
           <c:catch var="error">
             <c:import url="/plugin/${view_handler}"/>
           </c:catch>
         </c:when>
         <c:otherwise>
           <c:out value="${portlet.XMLContent}" escapeXml="false" />
         </c:otherwise>
       </c:choose>
     </div>
    </div>
   <% } %>
  </div>
  <div id="page-bottom"><s:snip name="snipsnap-copyright"/></div>
 </body>
</html>