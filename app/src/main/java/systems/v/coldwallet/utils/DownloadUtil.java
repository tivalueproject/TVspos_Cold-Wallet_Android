package systems.v.wallet.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

/**
 * 下载帮助类
 */
public class DownloadUtil {
    private final int HANDLER_WHAT_PROGRESS = 1;
    private final int HANDLER_WHAT_END = 2;
    private LinkedList<DownLoadTask> downLoadTasks = new LinkedList<>();
    private DownloadCallBack mDownloadCallBack;
    private int rcount = 0;
    private Handler handler;

    public DownloadUtil(DownloadCallBack mDownloadCallBack) {
        this(mDownloadCallBack, 3);
    }

    /**
     * @param downloadCallBack
     * @param rcount           失败重试次数
     */
    public DownloadUtil(DownloadCallBack downloadCallBack, int rcount) {
        this.mDownloadCallBack = downloadCallBack;
        this.rcount = rcount;
        handler = new CustomHandler(this);
    }

    /**
     * 下载
     *
     * @param param
     */
    public void download(DownloadParam param) {
        if (mDownloadCallBack != null)
            mDownloadCallBack.downLoadStart(param);
        DownLoadTask task = new DownLoadTask(param);
        synchronized (downLoadTasks) {
            downLoadTasks.add(task);
        }
        task.start();
    }

    /**
     * 取消所有下载任务
     */
    public void cancelAll() {
        synchronized (downLoadTasks) {
            for (DownLoadTask task : downLoadTasks) {
                task.cancel();
            }
            downLoadTasks.clear();
        }
    }

    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLER_WHAT_PROGRESS:
                MessageObj messageObj = (MessageObj) msg.obj;
                mDownloadCallBack.onProgressUpdate(messageObj.param, messageObj.downloadSize, messageObj.totalSize);
                break;
            case HANDLER_WHAT_END:
                MessageObj obj = (MessageObj) msg.obj;
                mDownloadCallBack.downloadEnd(obj.param);
                break;
        }
    }

    private static class CustomHandler extends Handler {
        WeakReference<DownloadUtil> weakReference;

        public CustomHandler(DownloadUtil downloadUtil) {
            this.weakReference = new WeakReference<>(downloadUtil);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadUtil downloadUtil = weakReference.get();
            if (downloadUtil != null)
                downloadUtil.handleMessage(msg);
        }
    }

    /**
     * 下载任务
     */
    private class DownLoadTask extends Thread {
        private final int timeout = 5 * 1000;
        private int downloadSize = 0;
        private boolean isStoped = false;
        private MessageObj messageObj;

        public DownLoadTask(DownloadParam param) {
            messageObj = new MessageObj(param);
        }

        public void cancel() {
            isStoped = true;
        }

        @Override
        public void run() {
            File tempFile = new File(messageObj.param.file.getPath() + ".download");
            int count = 0;
            boolean isOk = false;
            while (!isStoped) {
                if (rcount > 0 && count > rcount) {
                    break;
                }
                if (download(tempFile)) {
                    isOk = true;
                    break;
                }
                count++;
            }
            if (isOk) {
                isOk = tempFile.renameTo(messageObj.param.file);
            }
            deleteFile(tempFile);
            if (!isOk) {
                deleteFile(messageObj.param.file);
            }
            if (!isStoped && mDownloadCallBack != null) {
                Message msg = handler.obtainMessage();
                msg.what = HANDLER_WHAT_END;
                msg.obj = messageObj;
                handler.sendMessage(msg);
            }
            synchronized (downLoadTasks) {
                downLoadTasks.remove(this);
            }
        }

        private boolean download(File saveFile) {
            File pFile = saveFile.getParentFile();
            if(pFile != null && !pFile.exists()){
                pFile.mkdirs();
            }
            RandomAccessFile out = null;
            InputStream in = null;
            try {
                URLConnection urlConnection = new URL(messageObj.param.getUrl()).openConnection();
                urlConnection.setRequestProperty("range", "bytes="
                        + downloadSize + "-");
                urlConnection.setConnectTimeout(timeout);
                urlConnection.setReadTimeout(timeout);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.connect();
                if (isStoped) {
                    return false;
                }
                in = urlConnection.getInputStream();
                int count = urlConnection.getContentLength();
                out = new RandomAccessFile(saveFile, "rw");
                out.seek(downloadSize);

                byte[] buffer = new byte[10 * 1024];
                int total;
                Message msg;
                while (!isStoped) {
                    int len = in.read(buffer);
                    if (len <= 0) {
                        break;
                    }
                    out.write(buffer, 0, len);
                    if (!isStoped && mDownloadCallBack != null) {
                        downloadSize += len;
                        total = count <= 0 ? downloadSize * 2 : count;

                        messageObj.downloadSize = downloadSize;
                        messageObj.totalSize = total;
                        msg = handler.obtainMessage();
                        msg.what = HANDLER_WHAT_PROGRESS;
                        msg.obj = messageObj;
                        handler.sendMessage(msg);
                    }
                }
                if (count <= 0)
                    return downloadSize > 0;
                else
                    return downloadSize >= count;
            } catch (Exception e) {
                Log.e("download", e.getMessage(), e);
            } finally {
                closeIO(in);
                closeIO(out);
            }
            return false;
        }
    }

    private static void deleteFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            String[] fs = file.list();
            if (fs != null) {
                for (String f : fs) {
                    deleteFile(new File(file, f));
                }
            }
        }
        boolean flag = file.delete();
        if (!flag) {// 处理:open failed: EBUSY (Device or resource busy)
            File to = new File(file.getParent(), "delete_"
                    + System.currentTimeMillis());
            if (file.renameTo(to))
                to.delete();
        }
    }

    private static void closeIO(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (IOException e) {
            }
        }
    }

    private class MessageObj {
        public DownloadParam param;
        public int downloadSize;
        public int totalSize;

        public MessageObj(DownloadParam param) {
            this.param = param;
        }
    }

    /**
     * 下载参数
     */
    public static class DownloadParam {
        private String url;
        private File file;
        private Object tag;

        public DownloadParam(String url, File file) {
            this.url = url;
            this.file = file;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public Object getTag() {
            return tag;
        }

        public String getUrl() {
            return url;
        }

        public File getSaveFile() {
            return file;
        }
    }

    /**
     * 下载回调
     */
    public interface DownloadCallBack {
        /**
         * 开始下载
         *
         * @param param
         */
        void downLoadStart(DownloadParam param);

        /**
         * 下载进度
         *
         * @param param
         * @param downloadSize
         * @param totalSize
         */
        void onProgressUpdate(DownloadParam param, int downloadSize, int totalSize);

        /**
         * 下载完成,根据DownloadParam中file是否存在来判断下载成功/失败
         *
         * @param param
         */
        void downloadEnd(DownloadParam param);
    }
}
