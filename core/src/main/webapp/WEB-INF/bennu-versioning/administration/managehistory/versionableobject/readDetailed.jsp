<%@page import="com.qubit.solution.fenixedu.bennu.versioning.util.VersioningConstants"%>
<%@page import="com.qubit.solution.fenixedu.bennu.versioning.ui.administration.manageHistory.HistoryRetrieverController"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="datatables" uri="http://github.com/dandelion/datatables"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="pf"  uri="http://example.com/placeFunctions"%>

<spring:url var="datatablesUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.latest.min.js" />
<spring:url var="datatablesBootstrapJsUrl" value="/javaScript/dataTables/media/js/jquery.dataTables.bootstrap.min.js"></spring:url>
<script type="text/javascript" src="${datatablesUrl}"></script>
<script type="text/javascript" src="${datatablesBootstrapJsUrl}"></script>
<spring:url var="datatablesCssUrl" value="/CSS/dataTables/dataTables.bootstrap.min.css" />
<link rel="stylesheet" href="${datatablesCssUrl}" />
<spring:url var="datatablesI18NUrl" value="/javaScript/dataTables/media/i18n/${portal.locale.language}.json" />

<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/CSS/dataTables/dataTables.bootstrap.min.css" />


${portal.angularToolkit()} 

<link href="${pageContext.request.contextPath}/static/bennuversioning/css/dataTables.responsive.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/static/bennuversioning/js/dataTables.responsive.js"></script>
<link href="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/css/dataTables.tableTools.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/datatables-tools/2.2.4/js/dataTables.tableTools.js"></script>
<link href="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/css/select2.min.css" rel="stylesheet" />
<script src="${pageContext.request.contextPath}/webjars/select2/4.0.0-rc.2/dist/js/select2.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/bootbox/4.4.0/bootbox.js"></script>
<script src="${pageContext.request.contextPath}/static/bennuversioning/js/omnis.js"></script>

<script src="${pageContext.request.contextPath}/webjars/angular-sanitize/1.3.11/angular-sanitize.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.css" />
<script src="${pageContext.request.contextPath}/webjars/angular-ui-select/0.11.2/select.min.js"></script>


<%-- Constants --%>
<c:set var="FF_QUB_OPERATION_KIND" value="<%= VersioningConstants.FF_QUB_OPERATION_KIND %>" />
<c:set var="FF_QUB_CLASS_NAME" value="<%= VersioningConstants.FF_QUB_CLASS_NAME %>" />
<c:set var="FF_QUB_ROLE_NAME" value="<%= VersioningConstants.FF_QUB_ROLE_NAME %>" />
<c:set var="FF_QUB_TX_NUMBER" value="<%= VersioningConstants.FF_QUB_TX_NUMBER %>" />
<c:set var="FF_QUB_ROLE_OPERATION_KIND" value="<%= VersioningConstants.FF_QUB_ROLE_OPERATION_KIND %>" />
<c:set var="ROLE_ADDED_OBJECT" value="<%= VersioningConstants.ROLE_ADDED_OBJECT %>" />
<c:set var="ROLE_REMOVED_OBJECT" value="<%= VersioningConstants.ROLE_REMOVED_OBJECT %>" />

<%-- TITLE --%>
<div class="page-header">
    <h1>
        <spring:message code="label.manageHistory.readVersionableObject" />
        <small></small>
    </h1>
</div>

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
<%-- NAVIGATION --%>
<div class="well well-sm" style="display: inline-block">
    <span class="glyphicon glyphicon-arrow-left" aria-hidden="true"></span>
    &nbsp;
    <a class="" href="${pageContext.request.contextPath}<%=HistoryRetrieverController.SEARCH_URL%>">
        <spring:message code="label.event.back" />
    </a> 
    &nbsp;
</div>

<style>
.table-no-wrap {
    table-layout:fixed;
}

.table-no-wrap th {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis ;
}

.table-no-wrap td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis ;
}

.table-no-wrap td:hover {
    background: white;
    position: relative;
    z-index: 1;
    display: inline-block;
}

.table-no-wrap th:hover {
    background: white;
    position: relative;
    z-index: 1;
    display: inline-block;
}

</style>

<script>
    angular
    .module('angularAppHistoryRetriever', [ 'ngSanitize', 'ui.select','bennuToolkit' ])
    .controller(
            'HistoryRetrieverController',
            [
                '$scope',
                function($scope) {
                	 $scope.objectMapJson = [];
                	 $scope.updateDate;
                	 $scope.postBackUrl;
                	 $scope.postBack = createAngularPostbackFunction($scope);
                	 $scope.processViewDetails = function(externalId, objectMap, model) {
                         $scope.postBackUrl = "${pageContext.request.contextPath}<%= HistoryRetrieverController.CHECK_VERSION_URL%>" + externalId;
                         $scope.updateDate = objectMap['label.Versioning.updateDate'];
                         $scope.opType = objectMap['<%= VersioningConstants.FF_QUB_OPERATION_KIND %>'];
                         console.log($scope.updateDate);
                         $scope.postBack(model);
                         console.log($scope.object);
                		 url = "${pageContext.request.contextPath}<%= HistoryRetrieverController.READ_URL%>" + externalId;
                 	     $scope.objectMapJson = objectMap;
                 	     $("#viewForm").attr("action", url);
                	     $('#viewModal').modal('toggle');
                	 };                	
                } ]);
