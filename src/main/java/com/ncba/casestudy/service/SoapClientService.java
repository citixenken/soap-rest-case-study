package com.ncba.casestudy.service;

import com.ncba.casestudy.dto.FullCountryInfoDto;
import com.ncba.casestudy.dto.LanguageDto;
import com.ncba.casestudy.exception.CountryNotFoundException;
import com.ncba.casestudy.exception.SoapServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SoapClientService {

    private static final String SOAP_ENDPOINT =
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso";
    private static final String NAMESPACE =
            "http://www.oorsprong.org/websamples.countryinfo";

    private final RestTemplate restTemplate;

    public SoapClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls the CountryISOCode SOAP operation to retrieve the ISO code for a given country name.
     */
    public String getCountryIsoCode(String countryName) {
        log.info("Calling SOAP CountryISOCode for country: {}", countryName);

        String soapRequest = buildCountryIsoCodeRequest(countryName);
        String response = sendSoapRequest(soapRequest, "CountryISOCode");

        String isoCode = parseIsoCode(response);

        if (isoCode == null || isoCode.isBlank()) {
            throw new CountryNotFoundException(
                    "Country not found in external service: " + countryName);
        }

        log.info("Retrieved ISO code '{}' for country '{}'", isoCode, countryName);
        return isoCode;
    }

    /**
     * Calls the FullCountryInfo SOAP operation to retrieve detailed info for a given ISO code.
     */
    public FullCountryInfoDto getFullCountryInfo(String isoCode) {
        log.info("Calling SOAP FullCountryInfo for ISO code: {}", isoCode);

        String soapRequest = buildFullCountryInfoRequest(isoCode);
        String response = sendSoapRequest(soapRequest, "FullCountryInfo");

        FullCountryInfoDto dto = parseFullCountryInfo(response);
        log.info("Retrieved full country info for ISO code: {}", isoCode);
        return dto;
    }

    // -------------------------------------------------------------------------
    // Request builders
    // -------------------------------------------------------------------------

    private String buildCountryIsoCodeRequest(String countryName) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
               "<soap:Body>" +
               "<CountryISOCode xmlns=\"" + NAMESPACE + "\">" +
               "<sCountryName>" + escapeXml(countryName) + "</sCountryName>" +
               "</CountryISOCode>" +
               "</soap:Body>" +
               "</soap:Envelope>";
    }

    private String buildFullCountryInfoRequest(String isoCode) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
               "<soap:Body>" +
               "<FullCountryInfo xmlns=\"" + NAMESPACE + "\">" +
               "<sCountryISOCode>" + escapeXml(isoCode) + "</sCountryISOCode>" +
               "</FullCountryInfo>" +
               "</soap:Body>" +
               "</soap:Envelope>";
    }

    // -------------------------------------------------------------------------
    // HTTP transport
    // -------------------------------------------------------------------------

    private String sendSoapRequest(String soapBody, String operation) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set("SOAPAction", "\"\"");

        HttpEntity<String> entity = new HttpEntity<>(soapBody, headers);

        try {
            log.debug("Sending SOAP request for operation '{}' to {}", operation, SOAP_ENDPOINT);
            ResponseEntity<String> response = restTemplate.postForEntity(SOAP_ENDPOINT, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new SoapServiceException(
                        "SOAP service returned non-success status " + response.getStatusCode() +
                        " for operation: " + operation);
            }

            log.debug("Received SOAP response for operation '{}': {}", operation, response.getBody());
            return response.getBody();

        } catch (RestClientException ex) {
            log.error("HTTP error calling SOAP operation '{}': {}", operation, ex.getMessage());
            throw new SoapServiceException(
                    "Failed to reach SOAP service for operation '" + operation + "': " + ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // Response parsers
    // -------------------------------------------------------------------------

    private String parseIsoCode(String xmlResponse) {
        try {
            Document doc = parseXml(xmlResponse);
            String value = getFirstElementText(doc, "CountryISOCodeResult");
            return value != null ? value.trim() : null;
        } catch (SoapServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error parsing CountryISOCode response: {}", ex.getMessage());
            throw new SoapServiceException("Failed to parse CountryISOCode SOAP response: " + ex.getMessage(), ex);
        }
    }

    private FullCountryInfoDto parseFullCountryInfo(String xmlResponse) {
        try {
            Document doc = parseXml(xmlResponse);

            NodeList resultList = doc.getElementsByTagNameNS("*", "FullCountryInfoResult");
            if (resultList.getLength() == 0) {
                resultList = doc.getElementsByTagName("FullCountryInfoResult");
            }
            if (resultList.getLength() == 0) {
                throw new SoapServiceException("FullCountryInfoResult element not found in SOAP response");
            }

            Element result = (Element) resultList.item(0);

            FullCountryInfoDto dto = FullCountryInfoDto.builder()
                    .isoCode(getChildText(result, "sISOCode"))
                    .name(getChildText(result, "sName"))
                    .capitalCity(getChildText(result, "sCapitalCity"))
                    .phoneCode(getChildText(result, "sPhoneCode"))
                    .continentCode(getChildText(result, "sContinentCode"))
                    .currencyIsoCode(getChildText(result, "sCurrencyISOCode"))
                    .countryFlag(getChildText(result, "sCountryFlag"))
                    .languages(parseLanguages(result))
                    .build();

            return dto;

        } catch (SoapServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error parsing FullCountryInfo response: {}", ex.getMessage());
            throw new SoapServiceException("Failed to parse FullCountryInfo SOAP response: " + ex.getMessage(), ex);
        }
    }

    private List<LanguageDto> parseLanguages(Element resultElement) {
        List<LanguageDto> languages = new ArrayList<>();

        NodeList langNodes = resultElement.getElementsByTagNameNS("*", "tLanguage");
        if (langNodes.getLength() == 0) {
            langNodes = resultElement.getElementsByTagName("tLanguage");
        }

        for (int i = 0; i < langNodes.getLength(); i++) {
            Element lang = (Element) langNodes.item(i);
            languages.add(new LanguageDto(
                    getChildText(lang, "sISOCode"),
                    getChildText(lang, "sName")
            ));
        }

        return languages;
    }

    // -------------------------------------------------------------------------
    // XML helpers  (XXE-safe)
    // -------------------------------------------------------------------------

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // XXE prevention (OWASP recommendation)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private String getFirstElementText(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName(tagName);
        }
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : null;
    }

    private String getChildText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", tagName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagName(tagName);
        }
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    /**
     * Escapes XML special characters to prevent XML injection from user-supplied input.
     */
    private String escapeXml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
