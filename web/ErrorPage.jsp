<%@page contentType="text/html"%>
<%@taglib uri="SearchLib.tld" prefix="search" %>

<html>
<head>
  <title><search:msg key="errorSearchingFor"/> <search:stats_query/></title>
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
      </p>
      <p>
      <search:msg key="error.moreInfo"/><br>
      </p>

      <form name="search" action="search.jsp" method="get">
        <search:msg key="searchFor"/>:
        <search:input_hiddenparam name="index"/>
        <search:input_query/>
        <search:input_maxresults/>
        <search:input_submit text="{msg:search}"/>
      </form>
      
      <br/>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

  <%-- Add the stack trace as hidden text --%>
  <pre style="color:FFFFFF; font-size:small;">  
  Stacktrace:
  <search:msg key="errorMessage"/>:<search:error_stacktrace/>
  </pre>

</body>
</html>
