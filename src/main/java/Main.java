import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
//import java.nio.file.Paths;

public class Main {

  final static String CRLF = "\r\n";

  private static void handler(Socket clientSocket, String directory) throws IOException {

    InputStream inputStream = clientSocket.getInputStream();
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    BufferedReader request = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    String[] startLine = request.readLine().split(" ");
    String path = startLine[1];
    OutputStream outputStream = clientSocket.getOutputStream();

    if (path.equals("/")) {

      outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());

    } else if (path.startsWith("/echo/")) {
      String msg = path.substring(6);
      outputStream.write(("HTTP/1.1 200 OK" + CRLF +
                          "Content-Type: text/plain" + CRLF +
                          "Content-Length: " + msg.length() + CRLF + CRLF + msg)
                             .getBytes());
    } else if (path.equals("/user-agent")) {
      request.readLine();
      String userAgent = request.readLine().split(" ")[1];
      outputStream.write(
          ("HTTP/1.1 200 OK" + CRLF + "Content-Type: text/plain" + CRLF +
           "Content-Length: " + userAgent.length() + CRLF + CRLF + userAgent)
              .getBytes());
    } else if (path.startsWith("/files/")) {

      String HTTPmethod = startLine[0];
      String fileName = path.substring(7);
      File file = new File(new File(directory), fileName);

      switch (HTTPmethod) {
        case "GET" -> {
          if (!file.exists() || file.isDirectory()) {
            outputStream.write(("HTTP/1.1 404 Not Found" + CRLF +
                "Content-Type: text/plain" + CRLF +
                "Content-Length: 0" + CRLF + CRLF).getBytes());
          } else {
            outputStream.write(("HTTP/1.1 200 OK" + CRLF +
                "Content-Type: application/octet-stream" + CRLF +
                "Content-Length: " + file.length() + CRLF + CRLF).getBytes());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int len = bufferedInputStream.read(buffer, 0, 1024);
            while (len != -1) {
              outputStream.write(buffer, 0, len);
              len = bufferedInputStream.read(buffer, 0, 1024);
            }
          }
        }
        case "POST" -> {
          file.createNewFile();
          BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
          while (!request.readLine().isEmpty()) {
          }
          StringBuilder body = new StringBuilder();
          while (request.ready())
            body.append((char) request.read());
          bufferedWriter.write(body.toString());
          bufferedWriter.close();
          outputStream.write(("HTTP/1.1 201 Created" + CRLF + CRLF).getBytes());
        }
        default -> System.out.println("Unknown HTTP method");
      }

    } else {
      outputStream.write(("HTTP/1.1 404 Not Found" + CRLF + CRLF).getBytes());
    }
    outputStream.flush();
  }

  public static void main(String[] args) {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    // System.out.println("Logs from your program will appear here!");

    try (ServerSocket serverSocket = new ServerSocket(4221)) {
      serverSocket.setReuseAddress(true);
      while (true) {
        Socket clientSocket = serverSocket.accept();
        String directory = (args.length > 1 && args[0].equals("--directory")) ? args[1] : "./";
        new Thread(() -> {
          try {
            handler(clientSocket, directory);
          } catch (IOException e) {
            System.out.println("IOException(Thread): " +
                e.getMessage());
          }
        }).start();
      }
    } catch (IOException e) {
      System.out.println("IOException(Server socket): " + e.getMessage());
    }
  }
}