import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class Main {

  final static String CRLF = "\r\n";

  private static void handler(Socket clientSocket, String directory) throws IOException {

    InputStream inputStream = clientSocket.getInputStream();
    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    BufferedReader bufferredRead = new BufferedReader(inputStreamReader);
    String[] request = bufferredRead.readLine().split(" ");
    String path = request[1];

    OutputStream outputStream = clientSocket.getOutputStream();
    if (path.equals("/")) {
      outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
    } else if (path.startsWith("/echo/")) {
      String msg = path.substring(6);
      String contentType = "Content-Type: text/plain";
      String contentLength = "Content-Length: " + msg.length();

      outputStream
          .write(("HTTP/1.1 200 OK" + CRLF + contentType + CRLF + contentLength + CRLF + CRLF + msg).getBytes());
    } else if (path.contains("/user-agent")) {
      bufferredRead.readLine();
      String msg = bufferredRead.readLine().split("User-Agent: ")[1];
      String contentType = "Content-Type: text/plain";
      String contentLength = "Content-Length: " + msg.length();
      outputStream.write(("HTTP/1.1 200 OK" + CRLF + contentType + CRLF + contentLength + CRLF + CRLF + msg).getBytes());
    }else if(path.startsWith("/files/")){
      String fileName = path.substring(7);
      String contentType = "Content-Type: text/plain";
      File file = new File(Paths.get(directory, fileName).toString());
      if (!file.exists() || file.isDirectory()) {
        outputStream.write(("HTTP/1.1 404 Not Found" + CRLF + contentType + CRLF + "Content-Length: 0" + CRLF + CRLF).getBytes());
      } else {
        outputStream.write(("HTTP/1.1 200 OK" + CRLF + "Content-Type: application/octet-stream" + CRLF + "Content-Length: " + file.length() + CRLF + CRLF).getBytes());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[2048];
        int len = bufferedInputStream.read(buffer, 0, 2048);
        while (len != -1) {
          outputStream.write(buffer, 0, len);
          len = bufferedInputStream.read(buffer, 0, 2048);
        }
      }
    } else {
      outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
    }
    outputStream.flush();
  }

  public static void main(String[] args) {

    System.out.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    // Socket clientSocket = null;

    try {
      serverSocket = new ServerSocket(4221);
      serverSocket.setReuseAddress(true);

      while (true) {
        Socket clientSocket = serverSocket.accept();
        String directory = (args.length > 1 && args[0].equals("--directory")) ? args[1] : "./";
        System.out.println("accepted new connection");
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              handler(clientSocket, directory);
            } catch (Exception e) {
              System.out.println("IOException: " + e.getMessage());
            }
          }
        }).start();
      }

    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    }
  }
}
