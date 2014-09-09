/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 *
 * @author chaitanya
 */
public class Response {
    HashMap<String, String> headers;
    int responseCode;
    String startLine;
    String manualResponse;
    BufferedOutputStream bos;
    boolean binary;
    public static final String CRLF="\r\n";
    
    
    public Response(OutputStream st){
        bos=new BufferedOutputStream(st);
        
        headers=new HashMap<String, String>();
    }
    /*
     * This method is called from Server to initialize
     * variables to default values.
     */
    public void initialize(){
        responseCode=200;
        startLine="HTTP/1.1 "+responseCode+" OK";
        manualResponse="";
        binary=false;
        
        setDefaultHeaders();
    }
        
    /*
     * Sets header field key, value.
     */
    public void set(String key, String value){
        headers.put(key, value);
    }
    
    private void setDefaultHeaders() {
        set("Server", "Apache");
        set("Content-Type", "text/html; charset=UTF-8");
    }
    
    public void write(String st){
        if(!binary)
            manualResponse+=st;
    }
    /*
     * Used only for text/html response. The write(String) 
     * method only stores the response when it is invoked and 
     * transmits it only when send() is called. This enables
     * content-length to be calculated before transmitting.
     * It has no effect if write(File) is already invoked.
     */
    public void send() throws IOException{
        if(binary)
            return;
        set("Content-Length", ""+manualResponse.length());
        
        writeHeaders();
        writerPrint(manualResponse);
        writerPrint(CRLF);
        writerPrint(CRLF);
        bos.flush();
    }
    /*
     * This method does a write.print(..) and
     * also prints the same output to the console
     * for debugging purposes.
     */
    private void writerPrint(String st) throws IOException{
        bos.write(st.getBytes());
        System.out.print(st);
    }
    
    private void writeHeaders() throws IOException{
        writerPrint(startLine + CRLF);
        for(String s:headers.keySet()){
            writerPrint(s+": "+headers.get(s));
            writerPrint(CRLF);
        }
        writerPrint(CRLF); // empty line to indicate end of headers
        bos.flush();
    }
    /*
     * This method discards any write(String) messages and 
     * transmits the contents of the file.
     * Connection close, if any has to be performed by Server 
     * class.
     */
    public void write(File file) throws IOException{
        int i;
        FileInputStream is;
        try {
            is=new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            System.out.println("FILE NOT FOUND::"+file);
            return;
        }
        
        binary=true;
        set("Content-Type", getMime(file));
        set("Content-Length", ""+file.length());
        
        System.out.println("length::"+file.length());
        
        writeHeaders();
        long count=0;
        long length=file.length();
        
            while((i=is.read())!=-1){
                bos.write(i);
                count++;
                //System.out.println("count::"+count);
            }
            is.close();
            bos.flush();
            
            System.out.println("sending file complete::"+count);
    }
    /*
     * To be fully implemented to support other mime types
     */
    private String getMime(File file) {
        return "binary/octet-stream";
        //return "video/x-flv";
    }
}
