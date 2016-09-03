package fredboat.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class CacheUtil {

    private static HashMap<String, File> cachedURLFiles = new HashMap<>();

    public static File getImageFromURL(String url) {
        if (cachedURLFiles.containsKey(url) && cachedURLFiles.get(url).exists()) {
            //Already cached
            return cachedURLFiles.get(url);
        } else {
            InputStream is;
            FileOutputStream fos;
            File tmpFile = null;
            try {
                Matcher matcher = Pattern.compile("(\\.\\w+$)").matcher(url);
                String type = matcher.find() ? matcher.group(1) : "";
                tmpFile = File.createTempFile(UUID.randomUUID().toString(), type);
                is = Unirest.get(url).asBinary().getRawBody();
                FileWriter writer = new FileWriter(tmpFile);
                fos = new FileOutputStream(tmpFile);

                byte[] buffer = new byte[1024*10];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
                is.close();
                fos.close();

                //Use regex to find the file extension
                /*Matcher matcher = Pattern.compile("\\.(\\w+$)").matcher(url);
                String type = matcher.find() ? matcher.group(1) : "png";

                tmpFile = File.createTempFile(UUID.randomUUID().toString(), "." + type);
                is = Unirest.get(url).asBinary().getRawBody();

                RenderedImage img = ImageIO.read(is);
                ImageIO.write(img, type, tmpFile);
                
                tmpFile.deleteOnExit();*/
                cachedURLFiles.put(url, tmpFile);
                return tmpFile;
            } catch (IOException ex) {
                tmpFile.delete();
                throw new RuntimeException(ex);
            } catch (UnirestException ex) {
                tmpFile.delete();
                throw new RuntimeException(ex);
            }
        }
    }

}
