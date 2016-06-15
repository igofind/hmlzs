<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <title>日志管理</title>
    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>
<script>
    Ext.onReady(function () {
        var logFiled, logListStore, gridPanel, viewport;

        logFiled = [
            'id',
            'name',
            'action',
            'content',
            'ip',
            'status',
            'createDate'
        ];

        logListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'logListStore',
            fields: logFiled,
            pageSize: pageSize,
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: 'log/listAll',
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

                        // Ext.getCmp("btn-delete").setDisabled(count < 1);
                        //                        Ext.getCmp("btn-save").setDisabled(count < 1);
                        //                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        //                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                    }
                }
            },
            store: logListStore,
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
                    text: '帐户名',
                    dataIndex: 'name',
                    flex: 1
                },
                {
                    text: '操作',
                    dataIndex: 'action',
                    flex: 2
                },
                {
                    text: '内容',
                    dataIndex: 'content',
                    flex: 2
                },
                {
                    text: 'IP',
                    dataIndex: 'ip',
                    flex: 1
                },
                {
                    text: '创建时间',
                    dataIndex: 'createDate',
                    flex: 1
                }
            ],

            /*tbar: [
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
             url: 'log/update',
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
             /!*{
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
             Ext.Ajax.request({
             url: 'log/delete',
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
             },*!/
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
             url: 'log/accept',
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
             url: 'log/reject',
             method: "POST",
             params: {
             data: Ext.JSON.encode(data)
             },
             success: ajaxSuccess,
             failure: ajaxFailure
             });

             }
             }
             ],*/
            bbar: Ext.create('Ext.PagingToolbar', {
                store: logListStore,
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

        // make cover the whole screen the gridPanel
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [gridPanel]
        });

    });
</script>

</body>
</html>