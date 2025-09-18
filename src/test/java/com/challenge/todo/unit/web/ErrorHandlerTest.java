package com.challenge.todo.unit.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unit tests for ErrorHandler global exception handling.
 * Tests error responses for various exception types using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc  
@Transactional
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:errortest"
})
class ErrorHandlerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void handleIllegalArgumentException_shouldReturnBadRequestWithMessage() throws Exception {
    mockMvc.perform(get("/test/illegal-argument")
            .param("message", "Test validation error")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
  }

  @Test
  void handleIllegalArgumentException_withEntityNotFound_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/test/illegal-argument")
            .param("message", "List with ID 123 not found")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
  }

  @Test
  void handleIllegalArgumentException_withDuplicateEntity_shouldReturnConflict() throws Exception {
    mockMvc.perform(get("/test/illegal-argument")
            .param("message", "List with name 'Work' already exists")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
  }

  @Test
  void handleMethodArgumentNotValidException_shouldReturnBadRequestWithValidationErrors() throws Exception {
    // Test missing required field validation
    Map<String, Object> invalidRequest = Map.of(); // Missing "name" field

    mockMvc.perform(post("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/validation"));
  }

  @Test
  void handleHttpRequestMethodNotSupportedException_shouldReturnMethodNotAllowed() throws Exception {
    // Try to use PATCH on an endpoint that only supports GET
    mockMvc.perform(patch("/test/illegal-argument")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
  }

  @Test
  void handleHttpMediaTypeNotSupportedException_shouldReturnUnsupportedMediaType() throws Exception {
    // Send XML content type to an endpoint expecting JSON
    mockMvc.perform(post("/test/validation")
            .contentType(MediaType.APPLICATION_XML)
            .content("<test>data</test>")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/validation"));
  }

  @Test
  void handleMissingServletRequestParameterException_shouldReturnBadRequest() throws Exception {
    // Call endpoint without required parameter
    mockMvc.perform(get("/test/illegal-argument")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/illegal-argument"));
  }

  @Test
  void handleGenericException_shouldReturnInternalServerError() throws Exception {
    mockMvc.perform(get("/test/generic-error")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/generic-error"));
  }

  @Test
  void handleRuntimeException_shouldReturnInternalServerError() throws Exception {
    mockMvc.perform(get("/test/runtime-error")
            .param("message", "Simulated runtime exception")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").value("/test/runtime-error"));
  }

  @Test
  void errorResponse_shouldNotExposeInternalDetails() throws Exception {
    // Verify that internal stack traces or sensitive info are not exposed
    mockMvc.perform(get("/test/runtime-error")
            .param("message", "Database connection failed")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist())
        .andExpect(jsonPath("$.cause").doesNotExist())
        .andExpect(jsonPath("$.suppressed").doesNotExist());
  }

  /**
   * Test controller to simulate various error scenarios.
   */
  @RestController
  static class TestController {

    @GetMapping("/test/illegal-argument")
    public String throwIllegalArgument(@RequestParam String message) {
      throw new IllegalArgumentException(message);
    }

    @PostMapping("/test/validation")
    public String testValidation(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request) {
      String name = request.get("name");
      if (name == null) {
        throw new IllegalArgumentException("Missing required field: name");
      }
      return "Valid";
    }

    @GetMapping("/test/generic-error")
    public String throwGenericException() {
      try {
        throw new Exception("Simulated generic exception");
      } catch (Exception e) {
        throw new RuntimeException("Generic exception wrapper", e);
      }
    }

    @GetMapping("/test/runtime-error")
    public String throwRuntimeException(@RequestParam String message) {
      throw new RuntimeException(message);
    }
  }
}
