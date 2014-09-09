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
public class Main {
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket=new ServerSocket(10000);
        Socket socket;
        
        while(true){
            socket=serverSocket.accept();
            Thread server=new Server(socket);
            server.start();
        }
    }
}
