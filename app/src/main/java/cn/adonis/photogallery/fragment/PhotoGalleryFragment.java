package cn.adonis.photogallery.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import java.io.IOException;

import cn.adonis.photogallery.FlickrFetchr;
import cn.adonis.photogallery.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoGalleryFragment extends Fragment {

    private GridView mGridView;
    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView=(GridView)v.findViewById(R.id.gridView);
        return v;
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
//            try{
//                String result = new FlickrFetchr().getUrl("http://www.google.com");
//                if(result==null)
//                    result="";
//                Log.e("google",result);
//            }catch (IOException e){
//                e.printStackTrace();
//                Log.e("fail","failed to fetch");
//            }
            new FlickrFetchr().fetchItems();
            return null;
        }
    }

}
