package pe.com.mcc.security.infrastructure.adapter.in.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Helper compartido por UnauthorizedEntryPoint y ForbiddenAccessDeniedHandler: serializa un
 * ProblemDetail directamente al HttpServletResponse, replicando el formato del
 * GlobalExceptionHandler (con traceId y type urn:).
 */
@Component
@RequiredArgsConstructor
public class ProblemDetailWriter {

  private final ObjectMapper objectMapper;

  public void write(
      HttpServletResponse response, HttpStatus status, String slug, String title, String detail)
      throws IOException {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setType(URI.create("urn:problem-type:" + slug));
    pd.setTitle(title);
    String traceId = MDC.get(MdcConstants.TRACE_ID);
    if (traceId != null) {
      pd.setProperty("traceId", traceId);
    }

    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setStatus(status.value());
    response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(pd));
  }
}
