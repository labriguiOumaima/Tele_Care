package com.telecare.beneficiaires.servicebeneficiaires.util;
public class JsonCodec {
 private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
 public String writeValueAsString(Object value) { try { return mapper.writeValueAsString(value); } catch(Exception e) { throw new IllegalStateException("JSON serialization", e); } }
 public <T> T readValue(String value, Class<T> type) { try { return mapper.readValue(value,type); } catch(Exception e) { throw new IllegalStateException("JSON deserialization", e); } }
}
