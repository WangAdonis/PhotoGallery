package cn.adonis.photogallery.activity;


import android.support.v4.app.Fragment;

import cn.adonis.photogallery.R;
import cn.adonis.photogallery.fragment.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
