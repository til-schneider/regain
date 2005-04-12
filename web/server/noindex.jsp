<%@page contentType="text/html" errorPage="errorpage.jsp"%>
<%@taglib uri="regain-search.tld" prefix="search" %>

<html>
<head>
  <title>regain - <search:msg key="noIndex"/></title>
  <script src="regain.js" type="text/javascript"></script>
  <link href="regain.css" rel="stylesheet" type="text/css">
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="noIndex"/></b>
    </td></tr>
    <tr><td>
      <h4><search:msg key="noIndex"/></h4>
      
      <p><search:msg key="noIndex.help"/></p>
      <br>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>