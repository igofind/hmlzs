<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <title>媒体管理</title>
    <jsp:include page="_script.jspf"></jsp:include>

</head>
<body>
<script>

    Ext.onReady(function () {

        var gridPanel, mediaField, mediaStore, modeDepict,
                mediaForm, mediaWindow, formMask,
                mediaUpdateForm, mediaUpdateWindow, updateFormMask,
                ajaxSuccess,
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
                            mediaStore.reload();
                        }
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

        mediaField = ['id', 'creator', 'title', 'depict', 'categoryId', 'url', 'ukey',
            'ftype', 'hash', 'size', 'width', 'height', 'handle', 'status', 'createDate'];

        mediaStore = Ext.create('Ext.data.JsonStore', {
            autoLoad: true,
            fields: mediaField,
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
                            id: Ext.getCmp('mediaId').getValue(),
                            title: Ext.getCmp('title').getValue(),
                            category: Ext.getCmp('category').getValue(),
                            status: Ext.getCmp('status').getValue(),
                            creator: Ext.getCmp('creator').getValue(),
                            createDate: Ext.getCmp('createDate').getValue()
                        }
                    });
                },
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
                        var isAccept = false, isReject = false, isImg = false;

                        Ext.each(self.getSelection(), function (item, index, allItems) {
                            var status = parseInt(item.get("status"), 10);
                            var type = parseInt(item.get("category"), 10);

                            if (status > 0) {
                                isAccept = true;
                            }
                            if (status < 0) {
                                isReject = true;
                            }

                            if (type == 1) {
                                isImg = true;
                            }

                        });

                        Ext.getCmp("btn-update").setDisabled(count != 1 || !isImg);

                        Ext.getCmp("btn-accept").setDisabled(count < 1 || isAccept);
                        Ext.getCmp("btn-reject").setDisabled(count < 1 || isReject);

                        Ext.getCmp("btn-save").setDisabled(count < 1);
                        Ext.getCmp("btn-show").setDisabled(count < 1);
                    }
                }
            },
            store: mediaStore,
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1
            },
            columns: [
                {
                    text: 'ID',
                    dataIndex: 'id',
                    flex: 0.5
                },
                {
                    text: '标题 <span style="color:red;"> +</span>',
                    dataIndex: 'title',
                    editor: {
                        xtype: 'textfield',
                        allowBlank: false,
                        maxLength: 200,
                        maxLengthText: "长度不能超过200"
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
                    width: 100,
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        return record.data['category'] == 1 ? '<img src="' + value + '?imageView2/2/w/80/h/80/format/jpg/interlace/1" alt="预览失败">' : '<span style="color: red">暂不支持</span>';
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
            dockedItems: [{
                xtype: 'toolbar',
                dock: 'top',
                enableOverflow: true,
                layout: 'hbox',
                items: [
                    {
                        id: 'mediaId',
                        xtype: 'numberfield',
                        fieldLabel: 'ID',
                        width: 120,
                        labelWidth: 20,
                        labelAlign: 'left',
                        minValue: 1,
                        name: 'mediaId'
                    },
                    {
                        id: 'title',
                        xtype: 'textfield',
                        fieldLabel: '标题',
                        width: 200,
                        labelWidth: 40,
                        name: 'title',
                        queryMode: 'local',
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
                                    mediaStore.load();
                                }
                            }
                        }
                    },
                    {
                        id: 'category',
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
                            data: initData['category']
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
                    }
                ]
            }, {
                xtype: 'toolbar',
                dock: 'top',
                enableOverflow: true,
                overflowHandler: 'scroller',
                items: [
                    {
                        xtype: 'button',
                        text: '上传',
                        id: 'btn-create',
                        glyph: 'xf093@FontAwesome',
                        handler: function (self, e) {
                            mediaWindow.show();
                        }
                    },
                    {
                        xtype: 'button',
                        text: '保存',
                        id: 'btn-save',
                        disabled: true,
                        glyph: 'xf0c7@FontAwesome',
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
                                url: 'media/save',
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
                    {
                        xtype: 'button',
                        text: '修改',
                        id: 'btn-update',
                        disabled: true,
                        glyph: 'xf044@FontAwesome',
                        handler: function (self, e) {
                            mediaUpdateWindow.show();
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
                                url: 'media/accept',
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
                                url: 'media/reject',
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
                        text: '显示地址',
                        id: 'btn-show',
                        glyph: 'xf0c1@FontAwesome',
                        disabled: true,
                        handler: function (self, e) {
                            var data = "";
                            Ext.each(gridPanel.getSelection(), function (item, index, allItems) {
                                data = data + item.data['id'] + " : " + item.data['url'] + "</br>";
                            });
                            if (data.length == 0) {
                                return;
                            }
                            Ext.Msg.alert("媒体地址", data);
                        }
                    }
                ]
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
        });

        modeDepict = [
            '保持图片原有尺寸和比例。',
            '限定缩略图的宽最少为 w，高最少为 h，进行等比缩放，居中裁剪。如果只指定 w 参数或只指定 h 参数，代表限定为长宽相等的正方图',
            '限定缩略图的宽最多为 w，高最多为 h，进行等比缩放，不裁剪。如果只指定 w 参数则表示限定宽（长自适应），只指定 h 参数则表示限定长（宽自适应）。从应用场景来说，模式二适合PC上做缩略图。',
            '限定缩略图的宽最少为 w，高最少为 h，进行等比缩放，不裁剪。如果只指定 w 参数或只指定 h 参数，代表长宽限定为同样的值。你可以理解为模式一是模式三的结果再做居中裁剪得到的。'
        ];

        mediaForm = Ext.create('Ext.form.Panel', {
            bodyPadding: 15,
            fieldDefaults: {
                labelWidth: 55,
                margin: '0 0 5 0'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                fieldLabel: '标题',
                xtype: 'textfield',
                name: 'title'
            }, {
                fieldLabel: '类型',
                id: 'media-type',
                name: 'type',
                xtype: 'radiogroup',
                defaults: {name: 'type'},
                items: [
                    {inputValue: '1', boxLabel: '图片', checked: true},
                    {inputValue: '2', boxLabel: '音频'}
                ],
                listeners: {
                    change: function (self, newValue, oldValue, eOpts) {
                        var depictCmp = Ext.getCmp('mode-depict');
                        if (newValue['type'] == 1) {

                            Ext.getCmp("image-modal").enable();
                            depictCmp.show();
                        } else {
                            Ext.getCmp("image-modal").disable();
                            Ext.getCmp("image-size").disable();
                            Ext.getCmp("image-format").disable();
                            Ext.getCmp("image-interlace").disable();
                            depictCmp.hide();
                        }
                    }
                }
            }, {
                xtype: 'fieldcontainer',
                fieldLabel: '模式',
                layout: 'hbox',
                items: [{
                    id: 'image-modal',
                    hideLabel: true,
                    xtype: 'radiogroup',
                    name: 'modal',
                    columns: 1,
                    vertical: true,
                    defaults: {
                        name: 'modal'
                    },
                    items: [
                        {inputValue: '-1', boxLabel: '默认', checked: true},
                        {inputValue: '1', boxLabel: '模式一'},
                        {inputValue: '2', boxLabel: '模式二'},
                        {inputValue: '3', boxLabel: '模式三'}
                    ],
                    listeners: {
                        change: function (self, newValue, oldValue, eOpts) {
                            if (newValue['modal'] != -1) {
                                Ext.getCmp("image-size").enable();
                                Ext.getCmp("image-format").enable();
                                Ext.getCmp("image-interlace").enable();
                            } else {
                                Ext.getCmp("image-size").disable();
                                Ext.getCmp("image-format").disable();
                                Ext.getCmp("image-interlace").disable();
                            }
                            var depictCmp = Ext.getCmp('mode-depict');
                            switch (newValue['modal'] * 1) {
                                case 1:
                                    depictCmp.setConfig({html: modeDepict[1]});
                                    break;
                                case 2:
                                    depictCmp.setConfig({html: modeDepict[2]});
                                    break;
                                case 3:
                                    depictCmp.setConfig({html: modeDepict[3]});
                                    break;
                                default:
                                    depictCmp.setConfig({html: modeDepict[0]});
                                    break;
                            }
                        }
                    }
                }, {
                    xtype: 'fieldset',
                    title: '注：',
                    id: 'mode-depict',
                    margin: '0 0 0 20',
                    height: 110,
                    width: 270,
                    defaults: {
                        anchor: '100%'
                    },
                    html: modeDepict[0]
                }]
            }, {
                fieldLabel: '尺寸',
                id: 'image-size',
                xtype: 'fieldcontainer',
                layout: 'hbox',
                disabled: true,
                defaults: {
                    width: 80
                },
                items: [{
                    xtype: 'numberfield',
                    maxValue: 9999,
                    minValue: 0,
                    emptyText: "宽(长)",
                    name: 'width'
                }, {
                    xtype: 'displayfield',
                    margin: '0 10 0 10',
                    width: 10,
                    style: {
                        textAlign: 'center'
                    },
                    value: 'x'
                }, {
                    xtype: 'numberfield',
                    maxValue: 9999,
                    minValue: 0,
                    emptyText: "高(短)",
                    name: 'height'
                }, {
                    xtype: 'displayfield',
                    margin: '0 10 0 10',
                    width: 10,
                    flex: 1,
                    value: '(单边最长不得超过9999)'
                }]
            }, {
                fieldLabel: '转换',
                id: 'image-format',
                name: 'format',
                disabled: true,
                xtype: 'radiogroup',
                defaults: {
                    name: 'format'
                },
                items: [
                    {inputValue: 'jpg', boxLabel: 'jpg'},
                    {inputValue: 'gif', boxLabel: 'gif'},
                    {inputValue: 'png', boxLabel: 'png'},
                    {inputValue: 'bmp', boxLabel: 'bmp'}
                ]
            }, {
                xtype: 'fieldcontainer',
                fieldLabel: '文件',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [{
                    xtype: 'filefield',
                    name: 'imgFile',
                    allowBlank: false,
                    blankText: '请选择要上传的文件!',
                    msgTarget: 'side',
                    flex: 1,
                    validator: function (val) {

                        var mediaType = Ext.getCmp("media-type");
                        if (mediaType.getValue()['type'] == 1) {
                            var imgRegex = /^.+\.(jpg|jpeg|gif|png|bmp)$/;
                            return imgRegex.test(val) ? true : '不支持的图片类型! 请上传jpg|jpeg|gif|png|bmp格式的图片!';
                        }
                        if (mediaType.getValue()['type'] == 2) {
                            var audioRegex = /^.+\.(mp3)$/;
                            return audioRegex.test(val) ? true : '不支持的音频类型! 请上传mp3格式的音频文件!';
                        }
                    }
                }, {
                    xtype: 'fieldcontainer',
                    hidelabel: true,
                    layout: {
                        type: 'hbox',
                        align: 'stretch'
                    },
                    defaults: {
                        margin: '0 10 0 0'
                    },
                    items: [{
                        xtype: 'checkbox',
                        boxLabel: '保留文件名',
                        checked: true,
                        inputValue: 1,
                        name: 'keepFileName'
                    }, {
                        xtype: 'checkbox',
                        id: 'image-interlace',
                        boxLabel: '渐进显示(大图)',
                        disabled: true,
                        inputValue: 1,
                        name: 'interlace'
                    }]
                }]
            }
            ],
            buttons: [{
                text: "重置",
                id: 'reset',
                handler: function (self, e) {
                    mediaForm.getForm().reset();
                }
            }, {
                text: "提交",
                handler: function (self, e) {
                    if (mediaForm.isValid()) {
                        mediaForm.getForm().submit({
                            url: 'media/create',
                            method: 'POST',
                            submitEmptyText: false,
                            success: function (form, action) {
                                var result = action.result.result;
                                switch (result) {
                                    case 'success' :
                                        Ext.Msg.alert(alertTitle, '上传成功!', function () {

                                            mediaWindow.hide();
                                            /* reload the list */
                                            mediaStore.reload();
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
                beforeaction: function (self, action, eOpts) {
                    formMask.mask("提交中，请等待...");
                },
                actioncomplete: function (self, action, eOpts) {
                    formMask.unmask();
                },
                actionfailed: function (self, action, eOpts) {
                    formMask.unmask();
                }
            }
        });
        formMask = new Ext.LoadMask({
            target: mediaForm
        });
        mediaWindow = Ext.create("Ext.window.Window", {
            title: '文件上传',
            width: 450,
            resizable: false,
            layout: 'fit',
            closeAction: 'hide',
            modal: true,
            items: [mediaForm],
            listeners: {
                beforeshow: function () {
                    mediaForm.getForm().reset();
                }
            }
        });

        mediaUpdateForm = Ext.create('Ext.form.Panel', {
            bodyPadding: 15,
            fieldDefaults: {
                labelWidth: 55,
                margin: '0 0 5 0'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [{
                id: 'image-id',
                xtype: 'hiddenfield',
                name: 'id'
            }, {
                fieldLabel: '标题',
                id: 'image-update-title',
                xtype: 'textfield',
                name: 'title'
            }, {
                xtype: 'fieldcontainer',
                fieldLabel: '模式',
                layout: 'hbox',
                items: [{
                    id: 'image-update-modal',
                    hideLabel: true,
                    xtype: 'radiogroup',
                    name: 'modal',
                    columns: 1,
                    vertical: true,
                    defaults: {name: 'modal'},
                    items: [
                        {inputValue: '-1', boxLabel: '默认', checked: true},
                        {inputValue: '1', boxLabel: '模式一'},
                        {inputValue: '2', boxLabel: '模式二'},
                        {inputValue: '3', boxLabel: '模式三'}
                    ],
                    listeners: {
                        change: function (self, newValue, oldValue, eOpts) {
                            if (newValue['modal'] != -1) {
                                Ext.getCmp("image-update-size").enable();
                                Ext.getCmp("image-update-interlace").enable();
                            } else {
                                Ext.getCmp("image-update-size").disable();
                                Ext.getCmp("image-update-interlace").disable();
                            }
                            var depictCmp = Ext.getCmp('update-mode-depict');
                            switch (newValue['modal'] * 1) {
                                case 1:
                                    depictCmp.setConfig({html: modeDepict[1]});
                                    break;
                                case 2:
                                    depictCmp.setConfig({html: modeDepict[2]});
                                    break;
                                case 3:
                                    depictCmp.setConfig({html: modeDepict[3]});
                                    break;
                                default:
                                    depictCmp.setConfig({html: modeDepict[0]});
                                    break;
                            }
                        }
                    }
                }, {
                    xtype: 'fieldset',
                    title: '注：',
                    id: 'update-mode-depict',
                    margin: '0 0 0 20',
                    height: 110,
                    width: 270,
                    defaults: {anchor: '100%'},
                    html: modeDepict[0]
                }]
            }, {
                fieldLabel: '尺寸',
                id: 'image-update-size',
                xtype: 'fieldcontainer',
                disabled: true,
                layout: 'hbox',
                defaults: {
                    width: 80
                },
                items: [{
                    xtype: 'numberfield',
                    maxValue: 9999,
                    minValue: 0,
                    emptyText: "宽(长)",
                    id: 'update-width',
                    name: 'width'
                }, {
                    xtype: 'displayfield',
                    margin: '0 10 0 10',
                    width: 10,
                    style: {
                        textAlign: 'center'
                    },
                    value: 'x'
                }, {
                    xtype: 'numberfield',
                    maxValue: 9999,
                    minValue: 0,
                    emptyText: "高(短)",
                    id: 'update-height',
                    name: 'height'
                }, {
                    xtype: 'displayfield',
                    margin: '0 10 0 10',
                    width: 10,
                    flex: 1,
                    value: '(单边最长不得超过9999)'
                }]
            }, {
                fieldLabel: '显示',
                xtype: 'checkbox',
                disabled: true,
                id: 'image-update-interlace',
                boxLabel: '渐进显示(大图)',
                inputValue: 1,
                name: 'interlace'
            }],
            buttons: [{
                text: "重置",
                handler: function (self, e) {
                    mediaForm.getForm().reset();
                }
            }, {
                text: "提交",
                handler: function (self, e) {
                    if (mediaUpdateForm.isValid()) {
                        mediaUpdateForm.getForm().submit({
                            url: 'media/update',
                            method: 'POST',
                            submitEmptyText: false,
                            success: function (form, action) {
                                var result = action.result.result;
                                switch (result) {
                                    case 'success' :
                                        Ext.Msg.alert(alertTitle, '修改成功!', function () {

                                            mediaUpdateWindow.hide();
                                            /* reload the list */
                                            if (gridPanel.getSelectionModel()) gridPanel.getSelectionModel().deselectAll();
                                            mediaStore.reload();
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
                beforeaction: function (self, action, eOpts) {
                    updateFormMask.mask("提交中，请等待...");
                },
                actioncomplete: function (self, action, eOpts) {
                    updateFormMask.unmask();
                },
                actionfailed: function (self, action, eOpts) {
                    updateFormMask.unmask();
                }
            }
        });
        updateFormMask = new Ext.LoadMask({
            target: mediaUpdateForm
        });
        mediaUpdateWindow = Ext.create("Ext.window.Window", {
            title: '属性修改',
            width: 450,
            layout: 'fit',
            closeAction: 'hide',
            resizable: false,
            modal: true,
            items: [mediaUpdateForm],
            listeners: {
                beforeshow: function () {

                    mediaUpdateForm.getForm().reset();

                    var media = gridPanel.getSelection()[0]['data'];
                    Ext.getCmp('image-update-title').setValue(media['title']);
                    Ext.getCmp('image-id').setValue(media['id']);
                    var imageViewParam = media['handle'];

                    if (Ext.isEmpty(imageViewParam)) return;
                    var arr = imageViewParam.split(";");
                    var handleObj = {};
                    Ext.each(arr, function (item, index, allItem) {
                        var itemArr = item.split(":");
                        if (itemArr.length == 1 && !Ext.isEmpty(itemArr[0])) {
                            handleObj['modal'] = itemArr[0];
                        } else {
                            handleObj[itemArr[0]] = itemArr[1];
                        }
                    });
                    Ext.getCmp('image-update-modal').setValue({modal: handleObj['modal']});
                    Ext.getCmp('image-update-size').enable();
                    Ext.getCmp('update-width').setValue(handleObj['w']);
                    Ext.getCmp('update-height').setValue(handleObj['h']);
                    var interlace = Ext.getCmp('image-update-interlace');
                    interlace.enable();
                    interlace.setValue(handleObj['interlace']);
                }
            }
        });

        // make cover the whole screen the gridpanel
        viewport = Ext.create('Ext.container.Viewport', {
            layout: 'border',
            items: [
                gridPanel
            ]
        });

    });
</script>

</body>
</html>