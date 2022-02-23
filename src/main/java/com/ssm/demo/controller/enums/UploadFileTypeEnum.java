package com.ssm.demo.controller.enums;

public enum UploadFileTypeEnum {

    ERROR_TYPE("error_type", "格式错误"),
    JPG("jpg", "jpg格式图片"),
    JPEG("jpeg", "jpeg格式图片"),
    PNG("png", "png格式图片"),
    GIF("gif", "gif格式图片"),
    VIDEO_MP4("mp4", "mp4格式视频"),
    VIDEO_AVI("avi", "avi格式视频"),
    VIDEO_RMVB("rmvb", "rmvb格式视频"),
    ZIP("zip", "zip格式压缩文件"),
    rar("rar", "rar格式压缩文件"),
    EXCEL_XLSX("xlsx", "xlsx格式excel文件");

    private String type;

    private String description;

    UploadFileTypeEnum(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static UploadFileTypeEnum getFileEnumByType(String type) {
        for (UploadFileTypeEnum fileTypeEnum : values()) {
            if (type.equals(fileTypeEnum.getType())) {
                return fileTypeEnum;
            }
        }
        return ERROR_TYPE;
    }
}
