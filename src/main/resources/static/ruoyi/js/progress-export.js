/**
 * 带 WebSocket 实时进度的导出/导入进度弹窗 公共实现（供各模块页面复用）
 *
 * 页面引入本文件后：
 * 1. 页面需包含进度弹窗 DOM（id 约定：exportProgressModal / exportProgressBox /
 *    exportProgressTitleBar / exportProgressTitle / exportProgressBar /
 *    exportProgressText / exportProgressClose），可复制数据资料/维护记录页面写法；
 * 2. 业务 js 直接调用以下公共函数发起带进度任务：
 *    - showExportProgress(pct, text)        显示弹窗
 *    - updateExportProgress(pct, text)      更新进度条与文案（100% 时标题自动切换为“已完成”）
 *    - hideExportProgress()                 隐藏弹窗
 *    - ensureExportWs()                     建立（或复用）/ws/progress 连接（Promise）
 *    - handleExportWsMessage(evt)           处理进度消息（按 currentExportTaskId 过滤，
 *                                           RUNNING 更新进度 / SUCCESS 下载并保持弹窗 / FAILED 关闭并报错）
 *    - currentExportTaskId                  发起任务前先赋值，用于消息匹配
 *
 * 行为约定（与数据资料、维护记录模块完全一致）：
 * - 弹窗宽度 550px、可拖动（按住标题栏）、导出完成后必须点击右上角 × 手动关闭（绝不自动关闭）；
 * - 进度到 100% 时标题显示“已完成”。
 */