</script>

<form id="viewForm" name='form' method="get" class="form-horizontal"
    ng-app="angularAppHistoryRetriever" ng-controller="HistoryRetrieverController"
    action='#'>

    <input type="hidden" name="postback"
        value="{{ postBackUrl }}" />

    <input name="updateDate" type="hidden" value="{{ updateDate }}" />


<div class="modal fade" id="viewModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">
                    <spring:message code="label.version" />
                </h4>
                <h5 class="text-danger" ng-hide="object == 'true' || object == 'null' && opType == 'DELETE'">
                    <spring:message code="label.version.not.last"/>
                </h5>
                <h5 class="text-primary" ng-show="object == 'true' || object == 'null' && opType == 'DELETE'">
                    <spring:message code="label.version.last"/>
                </h5>
            </div>
            <div class="modal-body">
                <table class="table table-no-wrap">
                    <tbody>
                        <tr ng-repeat="(key, value) in objectMapJson">
                            <th scope="row">{{key}}</th>
                            <td>{{value}}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="modal-footer">
                <button id="closeButton" type="button" class="btn btn-default" data-dismiss="modal">
                    <spring:message code="label.close" />
                </button>
                <button id="viewButton" class="btn btn-primary" type="submit" ng-disabled="object == 'null'">
                    <spring:message code="label.view" />
                </button>
            </div>
        </div>
        <!-- /.modal-content -->
    </div>
    <!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.Versioning.info" />
        </h3>
    </div>
    <div class="panel-body">
        <table class="table">
            <tbody>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creator" /></th>
                    <td><c:out value='${versionableObject.versioningCreator}' /></td>
                </tr>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.creationDate" /></th>
                    <td><joda:format value="${versionableObject.versioningCreationDate}" style="SM" /></td>
                </tr>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.updatedby" /></th>
                    <td><c:out value='${versionableObject.versioningUpdatedBy}' /></td>
                </tr>
                <tr>
                    <th scope="row" class="col-xs-3"><spring:message code="label.Versioning.updateDate" /></th>
                    <td><joda:format value="${versionableObject.versioningUpdateDate}" style="SM" /></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<c:forEach items="${ retrieveModificationsInVersions }" var="version" varStatus="loop">
<div class="panel panel-primary">
    <div class="panel-heading">
        <h3 class="panel-title">
            <spring:message code="label.version" />
            <c:out value="${ loop.index + 1 }" />
        </h3>
    </div>
    <div class="panel-body">
        <table class="table">
            <tbody>
                <c:set var="txNumber" value="${ version.get(FF_QUB_TX_NUMBER) }" />
                <c:set var="relatedObjects" value="${ retrieveRelatedObjectsVersions.get(txNumber) }" />                    
                <c:forEach items="${ version }" var="entry">
                    <tr>
                        <th scope="row" class="col-xs-3">
                            <spring:message code="${ entry.key }" />
                        </th>
                        <td>
                            <c:if test="${ entry.value.getClass().getName().equals('java.lang.Long') }">
                                <a target="_blank" href="${pageContext.request.contextPath}<%= HistoryRetrieverController.READ_URL%>${ entry.value }"><c:out value="${ entry.value }" /></a>
                            </c:if>
                            <c:if test="${ not entry.value.getClass().getName().equals('java.lang.Long') }">
                                <c:out value="${ entry.value }" />
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                <c:forEach items="${ relatedObjects }" var="role">
                    <tr>
                        <th>
                            <c:out value="${ role.key }" />
                        </th>
                        <td>
                        <table>
                            <c:forEach items="${ role.value }" var="object">
                                <c:if test="${ object.get(FF_QUB_ROLE_OPERATION_KIND) == ROLE_ADDED_OBJECT}">
                                    <p class="text-success">
                                        <spring:message code="label.Versioning.object.added" />
                                </c:if>
                                <c:if test="${ object.get(FF_QUB_ROLE_OPERATION_KIND) == ROLE_REMOVED_OBJECT}">
                                    <p class="text-danger">
                                        <spring:message code="label.Versioning.object.removed" />
                                </c:if>
                                [
                                <a href="#" ng-click="processViewDetails('${object.get('OID')}', { <c:forEach items='${ object }' var='entry'> '${entry.key}' : '${entry.value}', </c:forEach> }, $model )">
                                    ${ object.get('OID') }
                                </a>
                                ]
                                <c:choose>
                                    <c:when test="${ object.get(FF_QUB_OPERATION_KIND) == 'CREATE' }">
                                        <spring:message code="label.Versioning.object.created" />
                                    </c:when>
                                    <c:when test="${ object.get(FF_QUB_OPERATION_KIND) == 'UPDATE' }">
                                        <spring:message code="label.Versioning.object.updated" />
                                    </c:when>
                                    <c:when test="${ object.get(FF_QUB_OPERATION_KIND) == 'DELETE' }">
                                        <spring:message code="label.Versioning.object.deleted" />
                                    </c:when>
                                </c:choose>
                                </p>
                            </c:forEach>
                        </table>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</c:forEach>

</form>
<script>
$(document).ready(function() {
	
}); 
</script>

