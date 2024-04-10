import java.util.HashMap;
import java.util.Map;
final class Request {
  final String method;
  final String path;
  final String version;
  final Map<String, String> headers;
  final String body;
  public Request(Builder builder) {
    this.method = builder.method;
    this.path = builder.path;
    this.version = builder.version;
    this.headers = builder.headers;
    this.body = builder.body;
  }
  public static class Builder {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private String body;
    public Builder() { headers = new HashMap<>(); }
    public static Builder newInstance() { return new Builder(); }
    public Builder setStartLine(String[] startLine) {
      this.method = startLine[0];
      this.path = startLine[1];
      this.version = startLine[2];
      return this;
    }
    public Builder addHeader(String[] header) {
      this.headers.put(header[0], header[1]);
      return this;
    }
    public Builder setBody(String body) {
      this.body = body;
      return this;
    }
    public Request build() { return new Request(this); }
  }

}