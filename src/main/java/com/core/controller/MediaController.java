package com.core.controller;

import com.core.security.annotation.AsRight;
import com.core.security.annotation.RightCheck;
import com.core.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Created by sunpeng
 */
@RightCheck(depict = "多媒体管理者")
@Controller
@RequestMapping("/admin")
public class MediaController extends BaseController {

    @Autowired
    private MediaService mediaService;

    @AsRight(id = 1, depict = "多媒体管理页面访问")
    @RequestMapping(value = "/mediaList", method = RequestMethod.GET)
    public String showPageNR() {

        request.setAttribute("initData", mediaService.getInitData());
        return getView("media");
    }

    @AsRight(id = 2, depict = "多媒体查询")
    @ResponseBody
    @RequestMapping(value = "/media/search", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String searchNR(int limit, int page, String id, String title, String category, String status, String creator, String createDate) {

        return mediaService.searchMedia(limit, page, id, title, category, status, creator, createDate);
    }

    @AsRight(id = 3, depict = "多媒体创建(上传)")
    @RequestMapping(value = "/media/create", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity createMediaNR(MultipartHttpServletRequest msr) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity(mediaService.createMedia(msr),headers, HttpStatus.OK);
    }

    @AsRight(id = 4, depict = "多媒体审核(启用)")
    @ResponseBody
    @RequestMapping(value = "/media/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String acceptArticleNR(String data) {

        return mediaService.updateAcceptMedia(data);
    }

    @AsRight(id = 5, depict = "多媒体审核(作废)")
    @ResponseBody
    @RequestMapping(value = "/media/reject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String rejectArticleNR(String data) {

        return mediaService.updateRejectMedia(data);
    }

    @AsRight(id = 6, depict = "多媒体更新(标题)")
    @RequestMapping(value = "/media/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateMediaTitleNR(String data) {

        return mediaService.updateMediaTitle(data);
    }

    @AsRight(id = 7, depict = "多媒体更新(图片操作)")
    @RequestMapping(value = "/media/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateMediaNR(String id,
                                String title,
                                @RequestParam(required = false) String modal,
                                @RequestParam(required = false) String width,
                                @RequestParam(required = false) String height,
                                @RequestParam(required = false) String interlace) {

        return mediaService.updateMediaHandle(id, title, modal, width, height, interlace);
    }

    @AsRight(id = 8, depict = "多媒体配置头条文章")
    @ResponseBody
    @RequestMapping(value = "/media/addHeadLineMedia", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateAddHeadLineMedia(String articleId, String mediaId, String focus) {

        return mediaService.updateAddHeadLineMedia(articleId, mediaId, focus);
    }

    @AsRight(id = 9, depict = "多媒体取消焦点图")
    @ResponseBody
    @RequestMapping(value = "/media/cancelHeadLineMedia", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String updateCancelHeadLineMedia(String data) {

        return mediaService.updateCancelHeadLineMedia(data);
    }

}
