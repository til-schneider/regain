<%@taglib uri="regain-search.tld" prefix="search" %>

<form name="search" action="search.jsp" method="get">
  <search:msg key="searchFor"/>:
  <search:input_hiddenparam name="index"/>
  <search:input_query/>
  <search:input_maxresults/>
  <search:input_submit text="{msg:search}"/>
</form>
