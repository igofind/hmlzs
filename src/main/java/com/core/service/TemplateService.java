package com.core.service;

import com.core.domain.Template;
import com.core.repository.sqlBuilder.Page;
import com.core.util.Constant;
import com.core.util.DateUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.HtmlUtils;

import java.io.*;
import java.util.*;

/**
 * Created by sunpeng
 */
@Service
public class TemplateService extends BaseService {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectNode createTemplate(String name, MultipartHttpServletRequest msr) {

        ObjectNode result = objectMapper.createObjectNode();

        Iterator<String> fileFiledNames = msr.getFileNames();

        if (!fileFiledNames.hasNext()) {
            result.put("result", "noFile");
            return result;
        }

        while (fileFiledNames.hasNext()) {

            String next = fileFiledNames.next();

            MultipartFile nextFile = msr.getFile(next);
            String fileName = nextFile.getOriginalFilename();
            File target = new File(getTemplatePath(fileName));
            try {

                if (target.exists()) {
                    target = new File(getTemplatePath(DateUtil.getNameWithTime(fileName)));
                } else {
                    target.mkdirs();
                }

                nextFile.transferTo(target);

                Template template = new Template();
                template.setFileName(target.getName());
                template.setName(name);
                template.setPath(target.getPath().replace(getTemplateRootPath(), ""));
                template.setCreateDate(new Date());
                template.setId(baseRepository.create(template));

                log("模版创建", template.toString());

            } catch (Exception e) {
                logger.error(e);
                if (target.exists()) {
                    target.delete();
                }
                throw new RuntimeException("");
            }
                /* just need one file (if you need save more file, delete the next line code)*/
            break;
        }

        result.put("result", "success");
        result.put("success", true);
        return result;

    }

    public ObjectNode list(int pageSize, int pageNum, String sql) {

        ObjectNode objectNode = objectMapper.createObjectNode();
        Page<Template> page = this.getPage(Template.class, sql, null, pageSize, pageNum);

        List<Template> resultList = new ArrayList<Template>();
        List<Template> _resultList = page.getResultList();

        int len = _resultList.size();

        for (int i = 0; i < len; i++) {
            Template template = _resultList.get(i);
            template.setName(HtmlUtils.htmlEscape(template.getName()));
            resultList.add(template);
        }

        objectNode.put("data", objectMapper.valueToTree(resultList));
        objectNode.put("totalData", page.getTotalData());
        objectNode.put("success", true);

        return objectNode;

    }

    public ObjectNode listAll(int pageSize, int pageNum) {
        return list(pageSize, pageNum, " ORDER BY createDate DESC ");
    }

    public ObjectNode updateTemplateTitle(String data) {
        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("templateIds:[ ");

            while (iterator.hasNext()) {
                ObjectNode nextNode = (ObjectNode) iterator.next();
                nextNode.put("createDate", DateUtil.extDateFix(nextNode.findValue("createDate").getTextValue()));
                nextNode.put("updateDate", DateUtil.extDateFix(nextNode.findValue("updateDate").getTextValue()));

                Template template = objectMapper.readValue(nextNode, Template.class);

                template.setName(template.getName());
                template.setUpdateDate(new Date());

                baseRepository.update(template);

                stringBuilder.append(template.getId()).append(" ");
            }

            log("模板更新(标题)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateTemplateFile(String id, String name, String content) {

        BufferedWriter bw = null;

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();
            StringBuilder stringBuilder = new StringBuilder("templateIds:[ ");

            int templateId = NumberUtils.toInt(id, 0);
            if (templateId > 0) {
                Template template = find(Template.class, NumberUtils.toInt(id));
                File file = new File(getTemplatePath(template.getPath()));
                bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Constant.ENCODE_UTF8));
                bw.write(content);

                template.setName(name);
                template.setUpdateDate(new Date());
                baseRepository.update(template);
                log("模板更新(标题)", stringBuilder.append("]").toString());
            }

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    public ObjectNode deleteTemplate(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("templateIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();

                if (id > 0) {
                    baseRepository.delete(Template.class, id);
                    stringBuilder.append(id).append(" ");
                }
            }

            log("模板删除", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateAcceptTemplate(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("templateIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Template template = find(Template.class, id);
                    template.setStatus(Constant.COMMON_STATUS_ACCEPT);
                    template.setUpdateDate(new Date());
                    baseRepository.update(template);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("模板审核(启用)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public ObjectNode updateRejectTemplate(String data) {

        try {
            ObjectNode resultNode = objectMapper.createObjectNode();

            JsonNode jsonNode = objectMapper.readTree(data);
            Iterator<JsonNode> iterator = jsonNode.iterator();

            StringBuilder stringBuilder = new StringBuilder("templateIds:[ ");

            while (iterator.hasNext()) {
                JsonNode nextNode = iterator.next();

                long id = nextNode.getLongValue();
                if (id > 0) {
                    Template template = find(Template.class, id);
                    template.setStatus(Constant.COMMON_STATUS_REJECT);
                    template.setUpdateDate(new Date());
                    baseRepository.update(template);

                    stringBuilder.append(id).append(" ");
                }
            }

            log("模板审核(废弃)", stringBuilder.append("]").toString());

            resultNode.put("result", "success");
            resultNode.put("success", true);
            return resultNode;

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("");
        }
    }

    public String getTemplateRootPath() {

        return (System.getProperty("webapp.root") + config.getTemplateDir() + File.separator)
                .replace("/", File.separator)
                .replace("\\", File.separator);
    }

    public String getTemplatePath(String fileName) {
        return getTemplateRootPath() + fileName;
    }

    public String getTemplateContent(String id) {
        long templateId = 0;
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("success", true);
        objectNode.put("result", "success");

        if (!NumberUtils.isNumber(id) || (templateId = NumberUtils.toInt(id)) <= 0) {
            return objectNode.toString();
        }

        Template template = find(Template.class, templateId);
        File file = new File(getTemplateRootPath() + template.getPath());
        objectNode.put("name", template.getName());
        StringBuilder sb = new StringBuilder();
        char[] buff = new char[Constant.TEMPLATE_FILE_BUFF_SIZE];
        int count = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constant.ENCODE_UTF8));
            while ((count = br.read(buff)) != -1) {
                sb.append(Arrays.copyOf(buff, count));
            }
            objectNode.put("content", sb.toString());
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                logger.error(e);
            }
        }

        return objectNode.toString();
    }

}
