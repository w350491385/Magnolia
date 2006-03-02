<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:cms="urn:jsptld:cms-taglib"
    xmlns:cmsu="urn:jsptld:cms-util-taglib"
    xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core"
    xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt">
    <jsp:directive.page contentType="text/html; charset=utf-8" />

    <jsp:text>
        <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
    </jsp:text>
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
    <c:import url="/templates/jsp/samples/global/head.jsp" />
    </head>
    <body>
        <c:import url="/templates/jsp/samples/global/mainBar.jsp" />
        <div id="contentDivMainColumn">
            <c:import url="/templates/jsp/samples/global/columnMain.jsp" />

            <form name="mgnlsearch" action=""><input id="query" name="query"
                value="${param.query}" /> <input type="submit" name="search"
                value="search" /></form>


            <c:if test="${!empty(param.query)}">
                <h1>Search results for:</h1>
                <h2>${param.query}</h2>

                <cmsu:simpleSearch query="${param.query}" var="results" />

                <c:if test="${empty(results)}">
                    <p>No results</p>
                </c:if>
                <c:forEach var="node" items="${results}">
                    <div class="searchresult">
                        <h4>${node.title}</h4>
                        <p>
                            <cmsu:searchResultSnippet query="${param.query}" page="${node}" />
                        </p>
                        <a href="${pageContext.request.contextPath}${node.handle}.html">${pageContext.request.contextPath}${node.handle}.html</a>
                        <em>last modification date: <fmt:formatDate dateStyle="full" value="${node.metaData.modificationDate.time}" /></em>
                    </div>
                </c:forEach>
            </c:if>

            <c:import url="/templates/jsp/samples/global/footer.jsp" />
        </div>
        <div id="contentDivRightColumn">
          <c:import url="/templates/jsp/samples/global/columnRight.jsp" />
        </div>
        <c:import url="/templates/jsp/samples/global/headerImage.jsp" />
        <cmsu:simpleNavigation />
    </body>
    </html>
</jsp:root>
