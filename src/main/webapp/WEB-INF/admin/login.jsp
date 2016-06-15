<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>系统登录</title>

    <script>
        if (window.parent != window) {
            window.parent.location = window.location;
        }
    </script>
    <jsp:include page="_script.jspf"></jsp:include>

</head>
<body>
<script>

    Ext.onReady(function () {

        var loginForm, loginFormSubmit, loginWindow, formMask, failedTitle = '登录失败';

        loginFormSubmit = function () {
            if (loginForm.getForm().isValid()) {
                loginForm.getForm().submit({
                    url: './login',
                    method: 'POST',
                    submitEmptyText: false,
                    success: function (_form, action) {
                        var result = action.result.result;
                        switch (result) {
                            case 'success' :
                                window.location.reload();
                                break;
                            case 'failed':
                                Ext.Msg.alert(failedTitle, "账户或密码错误!").setIcon(Ext.Msg.ERROR);
                                break;
                            default :
                                Ext.Msg.alert(failedTitle, action.result).setIcon(Ext.Msg.ERROR);
                                break;
                        }
                    },
                    failure: submitFailureFn
                });
            }
        };

        loginForm = Ext.create('Ext.form.Panel', {

            width: '100%',
            bodyPadding: 20,
            defaults: {
                labelWidth: 50,
                xtype: 'textfield'
            },
            layout: 'anchor',
            buttonAlign: 'center',
            items: [
                {
                    fieldLabel: '账户',
                    name: 'account',
                    allowBlank: false,
                    emptyText: '请填写帐户名',
                    blankText: '帐户名不能为空',
                    msgTarget: 'side',
                    padding: '5 0 10 5'
                },
                {
                    fieldLabel: '密码',
                    name: 'pwd',
                    allowBlank: false,
                    emptyText: '请填写您的密码',
                    blankText: '密码名不能为空',
                    inputType: 'password',
                    msgTarget: 'side',
                    padding: '0 0 0 5',
                    listeners: {
                        specialkey: function (field, e) {
                            if (e.getKey() == Ext.event.Event.ENTER) {
                                loginFormSubmit();
                            }
                        }
                    }
                }
            ],
            buttons: [
                {
                    text: '重置',
                    handler: function (self, e) {
                        loginForm.getForm().reset();
                    }
                },
                {
                    text: '登录',
                    handler: function () {
                        loginFormSubmit();
                    }
                }
            ],
            listeners: {
                beforeaction: function (self, action, eOpts) {
                    formMask.mask("登录中，请等待...");
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
            target: loginForm
        });

        loginWindow = Ext.create('Ext.window.Window', {
            title: '系统登录',
            width: 300,
            height: 190,
            resizable: false,
            modal: true,
            layout: 'fit',
            anchorSize: '100%',
            closable: false,
            items: [loginForm]
        });

        formBindEnterKey(loginForm, loginFormSubmit);

        loginWindow.show();

    });
</script>

</body>
</html>