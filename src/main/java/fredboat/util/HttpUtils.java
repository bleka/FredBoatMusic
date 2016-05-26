package fredboat.util;

import fredboat.MusicFredBoat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

@Deprecated
public class HttpUtils {

    public static final HashMap<String, String> scopePasswords = new HashMap<>();

    public static void init() {
        
    }
    
    public static String httpGet(String urlString) throws MalformedURLException, IOException {
        return httpGet(urlString, new HashMap<>());
    }
    
    public static String httpGet(String urlString, HashMap<String, String> headers) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        for(String k : headers.keySet()){
            conn.setRequestProperty(k, headers.get(k));
        }
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        String res = result.toString();
        System.err.println(res+" "+urlString);
        return res;
    }

}
