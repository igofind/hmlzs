<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>首页管理</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>

<script>
    var navPanel, bannerPanel, viewPort, ajaxSuccess,
            data = ${data};

    ajaxSuccess = function (response, options, successMsg) {

        var resp = Ext.JSON.decode(response.responseText);
        var result = resp['result'];
        successMsg = Ext.isEmpty(successMsg) ? '操作成功' : successMsg;
        switch (result) {
            case 'success' :
                Ext.Msg.alert(alertTitle, successMsg, function (button, text, opt) {
                    // 自定义
                }).setIcon(Ext.Msg.INFO);

                return;
            case 'login' :
                if (window.parent == window) {
                    window.location.reload();
                } else {
                    window.parent.location.reload();
                }
                return;
            case 'noRight':
                Ext.Msg.alert(alertTitle, '权限不足！').setIcon(Ext.Msg.ERROR);
                return;
            default:
                Ext.Msg.alert(alertTitle, resp).setIcon(Ext.Msg.ERROR);
                return;
        }
    };

    Ext.onReady(function () {

        navPanel = Ext.create("Ext.form.Panel", {

                    title: '导航更新',
                    width: 500,
                    height: 430,
                    defaultType: 'textarea',
                    bodyPadding: '20 0 20 20',
                    fieldDefaults: {
                        enableKeyEvents: true,
                        autoFitErrors: false,
                        labelAlign: 'right',
                        labelWidth: 65,
                        msgTarget: 'side',
                        anchor: '100%',
                        padding: '0 0 7 0'
                    },
                    items: [
                        {
                            id: 'navField',
                            fieldLabel: '导航(主)',
                            labelAlign: 'top',
                            height: 200,
                            name: 'navMain',
                            value: data[0],
                            allowBlank: false,
                            blankText: "此项不能为空",
                            emptyText: '请填写每个导航项的名称和链接地址以分号结尾:     首页,http://www.hmlzs.com;'
                        },
                        {
                            fieldLabel: '导航(次)',
                            labelAlign: 'top',
                            height: 100,
                            name: 'navSecond',
                            value: data[1]
                        }
                    ],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        ui: 'footer',
                        margin: '0 0 15 0',
                        layout: {pack: 'center'},
                        defaults: {minWidth: 75},
                        items: [
                            {
                                text: '应用',
                                handler: function (self, e) {
                                    Ext.Msg.confirm(alertTitle, "确定要将当前内容应用到首页么(若当前内容有修改请先保存)？", function (buttonId, text, opt) {
                                        if (buttonId == 'ok' || buttonId == 'yes') {

                                            Ext.Ajax.request({
                                                url: 'homepage/staticizeNav',
                                                method: 'POST',
                                                success: ajaxSuccess,
                                                failure: ajaxFailure
                                            });
                                        }
                                    }).setIcon(Ext.Msg.WARNING);
                                }
                            },
                            {
                                text: '重置',
                                handler: function (self, e) {
                                    navPanel.getForm().reset();
                                }
                            },
                            {
                                text: '保存',
                                handler: function (self, e) {
                                    var form = navPanel.getForm();
                                    if (form.isValid()) {

                                        form.submit({
                                            url: 'homepage/updateNav',
                                            method: 'POST',
                                            submitEmptyText: false,

                                            success: function (form, action) {

                                                var result = action.result.result;
                                                switch (result) {
                                                    case 'success' :
                                                        Ext.Msg.alert(alertTitle, "信息已更新!", function () {
                                                            window.location.reload();
                                                        }).setIcon(Ext.Msg.INFO);
                                                        return;
                                                    case 'login' :
                                                        if (window.parent == window) {
                                                            window.location.reload();
                                                        } else {
                                                            window.parent.location.reload();
                                                        }
                                                        return;
                                                    case 'noRight':
                                                        Ext.Msg.alert(alertTitle, '权限不足！').setIcon(Ext.Msg.ERROR);
                                                        return;
                                                    default:
                                                        Ext.Msg.alert(alertTitle, action.response.responseText).setIcon(Ext.Msg.ERROR);
                                                        return;
                                                }
                                            },
                                            failure: submitFailureFn
                                        });
                                    }
                                }
                            }
                        ]
                    }]
                }
        );

        bannerPanel = Ext.create("Ext.form.Panel", {

                    title: 'banner更新',
                    width: 500,
                    height: 220,
                    buttonAlign: 'center',
                    defaultType: 'textarea',
                    bodyPadding: '20 0 20 20',
                    fieldDefaults: {
                        enableKeyEvents: true,
                        autoFitErrors: false,
                        labelAlign: 'right',
                        labelWidth: 65,
                        msgTarget: 'side',
                        anchor: '100%',
                        padding: '0 0 7 0'
                    },
                    items: [
                        {
                            id: 'bannerField',
                            fieldLabel: 'banner',
                            labelAlign: 'top',
                            height: 100,
                            name: 'bannerStr',
                            value: data[2],
                            allowBlank: false,
                            blankText: '此项不能为空',
                            emptyText: '请填写banner的链接地址,多个地址间用分号相隔如: http://www.hmlzs.org/xxx/banner-1.jpg;'
                        }
                    ],
                    buttons: [
                        {
                            text: '应用',
                            handler: function (self, e) {
                                Ext.Msg.confirm(alertTitle, "确定要将当前内容应用到首页么(若当前内容有修改请先保存)？", function (buttonId, text, opt) {
                                    if (buttonId == 'ok' || buttonId == 'yes') {

                                        Ext.Ajax.request({
                                            url: 'homepage/staticizeBanner',
                                            method: 'POST',
                                            success: ajaxSuccess,
                                            failure: ajaxFailure
                                        });
                                    }
                                }).setIcon(Ext.Msg.WARNING);
                            }
                        },
                        {
                            text: '重置',
                            handler: function (self, e) {
                                bannerPanel.getForm().reset();
                            }
                        },
                        {
                            text: '保存',
                            handler: function (self, e) {
                                var form = bannerPanel.getForm();
                                if (form.isValid()) {

                                    form.submit({
                                        url: 'homepage/updateBanner',
                                        method: 'POST',
                                        submitEmptyText: false,

                                        success: function (form, action) {

                                            var result = action.result.result;
                                            switch (result) {
                                                case 'success' :
                                                    Ext.Msg.alert(alertTitle, "信息已更新!", function () {
                                                        window.location.reload();
                                                    }).setIcon(Ext.Msg.INFO);
                                                    return;
                                                case 'login' :
                                                    if (window.parent == window) {
                                                        window.location.reload();
                                                    } else {
                                                        window.parent.location.reload();
                                                    }
                                                    return;
                                                case 'noRight':
                                                    Ext.Msg.alert(alertTitle, '权限不足！').setIcon(Ext.Msg.ERROR);
                                                    return;
                                                default:
                                                    Ext.Msg.alert(alertTitle, action.response.responseText).setIcon(Ext.Msg.ERROR);
                                                    return;
                                            }
                                        },
                                        failure: submitFailureFn
                                    });
                                }
                            }
                        }
                    ]
                }
        );

        futianPanel = Ext.create("Ext.form.Panel", {

                    title: '广种福田',
                    width: 500,
                    height: 430,
                    defaultType: 'textfield',
                    bodyPadding: '20 0 20 10',
                    fieldDefaults: {
                        enableKeyEvents: true,
                        autoFitErrors: false,
                        labelAlign: 'right',
                        labelWidth: 45,
                        msgTarget: 'side',
                        anchor: '100%',
                        padding: '0 0 7 0'
                    },
                    items: [
                        {
                            fieldLabel: '描述',
                            xtype: 'textarea',
                            height: 160,
                            name: 'content',
                            value: data[3][0],
                            allowBlank: false,
                            blankText: "此项不能为空"
                        },
                        {
                            fieldLabel: '用途',
                            name: 'usage',
                            value: data[3][1],
                            allowBlank: false,
                            blankText: "此项不能为空"
                        },
                        {
                            fieldLabel: '开户行',
                            name: 'bank',
                            value: data[3][2],
                            allowBlank: false,
                            blankText: "此项不能为空"
                        },
                        {
                            fieldLabel: '户名',
                            name: 'account',
                            value: data[3][3],
                            allowBlank: false,
                            blankText: "此项不能为空"
                        },
                        {
                            fieldLabel: '账号',
                            name: 'number',
                            value: data[3][4],
                            allowBlank: false,
                            blankText: "此项不能为空"
                        }
                    ],
                    dockedItems: [{
                        xtype: 'toolbar',
                        dock: 'bottom',
                        ui: 'footer',
                        margin: '0 0 15 0',
                        layout: {pack: 'center'},
                        defaults: {minWidth: 75},
                        items: [
                            {
                                text: '应用',
                                handler: function (self, e) {
                                    Ext.Msg.confirm(alertTitle, "确定要将当前内容应用到首页么, 此操作将会同时应用代表法务的文章(若当前内容有修改请先保存)？", function (buttonId, text, opt) {
                                        if (buttonId == 'ok' || buttonId == 'yes') {

                                            Ext.Ajax.request({
                                                url: 'homepage/staticizeFutian',
                                                method: 'POST',
                                                success: ajaxSuccess,
                                                failure: ajaxFailure
                                            });
                                        }
                                    }).setIcon(Ext.Msg.WARNING);
                                }
                            },
                            {
                                text: '重置',
                                handler: function (self, e) {
                                    futianPanel.getForm().reset();
                                }
                            },
                            {
                                text: '保存',
                                handler: function (self, e) {
                                    var form = futianPanel.getForm();
                                    if (form.isValid()) {

                                        form.submit({
                                            url: 'homepage/updateFutian',
                                            method: 'POST',
                                            submitEmptyText: false,

                                            success: function (form, action) {

                                                var result = action.result.result;
                                                switch (result) {
                                                    case 'success' :
                                                        Ext.Msg.alert(alertTitle, "信息已更新!", function () {
                                                            window.location.reload();
                                                        }).setIcon(Ext.Msg.INFO);
                                                        return;
                                                    case 'login' :
                                                        if (window.parent == window) {
                                                            window.location.reload();
                                                        } else {
                                                            window.parent.location.reload();
                                                        }
                                                        return;
                                                    case 'noRight':
                                                        Ext.Msg.alert(alertTitle, '权限不足！').setIcon(Ext.Msg.ERROR);
                                                        return;
                                                    default:
                                                        Ext.Msg.alert(alertTitle, action.response.responseText).setIcon(Ext.Msg.ERROR);
                                                        return;
                                                }
                                            },
                                            failure: submitFailureFn
                                        });
                                    }
                                }
                            }
                        ]
                    }]
                }
        );

        otherPanel = Ext.create("Ext.form.Panel", {

                    title: '其他',
                    width: 500,
                    height: 220,
                    buttonAlign: 'center',
                    defaultType: 'button',
                    bodyPadding: '20 40 20 20',
                    defaults: {
                        margin: 10
                    },
                    items: [
                        {
                            text: '应用到活动通知',
                            handler: function (self, e) {
                                Ext.Msg.confirm(alertTitle, "确定要更新首页的通知码？", function (buttonId, text, opt) {
                                    if (buttonId == 'ok' || buttonId == 'yes') {

                                        Ext.Ajax.request({
                                            url: 'homepage/staticizeNotice',
                                            method: 'POST',
                                            success: ajaxSuccess,
                                            failure: ajaxFailure
                                        });
                                    }
                                }).setIcon(Ext.Msg.WARNING);
                            }
                        },
                        {
                            text: '应用到紫云法务',
                            handler: function (self, e) {
                                Ext.Msg.confirm(alertTitle, "确定要将当前内容应用到首页么, 此操作将会同时应用代表法务的文章(若当前内容有修改请先保存)？", function (buttonId, text, opt) {
                                    if (buttonId == 'ok' || buttonId == 'yes') {

                                        Ext.Ajax.request({
                                            url: 'homepage/staticizeFawu',
                                            method: 'POST',
                                            success: ajaxSuccess,
                                            failure: ajaxFailure
                                        });
                                    }
                                }).setIcon(Ext.Msg.WARNING);
                            }
                        },
                        {
                            xtype: 'button',
                            text: '应用文章列表页面',
                            id: 'btn-articleList',
                            handler: function (self, e) {
                                Ext.Msg.confirm("操作确认", "此操作将会将导航、banner、法务等应用到文章列表和图片列表页面，仍然继续？", function (buttonId, text, opt) {
                                    if (buttonId == 'yes' || buttonId == 'ok') {

                                        Ext.Ajax.request({
                                            url: 'article/listStatic',
                                            method: "POST",
                                            success: ajaxSuccess,
                                            failure: ajaxFailure
                                        });
                                    }
                                }).setIcon(Ext.Msg.WARNING);
                            }
                        },
                        {
                            text: '应用到联系我们',
                            handler: function (self, e) {
                                Ext.Msg.confirm(alertTitle, "此操作将会更新\"联系我们\"中的\"详细\"到对应的文章, 确定继续？", function (buttonId, text, opt) {
                                    if (buttonId == 'ok' || buttonId == 'yes') {
                                        Ext.Ajax.request({
                                            url: 'homepage/staticizeFutian',
                                            method: 'POST',
                                            success: ajaxSuccess,
                                            failure: ajaxFailure
                                        });
                                    }
                                }).setIcon(Ext.Msg.WARNING);
                            }
                        }
                    ]
                }
        );

        viewPort = Ext.create('Ext.container.Viewport', {
            layout: 'center',
            items: [
                {
                    xtype: 'container',
                    layout: 'table',
                    columns: 2,
                    defaults: {
                        margin: 10
                    },
                    items: [
                        {
                            xtype: 'container',
                            defaults: {
                                draggable: true,
                                collapsible: true,
                                modal : true
                            },
                            items: [navPanel, bannerPanel]
                        },
                        {
                            xtype: 'container',
                            defaults: {
                                draggable: true,
                                collapsible: true,
                                modal : true
                            },
                            items: [futianPanel, otherPanel]
                        }
                    ]
                }
            ]
        });
    });

</script>

</body>
</html>