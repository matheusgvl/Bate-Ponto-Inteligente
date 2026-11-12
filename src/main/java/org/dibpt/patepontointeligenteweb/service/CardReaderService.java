package org.dibpt.patepontointeligenteweb.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CardReaderService {
    private final RestTemplate restTemplate;
    private final String nfcApiUrl;

    public CardReaderService(@Value("${nfc.api.url:http://127.0.0.1:5001}") String nfcApiUrl) {
        this.nfcApiUrl = nfcApiUrl.replaceAll("/+$", "");
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(40000);
        this.restTemplate = new RestTemplate(factory);
    }

    public Map<String, Object> iniciarLeitura() {
        return solicitar("/card/read");
    }

    public Map<String, Object> pararLeitura() {
        return solicitar("/card/stop");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> solicitar(String endpoint) {
        try {
            Map<String, Object> response = restTemplate.postForObject(nfcApiUrl + endpoint, null, Map.class);
            return response != null ? response : Map.of("success", false, "error", "Leitor NFC sem resposta.");
        } catch (Exception e) {
            return Map.of("success", false, "error",
                    "Nao foi possivel comunicar com o leitor NFC. Inicie o servidor_nfc.py no Windows e verifique a conexao do Arduino.");
        }
    }
}
