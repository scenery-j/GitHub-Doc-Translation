package com.gitdoc.translation.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAM_ERROR(40001, "参数错误"),
    INSTALLATION_NOT_FOUND(40010, "GitHub App 未安装或授权已失效，请重新安装"),
    UNAUTHORIZED(40101, "未登录或Token已过期"),
    FORBIDDEN(40301, "无权操作"),
    NOT_FOUND(40401, "资源不存在"),
    QUOTA_INSUFFICIENT(40901, "翻译额度不足，请配置自己的API Key或充值"),
    TASK_QUEUE_FULL(42901, "任务队列已满，请稍后再试"),
    GITHUB_API_ERROR(50201, "GitHub API 调用失败"),
    AI_API_ERROR(50202, "AI翻译API调用失败"),
    INTERNAL_ERROR(50001, "服务内部错误");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
