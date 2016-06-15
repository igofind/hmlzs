<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>账号管理</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <link rel="stylesheet" href="${resURLPrefix}/static/base/ext/css/ItemSelector.css">
    <jsp:include page="_script.jspf"></jsp:include>

    <script src="${resURLPrefix}/static/base/ext/js/MultiSelect.js"></script>
    <script src="${resURLPrefix}/static/base/ext/js/ItemSelector.js"></script>
</head>
<body>
<script>
    var accountListStore, gridPanel, viewport,
            form, formWindow, formResetFn,
            roleStore, roleForm, roleFormSubmit, roleFormWindow,
            itemSelector, hasRoles = [],
            getSelectValue, stopSpaceKeyFn, ajaxSuccess;

    Ext.onReady(function () {

        stopSpaceKeyFn = function (self, e, eOpts) {
            if (e.event.keyCode == 32) {
                e.stopEvent();
            }
        };

        var accountFiled = [
            'id',
            'account',
            'name',
            'depict',
            'phone',
            'mail',
            'roles',
            'manager',
            'status',
            'createDate',
            'updateDate',
            'lastLoginDate'
        ];

        accountListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'accountListStore',
            autoLoad: true,
            fields: accountFiled,
            pageSize: pageSize,

            proxy: {
                type: 'ajax',
                url: 'account/listAll',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data',
                    totalProperty: 'totalData'
                },
                extraParams: {
                    pageSize: pageSize
                }
            },
            listeners: {
                load: storeLoadListenerFn
            }
        });

        ajaxSuccess = function (response, options, successMsg) {

            var resp = Ext.JSON.decode(response.responseText);
            var result = resp['result'];
            successMsg = Ext.isEmpty(successMsg) ? '操作成功' : successMsg;
            switch (result) {
                case 'success' :
                    Ext.Msg.alert(alertTitle, successMsg, function (button, text, opt) {
                        // 自定义
                        if (gridPanel.getSelectionModel()) gridPanel.getSelectionModel().deselectAll();
                        accountListStore.reload();
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

        gridPanel = Ext.create("Ext.grid.Panel", {
            scrollable: true,
            autoScroll: true,
            loadMask: true, // loading tip
            region: 'center',
            border: false,
            selModel: {
                type: 'checkboxmodel',
                listeners: {
                    selectionchange: function (self, selected, eOpts) {
                        var count = self.getCount();
                        var isAccept = false, isReject = false,
                                isManager = false;

                        Ext.each(self.getSelection(), function (item, index, allItems) {
                            var status = parseInt(item.get("status"), 10);
                            var manager = parseInt(item.get("manager"), 10);

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }

                            if (manager == 1) {
                                isManager = true;
                            }
                        });

                        // Ext.getCmp("btn-delete").setDisabled(count < 1);
                        // Ext.getCmp("btn-save").setDisabled(count < 1);

                        Ext.getCmp("btn-upgrade").setDisabled(count < 1 || isManager);
                        Ext.getCmp("btn-degrade").setDisabled(count < 1 || !isManager);
                        Ext.getCmp("btn-resetPwd").setDisabled(count < 1);

                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                        Ext.getCmp("btn-roles").setDisabled(count < 1);
                    }
                }
            },
            store: accountListStore,
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    flex: 0.4
                },
                {
                    text: '账号',
                    dataIndex: 'account',
                    flex: 0.6
                },
                {
                    text: '姓名',
                    dataIndex: 'name',
                    flex: 0.6
                },
                {
                    text: '描述',
                    dataIndex: 'depict',
                    flex: 1
                },
                {
                    text: '手机',
                    dataIndex: 'phone',
                    flex: 1
                },
                {
                    text: '邮箱',
                    dataIndex: 'mail',
                    flex: 1
                },
                {
                    text: '管理者',
                    dataIndex: 'manager',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        switch (value) {
                            case 1:
                                return '<span style="color: red">是</span>';
                            case 0:
                                return '否';
                            default :
                                return value;
                        }
                    },
                    flex: 0.5
                },
                {
                    text: '角色',
                    dataIndex: 'roles',
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
                                return '<span style="color: green">已解锁</span>';
                            case -1:
                                return '<span style="color: red">已锁定</span>';
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
                },
                {
                    text: '更新时间',
                    dataIndex: 'updateDate',
                    flex: 1
                },
                {
                    text: '上次登录时间',
                    dataIndex: 'lastLoginDate',
                    flex: 1
                }
            ],
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1
            },
            tbar: [
                {
                    xtype: 'button',
                    id: 'btn-add',
                    text: '新增',
                    glyph: 'xf055@FontAwesome',
                    handler: function (self, e) {
                        formWindow.show();
                    }
                },
                '-',
                {
                    xtype: 'button',
                    id: 'btn-accept',
                    text: '解锁',
                    glyph: 'xf09c@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'account/accept',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: function (response, options) {

                                ajaxSuccess(response, options, '操作成功！账户已解锁！');
                            },
                            failure: ajaxFailure
                        });
                    }
                },
                {
                    xtype: 'button',
                    text: '锁定',
                    id: 'btn-reject',
                    glyph: 'xf023@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'account/reject',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: function (response, options) {

                                ajaxSuccess(response, options, '操作成功！账户已锁定！');
                            },
                            failure: ajaxFailure
                        });

                    }
                },
                {
                    xtype: 'button',
                    text: '重置密码',
                    id: 'btn-resetPwd',
                    glyph: 'xf0e2@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }
                        Ext.Msg.confirm("操作确认", "如非必要请勿重置账户密码，是否继续？", function (buttonId, text, opt) {

                            if (buttonId == 'yes' || buttonId == 'ok') {

                                Ext.Ajax.request({
                                    url: 'account/resetPwd',
                                    method: "POST",
                                    params: {
                                        data: Ext.JSON.encode(data)
                                    },
                                    success: function (response, options) {

                                        ajaxSuccess(response, options, '操作成功！账户密码已重置！');
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
                    id: 'btn-upgrade',
                    text: '升级',
                    glyph: 'xf0aa@FontAwesome',
                    tooltip: '将账户升级为管理者账户',
                    tooltipType: 'title',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'account/upgrade',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: function (response, options) {
                                ajaxSuccess(response, options, '操作成功！账户已升级为管理者帐户！');
                            },
                            failure: ajaxFailure
                        });
                    }
                },
                {
                    xtype: 'button',
                    text: '降级',
                    id: 'btn-degrade',
                    glyph: 'xf0ab@FontAwesome',
                    tooltip: '将账户降级为普通账户',
                    tooltipType: 'title',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'account/degrade',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: function (response, options) {
                                ajaxSuccess(response, options, '操作成功！账户已降级为普通帐户！');
                            },
                            failure: ajaxFailure
                        });

                    }
                },
                '-',
                {
                    xtype: 'button',
                    id: 'btn-roles',
                    glyph: 'xf0e8@FontAwesome',
                    text: '角色',
                    tooltip: '为帐户分配角色',
                    tooltipType: 'title',
                    disabled: true,
                    handler: function (self, e) {
                        roleFormWindow.show();
                    }
                }
            ],
            bbar: Ext.create('Ext.PagingToolbar', {
                store: accountListStore,
                displayInfo: true,
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

        // full screen
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [gridPanel]
        });

        form = Ext.create('Ext.form.Panel', {
            width: 310,
            defaultType: 'textfield',
            defaults: {
                labelWidth: 55,
                msgTarget: 'side',
                enableKeyEvents: true,
                autoFitErrors: false,
                width: 310
            },
            items: [
                {
                    fieldLabel: '帐户',
                    name: 'account',
                    allowBlank: false,
                    emptyText: '字母或下划线开头+数字 , 3~10位',
                    validator: function (val) {
                        var regex = /^[a-zA-z][a-zA-Z0-9_]{2,9}$/;
                        return regex.test(val) ? true : '字母或下划线开头+数字 , 3~10位';
                    },
                    listeners: {
                        keydown: stopSpaceKeyFn
                    }
                },
                {
                    fieldLabel: '姓名',
                    name: 'name',
                    allowBlank: false,
                    emptyText: '此项必须填写',
                    maxLength: 200,
                    maxLengthText: "长度不能超过200",
                    blankText: '',
                    validator: function (val) {
                        var regex = /^([a-zA-Z]|[\u4e00-\u9fa5]){1,20}$/;
                        return regex.test(val) ? true : '请填写正确的中(英)文名字';
                    },
                    listeners: {
                        keydown: stopSpaceKeyFn
                    }
                },
                {
                    fieldLabel: '手机',
                    name: 'phone',
                    validator: function (val) {
                        return (val == '' || /^(((13[0-9]|14[57]|15[012356789]|17[0678]|18[0-9])[0-9]{8})|(1349[0-9]{7}))$/.test(val));
                    },
                    listeners: {
                        keydown: stopSpaceKeyFn
                    }
                },
                {
                    fieldLabel: '邮箱',
                    name: 'mail',
                    maxLength: 200,
                    maxLengthText: "长度不能超过200",
                    listeners: {
                        keydown: stopSpaceKeyFn
                    }
                },
                {
                    fieldLabel: '描述',
                    name: 'depict',
                    maxLength: 200,
                    maxLengthText: "长度不能超过200",
                    xtype: 'textarea',
                    listeners: {
                        keydown: stopSpaceKeyFn
                    }
                }
            ]

        });

        formWindow = Ext.create('Ext.window.Window', {
            // height: 350,
            width: 360,
            title: '添加账户',
            layout: 'fit',
            modal: true,
            resizable: false,
            anchorSize: '100%',
            scrollable: false,
            autoDestroy: true,
            constrain: true,
            bodyPadding: '15 15 10 20',
            closeAction: "hide",
            items: [form],
            buttonAlign: 'center',
            buttons: [
                {
                    text: '重置',
                    handler: function () {
                        formResetFn();
                    }
                },
                {
                    text: '提交',
                    handler: function () {
                        if (form.isValid()) {

                            form.getForm().submit({
                                clientValidation: true,
                                submitEmptyText: false,
                                url: '/cn/admin/account/create',
                                method: 'POST',

                                success: function (_form, action) {

                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :
                                            Ext.Msg.alert(alertTitle, '创建成功!').setIcon(Ext.Msg.INFO);
                                            formWindow.hide();
                                            // reload the list
                                            accountListStore.reload();
                                            return;
                                        case 'exit' :
                                            Ext.Msg.alert(alertTitle, '创建失败,该账号已存在!').setIcon(Ext.Msg.ERROR);
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
                beforeShow: function (self, eOpts) {
                    formResetFn();
                }
            }
        });

        roleStore = Ext.create('Ext.data.JsonStore', {
            fields: ['id', 'depict', {name: 'userHas', type: 'boolean'}],
            proxy: {
                type: 'ajax',
                url: 'account/role/filterList',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data'
                }
            },
            listeners: {
                load: function (self, records, successful, operation, eOpts) {

                    storeLoadListenerFn(self, records, successful, operation, eOpts);

                    if (getSelectValue() != null) {
                        var userHasRoles = [];
                        var leftRoles = [];
                        Ext.each(records, function (item, index, allItems) {

                            if (item.data['userHas']) {
                                userHasRoles.push(item);
                            } else {
                                leftRoles.push(item);
                            }
                        });

                        if (hasRoles.length == 0) {
                            hasRoles = userHasRoles;
                        }

                        itemSelector.setValue(hasRoles);
                        self.setData(leftRoles);
                    }
                }
            }
        });

        itemSelector = Ext.create({
            xtype: 'itemselector',
            name: 'roleIds',
            id: 'role-itemselector-field',
            height: 260,
            margin: '10 0 0 0',
            imagePath: '${resURLPrefix}/static/base/ext/images/',
            store: roleStore,
            fromStorePopulated: true,
            displayField: 'depict',
            valueField: 'id',
            // allowBlank: false,
            msgTarget: 'side',
            fromTitle: '可选角色',
            toTitle: '已选角色'
        });

        roleForm = Ext.create('Ext.form.Panel', {
            bodyPadding: 10,
            items: [
                {
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    items: [
                        {
                            id: 'addRoleSearch',
                            xtype: 'textfield',
                            width: 190,
                            emptyText: '角色过滤条件'
                        },
                        {
                            xtype: 'button',
                            id: 'search-btn',
                            text: '查询',
                            margin: '0 0 0 5',
                            oldSearch: '',
                            handler: function (self, e) {
                                var searchCmp = Ext.getCmp('addRoleSearch');
                                if (!Ext.isEmpty(Ext.String.trim(searchCmp.getValue()))) {
                                    if (this.oldSearch != searchCmp.getValue()) {
                                        roleStore.load({
                                            params: {
                                                str: searchCmp.getValue(),
                                                accountId: getSelectValue()
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
                                Ext.getCmp('addRoleSearch').setValue('');
                                roleStore.load({
                                    params: {
                                        accountId: getSelectValue()
                                    }
                                });
                            }
                        }
                    ]
                },
                itemSelector
            ]
        });

        roleFormWindow = Ext.create({
            xtype: 'window',
            title: '角色编辑',
            modal: true,
            width: 600,
            height: 410,
            resizable: false,
            layout: 'fit',
            closeAction: 'hide',
            items: [roleForm],
            buttonAlign: 'center',
            buttons: [
                {
                    text: '重置',
                    handler: function (self, e) {
                        roleForm.getForm().reset();
                        Ext.getCmp('search-btn')['oldSearch'] = '';
                        roleStore.load({
                            params: {
                                accountId: getSelectValue()
                            }
                        });
                    }
                },
                {
                    text: '提交',
                    handler: function (self, e) {
                        if (itemSelector.getValue() == null || itemSelector.getValue().length < 1) {
                            Ext.Msg.confirm("操作确认", "您未选择任何角色(将会删除账户的所有角色)，确定继续么？", function (buttonId, text, opt) {
                                if (buttonId == 'yes' || buttonId == 'ok') {
                                    roleFormSubmit();
                                }
                            }).setIcon(Ext.Msg.WARNING);
                        } else {
                            roleFormSubmit();
                        }
                    }
                }
            ],
            listeners: {
                beforehide: function (self, e) {
                    roleForm.getForm().reset();
                    hasRoles = [];
                },
                beforeshow: function (self, eOpts) {
                    roleStore.load({
                        params: {
                            accountId: getSelectValue()
                        }
                    });
                }
            }
        });

        roleFormSubmit = function () {

            roleForm.getForm().submit({
                url: 'account/addRole',
                submitEmptyText: false,

                params: {
                    accountId: getSelectValue()
                },
                success: function (form, action) {

                    var result = action.result.result;
                    switch (result) {
                        case 'success' :
                            Ext.Msg.alert(alertTitle, '角色分配成功!', function () {

                                roleFormWindow.hide();
                                accountListStore.reload();

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

        getSelectValue = function () {

            var selects = gridPanel.getSelection();

            if (selects && selects.length == 1) {
                return selects[0].data['id'];
            } else {
                var da = [];
                Ext.each(selects, function (item, index, allItem) {
                    da.push(item.data['id']);
                });
                return da;
            }
        };

        formResetFn = function () {
            form.getForm().reset();
        };
    });
</script>

</body>
</html>