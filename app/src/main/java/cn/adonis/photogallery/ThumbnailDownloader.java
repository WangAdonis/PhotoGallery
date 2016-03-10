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

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() { //一个Thread只能有一个Looper对象

        mHandler = new Handler(){  //handler创建时会关联一个looper，默认的构造方法将关联当前线程的looper
            /*
            消息的处理是通过核心方法dispatchMessage(Message msg)与钩子方法handleMessage(Message msg)完成的。
            dispatchMessage(Message msg)方法由looper调用：如果message设置了callback，即runnable消息，
            处理callback，直接运行runnable.run()方法；如果handler本身设置了callback，则执行callback；
            否则运行handleMessage(Message msg)方法。
             */
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

    public void queueThumbnail(Token token,String url){
        Log.i(TAG, "Got an URL: " + url);
        requestMap.put(token,url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget();  //mHandler发送消息至Looper的MessageQueue队列
        //当该消息出队时，Looper将调用发送该消息的Handler的dispatchMessage(Message msg)方法
    }


    private void handleRequest(final Token token){
        try{
            final String url=requestMap.get(token);
            if(url==null){
                return;
            }
            byte[] bitmapBytes=new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            mResponseHandler.post(new Runnable() {  //handler发送Message到关联的Looper中的MessageQueue中，这个Message只能由发送它的handler自己处理
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
