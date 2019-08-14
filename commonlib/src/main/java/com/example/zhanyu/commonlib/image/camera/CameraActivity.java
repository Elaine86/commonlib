package com.example.zhanyu.commonlib.image.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.example.zhanyu.commonlib.R;
import com.example.zhanyu.commonlib.Utils.BarUtils;
import com.example.zhanyu.commonlib.base.activity.BaseActivity;
import com.example.zhanyu.commonlib.image.commonutils.Utils;

import java.util.List;

public class CameraActivity extends BaseActivity {

    @Override
    public void initParams() {

    }

    @Override
    public int initViews() {
        return R.layout.activity_camera;
    }

    @Override
    public void loadData() {
        permissionApply();
    }

    public void permissionApply() {
        boolean granted = PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
        if (granted) {
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() == 0) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CameraFragment()).commitAllowingStateLoss();
            }
        } else {
            PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE);
        }

    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BarUtils.setDark(this);
        super.onCreate(savedInstanceState);
    }

    public void backTo(String filePath) {
        //刷新相册
        Utils.refreshAlbum(this, filePath);
        //返回上一级目录
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString("path", filePath);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isDenied = true;
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                isDenied = false;
                break;
            }
        }
        if (isDenied) {
            finish();
        }
    }
}
