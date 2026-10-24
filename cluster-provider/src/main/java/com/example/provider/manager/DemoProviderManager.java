package com.example.provider.manager;

import com.example.core.model.Demo;
import com.example.core.vo.DemoVO;
import com.example.provider.dal.dao.DemoProviderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Component
public class DemoProviderManager {

    @Autowired
    private DemoProviderMapper demoProviderMapper;

    public DemoVO getDemoVOById(Integer id) {
        Demo demo = demoProviderMapper.getById(id);
        DemoVO demoVO = new DemoVO();
        BeanUtils.copyProperties(demo, demoVO);

        setTimeAsNow(demoVO);
        return demoVO;
    }

    public DemoVO getDemoVOByIdAndName(Integer id, String name) {
        Demo demo = demoProviderMapper.getByIdAndName(id, name);
        DemoVO demoVO = new DemoVO();
        BeanUtils.copyProperties(demo, demoVO);

        setTimeAsNow(demoVO);
        return demoVO;
    }

    private void setTimeAsNow(DemoVO demoVO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formatDate = now.format(dateTimeFormatter);
        demoVO.setRandomStr(formatDate);
    }
}
