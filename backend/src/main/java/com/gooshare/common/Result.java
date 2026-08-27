package com.gooshare.common;

import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    //成功执行
    public static Result success() {
        Result result = new Result();
        result.code = 200;
        result.msg = "success";
        return result;
    }
    public static Result success(Object data) {
        Result result = new Result();
        result.code = 200;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static Result error(){
        Result result = new Result();
        result.code = 0;
        result.msg = "error";
        return result;
    }

    //执行失败
    public static Result error(String msg) {
        Result result = new Result();
        result.code = 0;
        result.msg = msg;
        return result;
    }

}
