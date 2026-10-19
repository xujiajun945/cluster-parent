package com.example.provider.service.impl;

import com.example.core.vo.DemoVO;
import com.example.provider.manager.DemoProviderManager;
import com.example.provider.service.DemoProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formatDate = now.format(dateTimeFormatter);
        demoVO.setRandomStr(formatDate);
        return demoVO;
    }
}
