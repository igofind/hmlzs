<%@ page import="com.core.security.UserInfo" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
    userInfo = (userInfo == null) ? new UserInfo() : userInfo;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Welcome</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>
    <script src="${resURLPrefix}/static/base/ext/js/TabCloseMenu.js"></script>

    <style type="text/css">
        #my-ul {
            list-style-type: none;
            margin: 38px 0 0 0;
            float: right;
        }

        #my-ul li {
            display: inline;
            padding-right: 20px;
        }

        .x-grid-cell-inner-treecolumn, .x-panel-header.x-header {
            cursor: pointer;
        }

        .x-panel-header-title-default > .x-title-icon-wrap-default > .x-title-glyph {
            color: #A7A7A7;
        }
    </style>
</head>
<body>
<script>

    Ext.onReady(function () {

        var refreshTimeFn,treeStore;

        treeStore = Ext.create('Ext.data.TreeStore', {
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: 'tree/nav',//请求
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    root: 'children'//数据
                }
            },
            root: {
                text: '管理菜单',
                expanded: true
            },
            listeners: {
                load: function (self, records, successful, operation, node, eOpts) {
                    var resp = operation.getResponse().responseText;
                    if (resp == "redirect:login") {
                        if (window.parent == window) {
                            window.location.reload();
                        } else {
                            window.parent.location.reload();
                        }
                    }
                }
            }
        });

        Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [
                {
                    id: "app-header",
                    region: 'north',
                    border: false,
                    height: 60,
                    bodyStyle: {
                        background: 'url(${resURLPrefix}/static/lzs/images/logo.png) 0px 1px no-repeat rgb(255, 255, 255)'
                    },
                    html: '<ul id="my-ul" class="my-list">' +
                    '<li id="item0"></li>' +
                    '<li id="item1"><span class="fa fa-user">&nbsp;</span><%=userInfo.getAccount()%> , 欢迎您</li>' +
                    '<li id="item2"><span class="fa fa-sign-out">&nbsp;</span><a href="logout" target="_self">退出登录</a></li>' +
                    '</ul>'
                },
                {
                    region: 'west',
                    collapsible: true,
                    title: '主菜单',
                    glyph: 'xf0c9@FontAwesome',
                    split: true,
                    width: 200,
                    xtype: 'panel',
                    layout: {
                        type: 'accordion',
                        animate: true
                    },

                    defaults: {
                        xtype: "treepanel",
                        autoScroll: true,
                        border: false,
                        containerScroll: true,
                        rootVisible: false,
                        listeners: {
                            itemclick: function (view, record, item, index, e, eOpts) {

                                var text = record.get('text'); // the treeNode's display name
                                var target = record.get('target');
                                if (!record.isRoot()) {
                                    // Ext.Msg.alert('click', 'you click the leaf, and list the article by ' + record.get('target') + ' type');
                                    var tabpanel = Ext.getCmp('center-tabpanel');

                                    var newPanel = null;
                                    var targetId = target.substring(target.lastIndexOf("=")) + 1;
                                    // title need to be unique, or the child's selector must use a single attr,like id.
                                    if (!(newPanel = Ext.getCmp('panel-' + targetId))) {
                                        newPanel = tabpanel.add({
                                            title: text,
                                            id: 'panel-' + targetId,
                                            closable: true,
                                            loadMask: true,
                                            html: '<iframe src="' + target + '" style="border: 0; width: 100%; height: 100% "></iframe>'
                                        });
                                    }
                                    tabpanel.setActiveTab(newPanel);
                                }
                            }
                        }
                    },
                    items: [
                        {
                            id: 'navTree',
                            title: 'Tree',
                            glyph: 'xf1bb@FontAwesome',
                            store: treeStore,
                            dockedItems: [
                                {
                                    xtype: 'toolbar',
                                    dock: 'top',
                                    items: [
                                        {
                                            tooltip: '展开',
                                            tooltipType: 'title',
                                            glyph: 'xf115@FontAwesome',
                                            handler: function (button, e) {
                                                Ext.getCmp('navTree').expandAll();
                                            }
                                        },
                                        {
                                            tooltip: '折叠',
                                            tooltipType: 'title',
                                            glyph: 'xf114@FontAwesome',
                                            handler: function (button, e) {
                                                Ext.getCmp('navTree').collapseAll();
                                            }
                                        },
                                        {
                                            tooltip: '刷新',
                                            tooltipType: 'title',
                                            glyph: 'xf01e@FontAwesome',
                                            handler: function (button, e) {
                                                treeStore.reload();
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            title: '内容管理',
                            glyph: 'xf02d@FontAwesome',
                            id: 'sysTree',
                            root: {
                                children: [
                                    {
                                        text: "文章检索",
                                        leaf: true,
                                        target: 'articleSearch'
                                    },
                                    {
                                        text: "媒体管理",
                                        leaf: true,
                                        target: 'mediaList'
                                    },
                                    {
                                        text: "Tree管理",
                                        leaf: true,
                                        target: 'treeList'
                                    },
                                    {
                                        text: "栏目管理",
                                        leaf: true,
                                        target: 'categoryList'
                                    },
                                    {
                                        text: "模板管理",
                                        leaf: true,
                                        target: 'templateList'
                                    },
                                    {
                                        text: "首页管理",
                                        leaf: true,
                                        target: 'navbanner'
                                    }
                                ]
                            }
                        },
                        {
                            title: '账户资料',
                            glyph: 'xf09d@FontAwesome',
                            id: 'userInfo',
                            root: {
                                children: [
                                    {
                                        text: '资料更新',
                                        leaf: true,
                                        target: 'accountInfo'
                                    },
                                    {
                                        text: '密码修改',
                                        leaf: true,
                                        target: 'accountPwd'
                                    }
                                ]
                            }
                        }
                        <% if(userInfo.isManager()) {%>
                        , {
                            title: '系统安全',
                            // glyph: 'xf0ad@FontAwesome',
                            glyph: 'xf085@FontAwesome',
                            id: 'sysSecurity',
                            root: {
                                expanded: false,
                                children: [
                                    {
                                        text: '日志管理',
                                        leaf: true,
                                        target: 'logList'
                                    },
                                    {
                                        text: '账户管理',
                                        leaf: true,
                                        target: 'accountList'
                                    },
                                    {
                                        text: '角色管理',
                                        leaf: true,
                                        target: 'roleList'
                                    },
                                    {
                                        text: '权限管理',
                                        leaf: true,
                                        target: 'rightList'
                                    }
                                ]
                            }
                        }
                        <%}%>
                    ],
                    listeners: {}
                },
                {
                    id: "center-tabpanel",
                    region: 'center',
                    xtype: 'tabpanel', // TabPanel itself has no title
                    activeTab: 0,      // First tab active by default
                    border: false,
                    plugins: Ext.create("Ext.ux.TabCloseMenu", {
                        closeTabText: '关闭',
                        closeOthersTabsText: '关闭其他',
                        closeAllTabsText: '关闭所有'
                    }),
                    items: {
                        title: 'Default Tab',
                        glyph: 'xf015@FontAwesome',
                        html: '<p style="text-indent:2em;">系统时间: <span id="now-date">' + Ext.Date.format(new Date(), 'Y-m-d h:m:s') + '</span><br/>' + '<p style="text-indent:2em;font-weight: 700">欢迎使用老祖寺后台管理系统！</p>'
                    }
                }
            ]
        });

        refreshTimeFn = function () {
            var span = Ext.get("now-date");
            span.setHtml(Ext.Date.format(new Date(), 'Y-m-d H:m:s'));
            Ext.get("item0").setHtml('<span class="fa fa-clock-o">&nbsp;</span>' + Ext.Date.format(new Date(), 'Y-m-d H:m:s'));
        };
        setInterval(refreshTimeFn, 1000);

        /* when the dom is loaded , execute the refreshTimeFn function once */
        refreshTimeFn();

    });
</script>

</body>
</html>

