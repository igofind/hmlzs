<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>黄梅老祖寺搜索</title>
    <meta name="author" content="4what"/>
    <meta name="description" content="黄梅老祖寺"/>
    <meta name="keywords" content="黄梅老祖寺"/>

    <meta http-equiv="Expires" content="0">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-control" content="no-cache">
    <meta http-equiv="Cache" content="no-cache">

    <link rel="stylesheet" type="text/css" href="http://hmlzs.com/article/static/lzs/css/style.css"/>
    <style>
        .s_tool_select{
            font-size: 12px;
        }
        .s_tool_btn{
            cursor:pointer;
            font-size: 14px;
        }
        .s_tool_panel{
            display: none;
        }

    </style>
</head>

<body>
<div class="main">
    <div class="container">
        <div class="topbar">
            <div class="wrapper">
		<span class="welcome">
			<em>欢迎来到黄梅四祖寺首页</em>
			<span class="datetime"></span>
            </span>
		<span class="tools">
			<a href="javascript: void(0);" onclick="addFavorite();">收藏本站</a> |
			<a href="javascript: void(0);" onclick="setHome();">设为首页</a> |
			<a href="#contact">来访路线</a> |
			<a href="javascript: void(0);">微信公众号</a>
		</span>
            </div>
        </div>

        <div class="header">
            <div class="wrapper">
                <h1><a href="/"><img src=http://hmlzs.com/article/static/lzs/images/logo.png alt="黄梅老祖寺"/></a></h1>

                <form action="search" method="get" id="search-frm" class="search">
                        <div class="keyword"><input type="text" name="kw" value="${keyword}"/></div>
                        <input type="submit" name="" value="搜索" class="submit"/>
                </form>
                <div class="qrcode"></div>
            </div>
        </div>

<div class="navbar">
    <div class="wrapper" id="navbar">
        <script type="text/javascript" src="http://hmlzs.com/article/base/nav.html"></script>
    </div>
</div>
<div class="content">
    <div class="notice">
        <span class="icon left"></span>
        <p>
            <strong>活动通知</strong>
            <script type="text/javascript" src="http://hmlzs.com/article/base/notice.html"></script>
        </p>
        <span class="icon right"></span>
    </div>
    <div class="breadcrumb">
    </div>

    <div class="layer2 layer2-1 article-list">
        <h2>
            <span>
                <em style="font-size:14px"> 本次为您找到相关结果约<strong>${resultPage.searchResult.count}</strong>个</em>
                <em style="margin-left: 48%;" class="s_tool_panel">
                    <select class="s_tool_select sort">
                        <option value="score" selected>按相关度排序</option>
                        <option value="date" ${"date".equals(sort)?"selected":""}>按发布时间排序</option>
                    </select>
                    <select class="s_tool_select category">
                        <option value="" selected>-类别不限-</option>
                        <c:forEach items="${categorys}" var="category">
                           <option value="${category.id}" ${c==category.id?"selected":""} >${category.depict}</option>
                        </c:forEach>
                    </select>
                    <select class="s_tool_select timerange">
                        <option value="" ${t==""?"selected":""}>-时间不限-</option>
                        <option value="1" ${t==1?"selected":""}>一天内</option>
                        <option value="7" ${t==7?"selected":""}>一周内</option>
                        <option value="30" ${t==30?"selected":""}>一月内</option>
                        <option value="365" ${t==365?"selected":""}>一年内</option>
                    </select>
                </em>
                <em style="margin-left: 85%;" class="s_tool_btn">
                    ◇搜索工具
                </em>
            </span>
        </h2>
        <dl>

            <c:choose>
                <c:when test="${resultPage.searchResult.count==0}">
                    <div style="color: #999999">
                        找不到和 <strong style="color: red">${keyword}</strong> 相符的内容或信息。
                        <p>建议您：请检查输入字词有无错误。请换用另外的查询字词。请改用较短、较为常见的字词。</p>
                    </div>
                </c:when>
                <c:otherwise>

                    <c:forEach items="${resultPage.searchResult.list}" var="article">
                        <dt>
                                  <span>
                                      <a target="_blank" href="${article.url}"> <c:out value="${article.title}" escapeXml="false"/></a>
                                  </span>
                                <em><fmt:formatDate value="${article.publishDate}" type="date" dateStyle="long" pattern="[yyyy-MM-dd]"/></em>
                        </dt>
                        <dd>
                            <c:out value="${article.content}" escapeXml="false"/><a target="_blank" href="${article.url}">[详细]</a>
                        </dd>
                    </c:forEach>

                </c:otherwise>
            </c:choose>

        </dl>

        <div class="paginator"></div>
    </div>

    <div class="rightside">
        <script type="text/javascript" src="http://hmlzs.com/article/base/fawu.html"></script>
        <script type="text/javascript" src="http://hmlzs.com/article/base/futian.html"></script>
    </div>
