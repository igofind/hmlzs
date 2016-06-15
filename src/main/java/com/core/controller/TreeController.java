package com.core.controller;

import com.core.domain.Tree;
import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.TreeService;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by sunpeng
 * <p/>
 * NR == NeedRight
 */
@RightCheck(depict = "树(Tree)管理者")
@Controller
@RequestMapping("/admin")
public class TreeController extends BaseController {

    @Autowired
    private TreeService treeService;

    @AsRight(id = 1, depict = "树(Tree)列表页面访问")
    @RequestMapping("/treeList")
    public String showPageNR() {

        return getView("tree");
    }

    @ResponseBody
    @RequestMapping(value = "/tree/nav", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String treeNavNR() {

        return treeService.buildTreeNav().toString();
    }

    @AsRight(id = 2, depict = "树(Tree)数据列表")
    @ResponseBody
    @RequestMapping(value = "/tree/list", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String listNR(String parentId, int pageSize, int page) {

        return treeService.treeList(parentId, pageSize, page).toString();
    }

    @AsRight(id = 3, depict = "树(Tree)创建")
    @ResponseBody
    @RequestMapping(value = "/tree/create", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String createTreeNR(Tree tree) {

        return treeService.createTreeOrNode(tree).toString();
    }

    @AsRight(id = 4, depict = "树(Tree)更新")
    @ResponseBody
    @RequestMapping(value = "/tree/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateTreeNR(String data) throws Exception {

        return treeService.updateTreeOrNode(data).toString();
    }

    @AsRight(id = 5, depict = "树(Tree)删除")
    @ResponseBody
    @RequestMapping(value = "/tree/delete", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String deleteTreeNR(String data) throws Exception {

        return treeService.deleteTreeOrNode(data).toString();
    }

    @AsRight(id = 6, depict = "树(Tree)审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/tree/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptTreeNR(String data) throws Exception {

        return treeService.updateAcceptTree(data).toString();

    }

    @AsRight(id = 7, depict = "树(Tree)审核(废弃)")
    @ResponseBody
    @RequestMapping(value = "/tree/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectTreeNR(String data) throws Exception {

        return treeService.updateRejectTree(data).toString();
    }

}
