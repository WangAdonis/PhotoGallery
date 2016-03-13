package cn.adonis.photogallery.fragment;


import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import java.util.ArrayList;
import cn.adonis.photogallery.FlickrFetchr;
import cn.adonis.photogallery.GalleryItem;
import cn.adonis.photogallery.R;
import cn.adonis.photogallery.ThumbnailDownloader;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoGalleryFragment extends Fragment {

    private GridView mGridView;
    private ArrayList<GalleryItem> mItems;
    private ThumbnailDownloader<ImageView> mThumbnailDownloader;
    private static final String TAG="PhotoGalleryFragment";

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
//        new FetchItemsTask().execute();
        updateItems();
        setHasOptionsMenu(true);

        mThumbnailDownloader=new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>(){
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if(isVisible()){
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mGridView=(GridView)v.findViewById(R.id.gridView);
        setupAdapter();
        return v;
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>>{
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
//            String query="android";
            Activity activity=getActivity();
            if(activity==null)
                return new ArrayList<GalleryItem>();
            String query= PreferenceManager.getDefaultSharedPreferences(activity).getString(FlickrFetchr.PREF_SEARCH_QUERY,null);
            if(query !=null){
                return new FlickrFetchr().search(query);
            } else{
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {  //入口参数为doInBackground的返回值，
        // 也是AsuncTask的第三个参数，onPostExecute方法会在AsyncTask任务结束时被调用
            mItems=items;
            setupAdapter();
        }
    }

    private void setupAdapter(){
        if(getActivity()==null||mGridView==null)
            return;
        if(mItems!=null){
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        }else {
            mGridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.gallery_item,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.mipmap.loading_photo);
            GalleryItem item=getItem(position);
            mThumbnailDownloader.queueThumbnail(imageView,item.getUrl());
            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            MenuItem searchItem=menu.findItem(R.id.menu_item_search);
//            searchItem.collapseActionView();

//            if(searchItem==null){
//                Log.e(TAG,"searchItem is null");
//            }
            SearchView searchView=(SearchView)searchItem.getActionView();
//            SearchView searchView=(SearchView)MenuItemCompat.getActionView(searchItem);
            if(searchView==null){
                Log.e(TAG,"searchView is null");
            }
//            searchView.setIconifiedByDefault(false);

            SearchManager searchManager=(SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName name=getActivity().getComponentName();
            SearchableInfo searchInfo=searchManager.getSearchableInfo(name);
            if(searchInfo==null)
                Log.e(TAG,"searchInfo is null");
            else
                searchView.setSearchableInfo(searchInfo);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(FlickrFetchr.PREF_SEARCH_QUERY,null).commit();
                updateItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateItems(){
        new FetchItemsTask().execute();
    }


}