</div>
<!-- /content -->

<!-- footer -->
<div class="footer">
    &copy; 2006-<span id="year">0</span> 黄梅老祖寺 鄂ICP备06004601号
</div>

<!-- /container -->

<div class="gototop">
    <a href="#"><span>回顶部</span><em></em></a>
</div>
</div>
</div>
<!-- /main -->
<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/header.js></script>

<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/jquery/jquery-1.8.3.min.js></script>

<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/jquery/plugin/jquery.cycle.all.js></script>

<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/audiojs/audio.min.js></script>

<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/jquery.qrcode.min.js></script>



<!-- page analytics -->
<script type="text/javascript" src=http://hmlzs.com/article/static/lzs/js/analytics.js></script>

<script type="text/javascript">
    $('.qrcode:eq(0)').qrcode({width: 64,height: 64,text: window.location.href})
    $('#code').qrcode({width: 80,height: 80,text: window.location.href})
    $('.qrcode > canvas').css({'margin-top': 10});
</script>

<script type="text/javascript">

    var scroll = function (target, options) {
        var defaults = {
                    duration: 0, // {Number|String}
                    offset: 0, // {Number}
                    position: "bottom" // {String} "bottom|top"
                },
                settings = $.extend(defaults, options);

        target = $(target);

        var position = (function () {
                    if (settings.position === "bottom") {
                        var outerHeight = target.outerHeight(true);
                        return function () {
                            return $(window).height() - outerHeight - settings.offset;
                        };
                    } else {
                        return function () {
                            return settings.offset;
                        };
                    }
                })(),

                prop = function () {
                    return {
                        "top": $(window).scrollTop() + position()
                        + "px" // for IE
                    };
                },

                handler = (function () {
                    return !settings.duration ?
                            function () {
                                target.css(prop());
                            } :
                            function () {
                                target.stop().animate(prop(), settings.duration);
                            };
                })();

        $(window).bind("load resize scroll", handler);
    }

    // gototop
    scroll($(".gototop"), {
        // duration: "slow",
        offset: 16
    });

    // header
    document.getElementsByClassName("datetime")[0].innerText = " " + getCalStr();
    // footer
    document.getElementById("year").innerText = new Date().getFullYear();
</script>
<script>
    /**
     *点击类别搜索
     */
    document.title = "${keyword}_黄梅老祖寺搜索";
    if($(".s_tool_panel .category").val()!=''||$(".s_tool_panel .timerange").val()!=''||$(".s_tool_panel .sort").val()!="score"){
        $(".s_tool_btn").html("◆收起工具").show();
        $(".s_tool_panel").show("fast");
    }
    $(".s_tool_btn").click(function(){
        if($(".s_tool_panel:visible").length){
            $(".s_tool_panel").hide("slow");
            $(".s_tool_btn").html("◇搜索工具")
        }else{
            $(".s_tool_btn").html("◆收起工具")
            $(".s_tool_panel").show("fast");
        }
    });
    $(".s_tool_select").change(function(){
        search();
    });

    function search(){
        var kw = $("#search-frm .keyword input").val();
        var url = document.location.href;
        var index = url.indexOf("?");
        url = url.substr(0,index+1)
        var param={};
        param.kw=kw;
        param.c=$(".s_tool_panel .category").val();;
        param.t=$(".s_tool_panel .timerange").val();
        param.sort=$(".s_tool_panel .sort").val();
        location = url + jQuery.param(param);
    }
    $('#navbar > ul > li').removeClass("current");
    var pageCnt = ${resultPage.pageCount};
    var page = ${resultPage.currentPage};

    var listUrl = url();
    function url(){
        var url = document.location.href;
        var index = url.indexOf("?");
        var param = url.substr(index+1);
        return url.substr(0,index+1)+param.replace(/&pn=[\d]*/,"")+"&pn="
    }

</script>
<script type="text/javascript" src="http://hmlzs.com/article/static/lzs/js/paginate.js"></script>
</body>
</html>
