/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chaitanya
 */
public class ProxyServer extends Thread{
    Request request;
    Socket clientSocket;
    BufferedReader clientReader;
    
    //BufferedInputStream clientInputStream;
    BufferedInputStream hostInputStream;
    BufferedOutputStream clientOutputStream;
    BufferedOutputStream hostOutputStream;
    Socket hostSocket;
    //Response response;
    boolean error;
    public final String ROOT="/home/chaitanya/Videos";
    private static final String CRLF="\r\n";
    
    boolean exit=false;
    
    String host;
    String resource; //eg. if url=www.google.com/test, resource=/test i.e., url-http://host
    
    public ProxyServer(Socket socket){
        try {
            this.clientSocket=socket;
            //clientInputStream=new BufferedInputStream(socket.getInputStream());
            clientReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientOutputStream=new BufferedOutputStream(socket.getOutputStream());
            //response=new Response(socket.getOutputStream());
            request=new Request();
            error=false;
            
        } catch (IOException ex) {
            System.err.println("UNABLE TO OPEN STREAMS");
            System.err.println(ex);
            error=true;
        }
    }
    
    @Override
    public void run(){
        if(error)
            return;
        while(!exit){
            //response.initialize();
            request.initialize();
            
         try {
               request.readAndParse(clientReader);
         } catch (IOException ex) {
             System.err.println("SOME ERROR");
             System.err.println(ex);
                try {
                    hostSocket.close();
                    clientSocket.close();
                    exit=true;
                } catch (IOException ex1) {
                    Logger.getLogger(ProxyServer.class.getName()).log(Level.SEVERE, null, ex1);
                }
                System.out.println("exiting");
             return;
         }
        
         //System.out.println(request);
            try {
                serve();
            } catch (IOException ex) {
                System.err.println("SOME ERROR");
                System.err.println(ex);
                try {
                    hostSocket.close();
                    clientSocket.close();
                } catch (IOException ex1) {
                    Logger.getLogger(ProxyServer.class.getName()).log(Level.SEVERE, null, ex1);
                }
                exit=true;
                System.out.println("exiting");
                return;
            }
        }
        
        System.out.println("exiting");
    }
    
    private void serve() throws IOException{
        host=request.getHeaderValue(Request.HOST);
        String url=request.url();
        //System.out.println("url= "+url+" host= "+host);
        resource=url.split(host)[1];
        
        bridge();
    }

    private void bridge() {
        try {
            String addrAndPort[]=host.split(":");
            
            host=addrAndPort[0];
            int port=(addrAndPort.length==1)?80:Integer.getInteger(addrAndPort[1]);
            
            //System.out.println("host: "+host+" port "+port+" resource "+resource);
            
            hostSocket=new Socket(InetAddress.getByName(host), port);
            hostOutputStream=new BufferedOutputStream(hostSocket.getOutputStream());
            hostInputStream=new BufferedInputStream(hostSocket.getInputStream());
            
            Thread th=new ResponseThread(hostInputStream, clientOutputStream, hostSocket, clientSocket, Thread.currentThread());
            th.start();
            
            bridgeClientOutputToServerInput();
        } catch (IOException ex) {
            System.out.println(ex);
            try {
                clientSocket.close();
                hostSocket.close();
            } catch (IOException ex1) {
               System.out.println(ex1);
            }
        }
    }

    private void bridgeClientOutputToServerInput() throws IOException {
        Out(request.startLine.replaceFirst(request.resourceString, resource));
        Out(CRLF);
        
        String tempHead;
        
        for(String s:request.headers.keySet()){
            tempHead=s;
            
            if(s.equals(Request.PROXY_CONNECTION)){
                tempHead=Request.CONNECTION;
            }
            
            Out((tempHead+": "+request.getHeaderValue(s)+CRLF));
        }
        
        Out(CRLF);
        
        int i;
        int count=-1;
        
        String contentLengthString=request.getHeaderValue(Request.CONTENT_LENGTH);
        
        if(contentLengthString!=null){
            
            count=Integer.parseInt(contentLengthString);
            //System.out.println("count     "+count);
            while((count--)>0){
                i=clientReader.read(); 
                //System.out.println("count="+count);
                Out(i);
            }
            System.out.println("done");
        }
    }
    
    private void Out(String s) throws IOException{
        //System.out.print(s);
        hostOutputStream.write(s.getBytes());
        hostOutputStream.flush();
    }
    
    private void Out(int i) throws IOException{
        //System.out.print((char)i);
        hostOutputStream.write(i);
        hostOutputStream.flush();
    }
}

class ResponseThread extends Thread{
    private final BufferedInputStream hostInputStream;
    private final BufferedOutputStream clientOutputStream;
    private final Socket hostSocket;
    private final Socket clientSocket;
    private final Thread parentThread;
    
    ResponseThread(BufferedInputStream hostReader, BufferedOutputStream clientOutputStream, Socket hostSocket, Socket clientSocket, Thread th){
        this.hostInputStream=hostReader;
        this.clientOutputStream=clientOutputStream; 
        this.parentThread=th;
        this.hostSocket=hostSocket;
        this.clientSocket=clientSocket;
    }
    
    @Override
    public void run(){
        try {
            int i;
            while((i=hostInputStream.read())!=-1){
                //System.out.print((char)i);
                clientOutputStream.write(i);
                clientOutputStream.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResponseThread.class.getName()).log(Level.SEVERE, null, ex);try {
                hostSocket.close();
                clientSocket.close();
                parentThread.interrupt();
                //System.exit(0);
            } catch (IOException ex1) {
                System.out.println(ex1);
            }
        }
    }
}
