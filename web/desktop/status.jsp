<%@page contentType="text/html; charset=UTF-8" errorPage="errorpage.jsp"%>

<html>
<head>
  <title>regain - <search:msg key="status"/></title>
  <script src="regain.js" type="text/javascript"></script>
  <link href="regain.css" rel="stylesheet" type="text/css">
  <status:autoupdate_meta/>
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="status"/></b>
    </td></tr>
    <tr><td>
      <h4><search:msg key="autoUpdate"/></h4>
      <p><status:autoupdate_form url="status.jsp"
            msgAutoupdate="{msg:autoupdate}"
            msgEnable="{msg:enable}"
            msgDisable="{msg:disable}"/></p>

      <h4><search:msg key="currentIndex"/></h4>
      <p><status:indexupdatecontrol url="status.jsp"
            msgBefore="{msg:indexUpdateControl}"
            msgStart="{msg:start}"
            msgPause="{msg:pause}"
            msgResume="{msg:resume}"/></p>
      <p><status:currentindex/></p>

      <h4><search:msg key="runningIndexUpdate"/></h4>
      <p><status:indexupdate/></p>

      <h4><search:msg key="timing"/></h4>
      <pre><status:profiler/></pre>

      <h4><search:msg key="lastLogMessages"/></h4>
      <pre><status:log/></pre>

      <br>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>