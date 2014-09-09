/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author chaitanya
 */
public class Server extends Thread{
    Request request;
    Socket socket;
    BufferedReader reader;
    //PrintWriter writer;
    Response response;
    boolean error;
    public final String ROOT="/home/chaitanya/Videos";
    private static final String CRLF="\r\n";
    
    public Server(Socket socket){
        try {
            this.socket=socket;
            reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            response=new Response(socket.getOutputStream());
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
        while(true){
            response.initialize();
            request.initialize();
            
         try {
               request.readAndParse(reader);
         } catch (IOException ex) {
             System.err.println("SOME ERROR");
             System.err.println(ex);
             return;
         }
        
         System.out.println(request);
            try {
                serve();
            } catch (IOException ex) {
                System.err.println("SOME ERROR");
                System.err.println(ex);
                return;
            }
        }
    }
    
    private void serve() throws IOException{
        String url=request.url();
        if("/".equals(url)||"/index.html".equals(url)||"/index.htm".equals(url))
            serveRoot();
        else
            serveFile(url);
    }

    private void serveRoot() throws IOException {
        
        response.write("<HTML><BODY>"+CRLF);
        String[] files=getRootDirFiles();
        
        for(int i=0;i<files.length;i++){
            if(new File(ROOT+"/"+files[i]).isFile()){
                response.write(toLink(files[i]));
                response.write("<br>");
            }
        }
        
        response.write("</BODY></HTML>");
        response.send();
    }

    private void serveFile(String url) throws IOException {
        url=url.replace("+", " ");
        response.write(new File(ROOT+url));
    }
    
    private String[] getRootDirFiles(){
        File file=new File(ROOT);
        return file.list();
    }
    /*
     * Takes a filename and makes a link out of it.
     */
    private String toLink(String file){
        String spaceFreeName=file.replaceAll(" ", "+");
        return "<a href="+"/"+spaceFreeName+">"+file+"</a>";
    }
}
