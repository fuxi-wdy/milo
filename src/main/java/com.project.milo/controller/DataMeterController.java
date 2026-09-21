package com.project.milo.controller;

import com.project.milo.common.AjaxResult;
import com.project.milo.common.BaseController;
import com.project.milo.common.page.TableDataInfo;
import com.project.milo.entity.DataMeter;
import com.project.milo.service.IDataMeterService;
import com.project.milo.utils.poi.EasyExcelUtils;
import com.project.milo.utils.poi.ProgressExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/dataMeter")
public class DataMeterController extends BaseController {


    @Autowired
    private IDataMeterService dataMeterService;


    /**
     * 跳转项目列表首页
     * @return
     */
    @GetMapping("/index")
    public String dataMeterIndex(){
        return "datameter/list";
    }


    /**
     * 项目列表
     * @return
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo dataMeterList(DataMeter dataMeter){
        startPage();
        //clearPage();
        List<DataMeter> list = dataMeterService.selectListDataMeter(dataMeter);
        return getDataTable(list);
    }

    /**
     * 跳转新增页面
     * @return
     */
    @GetMapping("/add")
    public String addDataMeter(){
        return "datameter/add";
    }

    /**
     * 新增保存
     * 修改保存
     * @param dataMeter
     * @return
     */
    @PostMapping("/addSave")
    @ResponseBody
        public AjaxResult addSave(DataMeter dataMeter, @RequestParam Long scId,@RequestParam String status) {
        if (status.equals("1")){
            dataMeter.setBoss("admin");
            dataMeter.setRemark("add");
            dataMeterService.insertDataMeter(dataMeter);
            return AjaxResult.success("添加成功");
        }else if(status.equals("2")){
            dataMeterService.updateDataMeter(dataMeter);
            return AjaxResult.success("修改成功");
        }
        return AjaxResult.error("操作失败");
    }

    /**
     * 跳转修改页面
     * @return
     */
    @GetMapping("/edit")
    public String editDataMeter(DataMeter dataMeter, HttpServletRequest request){
        DataMeter result = dataMeterService.selectDataMeterById(dataMeter.getScId());
        request.setAttribute("result",result);
        return "datameter/edit";
    }

    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult deleteDataMeter(DataMeter dataMeter) {

        if (dataMeterService.deleteDataMeter(dataMeter) > 0) {
            return AjaxResult.success("删除成功");
        }
        return AjaxResult.error("删除失败");
    }

    /**
     * 同步导出（原有功能，保留兼容；浏览器直接下载，无进度条）
     * 页面导出按钮已切换为 exportAsync + WebSocket 进度条方案
     */
    /*@PostMapping("/export")
    @ResponseBody
    public void export(DataMeter dataMeter, HttpServletResponse response)throws IOException {

        //实现二 easyexcel,注意在实体类需要用 注解
        List<DataMeter> list = dataMeterService.selectListDataMeter(dataMeter);
        EasyExcelUtils.exportExcel(response, "资料数据", "资料数据列表", DataMeter.class, list);
    }*/

    /**
     * 异步导出（带 WebSocket 实时进度条）
     * 接口立即返回，后台异步生成 Excel，进度通过 /ws/progress 推送，
     * 完成后进度消息携带下载地址，前端自动下载。
     * @param taskId 前端生成的导出任务ID（用于进度消息匹配，空则服务端自动生成）
     */
    @PostMapping("/exportAsync")
    @ResponseBody
    public AjaxResult exportAsync(DataMeter dataMeter, @RequestParam(value = "taskId", required = false) String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            taskId = "EXP-" + UUID.randomUUID().toString().substring(0, 8);
        }
        dataMeterService.exportDataMeterAsync(taskId.trim(), dataMeter);
        return AjaxResult.success("导出任务已创建");
    }

    /**
     * 下载异步导出的 Excel 文件（防路径穿越 + 扩展名白名单）
     * @param fileName 如 dataMeter_export_20260911103000.xlsx
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        // 统一走通用工具类：防路径穿越 + 扩展名白名单 + 中文展示名
        ProgressExcelUtil.writeDownload(response, fileName, "dataMeter_export_", "数据资料导出");
    }


    /**
     * 模糊查询input的单位名称*
     * @param dataM
     * @return
     */
    @PostMapping("/search")
    @ResponseBody
    public TableDataInfo searchUnits(DataMeter dataM) {
        startPage();
        //clearPage();
        List<DataMeter> list = dataMeterService.selectListDataMeter(dataM);
        return getDataTable(list);
    }

}
