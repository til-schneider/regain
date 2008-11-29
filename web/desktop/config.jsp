<%@page contentType="text/html; charset=UTF-8" errorPage="errorpage.jsp"%>

<html>
<head>
  <title>regain - <search:msg key="preferences"/></title>
  <script src="regain.js" type="text/javascript"></script>
  <link href="regain.css" rel="stylesheet" type="text/css">
</head>

<body>
  <table class="top"><tr>
    <td><img src="img/logo_regain.gif" width="201" height="66"></td>
  </tr></table>

  <table class="content">
    <tr class="headline"><td>
      <b><search:msg key="preferences"/></b>
    </td></tr>
    <tr><td>
      <config:form action="config.jsp">
        <br/>
        <p>
          <search:msg key="indexingInterval"/>: <config:interval/>
        </p>
        
        <p>
          <h4><search:msg key="directories"/></h4>
          <div class="hint"><search:msg key="directory.hint"/></div>
          <config:editlist name="dirlist" class="editlist"/>
        </p>
        
        <p>
          <h4><search:msg key="excludedDirectories"/></h4>
          <div class="hint"><search:msg key="directory.hint"/></div>
          <config:editlist name="dirblacklist" class="editlist"/>
        </p>
        
        <p>
          <h4><search:msg key="websites"/></h4>
          <div class="hint"><search:msg key="website.hint"/></div>
          <config:editlist name="sitelist" class="editlist"/>
        </p>
        
        <p>
          <h4><search:msg key="excludedWebsiteSubdirs"/></h4>
          <div class="hint"><search:msg key="website.hint"/></div>
          <config:editlist name="siteblacklist" class="editlist"/>
        </p>

        <p>
          <h4><search:msg key="imapserver"/></h4>
          <div class="hint"><search:msg key="imapserver.hint"/></div>
          <config:editlist name="imaplist" class="editlist"/>
        </p>
        
        <p>
          <h4><search:msg key="webserver"/></h4>
          <search:msg key="portNumber"/> <config:text name="port" size="4"/>
        </p>
    
        <p>
          <br>
          <search:input_submit text="{msg:savePreferences}"/>
        </p>
      </config:form>
    </td></tr>
  </table>

  <%@include file="footer.jsp" %>

</body>
</html>