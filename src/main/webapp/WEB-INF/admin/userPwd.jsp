<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>密码修改</title>

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

                    title: '密码修改',
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
                            fieldLabel: '<span style="color:red">* </span>旧密码',
                            inputType: 'password',
                            name: 'oldPwd',
                            itemId: 'oldPwd',
                            allowBlank: false,
                            emptyText: '必填'
                        },
                        {
                            fieldLabel: '<span style="color:red">* </span>新密码',
                            inputType: 'password',
                            name: 'pwd',
                            itemId: 'pass',
                            allowBlank: false,
                            emptyText: '6-20位,由数字、字母(大小写)、特殊符号构成',
                            validator: function (val) {
                                return /(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,20}/.test(val) ? true : '6-20位,由数字、字母(大小写)、特殊符号构成';
                            },
                            listeners: {
                                change: function (self, e, eOpts) {
                                    userPanel.getComponent('pass-cfrm').reset();
                                }
                            }
                        },
                        {
                            fieldLabel: '<span style="color:red">* </span>密码确认',
                            inputType: 'password',
                            itemId: 'pass-cfrm',
                            name: 'cfrmPwd',
                            emptyText: '必填',
                            validator: function (val) {
                                var pass = userPanel.getComponent('pass');
                                return (val == pass.getValue());
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
                                        url: 'account/updatePwd',
                                        method: 'POST',
                                        submitEmptyText: false,

                                        success: function (form, action) {

                                            var result = action.result.result;
                                            switch (result) {
                                                case 'success':
                                                    Ext.Msg.alert(alertTitle, "密码已更新!").setIcon(Ext.Msg.INFO);
                                                    userPanel.getForm().reset();
                                                    break;
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
                                                case 'wrongpwd':
                                                    Ext.Msg.alert(alertTitle, '旧密码输入错误!').setIcon(Ext.Msg.ERROR);
                                                    break;
                                                case 'different' :
                                                    Ext.Msg.alert(alertTitle, '两次输入的新密码不相同!').setIcon(Ext.Msg.ERROR);
                                                    break;
                                                case 'simple' :
                                                    Ext.Msg.alert(alertTitle, '密码不符合要求!').setIcon(Ext.Msg.ERROR);
                                                    break;
                                                default :
                                                    Ext.Msg.alert(alertTitle, result).setIcon(Ext.Msg.ERROR);
                                                    break;
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