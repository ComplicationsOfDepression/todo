package com.todo.common;

public enum Status {
    OK(200,"请求成功"),
    SYSTEM_ERROR(101,"系统异常"),
    SQL_ERROR(109,"SQL语句异常");
    ;
    //状态码
    private Integer code;
    //错误信息
    private String message;
    //构造器
    Status(Integer code,String message){
        this.code=code;
        this.message=message;
    }
    //只需要添加get方法就行
    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return code+":"+message;
    }
}
