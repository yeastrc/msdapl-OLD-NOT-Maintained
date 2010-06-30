<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>


<html>

<head>
 <title>GO Tree</title>
 	<link REL="stylesheet" TYPE="text/css" HREF="<yrcwww:link path='/css/global.css' />">
 	<link REL="stylesheet" TYPE="text/css" HREF="<yrcwww:link path='/css/jstree.css' />">
</head>

<body>

<script src="<yrcwww:link path='js/jquery.js'/>"></script>
<script  type="text/javascript" src="<yrcwww:link path='js/jquery.jstree.js'/>"></script>


<script>
$(function () {

	$("#search").click(function () {
		searchTree();
   	});
   	$("#searchbox").bind('keyup', function(e) {
   		var code = (e.keyCode ? e.keyCode : e.which);
 		if(code == 13) { //Enter keycode
 			searchTree();
 		}
   	});
   	
   	$("#clearSearch").click(function () {
        $("#tree").jstree("clear_search");
        $("#searchbox").val("");
   	});
   	$("#closeAll").click(function () {
        $("#tree").jstree("close_all"); 
   	});
   	$("#openAll").click(function () {
        $("#tree").jstree("open_all"); 
   	});
   	$("#showGoSlim").click(function() {
   		showGoSlimNodes();
   	});
   	
	$("#tree").jstree({ 
		"plugins" : [ "themes", "html_data", "search" ],
		"core" : { "initially_open" : [ <bean:write name="openNodes" filter="false"/> ] },
		"search": { "case_insensitive": true, "ajax": false},
		"html_data"  : {
			"ajax" : { "url"  : "/msdapl/pages/internal/proteinfer/goTreeNode.html"  },
			"correct_state": "true",
			"data": "<yrcwww:gotree treeName='goTree' />"
			}
	})
	.bind("search.jstree", function (e, data) {
		var matching = data.rslt.nodes.length;
		if(matching == 0) {
			alert("No GO nodes found matching '" + data.rslt.str + "'.");
		}
	});
	
	//$("#tree").jstree("open_all"); 
	
});

function searchTree() {
	var searchTerm = $("#searchbox").val();
	if(searchTerm.length > 0) {
       	$("#tree").jstree("search", searchTerm, true);
    }
}

function showGoSlimNodes() {
	var slimarr = [<bean:write name="slimTermIds" filter="false"/>];
	for(i = 0; i < slimarr.length; i++) {
		$("#"+slimarr[i]).parents(".jstree-closed").each(function () {
			$("#tree").jstree("open_node", this);
		});
	}
}

</script>

<logic:present name="goTree">
	<center>
		<div align="center" style="margin:10px; padding:5px; border: 1px gray dashed; width:80%;">
			<input type="text" id="searchbox"/>
			<input type="button" value="Search" id="search"/>
			<input type="button" value="Clear Search" id="clearSearch"/>
			<br/>
			<br/>
			<input type="button" value="Close All" id="closeAll"/>
			<input type="button" value="Open All" id="openAll"/>
			<input type="button" value="Show All GO Slim Nodes" id="showGoSlim"/>
			<br/>
			<br/>
			GO Slim Nodes in the tree below look like: <br/>
			<b><span style="color:navy; background-color:white;">GO:0008150: biological_process <font color="red">[#Annot. 892 #Exact 0]</font></span></b>
		</div>
		</center>
		<div id="tree" class="demo">
			<!--<yrcwww:gotree treeName="goTree" />-->
		</div>
	
</logic:present>

</body>
</html>