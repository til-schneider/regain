<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="regain-search.tld" prefix="search" %>

<html>
<head>
  <title><search:msg key="errorSearchingFor"/> <search:stats_query/></title>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <link href="regain.css" rel="stylesheet" type="text/css">
  <status:autoupdate_meta/>
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <search:msg key="errorSearchingFor"/> <b><search:stats_query/></b>
    </td></tr>
    <tr><td>
      <p>
      <br/>
      <search:msg key="error.checkInput"/>
      </p>
      <p>
      <search:msg key="errorMessage"/>: <code><search:error_message/></code>
	  <%-- Add the stack trace as hidden text --%>
	  <div style="display:none; color:grey; " id="stacktrace">
	  Stacktrace:
	  <pre><search:msg key="errorMessage"/>:<search:error_stacktrace/></pre>
	  </div>
	  <small><a href="#" onclick="document.getElementById('stacktrace').style.display=''; return false;"><search:msg key="error.showDetails"/></a></small>
      </p>
      <p>
      <search:msg key="error.moreInfo"/><br>
      </p>

      <%@include file="search_form.jsp" %>

      <br/>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>
