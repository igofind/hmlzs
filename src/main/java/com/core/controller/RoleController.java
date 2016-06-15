package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.RoleService;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "角色管理者")
@RequestMapping("/admin")
@Controller
public class RoleController extends BaseController {

    @Autowired
    private RoleService roleService;

    @AsRight(id = 1, depict = "角色页面访问")
    @RequestMapping("/roleList")
    public String showPageNR() {

        return getView("role");
    }

    @AsRight(id = 2, depict = "角色列表数据查询")
    @ResponseBody
    @RequestMapping(value = "/role/listAll", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listAllNR() {

        return roleService.listAll().toString();
    }

    @AsRight(id = 3, depict = "角色权限搜索")
    @ResponseBody
    @RequestMapping(value = "/role/filterList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listRightsNR(String[] roleIds, String str) {

        return roleService.roleRightSearch(roleIds, str).toString();
    }

    @AsRight(id = 4, depict = "角色创建")
    @ResponseBody
    @RequestMapping(value = "/role/create", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String createRoleNR(String name, String depict, String[] rightIds) {

        return roleService.createRole(name, depict, rightIds).toString();
    }

    @AsRight(id = 5, depict = "角色权限添加")
    @ResponseBody
    @RequestMapping(value = "/role/addRight", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String addRightNR(String[] roleIds, String[] rightIds) {

        return roleService.updateAddRight(roleIds, rightIds).toString();
    }

    @AsRight(id = 6, depict = "角色更新")
    @ResponseBody
    @RequestMapping(value = "/role/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateRoleNR(String data) {

        return roleService.update(data).toString();
    }

    @AsRight(id = 7, depict = "角色审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/role/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptRoleNR(String data) {

        return roleService.updateAccept(data).toString();
    }

    @AsRight(id = 8, depict = "角色审核(废弃)")
    @ResponseBody
    @RequestMapping(value = "/role/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectRoleNR(String data) {

        return roleService.updateReject(data).toString();
    }

    @AsRight(id = 9, depict = "角色删除")
    @ResponseBody
    @RequestMapping(value = "/role/deleteRole", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String deleteRoleNR(String data) {

        return roleService.deleteRole(data).toString();
    }

    @AsRight(id = 10, depict = "角色权限删除")
    @ResponseBody
    @RequestMapping(value = "/role/deleteRight", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String deleteRoleRightsNR(String data) {

        return roleService.deleteRoleRights(data).toString();
    }

    @AsRight(id = 11, depict = "角色克隆")
    @ResponseBody
    @RequestMapping(value = "/role/clone", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String cloneRoleNR(String data) {

        return roleService.updateCloneRole(data);
    }

}
