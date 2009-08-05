<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<yrcwww:contentbox title="List MS Upload Jobs" centered="true" width="1000" scheme="upload">

<logic:notPresent name="jobs" scope="request">
  <logic:forward name="listUploadJobs" />
</logic:notPresent>

<logic:empty name="jobs" scope="request">

	<logic:equal name="status" scope="request" value="pending">
		<p align="center">There are no pending MS upload jobs.</p>
		<p align="center"><a href="<yrcwww:link path='listUploadJobs.do?status=complete' />">View Completed Jobs</a></p>
	</logic:equal>
	
	<logic:notEqual name="status" scope="request" value="pending">
		<p align="center">There are no completed MS upload jobs.</p>
		<p align="center"><a href="<yrcwww:link path='listUploadJobs.do?status=pending' />">View Pending Jobs</a></p>
	</logic:notEqual>
	
</logic:empty>


<logic:notEmpty name="jobs" scope="request">


	<logic:equal name="status" scope="request" value="pending">
		<p align="center">Listed below are the <b>pending</b> MS uploads.</p>
		<p align="center"><a href="<yrcwww:link path='listUploadJobs.do?status=complete' />">View Completed Jobs</a></p>
	</logic:equal>
	
	<logic:notEqual name="status" scope="request" value="pending">
		<p align="center">Listed below are the <b>completed</b> MS uploads.</p>
		<p align="center"><a href="<yrcwww:link path='listUploadJobs.do?status=pending' />">View Pending Jobs</a></p>
	</logic:notEqual>
	
	
	<p align="center">Showing results <bean:write name="firstResult" scope="request" /> to <bean:write name="lastResult" scope="request" /> of <bean:write name="totalCount" scope="request" /></p>
	
	<p align="center">
		<logic:present name="previousIndex" scope="request">
			<a href="<yrcwww:link path='listUploadJobs.do?'/>status=<bean:write name="status" scope="request" />&index=<bean:write name="previousIndex" scope="request" />">View Previous 50</a>
		</logic:present>
		<logic:present name="nextIndex" scope="request">
			<a href="<yrcwww:link path='listUploadJobs.do?'/>status=<bean:write name="status" scope="request" />&index=<bean:write name="nextIndex" scope="request" />">View Next 50</a>
		</logic:present>
	</p>
		
	<table border="0" cellpadding="3" cellspacing="2" width="95%" class="table_basic" align="center">
	
		<thead>
		<tr>
			<th width="8%">&nbsp;</th>
			<th width="8%">Status</th>
			<th width="8%">Submitter</th>
			<th width="8%">Sub. Date</th>
			<th width="8%">Directory</th>
			<th width="8%">PI</th>
			<th width="8%">Comments</th>
		</tr>
		</thead>
	
		<tbody>
		<logic:iterate name="jobs" scope="request" id="job">
			<tr>
				<td align="left" valign="top" style="width:8%;font-size:8pt;"><a href="<yrcwww:link path='viewUploadJob.do?'/>id=<bean:write name="job" property="id" />">View Job</td>

				<td align="left" valign="top" style="width:8%;font-size:8pt;">

					<logic:equal name="job" property="status" value="4">
						<a href="<yrcwww:link path='viewYatesRun.do?'/>id=<bean:write name="job" property="runID" />"><bean:write name="job" property="statusDescription" /></a>
					</logic:equal>
					<logic:notEqual name="job" property="status" value="4">
						<bean:write name="job" property="statusDescription" />
					</logic:notEqual>
				</td>

				<td align="left" valign="top" style="width:10%;font-size:8pt;">
					<div style="width:100%;height:auto;overflow:auto;">
						<a href="<yrcwww:link path='viewResearcher.do?'/>id=<bean:write name="job" property="submitter" />">
							<bean:write name="job" property="researcher.lastName" />, <bean:write name="job" property="researcher.firstName" />
						</a>
					</div>
				</td>
					
				<td align="left" valign="top" style="width:10%;font-size:8pt;"><bean:write name="job" property="submitDate" /></td>

				<td align="left" valign="top" style="width:32%;font-size:8pt;" class="left_align">
					<div style="width:100%;height:auto;overflow:auto;">
						<bean:write name="job" property="serverDirectory" />
					</div>
				</td>
				
				<td align="left" valign="top" style="width:7%;font-size:8pt;">
					<div style="width:100%;height:auto;overflow:auto;">
						<bean:write name="job" property="project.PI.lastName" />
					</div>
				</td>

				<td align="left" valign="top" style="width:15%;font-size:8pt;" class="left_align">
					<div style="width:100%;height:auto;overflow:auto;">
						<bean:write name="job" property="comments" />
					</div>
				</td>
				
				
			</tr>
		</logic:iterate>
		</tbody>
	</table>

	<p align="center">
		<logic:present name="previousIndex" scope="request">
			<a href="<yrcwww:link path='listUploadJobs.do?'/>status=<bean:write name="status" scope="request" />&index=<bean:write name="previousIndex" scope="request" />">View Previous 50</a>
		</logic:present>
		<logic:present name="nextIndex" scope="request">
			<a href="<yrcwww:link path='listUploadJobs.do?'/>status=<bean:write name="status" scope="request" />&index=<bean:write name="nextIndex" scope="request" />">View Next 50</a>
		</logic:present>
	</p>

</logic:notEmpty>





</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>