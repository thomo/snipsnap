<%@ page import="com.neotis.snip.SnipSpace,
                 java.util.Iterator,
                 com.neotis.snip.Snip,
                 com.neotis.date.Month"%>
 <table class="menu" width="100%" border="0" cellpadding="4" cellspacing="1">
 <tr><td class="menuitem">Start<td></tr>
 <tr><td class="menuitem">index<td></tr>
 <tr><td class="menuitem">search<td></tr>
 <table>
  <tr><td><b>Recent changes:</b>
  <p>
  <% SnipSpace space = SnipSpace.getInstance();
   Iterator iterator = space.getChanged().iterator();
   while (iterator.hasNext()) {
     Snip snip = (Snip)iterator.next();
  %>
  <tr><td><a href="/space/<%= snip.getName() %>"><%= snip.getName() %></a></td></tr>
  <%
   }
  %>
  </p>
  </td></tr>
  </table>

  <p>
  <% Month m = new Month(); %>
  <%= m.getView(05,2002) %>
  </p>

  <p>
  <% Snip rolling = space.load("weblog::blogrolling"); %>
  <%= rolling.toXML() %>
  </p>

</table>


