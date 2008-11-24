
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<script src="/yrc/js/jquery.ui-1.6rc2/jquery-1.2.6.js"></script>
<script type="text/javascript" src="/yrc/js/jquery.ui-1.6rc2/ui/ui.core.js" ></script>
<script type="text/javascript" src="/yrc/js/jquery.ui-1.6rc2/ui/ui.tabs.js"></script>
<script type="text/javascript" src="/yrc/js/jquery.ui-1.6rc2/ui/ui.slider.js"></script>
<script type="text/javascript" src="/yrc/js/jquery.history.js"></script>
<link rel="stylesheet" href="/yrc/css/proteinfer.css" type="text/css" >
<%
	int clusterCount = (Integer)request.getAttribute("clusterCount");
	int pinferId = (Integer)request.getAttribute("pinferId");
	
%>
<script>

// ajax defaults
  $.ajaxSetup({
  	type: 'POST',
  	timeout: 10000,
  	dataType: 'html',
  	error: function(xhr) {
  				var statusCode = xhr.status;
		  		// status code returned if user is not logged in
		  		// reloading this page will redirect to the login page
		  		if(statusCode == 303)
 					window.location.reload();
 				
 				// otherwise just display an alert
 				else {
 					alert("Request Failed: "+statusCode+"\n"+xhr.statusText);
 				}
  			}
  });

  // set up the tabs and select the first tab
  $(document).ready(function(){
    $("#results > ul").tabs().tabs('select', 0);
  });


