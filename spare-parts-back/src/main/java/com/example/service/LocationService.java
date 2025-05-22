package com.example.service;




import com.example.config.AmapProperties;
import com.example.dto.GeoResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {

    private final RestTemplate restTemplate;
    private final AmapProperties amapProperties;

    @Autowired
    public LocationService(RestTemplate restTemplate, AmapProperties amapProperties) {
        this.restTemplate = restTemplate;
        this.amapProperties = amapProperties;
    }

    public GeoResponse getLocation(String longitude, String latitude) {
        String url = String.format("%s?key=%s&location=%s,%s&extensions=all",
                amapProperties.getGeocodeUrl(),
                amapProperties.getApiKey(),
                longitude,
                latitude);

        try {
            // 获取原始JSON字符串
            String rawResponse = restTemplate.getForObject(url, String.class);
            System.out.println("原始API响应: " + rawResponse);

            // 使用ObjectMapper手动反序列化
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(rawResponse, GeoResponse.class);
        } catch (HttpClientErrorException | JsonProcessingException e) {
            System.err.println("请求失败: " + e.getMessage());
            return new GeoResponse();
        }
    }

}
