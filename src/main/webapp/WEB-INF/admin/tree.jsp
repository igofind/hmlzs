<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>Tree管理</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>

</head>
<body>
<script>
    var treeListStore,
            gridPanel, viewport,
            form, formWindow, formResetFn, ajaxSuccess;

    Ext.onReady(function () {

        var treeFiled = [
            'id',
            'name',
            'parentId',
            'hasChild',
            'childs',
            'treepath',
            'storepath',
            'fullStorePath',
            'status',
            'createDate',
            'updateDate'
        ];

        treeListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'treeListStore',
            autoLoad: true,
            fields: treeFiled,
            pageSize: pageSize,

            proxy: {
                type: 'ajax',
                url: 'tree/list',
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
                        if (gridPanel.getSelectionModel()) gridPanel.getSelectionModel().deselectAll();
                        // 自定义
                        treeListStore.reload();
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
                        var isAccept = false, isReject = false;

                        Ext.each(self.getSelection(), function (item, index, allItems) {
                            var status = parseInt(item.get("status"), 10);

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }
                        });

                        Ext.getCmp("btn-delete").setDisabled(count < 1);
                        Ext.getCmp("btn-save").setDisabled(count < 1);
                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                    }
                }
            },
            store: treeListStore,
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    flex: 0.4
                },
                {
                    text: 'Tree名 <span style="color: red">+</span>',
                    dataIndex: 'name',
                    editor: {
                        xtype: 'textfield',
                        maxLength: 200,
                        maxLengthText: "长度不能超过200",
                        allowBlank: false
                    },
                    flex: 0.6
                },

                {
                    text: 'Tree路径',
                    dataIndex: 'treeRoad',
                    flex: 1
                },
                /*{
                    text: '存储路径 <span style="color: red">+</span>',
                    dataIndex: 'storePath',
                    hidden: true,
                    editor: {
                        xtype: 'textfield',
                        validator: function (val) {
                            var pathRegex = /^([a-zA-Z]|[0-9])+$/;
                            if (Ext.isEmpty(val)) {
                                return true;
                            }
                            return pathRegex.test(val) ? true : '请填写合法的路径：字母和数字的组合';
                        }
                    },
                    flex: 0.6
                },
                {
                    text: '存储全路径',
                    dataIndex: 'fullStorePath',
                    hidden: true,
                    flex: 1
                },*/
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
                },
                {
                    text: '更新时间',
                    dataIndex: 'updateDate',
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
                {
                    xtype: 'button',
                    id: 'btn-save',
                    text: '保存',
                    glyph: 'xf0c7@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            if (item.dirty) {
                                data.push(item.data);
                            }
                        });
                        if (data.length == 0) {
                            return;
                        }


                        Ext.Ajax.request({
                            url: 'tree/update',
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
                    text: '删除',
                    id: 'btn-delete',
                    glyph: 'xf00d@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        var data = [];
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }
                        Ext.Msg.confirm('操作确认', '数据删除后无法恢复，确认删除么？', function (buttonId, text, opt) {
                            if (buttonId == 'yes' || buttonId == 'ok') {

                                Ext.Ajax.request({
                                    url: 'tree/delete',
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
                    xtype: 'button',
                    text: '启用',
                    id: 'btn-accept',
                    glyph: 'xf00c@FontAwesome',
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
                            url: 'tree/accept',
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
                        Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                            data.push(item.data['id']);
                        });
                        if (data.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'tree/reject',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: ajaxSuccess,
                            failure: ajaxFailure
                        });

                    }
                }
            ],
            bbar: Ext.create('Ext.PagingToolbar', {
                store: treeListStore,
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

        //  viewport : Full Screen
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            scrollable: true,
            items: [gridPanel]
        });

        var createCombo = function (self, newValue, oldValue, eOpts) {
            var newRecord, count = 1, comboId = 'child-combo-';
            if (self.cls == 'ancestor-combo') {
                newRecord = self.getStore().findRecord('id', newValue);
            } else {
                newRecord = self.getStore().findRecord('id', newValue);
                var selfId = self.getId();
                count = Ext.Number.from(selfId.replace('child-combo-', ''), 0) + 1;
            }

            /*remove the child 'combo' at first*/
            form.remove(Ext.getCmp('child-combo-' + count));

            if (newRecord && newRecord.getData()["hasChild"]) {

                form.add({
                    xtype: 'combo',
                    fieldLabel: '子Tree',
                    id: comboId + count,
                    cls: 'childCombo',
                    queryMode: 'local',
                    editable: false,
                    valueField: 'id',
                    displayField: 'name',
                    store: Ext.create('Ext.data.JsonStore', {
                        storeId: 'subTreeListStore',
                        autoLoad: true,
                        fields: treeFiled,
                        pageSize: pageSize,
                        proxy: {
                            type: 'ajax',
                            url: 'tree/list',
                            actionMethods: {read: 'POST'},
                            reader: {
                                type: 'json',
                                rootProperty: 'data'
                            },
                            extraParams: {
                                pageSize: pageSize,
                                parentId: newValue
                            }
                        },
                        listeners: {
                            load: storeLoadListenerFn
                        }
                    }),
                    listeners: {
                        change: createCombo
                    }
                });
            }

            // change the savePath (parentId)
            Ext.getCmp("parentId").setValue(newValue);
        }

        form = Ext.create('Ext.form.Panel', {
            width: 300,
            defaultType: 'textfield',
            bodyPadding: '0 0 0 20',
            defaults: {
                labelWidth: 70,
                padding: '0 0 3 0',
                msgTarget: 'side'
            },
            items: [
                {
                    fieldLabel: 'Tree名称',
                    name: 'name',
                    allowBlank: false,
                    maxLength: 200,
                    maxLengthText: "长度不能超过200",
                    emptyText: 'Tree显示的名称',
                    blankText: '此项必须填写'
                },
                /*{
                    fieldLabel: '存储路径',
                    name: 'storePath',
                    emptyText: '默认为根目录 /',
                    validator: function (val) {
                        var pathRegex = /^([a-zA-Z]|[0-9])+$/;
                        if (Ext.isEmpty(val)) {
                            return true;
                        }
                        return pathRegex.test(val) ? true : '请填写合法的路径：字母和数字的组合';
                    }
                    // allowBlank: true
                },*/
                {
                    // xtype: 'checkboxfield',
                    xtype: 'hiddenfield',
                    id: 'auto-refresh',
                    fieldLabel: '刷新列表',
                    value: 1
                },
                {
                    fieldLabel: '父级Tree',
                    xtype: 'combo',
                    queryMode: 'local',
                    store: Ext.create('Ext.data.JsonStore', {
                        storeId: 'treeParentStore',
                        autoLoad: true,
                        fields: treeFiled,
                        pageSize: pageSize,

                        proxy: {
                            type: 'ajax',
                            url: 'tree/list',
                            actionMethods: {read: 'POST'},
                            reader: {
                                type: 'json',
                                rootProperty: 'data',
                                totalProperty: 'totalData'
                            },
                            extraParams: {
                                parentId: "0",
                                pageSize: pageSize
                            }
                        },
                        listeners: {
                            load: storeLoadListenerFn
                        }
                    }),
                    valueField: 'id',
                    displayField: 'name',
                    cls: 'ancestor-combo',
                    id: 'ancestor-combo',
                    name: '',
                    editable: false,
                    emptyText: '为空时，创建根Tree',
                    listeners: {
                        change: createCombo
                    }
                },
                {
                    xtype: 'hiddenfield',
                    id: 'parentId',
                    name: 'parentId',
                    value: '0'
                }
            ]
        });

        formWindow = Ext.create('Ext.window.Window', {
            // height: 350,
            width: 350,
            modal: true,
            title: '添加Tree',
            layout: 'fit',
            anchorSize: '100%',
            scrollable: false,
            autoDestroy: true,
            bodyPadding: 10,
            constrain: true,
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
                                url: '/cn/admin/tree/create',
                                method: 'POST',
                                submitEmptyText: false,

                                success: function (_form, action) {

                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :
                                            Ext.Msg.alert(alertTitle, '创建成功！').setIcon(Ext.Msg.INFO);
                                            formWindow.hide();

                                            var autoRefresh = Ext.getCmp('auto-refresh').getValue();
                                            if (autoRefresh) {
                                                // reload the list
                                                treeListStore.reload();
                                            }
                                            // reload the ancestor combo
                                            Ext.getCmp("ancestor-combo").getStore().reload();
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

        formResetFn = function () {
            var childCombo = Ext.query('.childCombo');
            var size = childCombo ? childCombo.length : 0;
            for (var i = 0; i < size; i++) {
                form.remove(Ext.getCmp('child-combo-' + (i + 1)));
            }
            form.getForm().reset();
        };
    });
</script>

</body>
</html>