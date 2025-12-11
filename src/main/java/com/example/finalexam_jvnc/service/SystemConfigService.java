package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {
    List<SystemConfigDTO> getAllConfigs();
    SystemConfigDTO getConfigByKey(String key);
    SystemConfigDTO updateConfig(String key, String value);
    SystemConfigDTO createOrUpdateConfig(SystemConfigDTO configDTO);
    void deleteConfig(String key);
    SystemConfigDTO toggleActive(String key);
    Double getShippingFee();
    Double getTaxRate();
    Double getCodFee();
    Double getGatewayFee();
}

