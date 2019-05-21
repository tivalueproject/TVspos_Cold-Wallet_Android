package systems.v.coldwallet.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.FeedbackActivity;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;

import systems.v.coldwallet.R;
import systems.v.coldwallet.ui.BaseActivity;
import systems.v.coldwallet.ui.view.main.MainActivity;
import systems.v.coldwallet.ui.view.wallet.WalletInitActivity;
import systems.v.coldwallet.utils.PermissionUtil;
import systems.v.coldwallet.utils.ToastUtil;
import systems.v.wallet.basic.AlertDialog;
import systems.v.wallet.basic.utils.FileUtil;

public class SplashActivity extends BaseActivity {
    private Dialog mUpdateDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (!PermissionUtil.permissionGranted(this)) {
            PermissionUtil.checkPermissions(this);
        } else {
            FeedbackActivity.setBarImmersive(true);
            PgyCrashManager.register();
            new PgyUpdateManager.Builder()
                    .setForced(false)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                    .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                    .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk， 默认为true
                    .setUpdateManagerListener(new UpdateManagerListener() {
                        @Override
                        public void onNoUpdateAvailable() {
                            //没有更新是回调此方法
                            Log.d("pgyer", "there is no new version");
                            launch();
                        }
                        @Override
                        public void onUpdateAvailable(AppBean appBean) {
                            //有更新回调此方法
                            Log.d("pgyer", "there is new version can update"
                                    + "new versionCode is " + appBean.getVersionCode());
                            //调用以下方法，DownloadFileListener 才有效；
                            //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                            final AppBean appBeanEx = appBean;
                            if (mUpdateDialog == null) {
                                mUpdateDialog = new AlertDialog.Builder(mActivity, R.style.BasicAlertDialog_Dark)
                                        .setTitle(R.string.update_title)
                                        .setMessage("")
                                        .setPositiveButton(R.string.basic_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                PgyUpdateManager.downLoadApk(appBeanEx.getDownloadURL());
                                                launch();
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

                        @Override
                        public void checkUpdateFailed(Exception e) {
                            //更新检测失败回调
                            //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                            Log.e("pgyer", "check update failed ", e);
                        }
                    })
                    //注意 ：
                    //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                    //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                    //想要使用蒲公英的默认下载进度的UI则不设置此方法
                    .setDownloadFileListener(new DownloadFileListener() {
                        @Override
                        public void downloadFailed() {
                            //下载失败
                            System.out.println("download apk failed");
                        }

                        @Override
                        public void downloadSuccessful(Uri uri) {
                            System.out.println("download apk success");
                            // 使用蒲公英提供的安装方法提示用户 安装apk
                            PgyUpdateManager.installApk(uri);
                        }

                        @Override
                        public void onProgressUpdate(Integer... integers) {
                            System.out.println("update download apk progress" + integers);
                        }})
                    .register();
            if(!isConnectIsNomarl()){
                launch();
            }
        }
    }

    private boolean isConnectIsNomarl() {
        ConnectivityManager ConnectivityManager = (android.net.ConnectivityManager) mActivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = ConnectivityManager.getActiveNetworkInfo();

        if (netInfo == null) {
            return false;
        } else {
            return netInfo.isAvailable();
        }
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
