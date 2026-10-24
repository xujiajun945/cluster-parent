package com.example.provider.service.impl;

import com.example.core.vo.DemoVO;
import com.example.provider.manager.DemoProviderManager;
import com.example.provider.service.DemoProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xujiajun
 * @since 2026/10/16
 */
@Service
public class DemoProviderServiceImpl implements DemoProviderService {

    @Autowired
    private DemoProviderManager demoProviderManager;

    @Override
    public DemoVO getById(Integer id) {
        DemoVO demoVO = demoProviderManager.getDemoVOById(id);
        return demoVO;
    }

    @Override
    public DemoVO getByIdAndName(Integer id, String name) {
        DemoVO demoVO = demoProviderManager.getDemoVOByIdAndName(id, name);
        return demoVO;
    }
}
