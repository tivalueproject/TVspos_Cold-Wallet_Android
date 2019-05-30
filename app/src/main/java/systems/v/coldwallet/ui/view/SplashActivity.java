package systems.v.coldwallet.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.FeedbackActivity;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import systems.v.coldwallet.R;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.main.MainActivity;
import systems.v.coldwallet.ui.view.wallet.WalletInitActivity;
import systems.v.coldwallet.utils.PermissionUtil;
import systems.v.coldwallet.utils.ToastUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;

public class SplashActivity extends BaseActivity {
    static String  appUrl = "https://link-e-pro.oss-cn-beijing.aliyuncs.com/wallet/cold.wallet_release.apk";
    static String  serverUrl = "http://47.75.180.164:8080/v1/appVsersion";
    private Dialog mUpdateDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            final Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    Bundle data = msg.getData();
                    String val = data.getString("value");
                    try {
                        JSONObject object = new JSONObject(val);
                        if(object.getString("message").equals("SUCCESS")) {
                            final  int serverVersion = object.getJSONObject("data").getInt("hotAppVersion");
                            if(serverVersion > GetCurrentAppVersion()){
                                if (mUpdateDialog == null) {
                                    mUpdateDialog = new AlertDialog.Builder(mActivity)
                                            .setTitle(R.string.update_title)
                                            .setMessage("")
                                            .setPositiveButton(R.string.basic_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DownloadApp(appUrl,String.valueOf(serverVersion));
                                                }
                                            })
                                            .setNegativeButton(R.string.basic_alert_dialog_cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    launch();
                                                }
                                            }).create();
                                }
                                mUpdateDialog.setCanceledOnTouchOutside(false);
                                mUpdateDialog.show();
                            }
                            else {
                                launch();
                            }
                        } else {
                            launch();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            new Thread(){
                public void run(){
                    String result = sendGet(serverUrl,"");
                    Message msg = Message.obtain();
                    Bundle data = new Bundle();
                    data.putString("value",result);
                    msg.setData(data);
                    if (result.isEmpty()){
                        launch();
                    }else {
                        handler.sendMessage(msg);
                    }
                }
            }.start();
        }
    }

    private int GetCurrentAppVersion(){
        int versionCode = 0;
        try {
            PackageInfo pkg = getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
            versionCode = pkg.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return  versionCode;
    }

    public static String sendGet(String url, String param)
    {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String getCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    private void DownloadApp(String url,String version){
        File file = new File(getCacheDir(mActivity), "cold.wallet_v"  + version + ".apk");
        if (file.exists()) {
            systems.v.wallet.utils.IntentUtils.installApk(mActivity, file);
            return;
        }
        systems.v.wallet.utils.DownloadUtil.DownloadParam downloadParam = new systems.v.wallet.utils.DownloadUtil.DownloadParam(url, file);
        new systems.v.wallet.utils.DownloadUtil(new systems.v.wallet.utils.DownloadUtil.DownloadCallBack() {
            @Override
            public void downLoadStart(systems.v.wallet.utils.DownloadUtil.DownloadParam param) {
                showLoading();
            }

            @Override
            public void onProgressUpdate(systems.v.wallet.utils.DownloadUtil.DownloadParam param, int downloadSize, int totalSize) {
            }

            @Override
            public void downloadEnd(systems.v.wallet.utils.DownloadUtil.DownloadParam param) {
                hideLoading();
                if (param.getSaveFile().exists()) {
                    systems.v.wallet.utils.IntentUtils.installApk(mActivity, param.getSaveFile());
                }
            }
        }).download(downloadParam);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE: {
                if (!PermissionUtil.permissionGranted(this)) {
                    ToastUtil.showToast(R.string.grant_permissions);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                } else {
                    launch();
                }
            }
            break;
        }
    }

    private void launch() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FileUtil.walletExists(mActivity)) {
                    MainActivity.launch(SplashActivity.this, false);
                } else {
                    WalletInitActivity.launch(mActivity);
                }
                finish();
            }
        }, 500);
    }
}
