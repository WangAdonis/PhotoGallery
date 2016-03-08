package cn.adonis.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/3/8.
 */
public class FlickrFetchr {
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode()!=HttpURLConnection.HTTP_OK)
                return null;
            int byteRead=0;
            byte[] buffer=new byte[1024];
            while ((byteRead=in.read(buffer))>0){
                out.write(buffer,0,byteRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
}
