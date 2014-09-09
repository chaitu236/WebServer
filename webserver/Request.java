/*
 * This is a 'static' class and is used like a structure.
 * It is used to parse request header and hold values of request headers.
 * It doesn't have any information about the client (ip addr. and the like)
 */
package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 *
 * @author chaitanya
 */
public class Request {
    String reqString;
    LinkedHashMap<String, String> headers;
    String startLine;
    String[] reqStringLines;
    String resourceString;
    String method;
    BufferedReader reader;
    
    public static final String CONNECTION="Connection";
    public static final String KEEP_ALIVE="Keep-Alive";
    public static final String CONTENT_LENGTH="Content-Length";
    public static final String HOST="Host";
    public static final String PROXY_CONNECTION="Proxy-Connection";
    public static final String CLOSE="Close";
    
    public static final String CRLF="\r\n";
    
    /*
     * This constructor is not much useful because the 
     * request string has to be passed to it.
     * 
     * More useful is the default constructor which is to
     * be used with readAndParse(..)
     */
    public Request(String reqString){
        this.reqString=reqString;
        headers=new LinkedHashMap<String, String>();
        
        parseString();
    }
    
    public Request(){
        headers=new LinkedHashMap<String, String>();
    }
    
    public String getHeaderValue(String name){
        return headers.get(name);
    }
    /*The header names are toLowerCase()'d but the values
     * are left as is. Both are trim()'med before storing.
     */
    private void parseString() {
        reqStringLines=reqString.split(CRLF);
        startLine=reqStringLines[0];
        
        String temp[]=null;
        temp=startLine.split(" ");
        method=temp[0];
        resourceString=temp[1];
        
        temp=null;
        String tempLine=null;
        
        for(int i=1;i<reqStringLines.length;i++){
            tempLine=reqStringLines[i];
            temp=tempLine.split(": ");
            if(temp.length<=1){
                System.out.println("debug symbol");
                System.out.println("tempLine= "+tempLine);
            }
            //System.out.println("putting  "+temp[0].toLowerCase().trim()+"->"+temp[1].trim());
            headers.put(temp[0].trim(), temp[1].trim());
        }
                
    }
    
    public void readAndParse(BufferedReader rd) throws IOException{
        this.reader=rd;        
        readAndParse();
    }
    /*
     * This method is called from within Server class to 
     * initialize variables to default values.
     */
    public void initialize(){
        headers.clear();
        reqString="";
        startLine="";
    }
    
    public void readAndParse() throws IOException{
        String temp;
        
        while(true){
            if((temp=reader.readLine())==null || "".equals(temp))
                continue;
            break;
        }
        while(true){
            this.reqString+=temp+CRLF;
            temp=reader.readLine();
            //System.out.println(temp);
            if("".equals(temp) || temp==null)
                break;
        }
        
        parseString();
    }
    
    /*private String readLine() throws IOException{
        boolean prevCR=false;
        String res=null;
        int i=0;
        while((i=reader.read())!=-1){
            if(prevCR && i=='\n')
                return res.substring(0, res.length()-1);
            if(i=='\r')
                prevCR=true;
            if(res==null)
                res="";
            res+=(char)i;
        }
        System.out.println("something's wrong");
        return (res==null)?null:res.substring(0, res.length()-1);
    }*/
    
    
    
    @Override
    public String toString(){
        String res=startLine+CRLF;
        
        for(String s:headers.keySet())
            res+=s+"  "+headers.get(s)+CRLF;
        
        return res;
    }
    
    public String url(){
        return this.resourceString;
    }
}
