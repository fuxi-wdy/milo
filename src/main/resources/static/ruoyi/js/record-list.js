
// ========== 搜索单位（输入时触发） ==========
var searchTimer = null;

// 输入框事件监听
$(document).on('input', '#searchKeyword', function() {
    var value = $(this).val();
    doQuery(value);
});

function doQuery(value) {
    // 防抖：避免频繁请求
    if (searchTimer) {
        clearTimeout(searchTimer);
    }
    searchTimer = setTimeout(function() {
        _doQuery(value);
    }, 300);
}

function _doQuery(value) {
    var $dropdown = $("#dropdown");

    // 如果输入为空，隐藏下拉列表并清空内容
    if (!value || value.trim() === "") {
        $dropdown.empty().hide();
        return;
    }

    // 显示加载状态
    $dropdown.html('<div style="padding: 8px 12px; color: #999;">加载中...</div>').show();

    $.ajax({
        type: "POST",
        url: "/api/record/search",
        data: { name: value.trim() },
        success: function(res) {
            var list = res.rows || res.data || [];
            $dropdown.empty();

            if (!list || list.length === 0) {
                // 没有数据，隐藏下拉列表
                $dropdown.hide();
                return;
            }

            // 有数据，填充下拉列表并显示
            $.each(list, function(index, item) {
                var $item = $('<div class="dropdown-item" style="padding: 8px 12px; cursor: pointer; border-bottom: 1px solid #f5f5f5;"></div>')
                    .text(item.name)
                    .data('id', item.id)
                    .data('name', item.name)
                    .on('click', function() {
                        selectItem($(this));
                    })
                    .on('mouseenter', function() {
                        $(this).css('background-color', '#f0f0f0');
                    })
                    .on('mouseleave', function() {
                        $(this).css('background-color', '');
                    });
                $dropdown.append($item);
            });

            $dropdown.show();

            // 可选：自动选中第一个（如需开启取消注释）
            // $dropdown.find('.dropdown-item:first').click();
        },
        error: function(xhr, status, error) {
            console.error("搜索失败:", status, error);
            $dropdown.empty().hide();
            // 可以显示错误提示（可选）
            // $dropdown.html('<div style="padding: 8px 12px; color: #ff6b6b;">搜索失败，请重试</div>').show();
        }
    });
}

// ========== 选中下拉项 ==========
function selectItem($item) {
    var id = $item.data('id');
    var name = $item.data('name');

    // 将选中的值填入输入框
    $("#searchKeyword").val(name);

    // 隐藏下拉列表
    $("#dropdown").empty().hide();

    // 触发自定义事件，方便其他逻辑处理
    $(document).trigger('unitSelected', { id: id, name: name });

    // 可选：存储选中的ID到隐藏域
    // $("#selectedUnitId").val(id);

    console.log('选中单位:', name, 'ID:', id);
}

// ========== 点击其他地方关闭下拉列表 ==========
$(document).on('click', function(e) {
    var $target = $(e.target);
    if (!$target.closest('.search-box').length) {
        $("#dropdown").empty().hide();
    }
});

// ========== 键盘上下键选择（提升用户体验） ==========
$(document).on('keydown', '#searchKeyword', function(e) {
    var $dropdown = $("#dropdown");
    if (!$dropdown.is(':visible') || $dropdown.children().length === 0) return;

    var $items = $dropdown.children('.dropdown-item');
    var $current = $items.filter('.active');

    if (e.key === 'ArrowDown') {
        e.preventDefault();
        if ($current.length === 0) {
            $items.first().addClass('active').css('background-color', '#f0f0f0');
        } else {
            $current.removeClass('active').css('background-color', '');
            var $next = $current.next('.dropdown-item');
            if ($next.length) {
                $next.addClass('active').css('background-color', '#f0f0f0');
            } else {
                $items.first().addClass('active').css('background-color', '#f0f0f0');
            }
        }
    } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        if ($current.length === 0) {
            $items.last().addClass('active').css('background-color', '#f0f0f0');
        } else {
            $current.removeClass('active').css('background-color', '');
            var $prev = $current.prev('.dropdown-item');
            if ($prev.length) {
                $prev.addClass('active').css('background-color', '#f0f0f0');
            } else {
                $items.last().addClass('active').css('background-color', '#f0f0f0');
            }
        }
    } else if (e.key === 'Enter') {
        e.preventDefault();
        if ($current.length) {
            selectItem($current);
        }
    } else if (e.key === 'Escape') {
        $dropdown.empty().hide();
    }
});

// ========== 获取选中的单位信息（供外部调用） ==========
function getSelectedUnit() {
    var name = $("#searchKeyword").val();
    // 如果有隐藏域存储ID，可以一起返回
    // var id = $("#selectedUnitId").val();
    return {
        name: name
        // id: id
    };
}

// ========== 设置选中的单位（供外部调用，回显数据） ==========
function setSelectedUnit(name, id) {
    $("#searchKeyword").val(name || '');
    // if (id) {
    //     $("#selectedUnitId").val(id);
    // }
    // 触发选中事件
    $(document).trigger('unitSelected', { id: id, name: name });
}

