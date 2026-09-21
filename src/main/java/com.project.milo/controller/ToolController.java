package com.project.milo.controller;

import com.project.milo.common.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/tool")
public class ToolController extends BaseController {


    /**
     * 环境部署*
     * @return
     */
    @GetMapping("/environment")
    public String index() {
        return "tool/environment";
    }


    /**
     * 创建用户或表空间*
     * @return
     */
    @GetMapping("/tablespace")
    public String tablespace() {
        return "tool/tablespace";
    }

    /**
     * oracle相关*
     * @return
     */
    @GetMapping("/oracle")
    public String oracle() {
        return "tool/oracle";
    }

    /**
     * 系统优化*
     * @return
     */
    @GetMapping("/xtyh")
    public String xtyh() {
        return "tool/xtyh";
    }

    /**
     * 问题区划*
     * @return
     */
    @GetMapping("/wtqh")
    public String wtqh() {
        return "tool/wtqh";
    }








    /**
     * 任务管理*
     * @return
     */
    @GetMapping("/rwgl")
    public String rwgl() {
        return "tool/rwgl";
    }

    /**
     * 独立部署配置*
     * @return
     */
    @GetMapping("/dlbspz")
    public String dlbspz() {
        return "tool/dlbspz";
    }

    /**
     * 维护问题汇总*
     * @return
     */
    @GetMapping("/whwthz")
    public String whwthz() {
        return "tool/whwthz";
    }


    /**
     *
     * 其他医疗* @return
     */
    @GetMapping("/qtyl")
    public String qtyl() {
        return "tool/qtyl";
    }


    /**
     * 工具下载
     * * @return
     */
    @GetMapping("/gjxz")
    public String gjxz() {
        return "tool/gjxz";
    }

    /**
     * 上线检查*
     * @return
     */
    @GetMapping("/sxjc")
    public String sxjc() {
        return "tool/sxjc";
    }


    @GetMapping("/tool-gl")
    public String test1() {
        return "tool/tool-gl";
    }

    @GetMapping("/city-sjld")
    public String test2() {
        return "tool/city-sjld";
    }
}
