<!--
  ** Template for redirection the root page to the start page
  ** @author Matthias L. Jugel
  ** 
  -->

<% response.sendRedirect(request.getContextPath() + "/"); return; %>