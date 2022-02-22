package com.ssm.demo.service.impl;

import com.ssm.demo.dao.PictureDao;
import com.ssm.demo.entity.Picture;
import com.ssm.demo.service.PictureService;
import com.ssm.demo.utils.PageResult;
import com.ssm.demo.utils.PageUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pictureService")
public class PictureServiceImpl implements PictureService {

    final static Logger logger = Logger.getLogger(ArticleServiceImpl.class);

    @Autowired
    private PictureDao pictureDao;

    @Override
    public PageResult getPicturePage(PageUtil pageUtil) {
        List<Picture> pictures = pictureDao.findPictures(pageUtil);
        int total = pictureDao.getTotalPictures(pageUtil);
        PageResult pageResult = new PageResult(pictures, total, pageUtil.getLimit(), pageUtil.getPage());
        return pageResult;
    }

    @Override
    public Picture queryObject(Integer id) {
        return pictureDao.findPictureById(id);
    }

    @Override
    public int save(Picture picture) {
        return pictureDao.insertPicture(picture);
    }

    @Override
    public int update(Picture picture) {
        return pictureDao.updPicture(picture);
    }

    @Override
    public int delete(Integer id) {
        return pictureDao.delPicture(id);
    }

    @Override
    public int deleteBatch(Integer[] ids) {
        return pictureDao.deleteBatch(ids);
    }

}
