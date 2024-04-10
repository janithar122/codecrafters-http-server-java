import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) {
    
    System.out.println("Logs from your program will appear here!");
    
     ServerSocket serverSocket = null;
     Socket clientSocket = null;
     String CRLF = "\r\n";
    
    try {
      serverSocket = new ServerSocket(4221);
       serverSocket.setReuseAddress(true);
       clientSocket = serverSocket.accept();
       System.out.println("accepted new connection");

       InputStream inputStream = clientSocket.getInputStream();
       InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
       BufferedReader bufferredRead = new BufferedReader(inputStreamReader);
       String[] request = bufferredRead.readLine().split(" ");
       String path = request[1];

       OutputStream outputStream = clientSocket.getOutputStream();
       if (path.equals("/")) {
        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
       }else if(path.contains("/echo/")){
        String msg = path.substring(6);
        String contentType = "Content-Type: text/plain";
        String contentLength = "Content-Length: " +msg.length();

        outputStream.write(("HTTP/1.1 200 OK" + CRLF + contentType + CRLF + contentLength + CRLF + CRLF + msg).getBytes());
       }else if(path.contains("/user-agent")){
        bufferredRead.readLine();
        String msg = bufferredRead.readLine().split("User-Agent: ")[1];
        String contentType = "Content-Type: text/plain";
        String contentLength = "Content-Length: " +msg.length();

        outputStream.write(("HTTP/1.1 200 OK" + CRLF + contentType + CRLF + contentLength + CRLF + CRLF + msg).getBytes());
       }else{
        outputStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     }
  }
}
