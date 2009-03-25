
<%@page import="edu.uwpr.protinfer.database.dto.ProteinUserValidation"%><%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script type="text/javascript">
	$(document).ready(function() {
		$("input[name='validationStatus'][value='All']").click(function() {
			$("input[name='validationStatus'][value!='All']").each(function() {
				this.checked = false;
			});
		});
		$("input[name='validationStatus'][value!='All']").click(function() {
			$("input[name='validationStatus'][value='All']").each(function() {
				this.checked = false;
			});
		});
	});
</script>

  <html:form action="/updateProteinInferenceResult" method="post" styleId="filterForm" >
  
  <html:hidden name="proteinInferFilterForm" property="pinferId" />
  
  
  <TABLE CELLPADDING="5px" CELLSPACING="5px" align="center" style="border: 1px solid gray;">
  
  <!-- Filtering options -->
  
  <tr>
  
  <td><table>
  <tr>
  <td>Min. Peptides: </td>
  <td><html:text name="proteinInferFilterForm" property="minPeptides" size="3"></html:text></td>
  </tr>
  <tr>
  <td>Min. Unique Peptides: </td>
  <td><html:text name="proteinInferFilterForm" property="minUniquePeptides" size="3"></html:text></td>
  </tr>
  </table></td>
  
  <td><table>
  <tr>
  <td>Min. Coverage:</td>
  <td><html:text name="proteinInferFilterForm" property="minCoverage" size="3"></html:text>(%)</td>
  </tr>
  <tr>
  <td>Min. Spectrum Matches: </td>
  <td><html:text name="proteinInferFilterForm" property="minSpectrumMatches" size="3"></html:text></td>
  </tr>
  </table></td>
  
  <td><table>
  <tr>
  	<td colspan="2">Group Indistinguishable Proteins: </td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="joinGroupProteins" value="true">Yes</html:radio>
  	</td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="joinGroupProteins" value="false">No</html:radio>
  	</td>
  </tr>
  <tr>
  	<td colspan="2">Show Proteins: </td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="showAllProteins" value="true">All</html:radio>
  	</td>
  	<td>
  		<html:radio name="proteinInferFilterForm" property="showAllProteins" value="false">Parsimonious</html:radio>
  	</td>
  </tr>
  </table></td>
  </tr>
  
  <!--  
  <tr>
  	<td colspan="2">
  		All-inclusive Common Name Lookup: 
  		<html:checkbox name="proteinInferFilterForm" property="exhaustiveCommonNameLookup" value="true"/>
  	</td>
  </tr>
  -->
  
  <tr>
  	<td colspan="2">
  		Validation Status: 
  		<html:multibox name="proteinInferFilterForm" property="validationStatus" value="All"/> All
  		<html:multibox name="proteinInferFilterForm" property="validationStatus" 
  					   value="<%=String.valueOf(ProteinUserValidation.UNVALIDATED.getStatusChar()) %>"/> Unvalidated 
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.ACCEPTED.getStatusChar()) %>"/> Accepted
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.REJECTED.getStatusChar()) %>"/> Rejected
  		<html:multibox name="proteinInferFilterForm" property="validationStatus"  
  		               value="<%=String.valueOf(ProteinUserValidation.NOT_SURE.getStatusChar()) %>"/> Not Sure
  	</td>
  </tr>
  
  <tr>
  	<td colspan="3">
  	<table align="left">
  		<tr>
  			<td valign="top">Fasta ID(s): </td>
  			<td valign="top"><html:text name="proteinInferFilterForm" property="accessionLike" size="40"></html:text><br>
  				<span style="font-size:8pt; color:#006400;">Enter a comma-separated list of complete or partial identifiers</span>
  			</td>
  			<td valign="top">Description: </td>
  			<td valign="top">
  				<html:text name="proteinInferFilterForm" property="descriptionLike" size="40"></html:text>
  			</td>
  		</tr>
  		<tr>
  		</tr>
  	</table>
  	</td>
  </tr>
  
  <tr>
    	<td colspan="3" align="center">
    		<html:submit styleClass="button" style="margin-top:2px;">Update</html:submit>
    		&nbsp;&nbsp;
    		<a href="/yrc/downloadProtInferResults.do?pinferId=<bean:write name="proteinInferFilterForm" property="pinferId" />">Download Results</a>
    	</td>
  </tr>
  
 </TABLE>
 
</html:form>