(function () {
    // 进度 WebSocket 连接（复用，避免重复建连）
    var exportWs = null;
    // 当前导出任务ID（挂到 window，业务 js 发起任务前直接赋值 currentExportTaskId = 'EXP-xxx' 即可匹配进度消息）
    window.currentExportTaskId = null;
    // 弹窗是否可见
    var exportModalVisible = false;

    /**
     * 建立（或复用）进度 WebSocket 连接
     * @returns {Promise<WebSocket>}
     */
    window.ensureExportWs = function () {
        return new Promise(function (resolve, reject) {
            if (exportWs && exportWs.readyState === WebSocket.OPEN) {
                resolve(exportWs);
                return;
            }
            var proto = location.protocol === 'https:' ? 'wss://' : 'ws://';
            var ws = new WebSocket(proto + location.host + "/ws/progress");
            exportWs = ws;
            ws.onopen = function () { resolve(ws); };
            ws.onerror = function () { reject(new Error('ws error')); };
            ws.onclose = function () {
                if (exportWs === ws) { exportWs = null; }
            };
            ws.onmessage = function (evt) { window.handleExportWsMessage(evt); };
        });
    };

    /**
     * 处理 WebSocket 推送的进度消息（导出/导入通用）
     */
    window.handleExportWsMessage = function (evt) {
        var msg;
        try {
            msg = JSON.parse(evt.data);
        } catch (e) {
            return;
        }
        if (!msg || !msg.status) { return; }
        // 只处理当前任务的消息
        if (window.currentExportTaskId && msg.taskId !== window.currentExportTaskId) { return; }

        if (msg.status === "RUNNING") {
            var pct = Number(msg.percent);
            if (isNaN(pct)) { pct = 0; }
            window.updateExportProgress(pct, msg.message || ("已处理 " + msg.processed + " 条"));
        } else if (msg.status === "SUCCESS") {
            var doneMsg = msg.message || "任务完成";
            if (msg.downloadUrl) {
                // 导出类任务：附带下载地址，自动触发下载
                window.updateExportProgress(100, doneMsg);
                window.location.href = msg.downloadUrl;
            } else {
                // 导入类任务：无下载地址，仅提示完成
                window.updateExportProgress(100, doneMsg);
                // 导入完成：通知页面自动刷新列表（页面监听 progressImportSuccess 事件后调用刷新）
                $(document).trigger('progressImportSuccess', [msg]);
            }
            // 成功后不自动关闭弹窗，由用户点击 × 手动关闭
            window.currentExportTaskId = null;
        } else if (msg.status === "FAILED") {
            window.hideExportProgress();
            layer.msg(msg.message || '任务失败', {icon: 5});
            window.currentExportTaskId = null;
        }
    };

    /**
     * 显示进度弹窗（将 transform 居中位置固化为 left/top，之后才能自由拖动）
     */
    window.showExportProgress = function (pct, text) {
        exportModalVisible = true;
        $("#exportProgressModal").show();
        var box = document.getElementById('exportProgressBox');
        if (box && box.style.transform && box.style.transform !== 'none') {
            var rect = box.getBoundingClientRect();
            box.style.left = rect.left + 'px';
            box.style.top = rect.top + 'px';
            box.style.transform = 'none';
        }
        window.updateExportProgress(pct, text);
    };

    /**
     * 更新进度条与文案（进度 100% 时标题显示“已完成”）
     */
    window.updateExportProgress = function (pct, text) {
        if (!exportModalVisible) { return; }
        pct = Math.max(0, Math.min(100, pct));
        var $bar = $("#exportProgressBar");
        $bar.css("width", pct + "%");
        $bar.text(Math.round(pct) + "%");
        $("#exportProgressTitle").text(pct >= 100 ? "已完成" : "正在导出数据");
        $("#exportProgressText").text(text || "");
    };

    /**
     * 隐藏进度弹窗
     */
    window.hideExportProgress = function () {
        exportModalVisible = false;
        $("#exportProgressModal").hide();
    };

    window.startProgressImport = function (config) {
        config = config || {};
        if (!config.url) {
            layer.msg("未配置导入接口", {icon: 5});
            return;
        }
        // 先建立（或复用）进度 WebSocket 连接，确保导入过程能实时收到进度推送；
        // 连接建立成功后再进入文件选择/上传流程，避免首次导入时进度收不到
        window.ensureExportWs().then(function () {
            var doImport = function (file) {
                if (!file) {
                    layer.msg("请先选择要导入的 Excel 文件", {icon: 5});
                    return;
                }
                if (!/\.(xlsx|xls)$/i.test(file.name)) {
                    layer.msg("请选择 Excel 文件（.xlsx / .xls）", {icon: 5});
                    return;
                }
                // 与导出共用同一任务ID通道与进度消息过滤
                window.currentExportTaskId = "IMP-" + Date.now() + "-" + Math.random().toString(36).slice(2, 8);
                window.showExportProgress(0, "正在上传文件...");
                var fd = new FormData();
                fd.append("file", file);
                // taskId 只走 URL query（后端 @RequestParam 读取），避免与表单字段合并成逗号串
                $.ajax({
                    url: config.url + "?taskId=" + encodeURIComponent(window.currentExportTaskId),
                    type: "POST",
                    data: fd,
                    processData: false,
                    contentType: false,
                    dataType: "json",
                    success: function (res) {
                        if (res && res.code === 0) {
                            // 只更新提示文案，不重置百分比（WS 可能已推送真实进度，避免进度条被覆盖回退到 0%）
                            $("#exportProgressText").text("导入任务已创建，正在解析数据...");
                            if (config.onDone) { config.onDone(res); }
                        } else {
                            window.hideExportProgress();
                            layer.msg((res && res.msg) || "导入任务创建失败", {icon: 5});
                        }
                    },
                    error: function () {
                        window.hideExportProgress();
                        layer.msg("导入请求失败", {icon: 5});
                        if (config.onError) { config.onError(); }
                    }
                });
            };
            // 向导场景：外部已选择文件，直接上传；否则自动弹出文件选择框
            if (config.file) {
                doImport(config.file);
                return;
            }
            var $file = $("#progressImportFile");
            if (!$file.length) {
                $file = $("<input type=\"file\" id=\"progressImportFile\" accept=\"" + (config.accept || ".xlsx,.xls") + "\" style=\"display:none;\">").appendTo("body");
            }
            $file.off("change.progressImport").on("change.progressImport", function () {
                var file = this.files && this.files[0];
                if (!file) { return; }
                $(this).val("");
                if (config.confirmText) {
                    $.modal.confirm(config.confirmText, function () {
                        doImport(file);
                    });
                } else {
                    doImport(file);
                }
                $(this).val('');
            });
            $file.trigger('click');
        }, function () {
            layer.msg("无法连接进度服务，请稍后重试", {icon: 5});
        });
    };

    // 手动关闭进度弹窗（成功后由用户点击 × 关闭）
    $(document).on('click', '#exportProgressClose', function () {
        window.hideExportProgress();
    });

    // 进度弹窗拖拽（按住标题栏移动窗口）
    $(document).on('mousedown', '#exportProgressTitleBar', function (e) {
        if ($(e.target).closest('#exportProgressClose').length) { return; }
        var box = document.getElementById('exportProgressBox');
        if (!box) { return; }
        var startX = e.clientX, startY = e.clientY;
        var rect = box.getBoundingClientRect();
        var origLeft = rect.left, origTop = rect.top;
        $(document).on('mousemove.progressDrag', function (ev) {
            box.style.left = (origLeft + ev.clientX - startX) + 'px';
            box.style.top = (origTop + ev.clientY - startY) + 'px';
        });
        $(document).on('mouseup.progressDrag', function () {
            $(document).off('mousemove.progressDrag').off('mouseup.progressDrag');
        });
        e.preventDefault();
    });
})();
