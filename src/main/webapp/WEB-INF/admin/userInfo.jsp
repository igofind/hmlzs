<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>资料更新</title>

    <META HTTP-EQUIV="pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate">
    <META HTTP-EQUIV="expires" CONTENT="0">

    <jsp:include page="_script.jspf"></jsp:include>
</head>
<body>

<script>
    var stopSpaceKeyFn, userPanel, viewPort;

    Ext.onReady(function () {
        stopSpaceKeyFn = function (self, e, eOpts) {
            if (e.event.keyCode == 32) {
                e.stopEvent();
            }
        };

        userPanel = Ext.create("Ext.form.Panel", {

                    title: '资料更新',
                    scrollable: true,
                    width: 500,
                    draggable: true,
                    buttonAlign: 'center',
                    defaultType: 'textfield',
                    bodyPadding: '20 40 20 20',
                    fieldDefaults: {
                        enableKeyEvents: true,
                        autoFitErrors: false,
                        labelAlign: 'right',
                        labelWidth: 65,
                        msgTarget: 'side',
                        labelPad: 30,
                        anchor: '100%',
                        padding: '0 0 7 0'
                    },
                    items: [
                        {
                            fieldLabel: '账号',
                            name: 'account',
                            value: '${user.account}',
                            disabled: true
                        },
                        {
                            fieldLabel: '<span style="color:red">* </span>姓名',
                            value: '${user.name}',
                            name: 'name',
                            allowBlank: false,
                            maxLength: 200,
                            maxLengthText: "长度不能超过200",
                            emptyText: '必填',
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
                            value: '${user.phone}',
                            maxLength: 11,
                            maxLengthText: "长度不能超过11",
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
                            value: '${user.mail}',
                            maxLength: 200,
                            maxLengthText: "长度不能超过200",
                            vtype: 'email',
                            listeners: {
                                keydown: stopSpaceKeyFn
                            }
                        },
                        {
                            fieldLabel: '描述',
                            name: 'depict',
                            value: '${user.depict}',
                            xtype: 'textarea',
                            maxLength: 200,
                            maxLengthText: "长度不能超过200",
                            listeners: {
                                keydown: stopSpaceKeyFn
                            }
                        }
                    ],
                    buttons: [
                        {
                            text: '重置',
                            handler: function (self, e) {
                                userPanel.getForm().reset();
                            }
                        },
                        {
                            text: '提交',
                            handler: function (self, e) {
                                var form = userPanel.getForm();
                                if (form.isValid()) {

                                    form.submit({
                                        url: 'account/updateInfo',
                                        method: 'POST',
                                        submitEmptyText: false,

                                        success: function (form, action) {

                                            var result = action.result.result;
                                            switch (result) {
                                                case 'success' :
                                                    Ext.Msg.alert(alertTitle, "信息已更新!", function () {
                                                        window.location.reload();
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
                    ]
                }
        );

        viewPort = Ext.create('Ext.container.Viewport', {
            layout: 'center',
            items: [userPanel]
        });
    })
    ;
</script>

</body>
</html>