package com.forever.util;

import java.io.Serializable;

/**
 * 请求包装类
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -8958103167908387913L;
    private String requestId;
    private String className;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
