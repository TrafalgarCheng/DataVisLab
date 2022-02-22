package com.ssm.demo.service;


import com.ssm.demo.entity.Article;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author 13
 * @date 2018-08-15
 */
public interface ArticleService {

    PageResult getArticlePage(PageUtil pageUtil);

    Article queryObject(Integer id);

    List<Article> queryList(Map<String, Object> map);

    int queryTotal(Map<String, Object> map);

    int save(Article article);

    int update(Article article);

    int delete(Integer id);

    int deleteBatch(Integer[] ids);

    int test(ExecutorService exec);
}
