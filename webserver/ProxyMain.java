/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author chaitanya
 */
public class ProxyMain {
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket=new ServerSocket(8000);
        Socket socket;
        
        while(true){
            socket=serverSocket.accept();
            System.out.println("NEW CONNECTION "+Thread.activeCount());
            Thread server=new ProxyServer(socket);
            server.start();
        }
    }
}
