/*
 * NOT TO BE USED, USE NEWER VERSION Main.java
 */
package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 *
 * @author chaitanya
 */
public class WebServer implements Runnable{

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final String ROOT="/home/chaitu/Videos";
    private String request;
    
    public WebServer(Socket socket) throws IOException{
         this.socket=socket;
         reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
         writer=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    
    private void serve() throws IOException{
        String temp=null;
        String url=null;
        request="";
        
        int count=0;
        System.out.println("*********************");
        while(!(temp=reader.readLine()).equals("")){
            if(count++==0){
                url=temp.split(" ")[1];
                System.out.println("url    "+url);
            }
            request+=temp;
            System.out.println(temp);
        }
        System.out.println("*********************");
                
        if("/".equals(url) || "/index.html".equals(url) || "/index.htm".equals(url))
            serveRoot();
        /*else if(request.contains("Streaming"))
            serveFileContent(url);
        else
            serveFileHeader(url);*/
        else
            serveFile(url);
    }
    
    private void notFound(){
        System.out.println("NOT FOUND");
        String header="HTTP/1.1 404 Not Found\r\n";
               header+="Content-Type: "+"text/html\r\n";
               header+="\r\n";
               
               header+="<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head><title>404 Not Found</title>\r\n"+
                       "<!-- machid: sWkFSZzctYUFHdmlaT0s2Qnh1NkFGbXNrUWRUNW9sT1BVdms5WllUX0w3Y0FYZlVUd1MyTVFR -->\r\n"+
                       "<!-- handler: vanity_404handler -->\r\n"+
                       "</head><body><div style=\"margin:auto;width:650px\"><iframe src=\"/error?src=404&amp;ifr=1\" width=650 height=550 frameborder=0 scrolling=no></iframe></div></body></html>";
               writer.write(header);
               writer.close();
    }
    
    private void serveFileHeader(String url) throws IOException{
        url=url.replaceAll("\\+", " ");
        System.out.println("serving file header"+url);
        
        if(!isFilePresent(url)){
            notFound();
            return;
        }
        
        String header="HTTP/1.1 200 OK\r\n";
        String type=getFileType(url);
        
        if(type.contains("text"))
            header+="Content-Type: "+"text/html";
        else if(type.contains("Flash"))
            header+="Content-Type: "+"video/x-flv";
        else if(type.contains("MPEG"))
            header+="Content-Type: "+"video/mpeg";
        else if(type.contains("JPEG"))
            header+="Content-Type: "+"image/jpeg";
        else
            header+="Content-Type: "+"binary/octet-stream";
            
        header+="\r\n";
        header+="\r\n";
        
        writer.write(header);
        writer.flush();
        writer.close();
    }
    
    private void serveFileContent(String url) throws IOException{
        String tempS=url.replaceAll("\\+", " ");
        System.out.println("Serving file content "+tempS);
        
        File file=new File(ROOT+tempS);
        BufferedInputStream is=new BufferedInputStream(new FileInputStream(file));
        
        int temp;
        while((temp=is.read())!=-1){
            writer.write(temp);
            writer.flush();
        }
        //writer.write(-1);
        System.out.println("close");
        writer.close();
        
        serveFile(url);
    }
    
    private void serveFile(String url) throws IOException{
        url=url.replaceAll("\\+", " ");
        System.out.println("serving file "+url);
        
        if(!isFilePresent(url)){
            notFound();
            return;
        }
        
        String header="HTTP/1.1 200 OK\r\n";
        String type=getFileType(url);
        
        if(type.contains("text"))
            header+="Content-Type: "+"text/html";
        else if(type.contains("Flash"))
            header+="Content-Type: "+"video/x-flv";
        else if(type.contains("MPEG"))
            header+="Content-Type: "+"video/mpeg";
        else if(type.contains("JPEG"))
            header+="Content-Type: "+"image/jpeg";
        else
            header+="Content-Type: "+"binary/octet-stream";
            
        header+="\r\n";
        header+="\r\n";
        
        File file=new File(ROOT+url);
        BufferedInputStream is=new BufferedInputStream(new FileInputStream(file));
        
        int temp;
        while((temp=is.read())!=-1){
            writer.write(temp);
            writer.flush();
        }
        //writer.write(-1);
        System.out.println("close");
        writer.close();
    }
    
    private String getUnixName(String name){
        String res="";
        for(int i=0;i<name.length();i++){
            if(name.charAt(i)==' '){
                res+='\\';
                res+=' ';
                continue;
            }
            res+=name.charAt(i);
        }
        return res;
    }
    
    private String getFileType(String url) throws IOException{
        //String spaceFreeURL=url.replaceAll(" ", "\\ ");
        String spaceFreeURL=getUnixName(url);
        System.out.println("url==="+spaceFreeURL);
        Process process=Runtime.getRuntime().exec("file "+ROOT+spaceFreeURL);
        BufferedReader rf=new BufferedReader(new InputStreamReader(process.getInputStream()));
        String st=rf.readLine();
        System.out.println("Content-Type: "+st);
        rf.close();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return st;
    }
    
    private  boolean isFilePresent(String url){
        File file=new File(ROOT+"/"+url);
        return file.exists();
    }
    
    private void serveRoot(){
        System.out.println("serving root");
        
        String header="HTTP/1.0 200 OK\r\n"+
                      "Content-Type: text/html\r\n\r\n";
        
        String resp="<html><body>";
        String[] files=getFiles(ROOT);
        System.out.println("files length "+files.length);
        for(int i=0;i<files.length;i++){
            if((new File(ROOT+"/"+files[i])).isFile()){
                resp+="<br>";
                resp+=link(files[i]);
            }
        }
        resp+="</body></html>";
        
        writer.write(header);
        System.out.println(resp);
        writer.write(resp);
        writer.write("\r\n\r\n");
        writer.flush();
        writer.close();
    }
    
    private String link(String file){
        String spaceFreeName=file.replaceAll(" ", "+");
        return "<a href="+"/"+spaceFreeName+">"+file+"</a>";
    }
    
    private String[] getFiles(String url){
        File file=new File(url);
        return file.list();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        WebServer webServer;
        ServerSocket serverSocket=new ServerSocket(10000);
        Socket socket=null;
        Thread th;
        
        while(true){
            socket=serverSocket.accept();
            webServer=new WebServer(socket);
            th=new Thread(webServer);
            th.start();
        }
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (IOException ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
