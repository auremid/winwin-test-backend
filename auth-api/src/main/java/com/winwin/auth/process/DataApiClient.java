package com.winwin.auth.process;

import com.winwin.auth.config.AppProperties;
import com.winwin.auth.error.DataApiUnavailableException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DataApiClient {

    private final RestClient restClient;
    private final String internalToken;

    public DataApiClient(RestClient.Builder builder, AppProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.dataApi().connectTimeout());
        requestFactory.setReadTimeout(properties.dataApi().readTimeout());
        this.restClient = builder
                .baseUrl(properties.dataApi().url())
                .requestFactory(requestFactory)
                .build();
        this.internalToken = properties.dataApi().internalToken();
    }

    public String transform(String text) {
        try {
            ProcessDtos.ProcessResponse response = restClient.post()
                    .uri("/api/transform")
                    .header("X-Internal-Token", internalToken)
                    .body(new ProcessDtos.ProcessRequest(text))
                    .retrieve()
                    .body(ProcessDtos.ProcessResponse.class);
            if (response == null || response.result() == null) {
                throw new DataApiUnavailableException();
            }
            return response.result();
        } catch (RestClientException exception) {
            throw new DataApiUnavailableException(exception);
        }
    }
}
