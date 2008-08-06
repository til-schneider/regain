<%@page contentType="text/html; charset=UTF-8" errorPage="errorpage.jsp"%>
<%@taglib uri="regain-search.tld" prefix="search" %>

<html>
<head>
  <title>regain - <search:msg key="advancedSearch"/></title>
  <link href="regain.css" rel="stylesheet" type="text/css">
</head>

<body>
  <search:check noIndexUrl="noindex.jsp"/>

  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="advancedSearch"/></b>
    </td></tr>
    <tr><td>

      <form name="search" action="search.jsp" method="get">
        <br/>
        <table>
          <tr>
            <td><search:msg key="searchFor"/>:</td>
            <td><input name="query" size="40"/></td>
          </tr>
          <tr>
            <td><search:msg key="fileExtension"/>:</td>
            <td><search:input_fieldlist field="mimetype" allMsg="{msg:allItem}"/></td>
          </tr>
          <tr>
            <td></td><td><search:input_submit text="{msg:search}"/></td>
          </tr>
        </table>
      </form>

      <br/>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>
