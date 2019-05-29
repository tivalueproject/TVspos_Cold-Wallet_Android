package systems.v.wallet.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.FileProvider;

import android.text.TextUtils;

import java.io.File;

/**
 * HTC——HTC
 * 华为——Huawei
 * 魅族——Meizu
 * 小米——Xiaomi
 * 索尼——Sony
 * oppo——OPPO
 * LG——LG
 * vivo——vivo
 * 三星——samsung
 * 乐视——Letv
 * 中兴——ZTE
 * 酷派——YuLong
 * 联想——LENOVO
 * Created by lgh on 2017/10/30.
 */

public class IntentUtils {


    /**
     * 安装APK
     *
     * @param context
     * @param file
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {
            Uri apkUri = FileProvider.getUriForFile(context, "systems.v.coldwallet.FileProvider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

//    /**
//     * 跳转权限设置界面
//     *
//     * @param context
//     */
//    public static void gotoPermissionSetting(Context context) {
//        String manufacturer = Build.MANUFACTURER;
//        manufacturer = manufacturer == null ? "" : manufacturer.toLowerCase();
//
//        Intent intent = null;
//        if (manufacturer.startsWith("xiaomi")) {
//            String rom = DeviceUtils.getMiuiVersion();
//            rom = rom == null ? "" : rom.toLowerCase();
//            if (TextUtils.equals("v5", rom)) {
//                Uri packageURI = Uri.parse("package:" + context.getApplicationInfo().packageName);
//                intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
//            } else if (TextUtils.equals("v6", rom) || TextUtils.equals("v7", rom)) {
//                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
//                intent.putExtra("extra_pkgname", context.getPackageName());
//            } else if (TextUtils.equals("v8", rom)) {
//                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
//                intent.putExtra("extra_pkgname", context.getPackageName());
//            } else {
//                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
//                ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
//                intent.setComponent(componentName);
//                intent.putExtra("extra_pkgname", context.getPackageName());
//            }
//        } else if (manufacturer.startsWith("meizu")) {
//            intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            intent.putExtra("packageName", context.getPackageName());
//        } else if (manufacturer.startsWith("huawei")) {
//            intent = new Intent();
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
//            intent.setComponent(comp);
//        } else if (manufacturer.startsWith("sony")) {
//            intent = new Intent();
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
//            intent.setComponent(comp);
//        } else if (manufacturer.startsWith("oppo")) {
//            intent = new Intent();
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
//            intent.setComponent(comp);
//        } else if (manufacturer.startsWith("lg")) {
//            intent = new Intent("android.intent.action.MAIN");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
//            intent.setComponent(comp);
//        } else if (manufacturer.startsWith("letv")) {
//            intent = new Intent();
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
//            intent.setComponent(comp);
//        } else if (manufacturer.startsWith("qihoo360")) {
//            intent = new Intent("android.intent.action.MAIN");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.putExtra("packageName", context.getPackageName());
//            ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
//            intent.setComponent(comp);
//        }
//        if (intent == null) {
//            intent = new Intent();
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            if (Build.VERSION.SDK_INT >= 9) {
//                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
//                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
//            } else if (Build.VERSION.SDK_INT <= 8) {
//                intent.setAction(Intent.ACTION_VIEW);
//                intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
//                intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
//            }
//        }
//        try {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        } catch (Exception e) {
//            intent = new Intent(Settings.ACTION_SETTINGS);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            try {
//                context.startActivity(intent);
//            } catch (Exception e2) {
//
//            }
//        }
//    }
}
