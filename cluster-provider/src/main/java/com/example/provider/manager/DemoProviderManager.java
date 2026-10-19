package com.example.provider.manager;

import com.example.core.model.Demo;
import com.example.core.vo.DemoVO;
import com.example.provider.dal.dao.DemoProviderMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        return demoVO;
    }
}
