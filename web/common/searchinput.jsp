<%@page contentType="text/html" errorPage="errorpage.jsp"%>
<%@taglib uri="regain-search.tld" prefix="search" %>

<html>
<head>
  <title>regain - <search:msg key="search"/></title>
  <script src="regain.js" type="text/javascript"></script>
  <link href="regain.css" rel="stylesheet" type="text/css">
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="search"/></b>
    </td></tr>
    <tr><td>

      <form name="search" action="search.jsp" method="get">
        <p class="searchinput">
          <b><search:msg key="searchFor"/>: </b>
          <input name="query" size="40"/>
          <search:input_submit text="{msg:search}"/>
        </p>
      </form>

      <br>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>
