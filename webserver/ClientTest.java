/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author chaitanya
 */
public class ClientTest {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    
    public ClientTest() throws IOException{
        socket=new Socket(InetAddress.getByName("www.youtube.com"), 80);
        System.out.println("socket   "+socket.toString());
        writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        request();
        readResponse();
    }
    
    private void readResponse() throws IOException{
        int read;
        
        while((read=reader.read())!=-1){
            System.out.print((char)read);
        }
    }
    
    private void request(){
        String header="";
        header+="GET /piachadfladkfjadfa HTTP/1.1\r\n";
        header+=("Host: www.youtube.com");
        //header+="From: kkk@test.com\r\n";
        //header+=("User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:6.0.2) Gecko/20100101 Firefox/6.0.2\r\n");
        //header+="Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n";
        //header+=("Accept-Language: en-us,en;q=0.5\r\n");
        //header+=("Accept-Encoding: gzip, deflate\r\n");
        //header+=("Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n");
        //header+=("Connection: keep-alive");
        
        writer.write(header);
        writer.write("\r\n\r\n");
        writer.flush();
        System.out.println("sent request");
    }
    
    public static void main(String[] args) throws IOException{
        new ClientTest();
    }
}
