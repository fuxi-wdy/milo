package com.project.milo.controller;

import com.project.milo.common.AjaxResult;
import com.project.milo.common.BaseController;
import com.project.milo.entity.Record;
import com.project.milo.service.IRecordService;
import com.project.milo.utils.TemplateFileUtil;
import com.project.milo.utils.file.MiloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@Controller
@RequestMapping("/api")
public class IndexController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    IRecordService recordService;

    /**
     * 查询30天内没有维护的单位
     */
    @GetMapping("/sy")
    public String indexSy(Record record) {
        List<Record> recordList = recordService.selectRecordByNameAndOp(record);
        getRequest().setAttribute("recordList", recordList);
        return "index/main";
    }

    @GetMapping("/mb")
    public String indexMb() {
        return "index/template";
    }

    /**
     * 上传模板文件
     * ✅ 完整路径：/api/upload
     */
    @PostMapping("/upload")
    @ResponseBody  // ✅ 重要：确保返回JSON格式
    public AjaxResult uploadTemplate(@RequestParam("file") MultipartFile file,@RequestParam("templateName") String templateName) {
        return TemplateFileUtil.uploadTemplate(file, templateName);
    }

    /**
     * 下载模板文件
     * ✅ 完整路径：/api/download
     */
    @GetMapping("/download")
    public AjaxResult downloadTemplate(@RequestParam("templateName") String templateName,HttpServletResponse response) {

        return TemplateFileUtil.downloadTemplate(templateName, response);
    }
}