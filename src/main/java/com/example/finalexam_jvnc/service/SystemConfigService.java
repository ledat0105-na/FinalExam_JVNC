package com.example.finalexam_jvnc.service;

import com.example.finalexam_jvnc.dto.SystemConfigDTO;

import java.util.List;

public interface SystemConfigService {
    List<SystemConfigDTO> getAllConfigs();
    SystemConfigDTO getConfigByKey(String key);
    SystemConfigDTO updateConfig(String key, String value);
    SystemConfigDTO createOrUpdateConfig(SystemConfigDTO configDTO);
    Double getShippingFee();
    Double getTaxRate();
    Double getCodFee();
    Double getGatewayFee();
}

