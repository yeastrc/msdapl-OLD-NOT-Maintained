
<%@page import="org.yeastrc.ms.domain.search.SORT_ORDER"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:empty name="filterForm">
  <logic:forward name="viewMascotResults" />
</logic:empty>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<%@ include file="/pages/internal/project/resultsTableJS.jsp" %>

<script src="<yrcwww:link path='js/peptideHighlighter.js'/>"></script>
<script type="text/javascript">

$(document).ready(function() { 
	$(".peptide").highlightPeptide();
});

</script>

<yrcwww:contentbox title="Mascot Results" centered="true" width="95" widthRel="true">
<center>

	<!-- SUMMARY -->
	<div style="padding:0 7 0 7; margin-bottom:5; border: 1px dashed gray;background-color: #FFFFE0;">
		<table>
			<tr>
				<td align="center"><b>Project ID:</b>
					<html:link action="viewProject.do" paramId="ID" paramName="projectId"><bean:write name="projectId" /></html:link>&nbsp;
				</td>
				<td align="center"><b>Experiment ID:</b>
					<bean:write name="experimentId" />&nbsp;
				</td>
				
				<td align="center"><b>Program: </b><bean:write name="program" /></td>
			</tr>
		</table>
	</div>
	
	
	<!-- FILTER FORM -->
	<%@ include file="mascotFilterForm.jsp" %>



	<!-- PAGE RESULTS -->
	<bean:define name="results" id="pageable" />
	<%@include file="/pages/internal/pager.jsp" %>
	
	
				
	<!-- RESULTS TABLE -->
	<div style="background-color: #FFFFFF; margin:5 0 5 0; padding:5;" > 
	<yrcwww:table name="results" tableId='mascot_results' tableClass="table_basic sortable_table" center="true" />
	</div>
	
	<%@include file="/pages/internal/pager_small.jsp" %>
	
</center>	
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>