<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <title>文章检索</title>
    <jsp:include page="_script.jspf"></jsp:include>

    <script src="${resURLPrefix}/static/base/ext/js/TinyMCETextArea.js"></script>
    <script src="${resURLPrefix}/static/base/tinymce/tinymce.min.js"></script>

</head>
<body>
<script>
    var openWin = function (url) {
        var win = window;
        if (window.parent != window) {
            win = window.parent;
        }
        win.open(url, "_blank");
    };

    Ext.onReady(function () {

        var searchPanel, gridPanel, articleField, articleStore, ajaxSuccess,
                addMeidaWindow,
                initData = ${initData};

        ajaxSuccess = function (response, options, successMsg, refresh) {

            var resp = Ext.JSON.decode(response.responseText);
            var result = resp['result'];
            successMsg = Ext.isEmpty(successMsg) ? '操作成功' : successMsg;
            switch (result) {
                case 'success' :
                    Ext.Msg.alert(alertTitle, successMsg, function (button, text, opt) {
                        // 自定义
                        if (refresh) {
                            if (gridPanel.getSelectionModel()) gridPanel.getSelectionModel().deselectAll();
                            articleStore.reload();
                        }
                    }).setIcon(Ext.Msg.INFO);
                    return;
                case 'preview':
                    var urls = resp['targets'];
                    var win = window;
                    if (window.parent != window) {
                        win = window.parent;
                    }
                    Ext.each(urls, function (item, index, allItem) {
                        win.open("article/previewPage?target=" + item, "_blank");
                    });
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

        articleField = [
            'id',
            'creator',
            'updater',
            'serial',
            'title',
            'depict',
            'templateId',
            'templateName',
            'url',
            'treePath',
            'categoryName',
            'categoryId',
            'treeId',
            'focus',
            'media',
            'headLine',
            'headLineOrder',
            'audit',
            'auditorId',
            'status',
            'createDate',
            'updateDate',
            'publishDate',
            'headLineDate',
            'deleteDate'
        ];

        articleStore = Ext.create('Ext.data.JsonStore', {
            autoLoad: true,
            fields: articleField,
            pageSize: pageSize,

            proxy: {
                type: 'ajax',
                url: 'search/search',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data',
                    totalProperty: 'totalData'
                }
            },
            listeners: {
                beforeload: function (store, operation, eOpts) {
                    articleStore.getProxy().setConfig({
                        extraParams: {
                            title: Ext.getCmp('title').getValue(),
                            category: Ext.getCmp('category').getValue(),
                            focus: Ext.getCmp('focus').getValue(),
                            headLine: Ext.getCmp('headLine').getValue(),
                            status: Ext.getCmp('status').getValue(),
                            creator: Ext.getCmp('creator').getValue(),
                            createDate: Ext.getCmp('createDate').getValue(),
                            updateDate: Ext.getCmp('updateDate').getValue(),
                            publishDate: Ext.getCmp('publishDate').getValue()
                        }
                    });
                },
                load: storeLoadListenerFn
            }
        });

        searchPanel = Ext.create('Ext.form.Panel', {
            region: 'north',
            items: [
                {
                    xtype: 'container',
                    layout: 'hbox',
                    defaults: {
                        labelAlign: 'right',
                        margin: '15 5 0 0'
                    },
                    items: [
                        {
                            id: 'title',
                            xtype: 'textfield',
                            fieldLabel: '标题',
                            width: 200,
                            labelWidth: 40,
                            name: 'title',
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            triggers: {
                                clear: {
                                    cls: Ext.baseCSSPrefix + 'form-clear-trigger',
                                    hidden: true,
                                    handler: function () {
                                        this.getTrigger('clear').hide();
                                        this.setValue('');
                                    }
                                },
                                search: {
                                    cls: Ext.baseCSSPrefix + 'form-search-trigger',
                                    handler: function () {
                                        if (!Ext.isEmpty(this.getValue())) {
                                            this.getTrigger('clear').show();
                                        }
                                        articleStore.load();
                                    }
                                }
                            }
                        },
                        {
                            id: 'category',
                            xtype: 'combo',
                            fieldLabel: '栏目',
                            width: 150,
                            editable: false,
                            labelWidth: 40,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            store: Ext.create('Ext.data.Store', {
                                fields: ['name', 'id'],
                                data: initData['category']
                            })
                        },
                        {
                            id: 'focus',
                            xtype: 'combo',
                            width: 110,
                            editable: false,
                            queryMode: 'local',
                            fieldLabel: '焦点图',
                            labelWidth: 50,
                            displayField: 'name',
                            valueField: 'value',
                            forceSelection: false,
                            store: Ext.create('Ext.data.Store', {
                                fields: ['name', 'value'],
                                data: [
                                    {'name': '否', 'value': 0},
                                    {'name': '是', 'value': 1}
                                ]
                            })
                        },
                        {
                            id: 'headLine',
                            xtype: 'combo',
                            width: 100,
                            editable: false,
                            queryMode: 'local',
                            fieldLabel: '头条',
                            labelWidth: 40,
                            displayField: 'name',
                            valueField: 'value',
                            forceSelection: false,
                            store: Ext.create('Ext.data.Store', {
                                fields: ['name', 'value'],
                                data: [
                                    {'name': '否', 'value': 0},
                                    {'name': '是', 'value': 1}
                                ]
                            })
                        },
                        {
                            id: 'status',
                            xtype: 'combo',
                            width: 105,
                            editable: false,
                            queryMode: 'local',
                            fieldLabel: '状态',
                            labelWidth: 40,
                            displayField: 'name',
                            valueField: 'value',
                            forceSelection: false,
                            store: Ext.create('Ext.data.Store', {
                                fields: ['name', 'value'],
                                data: [
                                    {'name': '启用', 'value': 1},
                                    {'name': '废弃', 'value': -1}
                                ]
                            })
                        },
                        {
                            id: 'creator',
                            xtype: 'combo',
                            width: 150,
                            editable: false,
                            fieldLabel: '创建人',
                            labelWidth: 50,
                            displayField: 'account',
                            valueField: 'account',
                            store: Ext.create('Ext.data.Store', {
                                fields: ['name', 'account'],
                                data: initData['users']
                            })
                        },
                        {
                            id: 'createDate',
                            xtype: 'datefield',
                            width: 200,
                            editable: false,
                            fieldLabel: '创建时间',
                            format: 'Y-m-d',
                            labelWidth: 60
                        },
                        {
                            id: 'updateDate',
                            xtype: 'datefield',
                            width: 200,
                            editable: false,
                            format: 'Y-m-d',
                            fieldLabel: '更新时间',
                            labelWidth: 60
                        },
                        {
                            id: 'publishDate',
                            xtype: 'datefield',
                            width: 200,
                            editable: false,
                            format: 'Y-m-d',
                            fieldLabel: '发布时间',
                            labelWidth: 60
                        }
                    ]
                }
            ]
        });

        gridPanel = Ext.create("Ext.grid.Panel", {
            loadMask: true, // loading tip
            region: 'center',
            border: false,
            reference: 'articleGrid',
            selModel: {
                type: 'checkboxmodel',
                listeners: {
                    selectionchange: function (self, selected, eOpts) {
                        var count = self.getCount();
                        var isAccept = false, isReject = false, isHeadLine = false, urlIsEmpty = false, isFocus = true;

                        Ext.each(self.getSelection(), function (item, index, allItems) {
                            var status = parseInt(item.get("status"), 10);
                            var headLine = parseInt(item.get("headLine"), 10);
                            var focus = parseInt(item.get("focus"), 10);

                            var url = item.get("url");

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }

                            if (headLine == 1) {
                                isHeadLine = true;
                            }

                            if (Ext.isEmpty(url)) {
                                urlIsEmpty = true;
                            }

                            if(focus == 0){
                                isFocus = false;
                            }
                        });

                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                        Ext.getCmp("btn-headLineMedia").setDisabled(count != 1);
                        Ext.getCmp("btn-cancelHeadLineMedia").setDisabled(count < 1 || !isFocus);

                        Ext.getCmp("btn-headLine").setDisabled(count < 1 || isHeadLine || isReject || urlIsEmpty);
                        Ext.getCmp("btn-cancel-headLine").setDisabled(count < 1 || !isHeadLine || isReject);
                        Ext.getCmp("btn-saveOrder").setDisabled(count < 1 || isReject);

                        Ext.getCmp("btn-staticize").setDisabled(count < 1 || isReject);
                        Ext.getCmp("btn-preview").setDisabled(count < 1 || isReject);

                    }
                }
            },
            store: articleStore,
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1
            },
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    //flex: 0.4
                    width: 60
                },
                {
                    text: '标题',
                    dataIndex: 'title',
                    //flex: 1
                    width: 150
                },
                {
                    text: '分类',
                    dataIndex: 'categoryName',
                    //flex: 1
                    width: 100
                },
                {
                    text: '媒体',
                    dataIndex: 'media',
                    //flex: 1
                    width: 150
                },
                {
                    text: '焦点图',
                    dataIndex: 'focus',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        switch (value) {
                            case 0:
                                return '否';
                            case 1:
                                return '<span style="color: orange">是</span>';
                            default :
                                return value;
                        }
                    },
                    // flex: 0.5
                    width: 60
                },
                {
                    text: '头条',
                    dataIndex: 'headLine',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        switch (value) {
                            case 0:
                                return '否';
                            case 1:
                                return '<span style="color: orangered">是</span>';
                            default :
                                return value;
                        }
                    },
                    //flex: 0.5
                    width: 60
                },
                {
                    text: '排序 <span style="color: red">+</span>',
                    dataIndex: 'headLineOrder',
                    editor: {
                        xtype: 'numberfield',
                        allowBlank: false,
                        editable: false,
                        minValue: 1
                    },
                    emptyText: 1,
                    //flex: 0.5
                    width: 60
                },
                {
                    text: '地址',
                    dataIndex: 'url',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var _value = Ext.String.trim(value);

                        return _value ? '<a href="javascript:void(0);" onclick="openWin(\'' + _value + '\')">' + _value + '</a>' : _value;
                    },
                    //flex: 1
                    width: 170
                },
                {
                    text: '路径',
                    dataIndex: 'treePath',
                    // flex: 1
                    width: 150
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
                                return '隐藏';
                            default :
                                return value;
                        }
                    },
                    flex: 0.5
                },
                {
                    text: '创建人',
                    dataIndex: 'creator',
                    flex: 0.6
                },
                {
                    text: '更新人',
                    dataIndex: 'updater',
                    flex: 0.6
                },
                {
                    text: '创建时间',
                    dataIndex: 'createDate',
                    hidden: true,
                    flex: 1
                },
                {
                    text: '更新时间',
                    dataIndex: 'updateDate',
                    hidden: true,
                    flex: 1
                },
                {
                    text: '发布时间',
                    dataIndex: 'publishDate',
                    hidden: true,
                    flex: 1
                },
                {
                    text: '设置头条时间',
                    dataIndex: 'headLineDate',
                    hidden: true,
                    flex: 1
                },
                {
                    text: '作废时间',
                    dataIndex: 'deleteDate',
                    hidden: true,
                    flex: 1
                }
            ],
            dockedItems:[{
                xtype: 'toolbar',
                dock: 'top',
                enableOverflow:true,
                items: [
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
                                url: 'article/accept',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, "", true);
                                },
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
                                url: 'article/reject',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, "", true);
                                },
                                failure: ajaxFailure
                            });

                        }
                    },
                    '-',
                    {
                        xtype: 'button',
                        text: '媒体',
                        id: 'btn-headLineMedia',
                        glyph: 'xf1c5@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            addMeidaWindow.show();
                        }
                    },
                    {
                        xtype: 'button',
                        text: '取消焦点图',
                        id: 'btn-cancelHeadLineMedia',
                        glyph: 'xf1c5@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                                data.push(item.data['id']);
                            });
                            if (data.length == 0) {
                                return;
                            }
                            Ext.Msg.confirm("操作确认", "确定要取消焦点图吗？", function (buttonId, text, opt) {
                                if (buttonId == 'yes' || buttonId == 'ok') {

                                    Ext.Ajax.request({
                                        url: 'media/cancelHeadLineMedia',
                                        method: "POST",
                                        params: {
                                            data: Ext.JSON.encode(data)
                                        },
                                        success: function (response, options) {
                                            ajaxSuccess(response, options, "", true);
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
                        text: '设置头条',
                        id: 'btn-headLine',
                        glyph: 'xf11e@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            var needOrder = false;
                            Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                                var obj = {};
                                obj['id'] = item.data['id'];
                                if (item.data['headLineOrder'] <= 0) {
                                    needOrder = true;
                                    return;
                                }
                                obj['order'] = item.data['headLineOrder'];
                                data.push(obj);
                            });

                            if (needOrder) {
                                Ext.Msg.alert(alertTitle, "设置头条前先指定该头条的排序！").setIcon(Ext.Msg.ERROR);
                                return;
                            }
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'article/headLine',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, "", true);
                                },
                                failure: ajaxFailure
                            });

                        }
                    },
                    {
                        xtype: 'button',
                        text: '取消头条',
                        id: 'btn-cancel-headLine',
                        glyph: 'xf11d@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                                data.push({id: item.data['id']});
                            });
                            if (data.length == 0) {
                                return;
                            }

                            Ext.Ajax.request({
                                url: 'article/cancelHeadLine',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, "", true);
                                },
                                failure: ajaxFailure
                            });

                        }
                    },
                    {
                        xtype: 'button',
                        id: 'btn-saveOrder',
                        text: '保存排序',
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
                                url: 'article/update',
                                method: "POST",
                                params: {
                                    data: Ext.JSON.encode(data)
                                },
                                success: function (response, options) {
                                    ajaxSuccess(response, options, '保存成功！', true);
                                },
                                failure: ajaxFailure
                            });
                        }
                    },
                    '-',
                    {
                        xtype: 'button',
                        text: '发布文章',
                        glyph: 'xf0f6@FontAwesome',
                        id: 'btn-staticize',
                        disabled: true,
                        handler: function (self, e) {
                            var data = [];
                            Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                                data.push(item.data['id']);
                            });
                            if (data.length == 0) {
                                return;
                            }
                            Ext.Msg.confirm("发布确认", "文章发布前请先预览，仍然继续？", function (buttonId, text, opt) {
                                if (buttonId == 'yes' || buttonId == 'ok') {

                                    Ext.Ajax.request({
                                        url: 'article/staticize',
                                        method: "POST",
                                        params: {
                                            data: Ext.JSON.encode(data)
                                        },
                                        success: function (response, options) {
                                            ajaxSuccess(response, options, "", true);
                                        },
                                        failure: ajaxFailure
                                    });
                                }
                            }).setIcon(Ext.Msg.WARNING);
                        }
                    },
                    {
                        xtype: 'button',
                        text: '预览文章',
                        glyph: 'xf0f6@FontAwesome',
                        id: 'btn-preview',
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
                                url: 'article/preview',
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
                        text: '应用到首页',
                        glyph: 'xf1ea@FontAwesome',
                        id: 'btn-staticizeAll',
                        handler: function (self, e) {

                            Ext.Ajax.request({
                                url: 'homepage/staticize',
                                method: "POST",
                                success: function (response, options, refresh) {
                                    ajaxSuccess(response, options, "", false);
                                },
                                failure: ajaxFailure
                            });
                        }
                    }

                ]
            }],
            bbar: Ext.create('Ext.PagingToolbar', {
                store: articleStore,
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

        var mediaStore = Ext.create('Ext.data.JsonStore', {
            autoLoad: true,
            fields: [
                'id',
                'creator',
                'title',
                'depict',
                'categoryId',
                'url',
                'ukey',
                'ftype',
                'hash',
                'size',
                'width',
                'height',
                'handle',
                'status',
                'createDate'
            ],
            pageSize: pageSize,

            proxy: {
                type: 'ajax',
                url: 'media/search',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data',
                    totalProperty: 'totalData'
                }
            },
            listeners: {
                beforeload: function (store, operation, eOpts) {
                    mediaStore.getProxy().setConfig({
                        extraParams: {
                            id: Ext.getCmp('media-id').getValue(),
                            title: Ext.getCmp('media-title').getValue(),
                            category: Ext.getCmp('media-category').getValue()
                        }
                    });
                },
                load: storeLoadListenerFn
            }
        });

        addMeidaWindow = Ext.create("Ext.window.Window", {
            title: '头条媒体设置',
            width: 800,
            minHeight: 400,
            layout: 'fit',
            modal: true,
            maximizable: true,
            closeAction: 'hide',
            constrain: true,
            items: [{
                id: 'addMediaGrid',
                xtype: 'gridpanel',
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
                            var isImage = false;
                            Ext.each(Ext.getCmp('addMediaGrid').getSelection(), function (item, index, allItems) {
                                var category = parseInt(item.get("category"), 10);
                                if (category == 1) {
                                    isImage = true;
                                }
                            });

                            Ext.getCmp("btn-add").setDisabled(count > 1);
                            Ext.getCmp("media-focus").setDisabled(count != 1 || !isImage);
                        }
                    }
                },
                store: mediaStore,
                columns: [
                    {
                        text: 'ID',
                        dataIndex: 'id',
                        flex: 0.5
                    },
                    {
                        text: '标题',
                        dataIndex: 'title',
                        editor: {
                            xtype: 'textfield',
                            allowBlank: false
                        },
                        flex: 1
                    },
                    {
                        text: '分类',
                        dataIndex: 'categoryName',
                        flex: 0.5
                    },
                    {
                        text: '地址',
                        dataIndex: 'url',
                        renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                            return '<a href="' + value + '" target="_blank"> ' + value + '</a>';
                        },
                        flex: 2
                    },
                    {
                        text: '预览',
                        dataIndex: 'url',
                        hidden: true,
                        flex: 1,
                        renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                            return record.data['category'] == 1 ? '<img src="' + value + '?imageView2/2/h/80/format/jpg/interlace/1" alt="预览失败">' : '<span style="color: red">暂不支持</span>';
                        }
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
                                    return '隐藏';
                                default :
                                    return value;
                            }
                        },
                        flex: 0.5
                    },
                    {
                        text: '创建人',
                        dataIndex: 'creator',
                        flex: 0.6
                    },
                    {
                        text: '创建时间',
                        dataIndex: 'createDate',
                        flex: 1
                    }
                ],
                tbar: [{
                    xtype: 'button',
                    text: '确定设置',
                    id: 'btn-add',
                    glyph: 'xf058@FontAwesome',
                    handler: function (self, e) {
                        var articleGridSelection = gridPanel.getSelection();
                        var mediaGridSelection = Ext.getCmp('addMediaGrid').getSelection();
                        if (articleGridSelection.length == 0 || mediaGridSelection.length == 0) {
                            return;
                        }

                        Ext.Ajax.request({
                            url: 'media/addHeadLineMedia',
                            method: "POST",
                            params: {
                                articleId: articleGridSelection[0]['data']['id'],
                                mediaId: mediaGridSelection[0]['data']['id'],
                                focus: Ext.getCmp("media-focus").getValue() ? 1 : 0
                            },
                            success: function (response, options) {
                                ajaxSuccess(response, options, "设置成功!", false);

                                addMeidaWindow.hide();
                                Ext.getCmp("media-focus").setValue(false);
                                Ext.getCmp('addMediaGrid').getSelectionModel().deselectAll();

                                articleStore.reload();
                            },
                            failure: ajaxFailure
                        });
                    }
                }, {
                    xtype: 'checkbox',
                    id: 'media-focus',
                    boxLabel: '作为焦点图',
                    name: 'focus',
                    disabled: true,
                    inputValue: 1
                }, {
                    xtype: 'numberfield',
                    id: 'media-id',
                    fieldLabel: 'ID',
                    labelWidth: 40,
                    width: 130,
                    minValue: 1,
                    labelAlign: 'right'
                }, {
                    xtype: 'textfield',
                    id: 'media-title',
                    fieldLabel: '标题',
                    labelWidth: 40,
                    labelAlign: 'right',
                    triggers: {
                        clear: {
                            cls: Ext.baseCSSPrefix + 'form-clear-trigger',
                            hidden: true,
                            handler: function () {
                                this.getTrigger('clear').hide();
                                this.setValue('');
                            }
                        },
                        search: {
                            cls: Ext.baseCSSPrefix + 'form-search-trigger',
                            handler: function () {
                                if (!Ext.isEmpty(this.getValue())) {
                                    this.getTrigger('search').show();
                                }
                                mediaStore.reload();
                            }
                        }
                    }
                }, {
                    id: 'media-category',
                    xtype: 'combo',
                    fieldLabel: '类型',
                    width: 150,
                    editable: false,
                    labelWidth: 40,
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'key',
                    store: Ext.create('Ext.data.Store', {
                        fields: ['name', 'key'],
                        data: initData['mediaCategory']
                    })
                }],
                bbar: Ext.create('Ext.PagingToolbar', {
                    store: mediaStore,
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
            }]

        });

        // make cover the whole screen the gridpanel
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            scrollable: true,
            items: [
                searchPanel,
                gridPanel
            ]
        });

    });

</script>

</body>
</html>