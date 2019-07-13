package com.forever.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化，反序列化的工具
 */
public class SerializationUitl {

    /**
     * 缓存Schema
     */
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap();

    private static Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj){
        final Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            byte[] bytes = ProtobufIOUtil.toByteArray(obj, schema, buffer);
            return bytes;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     * @param data 字节数组
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz){
        /*
         * 如果一个类没有参数为空的构造方法时候，那么你直接调用newInstance方法试图得到一个实例对象的时候是会抛出异常的
         * 通过ObjenesisStd可以完美的避开这个问题
         * */
        T message = (T) objenesis.newInstance(clazz);//实例化
        Schema<T> schema = getSchema(clazz);
        ProtobufIOUtil.mergeFrom(data,message,schema);
        return message;
    }

    /**
     * 获取类的schema
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (schema == null) {
            //这个schema通过RuntimeSchema进行懒创建并缓存
            //所以可以一直调用RuntimeSchema.getSchema(),这个方法是线程安全的
            schema = RuntimeSchema.getSchema(clazz);
            if (schema != null) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }

}