// ========== 清除选中的单位 ==========
function clearSelectedUnit() {
    $("#searchKeyword").val('');
    $("#dropdown").empty().hide();
    // $("#selectedUnitId").val('');
}

// ========== 关闭页面 ==========
function closeItem() {
    var index = parent.layer.getFrameIndex(window.name);
    parent.layer.close(index);
}


/*导出（异步 + WebSocket 进度条，失败自动回退原同步导出）*/
function exportExcelRecordPro() {
    // 正确获取各输入值（注意 .val()）
    var name = $('input[name="name"]').val() || "";
    var startDate = $('input[name="params[startDate]"]').val() || "";
    var endDate = $('input[name="params[endDate]"]').val() || "";
    var op = $('input[name="op"]').val() || "";

    console.log('导出数据：', name, startDate, endDate, op, currentExportTaskId);
    $.modal.confirm("确认要导出数据吗？", function() {
        // 前端生成任务ID，与后端进度消息匹配（避免与其他任务串扰）
        currentExportTaskId = 'EXP-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8);
        showExportProgress(0, "正在创建导出任务...");

        ensureExportWs().then(function() {
            $.ajax({
                url: "/api/record/RecordExportAsync",
                type: "POST",
                data: { "name": name, "startDate": startDate, "endDate": endDate, "op": op, "taskId": currentExportTaskId },
                dataType: "json",
                success: function(res) {
                    if (res && res.code === 0) {
                        updateExportProgress(0, "导出任务已创建，正在准备数据...");
                        // 后续进度由 WebSocket 推送
                    } else {
                        hideExportProgress();
                        layer.msg((res && res.msg) || '导出任务创建失败', {icon: 5});
                    }
                },
                error: function() {
                    // 异步接口失败：回退原同步导出流程
                    hideExportProgress();
                    //legacyExportExcelRecord(name, startDate, endDate, op);
                }
            });
        }, function() {
            // WebSocket 连接失败：回退原同步导出流程
            hideExportProgress();
            //legacyExportExcelRecord(name, startDate, endDate, op);
        });
    });
}




// ========== 导入成功回调：自动刷新列表（无需手动刷新） ==========
$(document).on('progressImportSuccess', function (event, msg) {
    if (typeof $.table !== 'undefined' && $.table.refresh) {
        $.table.refresh();
    } else {
        // 兼容：无 RuoYi 表格封装时回退为重新加载页面数据
        var $table = $("#bootstrap-table");
        if ($table.length && $.fn.bootstrapTable) {
            $table.bootstrapTable('refresh');
        }
    }
});








/**
 * 导入维护记录 Excel（带 WebSocket 实时进度条）：
 * 走公共封装 startProgressImport（位于 progress-export.js），
 * 上传后由后端 ProgressExcelUtil.importWithProgress 逐批入库并推送进度，
 * 成功/失败结果通过进度弹窗展示（成功后手动点 × 关闭）。
 */
/**
 * 导入维护记录 Excel（带 WebSocket 实时进度条）：三步向导
 * ① 点击"导入"弹出向导框 → ② 下载导入模板并上传已填好的 Excel 文件
 * → ③ 点击"开始导入"执行导入，进度实时展示，完成后手动点 × 关闭。
 * 上传与进度走公共封装 startProgressImport（位于 progress-export.js）。
 */
function importExcelRecordPro() {
    var wizardHtml = '<div style="padding:6px 2px;">'
        + '<div class="form-group" style="margin-bottom:14px;">'
        + '    <label style="font-weight:normal;color:#555;">① 下载导入模板：</label>'
        + '    <a class="btn btn-link" href="/api/record/importTemplate" style="padding:0 6px;font-size:14px;"><i class="fa fa-download"></i> 点击下载导入模板</a>'
        + '    <div class="text-muted" style="font-size:12px;margin-top:4px;">请按模板表头填写数据（示例行可直接删除替换）</div>'
        + '</div>'
        + '<div class="form-group" style="margin-bottom:14px;">'
        + '    <label style="font-weight:normal;color:#555;">② 选择要导入的 Excel 文件：</label>'
        + '    <input type="file" id="importWizardFile" accept=".xlsx,.xls" class="form-control" style="display:inline-block;width:280px;height:34px;"/>'
        + '</div>'
        + '<div class="text-muted" style="font-size:12px;">③ 点击下方"开始导入"按钮执行导入，导入过程将实时显示进度，完成后请手动点击 × 关闭进度窗口。</div>'
        + '</div>';
    layer.open({
        type: 1,
        title: "导入维护记录",
        area: ["540px", "auto"],
        content: wizardHtml,
        btn: ["开始导入", "取消"],
        yes: function (index) {
            var fileInput = document.getElementById("importWizardFile");
            var file = fileInput && fileInput.files && fileInput.files[0];
            console.log(file)
            console.log(fileInput)
            if (!file) {
                layer.msg("请先选择要导入的 Excel 文件", {icon: 5});
                return;
            }
            if (!/\.(xlsx|xls)$/i.test(file.name)) {
                layer.msg("请选择 .xlsx / .xls 格式的 Excel 文件", {icon: 5});
                return;
            }
            layer.close(index);
            startProgressImport({
                url: "/api/record/importAsync",
                file: file
            });
        }
    });
}
