<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>功能管理</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>
<script>
    var rightListStore, gridPanel, viewport,
            form, formWindow, formResetFn
            ;
    Ext.onReady(function () {

        var rightFiled = [
            'id',
            'name',
            'depict',
            'status',
            'createDate'
        ];

        rightListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'rightListStore',
            autoLoad: true,
            fields: rightFiled,
            proxy: {
                type: 'ajax',
                url: 'right/listAll',
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
                        rightListStore.reload();

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

                        Ext.getCmp("btn-save").setDisabled(count < 1);
                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                    }
                }
            },
            store: rightListStore,
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    flex: 0.4
                },
                {
                    text: '功能名',
                    dataIndex: 'name',
                    flex: 0.6
                },
                {
                    text: '功能描述<span style="color: red"> +</span>',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false
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
                clicksToEdit: 1
            },
            tbar: [
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
                            url: 'right/update',
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
                            url: 'right/accept',
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
                            url: 'right/reject',
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
                store: rightListStore,
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

    });
</script>

</body>
</html>