<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <title>article</title>
    <jsp:include page="_script.jspf"></jsp:include>

    <%--<link href="${resURLPrefix}/static/base/umeditor/themes/default/css/umeditor.css" type="text/css" rel="stylesheet">
    <script src="${resURLPrefix}/static/base/umeditor/third-party/jquery.min.js"></script>
    <script charset="utf-8" src="${resURLPrefix}/static/base/umeditor/umeditor.config.js"></script>
    <script charset="utf-8" src="${resURLPrefix}/static/base/umeditor/umeditor.js"></script>--%>

    <link href="${resURLPrefix}/static/base/umeditor/themes/default/css/umeditor.css" type="text/css" rel="stylesheet">
    <script charset="utf-8" src="${resURLPrefix}/static/base/ueditor/third-party/jquery-1.10.2.min.js"></script>
    <script charset="utf-8" src="${resURLPrefix}/static/base/ueditor/ueditor.config.js"></script>
    <script charset="utf-8" src="${resURLPrefix}/static/base/ueditor/ueditor.parse.js"></script>
    <script charset="utf-8" src="${resURLPrefix}/static/base/ueditor/ueditor.all.js"></script>


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
        var articleFiled, articleListStore, categoryListStore, templateStore,
                ajaxSuccess, gridPanel, viewport,
                form, formWindow,
                initData =${initData},
                editor;

        articleFiled = [
            'id',
            'creator',
            'author',
            'source',
            'updater',
            'serial',
            'title',
            'depict',
            'templateId',
            'url',
            'categoryId',
            'treeId',
            'headLine',
            'headLineOrder',
            'status',
            'createDate',
            'updateDate',
            'publishDate',
            'headLineDate',
            'deleteDate'
        ];

        articleListStore = Ext.create('Ext.data.JsonStore', {
            storeId: 'articleListStore',
            autoLoad: true,
            fields: articleFiled,
            pageSize: pageSize,

            proxy: {
                type: 'ajax',
                url: 'article/list',
                actionMethods: {read: 'POST'},
                reader: {
                    type: 'json',
                    rootProperty: 'data',
                    totalProperty: 'totalData'
                },
                extraParams: {
                    pageSize: pageSize,
                    treeId:${treeId}
                }
            },
            listeners: {
                load: storeLoadListenerFn
            }
        });

        templateStore = Ext.create("Ext.data.ArrayStore", {
            fields: ['id', 'name'],
            data: initData['templates']
        });

        categoryListStore = Ext.create('Ext.data.ArrayStore', {
            fields: ['id', 'name'],
            data: initData['categories']
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
                        articleListStore.reload();
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

        gridPanel = Ext.create("Ext.grid.Panel", {
            scrollable: true,
            autoScroll: true,
            region: 'center',
            border: false,
            reference: 'articleGrid',
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
                        Ext.getCmp("btn-save").setDisabled(count < 1 || isReject);
                        Ext.getCmp("btn-modify").setDisabled(count != 1 || isReject);
                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                        Ext.getCmp("btn-staticize").setDisabled(count < 1 || isReject);
                        Ext.getCmp("btn-preview").setDisabled(count < 1 || isReject);

                    }
                }
            },
            store: articleListStore,
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
                    text: '标题 <span style="color: red">+</span>',
                    dataIndex: 'title',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false,
                        listeners: {
                            change: function (self, newValue, oldValue, eOpts) {
                                self.setValue(Ext.String.trim(newValue));
                            }
                        }
                    },

                    flex: 1
                },
                {
                    text: '描述 <span style="color: red">+</span>',
                    dataIndex: 'depict',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false,
                        listeners: {
                            change: function (self, newValue, oldValue, eOpts) {
                                self.setValue(Ext.String.trim(newValue));
                            }
                        }
                    },
                    flex: 1
                },
                {
                    text: '作者 <span style="color: red">+</span>',
                    dataIndex: 'author',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false,
                        listeners: {
                            change: function (self, newValue, oldValue, eOpts) {
                                self.setValue(Ext.String.trim(newValue));
                            }
                        }
                    },
                    flex: 0.8
                },
                {
                    text: '来源 <span style="color: red">+</span>',
                    dataIndex: 'source',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false,
                        listeners: {
                            change: function (self, newValue, oldValue, eOpts) {
                                self.setValue(Ext.String.trim(newValue));
                            }
                        }
                    },
                    flex: 1
                },
                {
                    text: '栏目 <span style="color: red">+</span>',
                    dataIndex: 'categoryId',
                    id: 'combo-categoryId',
                    editor: {
                        xtype: 'combo',
                        id: 'category-combo',
                        store: categoryListStore,
                        forceSelection: true,
                        allowBlank: false,
                        editable: false,
                        displayField: 'name',
                        valueField: 'id'
                    },
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {

                        var data = categoryListStore['data'];

                        if (!data || !data.length) {
                            return record['data']['categoryName'];
                        }

                        return data.getByKey(value)['data']['name'];
                    },
                    flex: 0.8
                },
                {
                    text: '头条',
                    dataIndex: 'headLine',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        switch (value) {
                            case 0:
                                return '否';
                            case 1:
                                return '<span style="color: green">是</span>';
                            default :
                                return value;
                        }
                    },
                    flex: 0.5
                },
                {
                    text: '模板 <span style="color: red">+</span>',
                    dataIndex: 'templateId',
                    id: 'combo-templateId',
                    editor: {
                        xtype: 'combo',
                        id: 'template-combo',
                        autoRender: true,
                        forceSelection: true,
                        allowBlank: false,
                        editable: false,
                        queryMode: 'local',
                        store: templateStore,
                        displayField: 'name',
                        valueField: 'id'
                    },
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {

                        var data = templateStore['data'];

                        if (!data || !data.length) {
                            return record['data']['templateName'];
                        }

                        return data.getByKey(value)['data']['name'];
                    },
                    flex: 0.8
                },
                {
                    text: '地址',
                    dataIndex: 'url',
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var _value = Ext.String.trim(value);

                        return _value ? '<a href="javascript:void(0);" onclick="openWin(\'' + _value + '\')">' + _value + '</a>' : _value;
                    },
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
                    flex: 0.8
                },
                {
                    text: '更新人',
                    dataIndex: 'updater',
                    flex: 0.8
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
                    text: '发布时间',
                    dataIndex: 'publishDate',
                    flex: 1
                },
                {
                    text: '设置头条时间',
                    dataIndex: 'headLineDate',
                    flex: 1
                },
                {
                    text: '作废时间',
                    dataIndex: 'deleteDate',
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
                        formWindow.setTitle('新增');
                        formWindow.show();
                        form.getForm().reset();
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
                            url: 'article/update',
                            method: "POST",
                            params: {
                                data: Ext.JSON.encode(data)
                            },
                            success: function (response, options) {
                                ajaxSuccess(response, options, '保存成功！');
                            },
                            failure: ajaxFailure
                        });
                    }
                },
                {
                    xtype: 'button',
                    id: 'btn-modify',
                    text: '修改',
                    disabled: true,
                    glyph: 'xf044@FontAwesome',
                    handler: function (self, e) {

                        formWindow.setTitle('修改');

                        var data = gridPanel.getSelection()[0]['data'];
                        /* set form field value */
                        Ext.getCmp('article-id').setValue(data['id']);
                        Ext.getCmp('submit-type').setValue('modify');
                        Ext.getCmp('article-title').setValue(data['title']);
                        Ext.getCmp('article-depict').setValue(data['depict']);

                        Ext.getCmp('article-source').setValue(data['source']);
                        Ext.getCmp('article-author').setValue(data['author']);

                        Ext.getCmp('article-categoryId').setValue(data['categoryId']);

                        Ext.getCmp('article-templateId').setValue(data['templateId']);

                        formWindow.show();
                        editor.html(data['content']);

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
                        Ext.Msg.confirm("删除确认", "文章删除后无法恢复，继续删除？", function (buttonId, text, opt) {

                            if (buttonId == 'yes' || buttonId == 'ok') {

                                Ext.Ajax.request({
                                    url: 'article/delete',
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
                            url: 'article/accept',
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
                            url: 'article/reject',
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
                                    success: ajaxSuccess,
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
                }
            ],
            bbar: Ext.create('Ext.PagingToolbar', {
                store: articleListStore,
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

        formWindow = Ext.create('widget.window', {
            id: 'article-window',
            title: '新增',
            closable: true,
            closeAction: 'hide',
            autoScroll: false,
            autoRender: true,
            constrain: true,
            width: 800,
            height: 660,
            layout: 'fit',
            plain: true,
            modal: true,
            bodyPadding: '10 10 0 10',

            focusOnToFront: true,
            toFrontOnShow: false,
            preventFocusOnActivate: true,

            items: form = Ext.create('Ext.form.FormPanel', {
                id: 'article-form',
                bodyStyle: 'background-color: transparent',

                border: false,
                trackResetOnLoad: true,
                autoScroll: false,
                autoRender: true,

                fieldDefaults: {
                    labelWidth: 60,
                    anchor: '100%',
                    blankText: '该字段必须填写！',
                    msgTarget: 'side'
                },
                defaultType: 'textfield',
                items: [
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        defaultType: 'combo',
                        margin: '0 0 5 0',
                        anchor: '66.6%',
                        items: [
                            {
                                fieldLabel: '栏目',
                                queryMode: 'local',
                                name: 'categoryId',
                                id: 'article-categoryId',
                                allowBlank: false,
                                editable: false,
                                store: categoryListStore,
                                displayField: 'name',
                                valueField: 'id',
                                flex: 1
                            },
                            {
                                fieldLabel: '模板',
                                queryMode: 'local',
                                labelWidth: 50,
                                labelAlign: 'right',
                                name: 'templateId',
                                id: 'article-templateId',
                                allowBlank: false,
                                editable: false,
                                displayField: 'name',
                                valueField: 'id',
                                store: templateStore,
                                flex: 1
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        layout: 'hbox',
                        defaultType: 'textfield',
                        margin: '0 0 5 0',
                        items: [
                            {
                                name: 'id',
                                xtype: 'hiddenfield',
                                id: 'article-id',
                                value: 0
                            },
                            {
                                xtype: 'hiddenfield',
                                id: 'submit-type',
                                value: 'create'
                            },
                            {
                                fieldLabel: '标题',
                                name: 'title',
                                id: 'article-title',
                                allowBlank: false,
                                emptyText: '',
                                flex: 1
                            },
                            {
                                fieldLabel: '来源',
                                labelWidth: 50,
                                labelAlign: 'right',
                                name: 'source',
                                id: 'article-source',
                                allowBlank: false,
                                emptyText: '',
                                flex: 1
                            },
                            {
                                fieldLabel: '作者',
                                labelWidth: 50,
                                labelAlign: 'right',
                                name: 'author',
                                id: 'article-author',
                                allowBlank: false,
                                emptyText: '',
                                flex: 1
                            }
                        ]
                    },
                    {
                        fieldLabel: '描述',
                        name: 'depict',
                        id: 'article-depict',
                        emptyText: '',
                        allowBlank: false,
                        flex: 1
                    },
                    {
                        allowBlank: false,
                        anchor: '100% 100%',
                        xtype: 'panel',
                        html: '<script type="text/plain" id="myEditor" style="width: 100%; height: 400px;"><\/script>',
                        listeners: {
                            afterrender: function (self, eOpts) {
                                var ue = UE.getEditor('myEditor',{
                                    autoHeightEnabled: true,
                                    autoFloatEnabled: true,
                                    toolbars: [
                                        ['fullscreen', 'source', 'undo', 'redo'],
                                        ['bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall', 'cleardoc']
                                    ]
                                });
                            }
                        }
                    }
                ]
            }),
            listeners: {
                beforehide: function (self, eOpts) {
                    form.getForm().reset();
                    editor.html("");
                }
            },

            buttons: [
                {
                    text: '关闭',
                    handler: function () {
                        formWindow.hide();
                    }
                },
                {
                    text: '提交',
                    handler: function () {
                        if (form.isValid()) {

                            var submitType = Ext.getCmp('submit-type').getValue();
                            var successMsg = '创建成功！';
                            successMsg = submitType == 'modify' ? '修改成功！' : successMsg;

                            form.getForm().submit({
                                url: 'article/' + (submitType == 'modify' ? 'modify' : submitType),
                                method: 'POST',
                                params: {
                                    treeId: ${treeId},
                                    content: editor.isEmpty() ? "" : editor.html()
                                },
                                submitEmptyText: false,

                                success: function (_form, action) { // TODO

                                    var result = action.result.result;
                                    switch (result) {
                                        case 'success' :

                                            Ext.Msg.alert(alertTitle, successMsg, function () {
                                                formWindow.hide();
                                                /* reload the list */
                                                articleListStore.reload();
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

            buttonAlign: 'center'
        });
    });
</script>

</body>
</html>