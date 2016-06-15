<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>角色管理</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <style>
        .x-grid-record-right {
            background: #f0f0f0;
        }
    </style>
    <link rel="stylesheet" href="${resURLPrefix}/static/base/ext/css/ItemSelector.css">
    <jsp:include page="_script.jspf"></jsp:include>

    <script src="${resURLPrefix}/static/base/ext/js/MultiSelect.js"></script>
    <script src="${resURLPrefix}/static/base/ext/js/ItemSelector.js"></script>

</head>
<body>
<script>
    var roleListStore, treePanel, veiwPort,
            rightStore, addRoleForm, addRoleWindow, addRightForm, addRightWindow,
            getRootCheckedValueFn, btnResetFn, ajaxSuccess;
    Ext.onReady(function () {

        var roleFiled = [
            'key',
            'name',
            'depict',
            'status',
            'createDate'
        ];

        roleListStore = Ext.create('Ext.data.TreeStore', {
            storeId: 'roleListStore',
            autoLoad: true,
            fields: roleFiled,

            proxy: {
                type: 'ajax',
                url: 'role/listAll',
                actionMethods: {read: 'POST'}
            },
            listeners: {
                load: storeLoadListenerFn
            }
        });

        btnResetFn = function () {
            Ext.getCmp("btn-save").setDisabled(true);
            Ext.getCmp("btn-delete").setDisabled(true);

            Ext.getCmp("btn-add2").setDisabled(true);
            Ext.getCmp("btn-delete2").setDisabled(true);
            Ext.getCmp("btn-accept").setDisabled(true);
            Ext.getCmp("btn-reject").setDisabled(true);
        };

        getRootCheckedValueFn = function () {
            var checked = treePanel.getChecked();
            var rootChecked = [];
            Ext.each(checked, function (item, index, allItem) {
                if (!item.isLeaf()) {
                    rootChecked.push(item.data['key']);
                }
            })
            return rootChecked;
        }

        ajaxSuccess = function (response, options, successMsg) {

            var resp = Ext.JSON.decode(response.responseText);
            var result = resp['result'];
            successMsg = Ext.isEmpty(successMsg) ? '操作成功' : successMsg;
            switch (result) {
                case 'success' :
                    Ext.Msg.alert(alertTitle, successMsg, function (button, text, opt) {
                        // 自定义
                        roleListStore.reload();
                        btnResetFn();
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

        treePanel = Ext.create("Ext.tree.Panel", {
            scrollable: true,
            autoScroll: true,
            loadMask: true, // loading tip
            region: 'center',
            rootVisible: false,
            border: false,
            viewConfig: {
                getRowClass: function (record, rowIndex, rowParams, store) {
                    if (record.getData()['leaf']) {
                        return 'x-grid-record-right';
                    }
                }
            },
            listeners: {
                checkchange: function (node, checked, eOpts) {

                    var checkedArr = treePanel.getChecked();

                    var rootChecked = [], leafChecked = [], rootCnt, leafCnt;

                    var isAccept = false, isReject = false;

                    Ext.each(checkedArr, function (item, index, allItems) {

                        if (item.isLeaf()) {
                            leafChecked.push(item);
                        } else {
                            rootChecked.push(item);
                            var status = parseInt(item.get("status"), 10);

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }
                        }
                    });

                    rootCnt = rootChecked.length;
                    leafCnt = leafChecked.length;

                    Ext.getCmp("btn-delete").setDisabled(rootCnt < 1 || leafCnt > 0);
                    Ext.getCmp("btn-save").setDisabled(rootCnt < 1 || leafCnt > 0);

                    Ext.getCmp("btn-clone").setDisabled(rootCnt == 0);

                    Ext.getCmp("btn-accept").setDisabled(rootCnt < 1 || leafCnt > 0 || isAccept);
                    Ext.getCmp("btn-reject").setDisabled(rootCnt < 1 || leafCnt > 0 || isReject);

                    Ext.getCmp("btn-add2").setDisabled(rootCnt < 1 || leafCnt > 0);
                    Ext.getCmp("btn-delete2").setDisabled(leafCnt < 1 || rootCnt > 0);
                },
                afterrender: function (self, eOpts) {
                    var view = self.getView();
                    view.getRow();
                }
            },
            store: roleListStore,
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    xtype: 'hiddenfield',
                    flex: 0.4
                },
                {
                    text: '角色(功能)名',
                    xtype: 'treecolumn',
                    dataIndex: 'name',
                    flex: 0.6
                },
                {
                    text: '角色(功能)描述<span style="color: red"> +</span>',
                    editor: {
                        xtype: 'textarea',
                        allowBlank: false,
                        maxLength: 200,
                        maxLengthText: "长度不能超过200",
                    },
                    dataIndex: 'depict',
                    flex: 1
                },
                {
                    text: '状态',
                    dataIndex: 'status',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        switch (value) {
                            case 0:
                                return '正常';
                            case 1:
                                return '<span style="color: green">启用</span>';
                            case -1:
                                return '<span style="color: red">废弃</span>';
                            default :
                                return value;
                        }
                    },
                    flex: 0.4
                },
                {
                    text: '创建时间',
                    dataIndex: 'createDate',
                    flex: 1
                }
            ],
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1,
                listeners: {
                    beforeedit: function (editor, context, eOpts) {
                        if (context.record.data['leaf']) {
                            return false;
                        }
                    }
                }
            },
            tbar: {
                enableOverflow: true,
                items:[
                    {
                        xtype: 'button',
                        text: '添加角色',
                        id: 'btn-add',
                        glyph: 'xf055@FontAwesome',
                        handler: function (self, e) {
                            addRoleWindow.show();
                        }
                    },
                    {
                        xtype: 'button',
                        id: 'btn-clone',
                        text: '克隆角色',
                        glyph: 'xf0c5@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                data.push(item.data['key']);
                            });
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'role/clone',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, '克隆成功！');
                                },
                                failure: ajaxFailure
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        id: 'btn-save',
                        text: '保存',
                        glyph: 'xf0c7@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                if (item.dirty) {
                                    var itm = {};
                                    itm['id'] = item.data['key'];
                                    itm['name'] = item.data['name'];
                                    itm['depict'] = item.data['depict'];
                                    itm['status'] = item.data['status'];
                                    itm['createDate'] = item.data['createDate'];
                                    data.push(itm);
                                }
                            });
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'role/update',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, '更新成功！');
                                },
                                failure: ajaxFailure
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        text: '删除角色',
                        id: 'btn-delete',
                        glyph: 'xf00d@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                if (!item.isLeaf()) {
                                    data.push(item.data['key']);
                                }
                                data.push();
                            });
                            if (data.length == 0) {
                                return;
                            }
                            Ext.Msg.confirm("删除确认", "角色删除后无法恢复，继续删除？", function (buttonId, text, opt) {

                                if (buttonId == 'yes' || buttonId == 'ok') {

                                    Ext.Ajax.request({
                                        url: 'role/deleteRole',
                                        method: "POST",
                                        params: {
                                            data: Ext.JSON.encode(data)
                                        },
                                        success: function (response, options) {

                                            var resp = Ext.JSON.decode(response.responseText);
                                            var result = resp['result'];
                                            switch (result) {
                                                case 'success' :
                                                    Ext.Msg.alert(alertTitle, '删除成功！', function (button, text, opt) {
                                                        // 自定义
                                                        roleListStore.reload();
                                                        btnResetFn();
                                                    }).setIcon(Ext.Msg.INFO);
                                                    return;

                                                case 'using' :
                                                    var feedback = resp['feedback'];
                                                    var deleteMsg = '<div>删除失败！以下角色被使用中：<br/><span>(角色：账户)<br/></span>';

                                                    Ext.each(feedback, function (item, index, allItem) {
                                                        deleteMsg = deleteMsg +
                                                                '<span>' + item['depict'] + '：' + item['users'] + '</span><br/>';
                                                    });
                                                    deleteMsg = deleteMsg + '</div>';
                                                    Ext.Msg.alert(alertTitle, deleteMsg).setIcon(Ext.Msg.INFO);
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
                                        },
                                        failure: ajaxFailure
                                    });
                                }
                            }).setIcon(Ext.Msg.WARNING);
                        }
                    },
                    '-',
                    {
                        xtype: 'button',
                        text: '启用',
                        id: 'btn-accept',
                        glyph: 'xf00c@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                if (!item.isLeaf()) {
                                    data.push(item.data['key']);
                                }
                            });
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'role/accept',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: ajaxSuccess,
                                failure: ajaxFailure
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        text: '废弃',
                        id: 'btn-reject',
                        glyph: 'xf05e@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                if (!item.isLeaf()) {
                                    data.push(item.data['key']);
                                }
                            });
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'role/reject',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: ajaxSuccess,
                                failure: ajaxFailure
                            });

                        }
                    },
                    '-',
                    {
                        xtype: 'button',
                        text: '添加权限',
                        id: 'btn-add2',
                        glyph: 'xf055@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            addRightWindow.show();
                        }
                    },
                    {
                        xtype: 'button',
                        text: '删除权限',
                        id: 'btn-delete2',
                        glyph: 'xf00d@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {

                            var data = {};
                            Ext.each(treePanel.getChecked(), function (item, index, allItems) {
                                if (item.isLeaf()) {
                                    if (item.parentNode) {
                                        item.parentNode.expand();
                                    }
                                    var parentId = item.parentNode.data['key'];
                                    if (!data['' + parentId]) {
                                        data['' + parentId] = [];
                                    }
                                    data['' + parentId].push(item.data['key']);
                                }
                            });
                            if (!data) {
                                return;
                            }

                            Ext.Msg.confirm("删除确认", "角色删除后无法恢复，继续删除？", function (buttonId, text, opt) {
                                if (buttonId == 'yes' || buttonId == 'ok') {

                                    Ext.Ajax.request({
                                        url: 'role/deleteRight',
                                        method: "POST",
                                        params: {
                                            data: Ext.JSON.encode(data)
                                        },
                                        success: function (response, options) {
                                            ajaxSuccess(response, options, '删除成功！');
                                        },
                                        failure: ajaxFailure
                                    });
                                }
                            }).setIcon(Ext.Msg.WARNING);
                        }
                    },
                    '-',
                    {
                        text: '展开',
                        glyph: 'xf114@FontAwesome',
                        handler: function (button, e) {
                            var checked = treePanel.getChecked();
                            var noRootChecked = true;
                            if (checked && checked.length != 0) {
                                Ext.each(checked, function (item, index, allItems) {
                                    if (!item.isLeaf()) {
                                        item.expand();
                                        noRootChecked = false;
                                    }
                                })
                            } else {
                                treePanel.expandAll();
                            }
                            if (noRootChecked) {
                                treePanel.expandAll();
                            }
                        }
                    },
                    {
                        text: '折叠',
                        glyph: 'xf115@FontAwesome',
                        handler: function (button, e) {
                            var checked = treePanel.getChecked();
                            var noRootChecked = true;
                            if (checked && checked.length != 0) {
                                Ext.each(checked, function (item, index, allItems) {
                                    if (!item.isLeaf()) {
                                        item.collapse();
                                        noRootChecked = false;
                                    }
                                })
                            } else {
                                treePanel.collapseAll();
                            }
                            if (noRootChecked) {
                                treePanel.collapseAll();
                            }
                        }
                    }
                ]
            },
            bbar: Ext.create('Ext.PagingToolbar', {
                store: roleListStore,
                display提示: true,
                beforePageText: '第',
                afterPageText: '页 / 共 {0} 页',
                displayMsg: '当前第{0} ~ {1}条 - 共{2}条',
                emptyMsg: "没有数据",
                firstText: '首页',
                lastText: '尾页',
                nextText: '下一页',
                prevText: '上一页'
            })
        });

        /* make cover the whole screen the gridpanel */
        veiwPort = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [treePanel]
        });

        rightStore = Ext.create('Ext.data.ArrayStore', {
            fields: ['id', 'depict'],
            // autoLoad: true,
            proxy: {
                type: 'ajax',
                url: 'role/filterList',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data'
                }
            }
        });

        addRoleForm = Ext.create('Ext.form.Panel', {
            bodyPadding: 10,
            items: [
                {
                    xtype: 'fieldcontainer',
                    layout: 'vbox',
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            layout: 'hbox',
                            margin: '0 0 10 0',
                            width: 566,
                            defaults: {
                                labelWidth: 60,
                                labelAlign: 'top',
                                blankText: '不能为空',
                                msgTarget: 'side'
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    fieldLabel: '角色名',
                                    emptyText: '必填',
                                    allowBlank: false,
                                    maxLength: 200,
                                    maxLengthText: "长度不能超过200",
                                    blankText: '',
                                    validator: function (val) {
                                        var regex = /^[a-zA-Z]{1,100}[0-9]*$/;
                                        return regex.test(val) ? true : '请使用英文字母和数字命名';
                                    },
                                    padding: '0 26 0 0',
                                    name: 'name',
                                    flex: 1
                                },
                                {
                                    xtype: 'textfield',
                                    fieldLabel: '角色描述',
                                    name: 'depict',
                                    emptyText: '必填',
                                    maxLength: 200,
                                    maxLengthText: "长度不能超过200",
                                    allowBlank: false,
                                    padding: '0 0 0 0',
                                    flex: 1
                                }
                            ]
                        },
                        {
                            xtype: 'fieldcontainer',
                            layout: 'hbox',
                            defaults: {
                                margin: '0 6 0 0'
                            },
                            items: [
                                {
                                    id: 'addRoleSearch',
                                    xtype: 'textfield',
                                    width: 190,
                                    fieldLabel: '添加权限',
                                    labelAlign: 'top',
                                    emptyText: '权限描述'
                                },
                                {
                                    xtype: 'button',
                                    text: '查询',
                                    id: 'addRole-search-btn',
                                    margin: '26 6 0 0',
                                    oldSearch: '',
                                    handler: function (self, e) {
                                        var searchCmp = Ext.getCmp('addRoleSearch');
                                        if (!Ext.isEmpty(Ext.String.trim(searchCmp.getValue()))) {
                                            if (this.oldSearch != searchCmp.getValue()) {
                                                rightStore.reload({
                                                    params: {
                                                        roleId: "",
                                                        str: searchCmp.getValue(),
                                                        has: false
                                                    }
                                                });
                                                this.oldSearch = searchCmp.getValue();
                                            }
                                        } else {
                                            this.oldSearch = '';
                                        }
                                        searchCmp.focus();
                                    }
                                },
                                {
                                    xtype: 'button',
                                    margin: '26 6 0 0',
                                    glyph: 'xf021@FontAwesome',
                                    handler: function (self, e) {
                                        Ext.getCmp('addRoleSearch').setValue('');
                                        Ext.getCmp('addRole-search-btn')['oldSearch'] = '';
                                        rightStore.load();
                                    }
                                }
                            ]
                        }

                    ]
                },
                {
                    xtype: 'itemselector',
                    name: 'rightIds',
                    id: 'itemselector-field',
                    height: 260,
                    imagePath: '${resURLPrefix}/static/base/ext/images/',
                    store: rightStore,
                    fromStorePopulated: true,
                    displayField: 'depict',
                    valueField: 'id',
                    //allowBlank: false,
                    msgTarget: 'side',
                    fromTitle: '可选权限',
                    toTitle: '新增权限'
                }
            ]
        });

        addRoleWindow = Ext.create({
            xtype: 'window',
            title: '添加角色',
            modal: true,
            width: 600,
            height: 480,
            resizable: false,
            layout: 'fit',
            closeAction: 'hide',
            items: [addRoleForm],
            buttonAlign: 'center',
            buttons: [
                {
                    text: '重置',
                    handler: function (self, e) {
                        Ext.getCmp('addRole-search-btn')['oldSearch'] = '';
                        rightStore.load({
                            roleIds: null
                        });
                        addRoleForm.getForm().reset();
                    }
                },
                {
                    text: '提交',
                    handler: function (self, e) {
                        if (addRoleForm.getForm().isValid()) {

                            addRoleForm.getForm().submit({
                                url: 'role/create',
                                method: 'POST',
                                submitEmptyText: false,

                                success: function (form, action) {

                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :
                                            Ext.Msg.alert(alertTitle, '添加成功!', function () {

                                                addRoleWindow.hide();
                                                roleListStore.reload();
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
            ],
            listeners: {
                beforeshow: function (self, e) {
                    Ext.getCmp('addRole-search-btn')['oldSearch'] = '';
                    rightStore.load({
                        roleIds: null
                    });
                },
                beforehide: function (self, e) {
                    addRoleForm.getForm().reset();
                }
            }
        });

        addRightForm = Ext.create('Ext.form.Panel', {
            bodyPadding: 10,
            items: [
                {
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    items: [
                        {
                            id: 'addRightSearch',
                            xtype: 'textfield',
                            width: 190,
                            emptyText: '权限过滤条件'
                        },
                        {
                            xtype: 'button',
                            id: 'addRight-search-btn',
                            text: '查询',
                            margin: '0 0 0 5',
                            oldSearch: '',
                            handler: function (self, e) {
                                var searchCmp = Ext.getCmp('addRightSearch');
                                if (!Ext.isEmpty(Ext.String.trim(searchCmp.getValue()))) {
                                    if (this.oldSearch != searchCmp.getValue()) {
                                        rightStore.reload({
                                            params: {
                                                roleIds: getRootCheckedValueFn(),
                                                str: searchCmp.getValue()
                                            }
                                        });
                                        this.oldSearch = searchCmp.getValue();
                                    }
                                } else {
                                    this.oldSearch = '';
                                }
                                searchCmp.focus();
                            }
                        },
                        {
                            xtype: 'button',
                            glyph: 'xf021@FontAwesome',
                            margin: '0 0 0 5',
                            handler: function (self, e) {
                                Ext.getCmp('addRightSearch').setValue('');
                                Ext.getCmp('addRight-search-btn')['oldSearch'] = '';
                                rightStore.load({
                                    params: {
                                        roleIds: getRootCheckedValueFn()
                                    }
                                });
                            }
                        }
                    ]
                },
                {
                    xtype: 'itemselector',
                    name: 'rightIds',
                    id: 'addRight-itemselector-field',
                    height: 260,
                    margin: '10 0 0 0',
                    imagePath: '${resURLPrefix}/static/base/ext/images/',
                    store: rightStore,
                    fromStorePopulated: true,
                    displayField: 'depict',
                    valueField: 'id',
                    allowBlank: false,
                    msgTarget: 'side',
                    fromTitle: '可选权限',
                    toTitle: '新增权限'
                }
            ]
        });

        addRightWindow = Ext.create({
            xtype: 'window',
            title: '添加权限',
            modal: true,
            width: 600,
            height: 410,
            resizable: false,
            layout: 'fit',
            closeAction: 'hide',
            items: [addRightForm],
            buttonAlign: 'center',
            buttons: [
                {
                    text: '重置',
                    handler: function (self, e) {
                        addRightForm.getForm().reset();
                        Ext.getCmp('addRight-search-btn')['oldSearch'] = '';
                        rightStore.load({
                            roleIds: null
                        });
                    }
                },
                {
                    text: '提交',
                    handler: function (self, e) {
                        if (addRightForm.getForm().isValid()) {

                            addRightForm.getForm().submit({
                                url: 'role/addRight',
                                params: {
                                    roleIds: getRootCheckedValueFn()
                                },
                                submitEmptyText: false,

                                success: function (form, action) {

                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :
                                            Ext.Msg.alert(alertTitle, '添加成功!', function () {

                                                addRightWindow.hide();
                                                roleListStore.reload();
                                                btnResetFn();
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
            ],
            listeners: {
                beforehide: function (self, e) {
                    addRightForm.getForm().reset();
                    Ext.getCmp('addRight-itemselector-field').reset();
                },
                beforeshow: function (self, eOpts) {
                    var rootCheckedValue = getRootCheckedValueFn();
                    Ext.getCmp('addRight-search-btn')['oldSearch'] = '';
                    rightStore.load({
                        params: {
                            roleIds: rootCheckedValue
                        }
                    });
                }
            }
        });

    });
</script>

</body>
</html>