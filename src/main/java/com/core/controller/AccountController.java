package com.core.controller;

import com.core.security.SecuritySupport;
import com.core.security.UserInfo;
import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.security.domain.User;
import com.core.service.AccountService;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "账户管理者")
@Controller
@RequestMapping("/admin")
public class AccountController extends BaseController {

    @Autowired
    private AccountService accountService;

    @AsRight(id = 1, depict = "账户列表页面访问")
    @RequestMapping("/accountList")
    public String showPageNR() {
        return getView("accounts");
    }

    @AsRight(id = 2, depict = "账户列表查询")
    @ResponseBody
    @RequestMapping(value = "/account/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR(int pageSize, int page) {

        return accountService.listAll(pageSize, page).toString();
    }

    @AsRight(id = 3, depict = "账户创建")
    @ResponseBody
    @RequestMapping("/account/create")
    public String createAccountNR(User user){

        return accountService.createAccount(user).toString();
    }

    @AsRight(id = 4, depict = "账户解锁")
    @ResponseBody
    @RequestMapping(value = "/account/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptAccountNR(String data) {

        return accountService.updateAcceptAccount(data).toString();
    }

    @AsRight(id = 5, depict = "账户锁定")
    @ResponseBody
    @RequestMapping(value = "/account/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectAccountNR(String data) {

        return accountService.updateRejectAccount(data).toString();
    }

    // @AsRight(id = 6, depict = "账户详细信息")
    @RequestMapping(value = "/accountInfo")
    public String userInfoNR(ModelMap modelMap) throws Exception {

        SecuritySupport securitySupport = supportFactory.getSecuritySupport();
        UserInfo userInfo = securitySupport.getUserInfo();
        modelMap.put("user", userInfo);

        return getView("userInfo");
    }

    @AsRight(id = 7, depict = "账户信息更新")
    @ResponseBody
    @RequestMapping(value = "/account/updateInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateInfoNR(User user) {

        return accountService.updateInfo(user).toString();
    }

    //@AsRight(id = 8, depict = "账户密码修改页面")
    @RequestMapping(value = "/accountPwd")
    public String showPwdPageNR(ModelMap modelMap) throws Exception {

        SecuritySupport securitySupport = supportFactory.getSecuritySupport();
        UserInfo userInfo = securitySupport.getUserInfo();
        modelMap.put("user", userInfo);

        return getView("userPwd");
    }

    @AsRight(id = 9, depict = "账户密码修改")
    @ResponseBody
    @RequestMapping(value = "/account/updatePwd", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updatePwdNR(String oldPwd, String pwd, String cfrmPwd) {

        return accountService.updatePwd(oldPwd, pwd, cfrmPwd).toString();
    }

    @AsRight(id = 10, depict = "账户:角色查询")
    @ResponseBody
    @RequestMapping(value = "/account/role/filterList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String roleFilterList(String[] accountId, String str) {

        return accountService.listUsersRoles4Filter2(accountId, str).toString();
    }

    @AsRight(id = 11, depict = "账户:角色添加")
    @ResponseBody
    @RequestMapping(value = "/account/addRole", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String addRole(String[] accountId, String[] roleIds) {

        return accountService.createUserRoles(accountId, roleIds).toString();
    }

    @AsRight(id = 12, depict = "账户升级")
    @ResponseBody
    @RequestMapping(value = "/account/upgrade", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String upgrade(String data) {

        return accountService.updateUpgrade(data).toString();
    }

    @AsRight(id = 13, depict = "账户降级")
    @ResponseBody
    @RequestMapping(value = "/account/degrade", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String degrade(String data) {

        return accountService.updateDegrade(data).toString();
    }

    @AsRight(id = 14, depict = "账户密码重置")
    @ResponseBody
    @RequestMapping(value = "/account/resetPwd", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String resetPwd(String data) {

        return accountService.updateResetPwd(data).toString();
    }
}