// FOR HISTORY
function callback(hash)
{
	var $tabs = $("#results").tabs();
    // do stuff that loads page content based on hash variable
    if(hash) {
    	$("#load").text(hash + ".html");
		var $tabs = $("#results").tabs();
		
		if(hash == 'protlist')
			$tabs.tabs('select', 0);
		else if (hash == 'protclusters')
			$tabs.tabs('select', 1);
		else if (hash == 'protdetails')
			$tabs.tabs('select', 2);
		else if (hash == 'input')
			$tabs.tabs('select', 3);
	} else {
		$tabs.tabs('select', 0);
	}
}
// FOR HISTORY
$(document).ready(function() {
    $.history.init(callback);
    $("a[@rel='history']").click(function(){
    	var hash = this.href;
		hash = hash.replace(/^.*#/, '');
        $.history.load(hash);
        return false;
    });
});

	
  function setupProteinListTable() {
  	$("#protlisttable tbody tr.protgrp_row").addClass("ms_A");
  	$("#protlisttable > thead > tr > th").addClass("ms_A");
  	$(".showpept").click(function() {
  	
  		var id = this.id;
  		if($(this).text() == "Show Peptides") {
  			$(this).text("Hide Peptides");
  			$("#peptforprot_"+id).show();
  		}
  		else {
  			$(this).text("Show Peptides");
  			$("#peptforprot_"+id).hide();
  		}
  			
  	});
  	makeProteinListSortable($("#protlisttable"));
  }
  
  // stripe the proteins table and the input list table
  $(document).ready(function() {
  	$(".stripe_table th").addClass("ms_A");
  	$(".stripe_table tbody > tr:odd").addClass("ms_A");
  	
  	setupProteinListTable();
  	
  	$("#coverage").slider({
  		min: 0, 
        max: 100, 
        stop: function(e, ui) { 
        	$("#coverage_val").text(Math.round(ui.value));
        }, 
        slide: function(e, ui) { 
            $("#coverage_val").text(Math.round(ui.value));
        } 
  	});
  	$("#peptcnt").slider({
  		min: <%= (Integer)request.getAttribute("minPeptides")%>, 
        max: <%= (Integer)request.getAttribute("maxPeptides")%>, 
        startValue: <%= (Integer)request.getAttribute("minPeptides")%>,
        stop: function(e, ui) { 
        	$("#peptcnt_val").text(Math.round(ui.value));
        }, 
        slide: function(e, ui) { 
            $("#peptcnt_val").text(Math.round(ui.value));
        } 
  	});
  	$("#uniqpeptcnt").slider({
  		min: 0, 
        max: <%= (Integer)request.getAttribute("maxUniqPeptides")%>, 
        stop: function(e, ui) { 
        	$("#uniqpeptcnt_val").text(Math.round(ui.value));
        }, 
        slide: function(e, ui) { 
            $("#uniqpeptcnt_val").text(Math.round(ui.value));
        } 
  	});
  	$("#spectracnt").slider({
  		min: 1, 
        max: <%= (Integer)request.getAttribute("maxSpectra")%>, 
        stop: function(e, ui) { 
        	$("#spectracnt_val").text(Math.round(ui.value));
        }, 
        slide: function(e, ui) { 
            $("#spectracnt_val").text(Math.round(ui.value));
        } 
  	});
  	
  	
  	// add a click handler to the filter button
  	$("#filterbutton").click(function() {
  		var cov = $("#coverage_val").text();
  		var pepCnt = $("#peptcnt_val").text();
  		var uniqPeptCnt = $("#uniqpeptcnt_val").text();
  		var specCnt = $("#spectracnt_val").text();
  		
  		$("#proteinlist_table").text("");
  		
  		$("#proteinlist_table").load("filterProteinferResults.do", 			//url
  									{'pinferId': <%=pinferId%>, 	// data
  									 'coverage': cov,
  									 'peptideCnt': pepCnt,
  									 'uniqPeptideCnt': uniqPeptCnt,
  									 'spectraCnt': specCnt
  									 },
  									function(responseText, status, xhr) {		// callback
  										setupProteinListTable();
  								    });
  		//alert("Filter on -- cov: "+cov+"; peptCnt: "+pepCnt+"; uniqPeptCnt: "+uniqPeptCnt+"; specCnt: "+specCnt);
  	});
  });
  
  
  
  // View the protein sequence
  function toggleProteinSequence (nrseqid, pinferId) {
  
  		//alert("protein id: "+nrseqid+" pinferId: "+pinferId);
  		var button = $("#protseqbutton_"+nrseqid);
  		
  		if(button.text() == "[View Sequence]") {
  			//alert("View");
  			if($("#protsequence_"+nrseqid).html().length == 0) {
  				//alert("Getting...");
  				// load data in the appropriate div
  				$("#protsequence_"+nrseqid).load("proteinSequence.do",   						// url
  								                 {'nrseqid': nrseqid, 'pinferId': pinferId}); 	// data
 			}
 			button.text("[Hide Sequence]");
 			$("#protseqtbl_"+nrseqid).show();
 		}
 		else {
 			button.text("[View Sequence]");
 			$("#protseqtbl_"+nrseqid).hide();
 		}
  }
  
  function viewSpectrum (scanId, hitId) {
  		alert("View spectrum for "+scanId+"; hit: "+hitId);
  		var winHeight = 500
		var winWidth = 970;
		var doc = "/yrc/viewSpectrum.do?scanID="+scanId+"&runSearchResultID="+hitId;
		//alert(doc);
		window.open(doc, "SPECTRUM_WINDOW", "width=" + winWidth + ",height=" + winHeight + ",status=no,resizable=yes,scrollbars=yes");
  }
  
  
  function showProteinDetails(proteinId) {
  	// first hide all divs 
  	$(".protdetail_prot").hide();
  	
  	// load content in the appropriate div
  	$("#protein_"+proteinId).load("proteinDetails.do",   									// url
  								  {'pinferId': <%=pinferId%>, 'nrseqProtId': proteinId}, 	// data
  								  function(responseText, status, xhr) {						// callback
  								  		// stripe the table
  										$("#protdetailstbl_"+proteinId+" th").addClass("ms_A");
  										$("#protdetailstbl_"+proteinId+" tbody tr.main").addClass("ms_A");
  										$(this).show();
  										var $tabs = $("#results").tabs();
  										$("#protdetailslink").click(); // so that history works
  										//$tabs.tabs('select', 2);
  								  });	
  }
  
  
  function showProteinCluster(proteinClusterIdx) {
  
  	$("#clusterlist")[0].selectedIndex = proteinClusterIdx - 1;
  	selectProteinCluster();
  	
  	var $tabs = $("#results").tabs();
  	$("#protclusterslink").click();
  	//$tabs.tabs('select', 1);
  	return false;
  }
  
  
  function selectProteinCluster() {
  
  	var clusterId = $("#clusterlist")[0].selectedIndex + 1;
  	
  	// hide all other first
  	for(var i = 1; i <= <%=clusterCount%>; i++) {
  		$("#protcluster_"+i).hide();
  	}
  	// get data from the server and put it in the appropriate div
  	$("#protcluster_"+clusterId).load("proteinCluster.do",   								// url
  								  	  {'pinferId': <%=pinferId%>, 'clusterId': clusterId}, 	// data
  								      function(responseText, status, request) {				// callback
	  								  		
	  								  		$("#assoctable_"+clusterId).css('border', '1px dashed gray').css('border-spacing', '2px');
	  										$("#assoctable_"+clusterId+"  td").css('border', '1px #CCCCCC dashed').css('padding', '4px');
										  	$("#assoctable_"+clusterId+"  th").css('border', '1px #CCCCCC dashed').css('padding', '4px').addClass("ms_A");
										  	
										  	$("#prot_grp_table_"+clusterId).css('border', '1px dashed gray').css('border-spacing', '2px');
										  	$("#prot_grp_table_"+clusterId+"  td").css('border', '1px #CCCCCC dashed').css('padding', '4px');
										  	$("#prot_grp_table_"+clusterId+"  th").css('border', '1px #CCCCCC dashed').css('padding', '4px').addClass("ms_A");
										  	
										  	$("#pept_grp_table_"+clusterId).css('border', '1px dashed gray').css('border-spacing', '2px');
										  	$("#pept_grp_table_"+clusterId+"  td").css('border', '1px #CCCCCC dashed').css('padding', '4px');
										  	$("#pept_grp_table_"+clusterId+"  th").css('border', '1px #CCCCCC dashed').css('padding', '4px').addClass("ms_A");
	  										
	  										$(this).show();
  								  });	
  }
  
  
  function showSpectrumMatches(runSearchId, runName) {
  		$(".input_psm").hide();
  		$("#psm_"+runSearchId).show();
  		
  		if($("#psm_"+runSearchId).html().length == 0) {
  			$("#psm_"+runSearchId).html("<b>Loading Peptide Spectrum Matches for: "+runName+"...</b>");
  			$("#psm_"+runSearchId).load("psmMatches.do", //url
  									{'pinferId': <%=pinferId%>, 'runSearchId': runSearchId},
  									function(responseText, status, xhr) {						// callback
  								  		// stripe the table
  										$("#psmtbl_"+runSearchId+" th").addClass("ms_A");
  										$("#psmtbl_"+runSearchId+" tr:even").addClass("ms_A");
  										makeSortable($("#psmtbl_"+runSearchId));
  										$(this).show();
  								  });
  		}
  }
  
  
  var lastSelectedProteinGroupId = -1;
  var lastSelectedPeptGrpIds = new Array(0);
  
  function highlightProteinAndPeptides() {
  	var proteinGroupId = arguments[0];
  	var peptGrpIds = arguments[1].split(",");
  	var uniqPeptGrpIds = arguments[2].split(",");
  	//alert(proteinGroupId+" AND "+peptGrpIds+" AND "+uniqPeptGrpIds);
  	
  	if(proteinGroupId == lastSelectedProteinGroupId) {
  		removeProteinAndPeptideHighlights();
  	}
  	else {
  		
	  	// deselect any last selected cell
	  	removeProteinAndPeptideHighlights();
	  	
	  	// select the PROTEIN group cells the user wants to select
	  	$("#protGrp_"+proteinGroupId).css("background-color","#FFFF00");
	  	
	  	
	  	// now select the PEPTIDE group cells we want AND the PROTEIN-PEPTIDE association cells
  		lastSelectedPeptGrpIds = [];
  		var j = 0;
  		// peptide groups NOT unique to protein
  		for(var i = 0; i < peptGrpIds.length; i++) {
  			$(".peptGrp_"+peptGrpIds[i]).each(function() {$(this).css("background-color","#FFFF00");});
  			$("#peptEvFor_"+proteinGroupId+"_"+peptGrpIds[i]).css("background-color","#FFFF00");
  			lastSelectedPeptGrpIds[j] = peptGrpIds[i];
  			j++;
  		}
  		// peptide groups UNIQUE to protein
  		for(var i = 0; i < uniqPeptGrpIds.length; i++) {
  			$(".peptGrp_"+uniqPeptGrpIds[i]).each(function() {$(this).css("background-color","#00FFFF");});
  			$("#peptEvFor_"+proteinGroupId+"_"+uniqPeptGrpIds[i]).css("background-color","#00FFFF");
  			lastSelectedPeptGrpIds[j] = uniqPeptGrpIds[i];
  			j++;
  		}
  		lastSelectedProteinGroupId = proteinGroupId;
  	}
  }
  	
  	function removeProteinAndPeptideHighlights() {
  		
  		if(lastSelectedPeptGrpIds != -1) {
	  		// deselect any last selected protein group cells.
	  		$("#protGrp_"+lastSelectedProteinGroupId).css("background-color","");
	  		
	  		// deselect any last selected peptide group cells AND protein-peptide association cells
	  		if(lastSelectedPeptGrpIds.length > 0) {
		  		for(var i = 0; i < lastSelectedPeptGrpIds.length; i++) {
		  			$(".peptGrp_"+lastSelectedPeptGrpIds[i]).each(function() {
		  				$(this).css("background-color","");
		  				});
		  			$("#peptEvFor_"+lastSelectedProteinGroupId+"_"+lastSelectedPeptGrpIds[i]).css("background-color","");
		  		}
		  	}
		  	lastSelectedProteinGroupId = -1;
		  	lastSelectedPeptGrpIds = [];
	  	}
  	}
  	

  function toggleProteinList() {
  	var $mydiv = $(".proteins");
  	if($mydiv.is(':visible'))
  		$mydiv.hide();
  	else
  		$mydiv.show();
  }
  

  $(document).ready(function() {
  
   	$('table.sortable').each(function() {
    	var $table = $(this);
    	makeSortable($table);
  	});
  });
  
  
  function makeSortable(table) {
  	
  	var $table = table;
  	$('th', $table).each(function(column) {
    		
    		if ($(this).is('.sort-alpha') || $(this).is('.sort-int') 
    			|| $(this).is('.sort-int-special') || $(this).is('.sort-float') ) {
    		
    			var $header = $(this);
        		$(this).addClass('clickable').hover(
        			function() {$(this).addClass('ms_hover');} , 
        			function() {$(this).removeClass('ms_hover');}).click(function() {

						// remove row striping
						if($table.is('.stripe_table')) {
							$("tbody > tr:odd", $table).removeClass("ms_A");
						}
						
						// sorting direction
						var newDirection = 1;
          				if ($(this).is('.sorted-asc')) {
            				newDirection = -1;
          				}
          				
          				var rows = $table.find('tbody > tr').get();
          				
          				if ($header.is('.sort-alpha')) {
          					$.each(rows, function(index, row) {
  								row.sortKey = $(row).children('td').eq(column).text().toUpperCase();
							});
						}
						
						if ($header.is('.sort-int')) {
          					$.each(rows, function(index, row) {
  								var key = parseInt($(row).children('td').eq(column).text());
								row.sortKey = isNaN(key) ? 0 : key;
							});
						}
						
						if ($header.is('.sort-int-special')) {
          					$.each(rows, function(index, row) {
  								var key = parseInt($(row).children('td').eq(column).text().replace(/\(\d*\)/, ''));
								row.sortKey = isNaN(key) ? 0 : key;
							});
						}
						
						if ($header.is('.sort-float')) {
          					$.each(rows, function(index, row) {
  								var key = parseFloat($(row).children('td').eq(column).text());
								row.sortKey = isNaN(key) ? 0 : key;
							});
						}

          				rows.sort(function(a, b) {
            				if (a.sortKey < b.sortKey) return -newDirection;
  							if (a.sortKey > b.sortKey) return newDirection;
  							return 0;
          				});

          				$.each(rows, function(index, row) {
            				$table.children('tbody').append(row);
            				row.sortKey = null;
          				});
          				
          				var $sortHead = $table.find('th').filter(':nth-child(' + (column + 1) + ')');

			          	if (newDirection == 1) {$sortHead.addClass('sorted-asc'); $sortHead.removeClass('sorted-desc');} 
			          	else {$sortHead.addClass('sorted-desc'); $sortHead.removeClass('sorted-asc');}
          
          				// add row striping back
          				if($table.is('.stripe_table')) {
							$("tbody > tr:odd", $table).addClass("ms_A");
          				}
        			});
			}
      	});
  	}
  	
  function makeProteinListSortable(table) {
  	
  	var $table = table;
  	$('th', $table).each(function(column) {
    		
    		if ($(this).is('.sort-int') || $(this).is('.sort-float') ) {
    		
    			var $header = $(this);
        		$(this).addClass('clickable').hover(
        			function() {$(this).addClass('ms_hover');} , 
        			function() {$(this).removeClass('ms_hover');}).click(function() {

						$('th', $table).each(function(){$(this).removeClass('ms_selected_header');});
						$header.addClass('ms_selected_header');
						
						// sorting direction
						var newDirection = 1;
          				if ($(this).is('.sorted-asc')) {
            				newDirection = -1;
          				}
          				
          				var rows = $table.find('tbody > tr.sorting_row').get();
          				
						if ($header.is('.sort-int')) {
          					$.each(rows, function(index, row) {
  								var key = parseInt($(row).children('td').eq(column).text());
								row.sortKey = isNaN(key) ? 0 : key;
							});
						}
						
						if ($header.is('.sort-float')) {
          					$.each(rows, function(index, row) {
  								var key = parseFloat($(row).children('td').eq(column).text());
								row.sortKey = isNaN(key) ? 0 : key;
							});
						}
          				rows.sort(function(a, b) {
            				if (a.sortKey < b.sortKey) return -newDirection;
  							if (a.sortKey > b.sortKey) return newDirection;
  							return 0;
          				});

						var found = 0;
          				$.each(rows, function(index, row) {
          					var $row = $(this);
          					var myrows = [];
            				//$table.children('tbody').append($row);
            				myrows[0] = $row;
            				var $nextrow = $row.next();
            				var idx = 1;
            				while($nextrow.is('.linked_row')) {
            					myrows[idx] = $nextrow;
            					$nextrow = $nextrow.next();
            					idx++;
            					found++;
            				}
            				
            				for(var x = 0; x < myrows.length; x++) {
            					$table.children('tbody').append(myrows[x]);
            				}
            				row.sortKey = null;
          				});
          				
          				var $sortHead = $table.find('th').filter(':nth-child(' + (column + 1) + ')');

			          	if (newDirection == 1) {$sortHead.addClass('sorted-asc'); $sortHead.removeClass('sorted-desc');} 
			          	else {$sortHead.addClass('sorted-desc'); $sortHead.removeClass('sorted-asc');}
          				
        			});
			}
      	});
  	}
  	
</script>





<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

 
<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<CENTER>

<yrcwww:contentbox title="IDPicker Results" centered="true" width="1000" scheme="ms">

	<table align="center" cellpadding="4">
	<tr><td>
	<table cellpadding="2" align="center" style="font-family: Trebuchet MS,Trebuchet,Verdana,Helvetica,Arial,sans-serif;font-size:12px; border: 1px solid gray; border-spacing: 2px">
  	<tr class="ms_A"><td colspan="4" align="center"><b>Parameters</b></td></tr>
  	<tr>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Max. Abs. FDR</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;"><bean:write name="params" property="maxAbsoluteFdr" /></td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Max. Rel. FDR</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;"><bean:write name="params" property="maxRelativeFdr" /></td>
   </tr>
   <tr>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Decoy Prefix</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;"><bean:write name="params" property="decoyPrefix" /></td>
    
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Decoy Ratio</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;"><bean:write name="params" property="decoyRatio" /></td>
   </tr>
   <tr>
   	<td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Min. Distinct Peptides</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;"><bean:write name="params" property="minDistinctPeptides" /></td>
   
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;color: #3D902A;">Parsimony Analysis</td>
    <td VALIGN="top" style="border: 1px #CCCCCC dotted;">
    	<logic:equal name="params" property="doParsimonyAnalysis"value="true">Yes</logic:equal>
    	<logic:equal name="params" property="doParsimonyAnalysis"value="false">No</logic:equal>
    </td>
   </tr>
  </table>
  </td>
  </tr>
  </table>
  
  <div id="results" class="flora">
      <ul>
          <li><a href="#protlist" rel="history" id="protlistlink"><span>Protein List</span></a></li>
          <li><a href="#protclusters" rel="history" id="protclusterslink"><span>Protein Clusters</span></a></li>
          <li><a href="#protdetails" rel="history" id="protdetailslink"><span>Protein Details</span></a></li>
          <li><a href="#input" rel="history" id="inputlink"><span>Input Summary</span></a></li>
      </ul>
    
      
    <!-- PROTEIN LIST -->
    
    
	<div id="protlist">
	
	<div>
    <table align="center" cellpadding="0" cellspacing="0">
    <tr>
    	<td>Coverage(%): </td>
    	<td>
    	<div id='coverage' class='ui-slider-1' style="margin:10px;">
			<div class='ui-slider-handle'></div>	
		</div>
    	</td>
    	<td align="center"><div id="coverage_val" style="background-color: #A0DF82; color:white;padding:5px; margin:2px;">0</div></td>
    	<td>Spectrum Count: </td>
    	<td>
    	<div id='spectracnt' class='ui-slider-1' style="margin:10px;">
			<div class='ui-slider-handle'></div>	
		</div>
    	</td>
    	<td align="center"><div id="spectracnt_val" style="background-color: #A0DF82; color:white;padding:5px; margin:2px;">1</div></td>
    </tr>
    <tr>	
    	<td>Total Peptides: </td>
    	<td>
    	<div id='peptcnt' class='ui-slider-1' style="margin:10px;">
			<div class='ui-slider-handle'></div>	
		</div>
    	</td>
    	<td align="center">
    	<div id="peptcnt_val" style="background-color: #A0DF82; color:white;padding:5px; margin:2px;"><bean:write name="minPeptides" /></div>
    	</td>
    	
    	<td>Unique Peptides: </td>
    	<td>
    	<div id='uniqpeptcnt' class='ui-slider-1' style="margin:10px;">
			<div class='ui-slider-handle'></div>	
		</div>
    	</td>
    	<td align="center"><div id="uniqpeptcnt_val" style="background-color: #A0DF82; color:white;padding:5px; margin:2px;">0</div></td>
    </tr>
    <tr>
    	<td colspan="6" align="center"><button class="button" id="filterbutton">Filter</button></td>
    </tr>
    </table>
    </div>
    <br>
    
    <div id="proteinlist_table">
		<%@ include file="proteinlist.jsp" %>
    </div>
    </div>
    
    
      <!-- PROTEIN CLUSTER -->
      <div id="protclusters"><font color="black">
          	<b>Select Protein Cluster: </b>
          	<select id="clusterlist" onchange="selectProteinCluster()">
          		<%for(int i = 1; i <= clusterCount; i++) { %>
          			<option value="<%=i%>"><%=i%></option>
          		<%} %>
          	</select>
          	
          	<!-- create a placeholder div for each protein cluster -->
          	<%for(int i = 1; i <= clusterCount; i++) { %>
          		<div id="protcluster_<%=i %>" style="display: none;"></div>
          	<%} %>
          	
      </font></div>
      
      
      <!-- PROTEIN DETAILS -->
      <div id="protdetails">
      		<!-- create a placeholder div for each protein -->
      		<logic:iterate name="proteinGroups" id="proteinGroup">
      			<logic:iterate name="proteinGroup" property="proteins" id="protein">
      				<div id="protein_<bean:write name="protein" property="nrseqProteinId" />" style="display: none;" class="protdetail_prot"></div>
      			</logic:iterate>
      		</logic:iterate>
      </div>
      
      <!-- INPUT SUMMARY -->
      <div id="input">
      
      
      	<table cellpadding="2" align="center" style="font-family: Trebuchet MS,Trebuchet,Verdana,Helvetica,Arial,sans-serif;font-size:12px; border: 1px solid gray; border-spacing: 3px">
	  		<tr class="ms_A">
	  		<th align="center" style="font-size: 10pt;"><b>Total Proteins</b></td>
	  		<th align="center" style="font-size: 10pt;"><b>Filtered Proteins</b></td>
	  		</tr>
	  		<tr>
	    	<td VALIGN="top" align="center" style="border: 1px #CCCCCC dotted;"><bean:write name="searchSummary" property="allProteins" /></td>
	   		<td VALIGN="top" align="center" style="border: 1px #CCCCCC dotted;"><bean:write name="searchSummary" property="filteredProteinsParsimony" /></td>
	   		</tr>
		</table>
      	<br><br>
      
		<table cellpadding="4" cellspacing="2" align="center" width="90%" class="sortable stripe_table">
		<logic:notEmpty name="searchSummary" property="runSearchList">
		<thead>
		<tr>
		<th class="sort-alpha" align="left"><b><font size="2pt">File Name</font></b></th>
		<th class="sort-int" align="left"><b><font size="2pt">Decoy Hits</font></b></th>
		<th class="sort-int" align="left"><b><font size="2pt">Target Hits</font></b></th>
		<th class="sort-int" align="left"><b><font size="2pt">Filtered Target Hits</font></b></th>
		</tr>
		</thead>
		</logic:notEmpty>
		<tbody>
	  	<logic:iterate name="searchSummary" property="runSearchList" id="runSearch">
	  		<logic:equal name="runSearch" property="isSelected" value="true">
	  			<tr>
	  				<td>
	  					<span style="text-decoration: underline; cursor: pointer;"
	  								onclick="showSpectrumMatches(<bean:write name="runSearch" property="runSearchId" />, '<bean:write name="runSearch" property="runName" />')">
	  					<bean:write name="runSearch" property="runName" />
	  					</span>
	  				</td>
	  				<td><bean:write name="runSearch" property="totalDecoyHits" /></td>
	  				<td><bean:write name="runSearch" property="totalTargetHits" /></td>
	  				<td><bean:write name="runSearch" property="filteredTargetHits" /></td>
	  			</tr>
	  		</logic:equal>
	  	</logic:iterate>
	  	<tr>
	  		<td><b>TOTAL</b></td>
	  		<td><b><bean:write name="searchSummary" property="totalDecoyHits" /></b></td>
	  		<td><b><bean:write name="searchSummary" property="totalTargetHits" /></b></td>
	  		<td><b><bean:write name="searchSummary" property="filteredTargetHits" /></b></td>
	  	</tr>
	  	</tbody>
 		</table>
		<br><br>
		<logic:iterate name="searchSummary" property="runSearchList" id="runSearch">
		<logic:equal name="runSearch" property="isSelected" value="true">
			<div id="psm_<bean:write name="runSearch" property="runSearchId" />" style="display: none;" class="input_psm"></div>
	  	</logic:equal>
	  	</logic:iterate>
	</div>

</yrcwww:contentbox>
</CENTER>

<%@ include file="/includes/footer.jsp" %>