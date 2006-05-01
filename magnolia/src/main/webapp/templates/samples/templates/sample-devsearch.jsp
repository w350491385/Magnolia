<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" xmlns:cms="urn:jsptld:cms-taglib"
  xmlns:cmsu="urn:jsptld:cms-util-taglib" xmlns:c="urn:jsptld:http://java.sun.com/jsp/jstl/core">
  <jsp:directive.page contentType="text/html; charset=UTF-8" session="false" />
  <jsp:directive.page import="info.magnolia.cms.core.search.Query" />
  <jsp:directive.page import="info.magnolia.cms.core.search.QueryResult" />
  <jsp:directive.page import="info.magnolia.cms.beans.runtime.MgnlContext" />
  <jsp:directive.page import="info.magnolia.cms.beans.config.ContentRepository" />
  <jsp:text>
    <![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> ]]>
  </jsp:text>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en">
    <head>
      <c:import url="/templates/samples/templates/inc/head.jsp" />
    </head>
    <body>
      <cms:mainBar paragraph="samplesPageProperties" />
      <div id="contentDivMainColumn">
        <c:choose>
          <c:when test="${!empty(param.sql)}">
            <c:set var="formvalue" value="${param.sql}" />
          </c:when>
          <c:otherwise>
            <c:set var="formvalue" value="SELECT * FROM nt:base where jcr:path like '/%' and title like '%'" />
          </c:otherwise>
        </c:choose>
        <form name="mgnlsearch" action="">
          <textarea id="sql" name="sql" cols="40" rows="10">${formvalue}</textarea>
          <select name="language">
            <option value="sql">sql</option>
            <c:choose>
              <c:when test="${param.language == 'xpath'}">
                <option value="xpath" selected="selected">xpath</option>
              </c:when>
              <c:otherwise>
                <option value="xpath">xpath</option>
              </c:otherwise>
            </c:choose>
          </select>
          <input type="submit" name="search" value="search" />
        </form>
        <c:if test="${!empty(param.sql)}">
          <h1>Search results for:</h1>
          <h2>${param.sql}</h2>
          <c:catch var="exc">
            <jsp:scriptlet>
              String sql = request.getParameter("sql"); String language = request.getParameter("language"); Query q =
              MgnlContext.getQueryManager(ContentRepository.WEBSITE).createQuery(sql, language); QueryResult result =
              q.execute(); pageContext.setAttribute("result", result.getContent("mgnl:content").iterator());
            </jsp:scriptlet>
            <h3>Resulting objects of NodeType (mgnl:content)</h3>
            <c:forEach var="node" items="${result}">
              <strong>${node.title}</strong>
              <br />
              <a href="${pageContext.request.contextPath}${node.handle}.html">
                ${pageContext.request.contextPath}${node.handle}.html
              </a>
              <br />
            </c:forEach>
            <h3>Resulting objects of NodeType (mgnl:contentNode)</h3>
            <jsp:scriptlet>
              pageContext.setAttribute("result", result.getContent("mgnl:contentNode").iterator());
            </jsp:scriptlet>
            <c:forEach var="node" items="${result}">
              <strong>${node.title}</strong>
              <br />
              <em>${node.handle}</em>
              <br />
            </c:forEach>
          </c:catch>
          <c:if test="${!empty(exc)}">
            <h1>${exc.message}</h1>
          </c:if>
        </c:if>
        <div id="footer">
          <cms:adminOnly>
            <fmt:message key="buttons.editfooter" var="label" />
            <cms:editButton label="${label}" paragraph="samplesPageFooter" contentNodeName="footerPar" />
          </cms:adminOnly>
          <cms:ifNotEmpty nodeDataName="footerText" contentNodeName="footerPar">
            <p>
              <cms:out nodeDataName="footerText" contentNodeName="footerPar" />
            </p>
          </cms:ifNotEmpty>
          <a href="http://www.magnolia.info">
            <img src="${pageContext.request.contextPath}/docroot/samples/imgs/poweredSmall.gif" />
          </a>
        </div>
      </div>
      <div id="contentDivRightColumn">
        <cms:contentNodeIterator contentNodeCollectionName="rightColumnParagraphs">
          <div style="clear:both;">
            <cms:editBar adminOnly="true" />
            <cms:includeTemplate />
          </div>
        </cms:contentNodeIterator>
        <cms:adminOnly>
          <div style="clear:both;">
            <cms:newBar contentNodeCollectionName="rightColumnParagraphs" paragraph="samplesRightColumn" />
          </div>
        </cms:adminOnly>
      </div>
      <div style="position:absolute;left:0px;top:0px;">
        <cms:out var="headerImage" nodeDataName="headerImage" inherit="true" />
        <cms:out var="headerImageAlt" nodeDataName="headerImageAlt" inherit="true" />
        <c:if test="${empty(headerImage)}">
          <c:set var="headerImage" value="/docroot/samples/imgs/header.jpg" />
          <c:set var="headerImageAlt" value="Magnolia - for content management" />
        </c:if>
        <img src="${pageContext.request.contextPath}${headerImage}" alt="${headerImageAlt}" />
      </div>
      <cmsu:simpleNavigation />
    </body>
  </html>
</jsp:root>