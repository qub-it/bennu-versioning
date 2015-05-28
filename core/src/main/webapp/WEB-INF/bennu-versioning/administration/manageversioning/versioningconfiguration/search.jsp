<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js"/>
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css"/>
<link rel="stylesheet" href="${datatablesCssUrl}"/>
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json"/>

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css"/>

<link href="//cdn.datatables.net/responsive/1.0.4/css/dataTables.responsive.css" rel="stylesheet"/>
<script src="//cdn.datatables.net/responsive/1.0.4/js/dataTables.responsive.js"></script>
<link href="//cdn.datatables.net/tabletools/2.2.3/css/dataTables.tableTools.css" rel="stylesheet"/>
<script src="//cdn.datatables.net/tabletools/2.2.3/js/dataTables.tableTools.min.js"></script>
<link href="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/css/select2.min.css" rel="stylesheet" />
<script src="//cdnjs.cloudflare.com/ajax/libs/select2/4.0.0-rc.1/js/select2.min.js"></script>

${portal.toolkit()}

<%-- TITLE --%>
<div class="page-header">
	<h1><spring:message code="label.administration.manageVersioning.searchVersioningConfiguration" />
		<small></small>
	</h1>
</div>
<%-- NAVIGATION --%>
	<c:if test="${not empty infoMessages}">
				<div class="alert alert-info" role="alert">
					
					<c:forEach items="${infoMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty warningMessages}">
				<div class="alert alert-warning" role="alert">
					
					<c:forEach items="${warningMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>
			<c:if test="${not empty errorMessages}">
				<div class="alert alert-danger" role="alert">
					
					<c:forEach items="${errorMessages}" var="message"> 
						<p>${message}</p>
					</c:forEach>
					
				</div>	
			</c:if>


<div class="panel panel-default">
<div class="panel-body">
<form method="get" class="form-horizontal">
<div class="form-group row">
<div class="col-sm-2 control-label"><spring:message code="label.VersioningConfiguration.classname"/></div> 

<div class="col-sm-10">
	<input id="versioningConfiguration_classname" class="form-control" type="text" name="classname"  value="${not empty param.classname ? param.classname : versioningConfiguration.classname }"/>
</div>	
</div>		
<div class="form-group row">
<div class="col-sm-2 control-label"><spring:message code="label.VersioningConfiguration.active"/></div> 

<div class="col-sm-2">
<select id="versioningConfiguration_active" name="active">
<option value=""></option> <%-- empty option remove it if you don't want to have it or give it a label --%>
<option value="true"><spring:message code="label.yes"/></option>				
<option value="false"><spring:message code="label.no"/></option>
</select>
	<script>
		$("#versioningConfiguration_active").val("${not empty param.active ? param.active : versioningConfiguration.active }");
	</script>	
</div>
</div>		
</div>
<div class="panel-footer">
	<input type="submit" class="btn btn-default" role="button" value="<spring:message code="label.search" />"/>
</div>
</form>
</div>


<c:choose>
	<c:when test="${not empty searchversioningconfigurationResultsDataSet}">
		<table id="searchversioningconfigurationTable" class="table responsive table-bordered table-hover">
			<thead>
				<tr>
					<%--!!!  Field names here --%>
<th><spring:message code="label.VersioningConfiguration.classname"/></th>
<th><spring:message code="label.VersioningConfiguration.active"/></th>
<%-- Operations Column --%>
					<th></th>
				</tr>
			</thead>
			<tbody>
				
			</tbody>
		</table>
	</c:when>
	<c:otherwise>
				<div class="alert alert-info" role="alert">
					
					<spring:message code="label.noResultsFound"/>
					
				</div>	
		
	</c:otherwise>
</c:choose>

<script>
	var searchversioningconfigurationDataSet = [
			<c:forEach items="${searchversioningconfigurationResultsDataSet}" var="searchResult">
				<%-- Field access here --%>
				[
"${searchResult.classname}",
"${searchResult.active}",
<c:if test="${not searchResult.active}">
"<a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/audittest/administration/manageversioning/versioningconfiguration/activate/${searchResult.externalId}\"><spring:message code='label.administration.manageVersioning.searchVersioningConfiguration.activate'/></a>"
</c:if>
<c:if test="${searchResult.active}">
"<a  class=\"btn btn-default btn-xs\" href=\"${pageContext.request.contextPath}/audittest/administration/manageversioning/versioningconfiguration/deactivate/${searchResult.externalId}\"><spring:message code='label.administration.manageVersioning.searchVersioningConfiguration.deactivate'/></a>"
</c:if>
],
            </c:forEach>
    ];
	
	$(document).ready(function() {

	


		var table = $('#searchversioningconfigurationTable').DataTable({language : {
			url : "${datatablesI18NUrl}",			
		},
		"data" : searchversioningconfigurationDataSet,
		// to choose the search box, please choose the following line		
// 		"dom": 'T<"clear">lfrtip', //Version with SearchBox
 		"dom": 'T<"clear">lrtip', //Version without SearchBox
        "tableTools": {
//to disable the export options coment the following line
            "sSwfPath": "//cdn.datatables.net/tabletools/2.2.3/swf/copy_csv_xls_pdf.swf"
        }
		});
		table.columns.adjust().draw();
		
		  $('#searchversioningconfigurationTable tbody').on( 'click', 'tr', function () {
		        $(this).toggleClass('selected');
		    } );
		  
	}); 
</script>

