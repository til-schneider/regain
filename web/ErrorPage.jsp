<%@page contentType="text/html"%>
<%@taglib uri="SearchLib.tld" prefix="search" %>

<html>
<head>
  <title>Fehler bei Suche nach <%= (request.getParameter("query") == null) ? "" : request.getParameter("query")%></title>
</head>

<body>

  <%
  Throwable exc = (Throwable) request.getAttribute("javax.servlet.jsp.jspException");
  %>

  <!--
  Stacktrace:
  <%
  exc.printStackTrace(new java.io.PrintWriter(out));
  %>
  -->

  <p>
  Ihre Suchanfrage konnte nicht verarbeitet werden.<br>
  Bitte &uuml;berpr&uuml;fen Sie Ihre Eingabe.<br>
  <br>
  Fehlermeldung:
  <%
  out.print(exc.getMessage());
  %>
  <br>
  Weitere Informationen über die Eingabemöglichkeiten finden Sie
  <a href="http://jakarta.apache.org/lucene/docs/queryparsersyntax.html">hier</a>.<br>
  </p>

  <form name="search" action="SearchOutput.jsp" method="get">
    <p>
      <b>Suchen nach: </b>
      <input name="index" type="hidden"
             value="<%= (request.getParameter("index") == null) ? "main" : request.getParameter("index")%>">
      <search:input_query/>
      <search:input_maxresults/>
      <input type="submit" value="Search"/>
    </p>
  </form>

</body>
</html>
