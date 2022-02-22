//解决编辑器弹出层文本框不能输入的问题
$('#articleModal').off('shown.bs.modal').on('shown.bs.modal', function (e) {
    $(document).off('focusin.modal');
});
//KindEditor变量
var editor;
$(function () {
    //隐藏错误提示框
    $('.alert-danger').css("display", "none");

    initFlatPickr();

    //详情编辑器
    editor = KindEditor.create('textarea[id="editor"]', {
        items: ['source', '|', 'undo', 'redo', '|', 'preview', 'print', 'template', 'code', 'cut', 'copy', 'paste',
            'plainpaste', 'wordpaste', '|', 'justifyleft', 'justifycenter', 'justifyright',
            'justifyfull', 'insertorderedlist', 'insertunorderedlist', 'indent', 'outdent', 'subscript',
            'superscript', 'clearhtml', 'quickformat', 'selectall', '|', 'fullscreen', '/',
            'formatblock', 'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold',
            'italic', 'underline', 'strikethrough', 'lineheight', 'removeformat', '|', 'multiimage',
            'table', 'hr', 'emoticons', 'baidumap', 'pagebreak',
            'anchor', 'link', 'unlink'],
        uploadJson: 'upload/file',
        filePostName: 'file'
    });

    $('#articleModal').on('hidden.bs.modal', function () {
        editor.html('请输入...');
    })

    $('#articleModal').modal('hide');

    $("#jqGrid").jqGrid({
        url: 'articles/list',
        datatype: "json",
        colModel: [
            {label: 'id', name: 'id', index: 'id', width: 50, key: true, hidden: true},
            {label: '文章标题', name: 'articleTitle', index: 'articleTitle', width: 240},
            {label: '添加人', name: 'addName', index: 'addName', width: 120},
            {label: '发布时间', name: 'createTime', index: 'createTime', width: 120},
            {label: '最后更新时间', name: 'updateTime', index: 'updateTime', width: 120}
        ],
        height: 560,
        rowNum: 10,
        rowList: [10, 20, 50],
        styleUI: 'Bootstrap',
        loadtext: '信息读取中...',
        rownumbers: false,
        rownumWidth: 20,
        autowidth: true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader: {
            root: "data.list",
            page: "data.currPage",
            total: "data.totalPage",
            records: "data.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order",
        },
        gridComplete: function () {
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });

});

/**
 * 初始化时间选择框
 */
function initFlatPickr() {
    $('.startTime').flatpickr();
    $('.endTime').flatpickr();
    //创建一个当前日期对象
    var now = new Date();
    //格式化日，如果小于9，前面补0
    var day = ("0" + now.getDate()).slice(-2);
    //格式化月，如果小于9，前面补0
    var month = ("0" + (now.getMonth() + 1)).slice(-2);
    //小时
    var hour = ("0" + now.getHours()).slice(-2);
    //分钟
    var minute = ("0" + now.getMinutes()).slice(-2);
    //秒
    var seconds = ("0" + now.getSeconds()).slice(-2);
    //拼装完整日期格式
    var todayTime = now.getFullYear() + "-" + (month) + "-" + (day) + " 00:00:00";
    var nowTime = now.getFullYear() + "-" + (month) + "-" + (day) + " " + hour + ":" + minute + ":" + seconds;
    $('.startTime').val(todayTime);
    $('.endTime').val(nowTime);
}

//绑定modal上的保存按钮
$('#saveButton').click(function () {
    //验证数据
    if (validObject()) {
        //一切正常后发送网络请求
        //ajax
        var id = $("#articleId").val();
        var title = $("#articleName").val();
        var addName = $("#articleAuthor").val();
        var content = editor.html();
        var data = {"articleTitle": title, "articleContent": content, "addName": addName};
        var url = 'articles/save';
        //id>0表示编辑操作
        if (id > 0) {
            data = {"id": id, "articleTitle": title, "articleContent": content, "addName": addName};
            url = 'articles/update';
        }
        $.ajax({
            type: 'POST',//方法类型
            dataType: "json",//预期服务器返回的数据类型
            url: url,//url
            contentType: "application/json; charset=utf-8",
            beforeSend: function (request) {
                //设置header值
                request.setRequestHeader("token", getCookie("token"));
            },
            data: JSON.stringify(data),
            success: function (result) {
                checkResultCode(result.resultCode);
                if (result.resultCode == 200) {
                    $('#articleModal').modal('hide');
                    swal("保存成功", {
                        icon: "success",
                    });
                    reload();
                }
                else {
                    $('#articleModal').modal('hide');
                    swal("保存失败", {
                        icon: "error",
                    });
                }
                ;
            },
            error: function () {
                swal("操作失败", {
                    icon: "error",
                });
            }
        });

    }
});

function articleAdd() {
    reset();
    $('.modal-title').html('添加');
    $('#articleModal').modal('show');
}

function articleEdit() {
    reset();
    $('.modal-title').html('编辑');

    var id = getSelectedRow();
    if (id == null) {
        return;
    }
    //请求数据
    $.get("articles/info/" + id, function (r) {
        if (r.resultCode == 200 && r.data != null) {
            //填充数据至modal
            $('#articleId').val(r.data.id);
            $('#articleName').val(r.data.articleTitle);
            $('#articleAuthor').val(r.data.addName);
            editor.html('');
            editor.html(r.data.articleContent);
        }
    });
    //显示modal
    $('#articleModal').modal('show');
}

/**
 * 搜索功能
 */
function search() {
    //标题关键字
    var keyword = $('#keyword').val();
    if (!validLength(keyword, 20)) {
        swal("搜索字段长度过大!", {
            icon: "error",
        });
        return false;
    }

    //开始时间、结束时间
    var startTimeStr = $('.startTime').val();
    var endTimeStr = $('.endTime').val();
    var startTime = new Date(startTimeStr.replace(/-/, "/"));
    var endTime = new Date(endTimeStr.replace(/-/, "/"));

    if (startTime >= endTime) {
        swal("开始时间不能大于结束时间!", {
            icon: "error"
        });
        return false;
    }

    //数据封装
    var searchData = {keyword: keyword, startTime: startTimeStr, endTime: endTimeStr};

    //传入查询条件参数
    $("#jqGrid").jqGrid("setGridParam", {postData: searchData});
    //点击搜索按钮默认都从第一页开始
    $("#jqGrid").jqGrid("setGridParam", {page: 1});
    //提交post并刷新表格
    $("#jqGrid").jqGrid("setGridParam", {url: 'articles/search'}).trigger("reloadGrid");


}

/**
 * 数据验证
 */
function validObject() {
    var articleName = $('#articleName').val();
    if (isNull(articleName)) {
        showErrorInfo("标题不能为空!");
        return false;
    }
    if (!validLength(articleName, 120)) {
        showErrorInfo("标题字符不能大于120!");
        return false;
    }
    var articleAuthor = $('#articleAuthor').val();
    if (isNull(articleAuthor)) {
        showErrorInfo("作者不能为空!");
        return false;
    }
    if (!validLength(articleAuthor, 10)) {
        showErrorInfo("作者字符不能大于10!");
        return false;
    }
    var ariticleContent = editor.html();
    if (isNull(ariticleContent) || ariticleContent == '请输入...') {
        showErrorInfo("内容不能为空!");
        return false;
    }
    if (!validLength(ariticleContent, 8000)) {
        showErrorInfo("内容字符不能大于8000!");
        return false;
    }
    return true;
}

/**
 * 重置
 */
function reset() {
    //隐藏错误提示框
    $('.alert-danger').css("display", "none");
    //清空数据
    $('#articleId').val(0);
    $('#keyword').val('');
    $('#articleName').val('');
    $('#articleAuthor').val('');
    $('#ariticleContent').val('');
}

function deleteArticle() {
    var ids = getSelectedRows();
    if (ids == null) {
        return;
    }
    swal({
        title: "确认弹框",
        text: "确认要删除数据吗?",
        icon: "warning",
        buttons: true,
        dangerMode: true,
    }).then((flag) => {
        if (flag) {
            $.ajax({
                type: "POST",
                url: "articles/delete",
                contentType: "application/json",
                beforeSend: function (request) {
                    //设置header值
                    request.setRequestHeader("token", getCookie("token"));
                },
                data: JSON.stringify(ids),
                success: function (r) {
                    checkResultCode(r.resultCode);
                    if (r.resultCode == 200) {
                        swal("删除成功", {
                            icon: "success",
                        });
                        $("#jqGrid").trigger("reloadGrid");
                    } else {
                        swal(r.message, {
                            icon: "error",
                        });
                    }
                }
            });
        }
    });
}

/**
 * jqGrid重新加载
 */
function reload() {
    reset();
    initFlatPickr();
    var page = $("#jqGrid").jqGrid('getGridParam', 'page');
    $("#jqGrid").jqGrid('setGridParam', {
        page: page
    }).trigger("reloadGrid");
}