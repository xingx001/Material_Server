package com.simple.springbootbasic.basic.base;

import java.util.List;

/**
 * @Description 通用service层
 * @Author Simple
 * @Date 2018/9/17 21:15
 * @Version 1.0
 */
public interface BaseService<T> {

    List<T> selectAll();

    T selectByKey(Object key);

    int save(T entity);

    int delete(Object key);

    int batchDelete(List<String> list, String property, Class<T> clazz);

    int updateAll(T entity);

    int updateNotNull(T entity);

    List<T> selectByExample(Object example);
    List<T> pageList(Integer page, Integer size) throws Exception;
}
