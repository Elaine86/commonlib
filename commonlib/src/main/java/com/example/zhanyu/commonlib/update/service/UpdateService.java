package com.example.zhanyu.commonlib.update.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.zhanyu.commonlib.R;
import com.example.zhanyu.commonlib.network.OKHttpUtils;
import com.example.zhanyu.commonlib.update.bean.UpdateModel;
import com.example.zhanyu.commonlib.update.params.InitParams;
import com.example.zhanyu.commonlib.update.utils.NotificationUtils;
import com.example.zhanyu.commonlib.update.utils.RxBus;
import com.example.zhanyu.commonlib.update.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class UpdateService extends Service {

    OKHttpUtils okHttpUtils;

    public static ArrayList<String> downloadUrls;
    HashMap<String, Integer> ids;

    static {
        downloadUrls = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ids = new HashMap<>();

        okHttpUtils = new OKHttpUtils();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // android o 开启前台服务
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter().showStartForeground(
                    this,
                    "提示",
                    "升级服务",
                    "App在升级",
                    R.color.colorPrimary,
                    intent.getExtras().getInt("smallIcon"),
                    intent.getExtras().getInt("largeIcon"),
                    1000);
        }
        if (intent == null || intent.getExtras() == null || intent.getExtras().getString("url") == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String url = intent.getExtras().getString("url");
        if (intent.getExtras().getBoolean("download")) {
            //新增下载添加标志
            downloadUrls.add(url);
            //这个无所谓是不是清空，只要保留键值对即可
            ids.put(url, intent.getExtras().getInt("ids"));

            NotificationUtils.getNotificationCenter()
                    .createDownloadNotification(
                            intent.getExtras().getInt("ids"),
                            intent.getExtras().getString("name"),
                            intent.getExtras().getString("name"),
                            ContextCompat.getColor(this, R.color.colorPrimary),
                            intent.getExtras().getInt("smallIcon"), intent.getExtras().getInt("largeIcon"));

            okHttpUtils.asyncDownload(url, InitParams.FILE_PATH, new OKHttpUtils.RequestListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(String string) {
                    UpdateModel model = new UpdateModel();
                    model.setState(UpdateModel.State.SUCCESS);
                    model.setUrl(url);
                    model.setLocalPath(string);
                    model.setNotificationTitle(intent.getExtras().getString("name"));
                    updateInfo(model);
                }

                @Override
                public void onError() {
                    UpdateModel model = new UpdateModel();
                    model.setState(UpdateModel.State.FAIL);
                    model.setUrl(url);
                    model.setNotificationTitle(intent.getExtras().getString("name"));
                    updateInfo(model);
                }
            }, (progress, bytesRead, contentLength) -> {
                UpdateModel model = new UpdateModel();
                model.setState(UpdateModel.State.DOWNLOADING);
                model.setUrl(url);
                model.setProcess(progress);
                model.setBytesRead(bytesRead);
                model.setContentLength(contentLength);
                model.setNotificationTitle(intent.getExtras().getString("name"));
                updateInfo(model);
            });
        } else {
            if (ids.containsKey(url)) {
                NotificationUtils.getNotificationCenter().cancelNotification(ids.get(url));
            }
            //取消下载移除标志
            downloadUrls.remove(url);
            okHttpUtils.cancel(url);

            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateInfo(UpdateModel model) {
        if (model.getState() == UpdateModel.State.DOWNLOADING) {
            if (ids.containsKey(model.getUrl())) {
                NotificationUtils.getNotificationCenter().updateDownloadNotification(ids.get(model.getUrl()), model.getProcess(), model.getNotificationTitle());
            }
        } else if (model.getState() == UpdateModel.State.SUCCESS) {
            //区分文件下载完整
            if (Utils.checkAPKState(this, new File(model.getLocalPath()).getPath())) {
                Toast.makeText(this, "下载成功", Toast.LENGTH_SHORT).show();
                if (ids.containsKey(model.getUrl())) {
                    NotificationUtils.getNotificationCenter().showEndNotification(ids.get(model.getUrl()));
                }

                File file = fileExists(model);
                if (file != null) {
                    startActivity(Utils.install(this, file.getPath()));
                    stopSelf();
                }
            } else {
                model.setState(UpdateModel.State.FAIL);
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                if (ids.containsKey(model.getUrl())) {
                    NotificationUtils.getNotificationCenter().cancelNotification(ids.get(model.getUrl()));
                }
            }
            downloadUrls.remove(model.getUrl());
        } else if (model.getState() == UpdateModel.State.FAIL) {
            if (!downloadUrls.contains(model.getUrl())) {
                Toast.makeText(this, "下载已取消", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show();
                if (ids.containsKey(model.getUrl())) {
                    NotificationUtils.getNotificationCenter().cancelNotification(ids.get(model.getUrl()));
                }
                downloadUrls.remove(model.getUrl());
            }
        }
        RxBus.getDefault().post(model);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String downloadUrl : downloadUrls) {
            okHttpUtils.cancel(downloadUrl);
        }
        // android o 关闭后台服务
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            NotificationUtils.getNotificationCenter().hideStartForeground(this, 1000);
        }
    }

    private File fileExists(UpdateModel model) {
        File file;
        String url = model.getUrl();
        if (url.indexOf("?") != -1) {
            String url_ = url.substring(0, url.indexOf("?"));
            file = new File(InitParams.FILE_PATH + File.separator + url_.substring(url_.lastIndexOf("/") + 1));
        } else {
            file = new File(InitParams.FILE_PATH + File.separator + url.substring(url.lastIndexOf("/") + 1));
        }
        if (file.exists() && Utils.checkAPKState(this, file.getPath())) {
            return file;
        }
        return null;
    }
}
