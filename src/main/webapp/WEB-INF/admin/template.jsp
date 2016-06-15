<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <title>template</title>
    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>
<script>
    Ext.onReady(function () {
        var templateFiled, templateListStore, gridPanel, viewport,
                form, formWindow, updateWindow,
                contentStore, updateForm, updateWindow, updateMask;

        templateFiled = [
            'id',
            'name',
            'path',
            'status',
            'createDate',
            'updateDate'
        ];

        templateListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'templateListStore',
            fields: templateFiled,
            pageSize: pageSize,
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: 'template/listAll',
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
                        templateListStore.reload();
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
                        var isAccept = false, isReject = false, isBuildIn = false;

                        Ext.each(self.getSelection(), function (item, index, allItems) {
                            var status = parseInt(item.get("status"), 10);

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }

                            if (status = -2) {
                                isBuildIn = true;
                            }
                        });

                        Ext.getCmp("btn-save").setDisabled(count < 1);
                        Ext.getCmp("btn-update").setDisabled(count != 1);
                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept || isBuildIn);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject || isBuildIn);

                    }
                }
            },
            store: templateListStore,
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1
            },
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    flex: 0.4
                },
                {
                    text: '名称 <span style="color:red;">+</span>',
                    dataIndex: 'name',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false
                    },
                    flex: 1
                },
                {
                    text: '地址',
                    dataIndex: 'path',
                    flex: 2
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
                            case -2:
                                return '<span style="color: orange">内建</span>';
                            default :
                                return value;
                        }
                    },
                    flex: 0.5
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
                            url: 'template/save',
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
                    id: 'btn-update',
                    text: '修改',
                    glyph: 'xf044@FontAwesome',
                    disabled: true,
                    handler: function (self, e) {
                        updateWindow.show()
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
                            url: 'template/accept',
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
                            url: 'template/reject',
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
                store: templateListStore,
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

        /* make cover the whole screen the gridpanel */
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [gridPanel]
        });

        formWindow = Ext.create('Ext.window.Window', {
            id: 'template-window',
            title: '新增模板',
            closable: true,
            closeAction: 'hide',
            width: 310,
            height: 175,
            layout: 'fit',
            modal: true,
            resizable: false,
            constrain: true,
            bodyPadding: '20 10 10 20',

            focusOnToFront: true,
            toFrontOnShow: false,
            preventFocusOnActivate: true,

            items: form = Ext.create('Ext.form.Panel', {
                id: 'template-form',
                border: false,
                trackResetOnLoad: true,
                autoScroll: true,

                defaults: {
                    labelWidth: 40,
                    msgTarget: 'side',
                    autoFitErrors: false,
                    width: 270,
                    allowBlank: false,
                    maxLength: 200,
                    maxLengthText: "长度不能超过200",
                    margin: '0 0 10 0',
                    blankText: '该字段不能为空！'
                },
                defaultType: 'textfield',

                items: [
                    {
                        fieldLabel: '名称',
                        name: 'name',
                        emptyText: ''
                    },
                    {
                        fieldLabel: '文件',
                        xtype: 'filefield',
                        name: 'file',
                        buttonText: '选择文件',
                        blankText: '',
                        validator: function (val) {
                            if (!Ext.String.trim(val)) {
                                return '请选择文件！';
                            }
                            if (!Ext.String.endsWith(val, ".vm", false)) {
                                return "文件格式(.vm)错误！";
                            }
                            return true;
                        }
                    }
                ]
            }),
            buttonAlign: 'center',
            buttons: [
                {
                    text: '重置',
                    handler: function () {
                        form.getForm().reset();
                    }
                },
                {
                    text: '提交',
                    handler: function () {
                        if (form.isValid()) {
                            form.getForm().submit({
                                url: 'template/create',
                                method: 'POST',
                                submitEmptyText: false,
                                success: function (_form, action) {
                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :
                                            Ext.Msg.alert(alertTitle, '创建成功!', function () {

                                                formWindow.hide();
                                                // reload the list
                                                templateListStore.reload();
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
                beforeshow: function () {
                    form.getForm().reset();
                }
            }
        });

        updateForm = Ext.create({
            xtype: 'form',
            id: 'update-form',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                xtype: 'hiddenfield',
                id: 'update-id',
                name: 'id'
            }, {
                xtype: 'textfield',
                id: 'update-name',
                name: 'name',
                labelWidth: 50,
                fieldLabel: '名称<span style="color:red;"> * </span>',
                allowBlank: false,
                blankText: '此项必须填写'
            }, {
                xtype: 'textarea',
                name: 'content',
                id: 'update-content',
                fieldLabel: '模板内容<span style="color:red;"> * </span>',
                allowBlank: false,
                labelAlign: 'top',
                blankText: '此项必须填写',
                flex: 1
            }]
        });

        updateMask = new Ext.LoadMask({
            target: updateForm
        });

        contentStore = Ext.create("Ext.data.JsonStore", {
            fields: ['name', 'content'],
            autoLoad: false,
            proxy: {
                type: 'ajax',
                url: 'template/content',
                actionMethods: {read: 'POST'}
            },
            listeners: {
                beforeload: function (self, operation, eOpts) {
                    updateMask.mask("加载中...");
                    var _id = gridPanel.getSelection()[0].get("id");
                    Ext.getCmp('update-id').setValue(_id);
                    self.getProxy().setConfig({
                        extraParams: {
                            id: _id
                        }
                    });
                },
                load: function (self, records, successful, operation, eOpts) {
                    if (successful) {
                        var template = records[0];
                        Ext.getCmp('update-name').setValue(template.get('name'));
                        Ext.getCmp('update-content').setValue(template.get('content'));
                        updateMask.unmask();
                    } else {
                        Ext.Msg.alert("错误", "数据加载出错! 请联系管理员! ").setIcon(Ext.Msg.ERROR);
                    }
                }
            }
        });

        updateWindow = Ext.create('Ext.window.Window', {
            title: "修改模板",
            width: 800,
            height: 600,
            maximizable: true,
            closeAction: 'hide',
            modal: true,
            constrain: true,
            bodyPadding: 10,
            layout: 'fit',
            items: [updateForm],
            buttons: [{
                text: '取消',
                handler: function (self, e) {
                    updateWindow.hide();
                }
            }, {
                text: '提交',
                handler: function (self, e) {
                    if (updateForm.isValid()) {
                        updateForm.getForm().submit({
                            clientValidation: true,
                            url: 'template/update',
                            method: 'POST',
                            submitEmptyText: false,
                            success: function (_form, action) {
                                var result = action.result.result;
                                switch (result) {
                                    case 'success' :
                                        Ext.Msg.alert(alertTitle, '修改成功!', function () {
                                            updateWindow.hide();
                                            templateListStore.reload();
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
            }],
            buttonAlign: 'center',
            listeners: {
                beforeshow: function (self, eOpts) {
                    Ext.getCmp('update-form').getForm().reset();
                },
                show: function (self, eOpts) {
                    contentStore.load();
                }
            }
        });

    });
</script>

</body>
</html>