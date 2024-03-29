package com.example.zhanyu.commonlib.image.preview;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.example.zhanyu.commonlib.R;
import com.example.zhanyu.commonlib.base.activity.BaseActivity;
import com.example.zhanyu.commonlib.base.adapter.ViewPagerFragmentAdapter;
import com.example.zhanyu.commonlib.image.commonutils.Utils;
import com.example.zhanyu.commonlib.image.params.CommonParams;
import com.example.zhanyu.commonlib.image.preview.impl.RightNavClickImpl;
import com.facebook.imagepipeline.image.ImageInfo;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.relex.circleindicator.CircleIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ImagePreviewActivity extends BaseActivity {

    MultiTouchViewPager imagepreview_viewpager;
    ViewPagerFragmentAdapter adapter;
    CircleIndicator imagepreview_indicator;
    RelativeLayout layout_imagepreview_edit;
    TextView imagepreview_edit;
    TextView tv_nav_title;
    TextView tv_nav_right;
    ImageButton ib_nav_left;

    // 图片路径
    ArrayList<String> urls;
    ArrayList<Fragment> fragments;
    //是否可以编辑
    boolean canEdit;

    // 图片当前位置
    int currentPosition = 0;

    // 记录图片原始尺寸，比便于复原
    HashMap<String, ImageInfo> point;

    @Override
    public void initParams() {

    }

    @Override
    public int initViews() {
        return R.layout.activity_imagepreview;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return Color.BLACK;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        point = new HashMap<>();

        imagepreview_viewpager = findViewById(R.id.imagepreview_viewpager);
        imagepreview_indicator = findViewById(R.id.imagepreview_indicator);
        layout_imagepreview_edit = findViewById(R.id.layout_imagepreview_edit);
        imagepreview_edit = findViewById(R.id.imagepreview_edit);
        tv_nav_title = findViewById(R.id.tv_nav_title);
        tv_nav_title.setTextColor(Color.WHITE);
        tv_nav_right = findViewById(R.id.tv_nav_right);
        tv_nav_right.setTextColor(Color.WHITE);
        if (!TextUtils.isEmpty(getIntent().getExtras().getString("rightNav"))) {
            tv_nav_right.setText(getIntent().getExtras().getString("rightNav"));
            tv_nav_right.setOnClickListener(view -> {
                if (getIntent().getExtras().getParcelable("rightNavClick") != null) {
                    ((RightNavClickImpl) getIntent().getExtras().getParcelable("rightNavClick")).click(ImagePreviewActivity.this);
                }
            });
        }
        ib_nav_left = findViewById(R.id.ib_nav_left);
        ib_nav_left.setImageResource(R.mipmap.icon_back_white);
        ib_nav_left.setOnClickListener(view -> onBackPressed());

        int choicePosition = getIntent().getExtras().getInt("position");
        urls = getIntent().getExtras().getStringArrayList("urls");
        canEdit = getIntent().getExtras().getBoolean("canEdit");
        if (canEdit) {
            layout_imagepreview_edit.setVisibility(View.VISIBLE);
        }
        fragments = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            ImagePreviewFragment fragment = ImagePreviewFragment.newInstance(urls.get(i), i);
            fragment.setOnPicChangedListener((position, imageInfo) -> {
                // 添加图片尺寸信息
                point.put("" + position, imageInfo);
            });
            fragments.add(fragment);
        }
        adapter = new ViewPagerFragmentAdapter(fragments, getSupportFragmentManager());
        imagepreview_viewpager.setAdapter(adapter);
        imagepreview_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                currentPosition = position;

                Observable.timer(300, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            // 图片尺寸复原
                            if (position - 1 >= 0 && point.containsKey("" + (position - 1))) {
                                ((ImagePreviewFragment) fragments.get(position - 1)).update(point.get("" + (position - 1)).getWidth(), point.get("" + (position - 1)).getHeight());
                            }
                            if (position + 1 <= urls.size() && point.containsKey("" + (position + 1))) {
                                ((ImagePreviewFragment) fragments.get(position + 1)).update(point.get("" + (position + 1)).getWidth(), point.get("" + (position + 1)).getHeight());
                            }
                        });
                tv_nav_title.setText((position + 1) + "/" + urls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        imagepreview_indicator.setViewPager(imagepreview_viewpager);
        imagepreview_viewpager.setCurrentItem(choicePosition);
        imagepreview_edit.setOnClickListener(v -> {
            if (urls.get(imagepreview_viewpager.getCurrentItem()).indexOf("http") != -1) {
                Toast.makeText(ImagePreviewActivity.this, "网络图片不能修改", Toast.LENGTH_SHORT).show();
                return;
            }
            Utils.cropImage(urls.get(imagepreview_viewpager.getCurrentItem()), ImagePreviewActivity.this, CommonParams.RESULT_CROP, 0);
        });
        tv_nav_title.setText((choicePosition + 1) + "/" + urls.size());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonParams.RESULT_CROP && resultCode == RESULT_OK) {
            String path = data.getExtras().getString("path");
            Utils.refreshAlbum(this, path);
            int position = imagepreview_viewpager.getCurrentItem();
            urls.remove(position);
            urls.add(position, path);
            fragments.clear();
            point.clear();
            for (int i = 0; i < urls.size(); i++) {
                fragments.add(ImagePreviewFragment.newInstance(urls.get(i), i));
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("urls", urls);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 获取当前选中的Position
     *
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }
}
