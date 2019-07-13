package com.forever.util;

import java.io.Serializable;

/**
 * 响应包装类
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 6847002570423960352L;

    private String requestId;
    private Throwable error;
    private Object result;

    public boolean isError() {
        return error != null;
    }
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
