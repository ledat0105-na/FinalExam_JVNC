package com.example.finalexam_jvnc.service.impl;

import com.example.finalexam_jvnc.dto.SystemConfigDTO;
import com.example.finalexam_jvnc.model.SystemConfig;
import com.example.finalexam_jvnc.repository.SystemConfigRepository;
import com.example.finalexam_jvnc.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Override
    public List<SystemConfigDTO> getAllConfigs() {
        return systemConfigRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigDTO getConfigByKey(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        return convertToDTO(config);
    }

    @Override
    public SystemConfigDTO updateConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        config.setConfigValue(value);
        config = systemConfigRepository.save(config);
        return convertToDTO(config);
    }

    @Override
    public SystemConfigDTO createOrUpdateConfig(SystemConfigDTO configDTO) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configDTO.getConfigKey())
                .orElse(null);

        if (config == null) {
            config = SystemConfig.builder()
                    .configKey(configDTO.getConfigKey())
                    .configName(configDTO.getConfigName())
                    .configValue(configDTO.getConfigValue())
                    .description(configDTO.getDescription())
                    .isActive(configDTO.getIsActive() != null ? configDTO.getIsActive() : true)
                    .build();
        } else {
            config.setConfigName(configDTO.getConfigName());
            config.setConfigValue(configDTO.getConfigValue());
            config.setDescription(configDTO.getDescription());
            if (configDTO.getIsActive() != null) {
                config.setIsActive(configDTO.getIsActive());
            }
        }

        config = systemConfigRepository.save(config);
        return convertToDTO(config);
    }

    @Override
    public Double getShippingFee() {
        return getConfigValueAsDouble("SHIPPING_FEE", 0.0);
    }

    @Override
    public Double getTaxRate() {
        return getConfigValueAsDouble("TAX_RATE", 0.0);
    }

    @Override
    public Double getCodFee() {
        return getConfigValueAsDouble("COD_FEE", 0.0);
    }

    @Override
    public Double getGatewayFee() {
        return getConfigValueAsDouble("GATEWAY_FEE", 0.0);
    }

    @Override
    public void deleteConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        systemConfigRepository.delete(config);
    }

    @Override
    public SystemConfigDTO toggleActive(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new RuntimeException("Config not found with key: " + key));
        config.setIsActive(!config.getIsActive());
        config = systemConfigRepository.save(config);
        return convertToDTO(config);
    }

    private Double getConfigValueAsDouble(String key, Double defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        return Double.parseDouble(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private SystemConfigDTO convertToDTO(SystemConfig config) {
        return SystemConfigDTO.builder()
                .configId(config.getConfigId())
                .configKey(config.getConfigKey())
                .configName(config.getConfigName())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .isActive(config.getIsActive())
                .build();
    }
}

