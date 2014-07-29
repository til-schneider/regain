<%@page contentType="text/html; charset=UTF-8" errorPage="errorpage.jsp"%>

<html>
<head>
  <title>regain - <search:msg key="welcome"/></title>
  <script src="regain.js" type="text/javascript"></script>
  <link href="regain.css" rel="stylesheet" type="text/css">
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="welcome"/></b>
    </td></tr>
    <tr><td>
      <h4><search:msg key="welcomeToRegain"/></h4>

      <p><search:msg key="welcome.help.taskbar"/></p>
      <img src="img/taskbar.gif"/>
      <p><search:msg key="welcome.help.rightClick"/></p>
      <p><search:msg key="welcome.help.configPage"/></p>
      <br>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>