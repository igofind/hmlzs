<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>权限错误</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>
<script>
    Ext.onReady(function () {

        Ext.Msg.show({
            title: '权限错误',
            message: '对不起, 您没有权限访问该页面！',
            width: 300,
            closable: false,
            modal: true,
            fixed: true,
            resizable: false,
            icon: Ext.Msg.ERROR
        });

    });
</script>
</body>
</html>