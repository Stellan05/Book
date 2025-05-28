package org.example.baozi.book.response;

import lombok.Data;

/**
 * 统一响应消息类
 * @param <T> 响应数据类型
 */
@Data
public class ResponseMessage<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功响应
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 响应消息
     */
    public static <T> ResponseMessage<T> success(T data) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 成功响应（带消息）
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应消息
     */
    public static <T> ResponseMessage<T> success(String message) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(200);
        response.setMessage(message);
        return response;
    }

    /**
     * 成功响应（带消息）
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应消息
     */
    public static <T> ResponseMessage<T> success(String message,T data) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    /**
     * 错误响应
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应消息
     */
    public static <T> ResponseMessage<T> error(String message) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 未找到响应
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应消息
     */
    public static <T> ResponseMessage<T> notfound(String message) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(404);
        response.setMessage(message);
        return response;
    }

    /**
     * 自定义信息
     * @param message 信息
     * @param code 错误码
     * @return 相应
     * @param <T> 类型
     */
    public static <T> ResponseMessage<T> info(int code,String message) {
        ResponseMessage<T> response = new ResponseMessage<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
