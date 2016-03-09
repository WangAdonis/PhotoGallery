package cn.adonis.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GpsStatus;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/9.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {

    private static final String TAG="ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD=0;

    private Handler mHandler;
    private Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());

    private Handler mResponseHandler;
    private Listener<Token> mListener;

    public interface Listener<Token>{
        void onThumbnailDownloaded(Token token,Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener){
        mListener=listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler=responseHandler;
    }

    public void queueThumbnail(Token token,String url){
        Log.i(TAG, "Got an URL: " + url);
        requestMap.put(token,url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==MESSAGE_DOWNLOAD){
                    @SuppressWarnings("unchecked")
                    Token token = (Token)msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    private void handleRequest(final Token token){
        try{
            final String url=requestMap.get(token);
            if(url==null){
                return;
            }
            byte[] bitmapBytes=new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(requestMap.get(token)!=url){
                        return;
                    }
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token,bitmap);
                }
            });
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
