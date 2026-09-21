package com.project.milo.controller;

import com.project.milo.common.AjaxResult;
import com.project.milo.common.BaseController;
import com.project.milo.common.page.TableDataInfo;
import com.project.milo.entity.DataMeter;
import com.project.milo.entity.Record;
import com.project.milo.service.IDataMeterService;
import com.project.milo.service.IRecordService;
import com.project.milo.utils.poi.EasyExcelUtils;
import com.project.milo.utils.poi.ProgressExcelUtil;
import com.project.milo.utils.poi.FastExcelUtil;
import cn.idev.excel.FastExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/record")
public class RecordController extends BaseController {

    @Autowired
    private IRecordService recordService;

    @Autowired
    private IDataMeterService dataMeterService;

    /** 导出文件存放目录（与数据资料模块共用） */
    @Value("${milo.export-dir:${user.dir}/export-files}")
    private String exportDir;


    @GetMapping("/index")
    public String index() {
        return "record/list";
    }

    /**
     * 查询维护记录列表*
     * @param record
     * @return
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo recordList(Record record){
        startPage();
        List<Record> list = recordService.selectListRecord(record);
        return getDataTable(list);
    }

    /**
     * 删除维护记录
     */
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult deleteRecord(Record record) {

        if (recordService.deleteRecord(record) > 0) {
            return AjaxResult.success("删除成功");
        }
        return AjaxResult.error("删除失败");
    }

    /**
     * 跳转新增页面
     * @return
     */
    @GetMapping("/add")
    public String addRecord(){
        return "record/add";
    }


    @PostMapping("/query")
    @ResponseBody
    public TableDataInfo queryDataMeterName(DataMeter dataMeter) {
       startPage();
       List<DataMeter> list = dataMeterService.selectListDataMeter(dataMeter);
       return getDataTable(list);
    }

    /**
     * 跳转修改页面
     * @return
     */
    @GetMapping("/edit")
    public String editRecord(Record record,HttpServletRequest request) {
        Record result = recordService.selectRecordById(record);
        request.setAttribute("result",result);
        return "record/edit";
    }



    /**
     * 新增保存
     * @param record
     * @return
     */
    @PostMapping("/addSave")
    @ResponseBody
    public AjaxResult addSave(Record record) {
            int result = recordService.insertRecord(record);
        System.out.println("result:" + result);
            if (result > 0){

                return AjaxResult.success("添加成功");
            }
            return AjaxResult.error("添加失败");
        }

    /**
     * 修改保存
     * @param record
     * @return
     */
    @PostMapping("/editSave")
    @ResponseBody
    public AjaxResult editSave(Record record) {
        int result = recordService.updateRecord(record);
        System.out.println("result:" + result);
        if (result > 0){

            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("修改失败");
    }

    @PostMapping("/export")
    @ResponseBody
    public void export(@RequestBody Record record, HttpServletResponse response)throws IOException {
        List<Record> list = recordService.selectListRecord(record);
        FastExcelUtil.export(response, record.getOp()+"维护记录列表", "维护记录", Record.class, list);
    }


    /**
     * 异步导出维护记录（带 WebSocket 实时进度条）
     * 接口立即返回，后台异步生成 Excel，进度通过 /ws/progress 推送，
     * 完成后进度消息携带下载地址，前端自动下载。
     * @param taskId 前端生成的导出任务ID（用于进度消息匹配，空则服务端自动生成）
     */
    @PostMapping("/RecordExportAsync")
    @ResponseBody
    public AjaxResult exportAsync(Record record, @RequestParam(value = "taskId", required = false) String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            taskId = "EXP-" + UUID.randomUUID().toString().substring(0, 8);
        }
        recordService.exportRecordAsync(taskId.trim(), record);
        return AjaxResult.success("导出任务已创建");
    }

    /**
     * 下载异步导出的 Excel 文件（防路径穿越 + 扩展名白名单）
     * @param fileName 如 record_export_20260911103000.xlsx
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        // 统一走通用工具类：防路径穿越 + 扩展名白名单 + 中文展示名
        ProgressExcelUtil.writeDownload(response, fileName, "record_export_", "维护记录导出");
    }

    /**
     * 异步导入维护记录 Excel（带 WebSocket 实时进度条）
     * 接口立即返回，后台异步逐批入库，进度通过 /ws/progress 推送，
     * 完成后进度消息携带成功/失败条数。
     * @param file   上传的 Excel 文件（.xlsx）
     * @param taskId 前端生成的导入任务ID（用于进度消息匹配，空则服务端自动生成）
     */

    /**
     * 下载导入模板（表头 + 一行示例，供用户按模板填写后导入）
     */
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encoded = URLEncoder.encode("维护记录导入模板.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8'" + encoded);
        // 模板：仅表头 + 一行示例，方便对照填写
        List<Record> sample = new ArrayList<>();
        Record demo = new Record();
        demo.setName("示例单位");
        demo.setRiqi("2026-09-01");
        demo.setQuestion("示例问题描述");
        demo.setAnswer("示例处理方式");
        demo.setOp("wr");
        sample.add(demo);
        FastExcel.write(response.getOutputStream(), Record.class).sheet("维护记录").doWrite(sample);
    }
    @PostMapping("/importAsync")
    @ResponseBody
    public AjaxResult importAsync(@RequestParam("file") MultipartFile file,@RequestParam(value = "taskId", required = false) String taskId) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("请选择要导入的 Excel 文件");
        }
        if (taskId == null || taskId.trim().isEmpty()) {
            taskId = "IMP-" + UUID.randomUUID().toString().substring(0, 8);
        }
        // 必须在请求线程内转存到本地临时文件：
        // 异步线程中 MultipartFile 的临时文件已被 Tomcat 清理，直接使用会报“系统找不到指定的文件”
        java.io.File temp;
        try {
            temp = java.io.File.createTempFile("record_import_", ".xlsx");
            file.transferTo(temp);
        } catch (Exception ex) {
            return AjaxResult.error("文件转存失败：" + ex.getMessage());
        }
        recordService.importRecordAsync(taskId.trim(), temp);
        return AjaxResult.success("导入任务已创建");
    }
    /**
     * 查询在input输入单位名称*
     * @param record
     * @return
     */
    @PostMapping("/search")
    @ResponseBody
    public TableDataInfo searchList(Record record){
        // 输入下拉联想：全局按单位名称搜索（去重、不受当月限制），供搜索框下拉提示
        List<java.util.Map<String, Object>> names = recordService.searchUnitNames(record.getName());
        TableDataInfo data = new TableDataInfo();
        data.setCode(0);
        data.setRows(names);
        data.setTotal(names.size());
        return data;
    }

}
