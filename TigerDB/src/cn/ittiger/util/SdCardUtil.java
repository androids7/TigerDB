package cn.ittiger.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * SDCard帮助类
 * @author: huylee
 * @time:	2015-8-13下午10:13:31
 */
public class SdCardUtil {
    private static final String TAG = SdCardUtil.class.getSimpleName();

    /**
     * 获取 {@link android.os.StatFs}
     */
    public static StatFs getStatFs(String path) {
        return new StatFs(path);
    }

    /**
     * 获取手机自身内存路径
     */
    public static String getPhoneCardPath() {
        return Environment.getDataDirectory().getPath();
    }
    
    /**
	 * 是否存在SD卡
	 * 
	 * @return
	 */
	public static boolean isHasSDCard() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

    /**
     * 获取sd卡路径
     */
    public static String getNormalSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }
    
    public static String getRootPath() {
    	return isHasSDCard() ? getNormalSDCardPath() : getPhoneCardPath();
    }

    /**
     * 获取sd（外置）卡路径
     */
    public static String getSDCardPath() {
        String cmd = "cat /proc/mounts";
        String sdcard = null;
        Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
        BufferedReader bufferedReader = null;
        try {
            Process p = run.exec(cmd);// 启动另一个进程来执行命令
            bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));
            String lineStr;
            while ((lineStr = bufferedReader.readLine()) != null) {
                Log.i(TAG, "proc/mounts:   " + lineStr);
                if (lineStr.contains("sdcard")
                    && lineStr.contains(".android_secure")) {
                    String[] strArray = lineStr.split(" ");
                    if (strArray.length >= 5) {
                        sdcard = strArray[1].replace("/.android_secure", "");
                        Log.i(TAG, "find sd card path:   " + sdcard);
                        return sdcard;
                    }
                }
                if (p.waitFor() != 0 && p.exitValue() == 1) {
                    // p.exitValue()==0表示正常结束，1：非正常结束
                    Log.e(TAG, cmd + " 命令执行失败");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sdcard = Environment.getExternalStorageDirectory().getPath();
        Log.i(TAG, "not find sd card path return default:   " + sdcard);
        return sdcard;
    }

    /**
     * 查看所有的sd路径
     */
    public static ArrayList<String> getSDCardPathEx() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                Log.i(TAG, "mount:  " + line);
                if (line.contains("secure")) {
                    continue;
                }
                if (line.contains("asec")) {
                    continue;
                }

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns.length > 1) {
                        list.add("*" + columns[1]);
                    }
                } else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (columns.length > 1) {
                        list.add(columns[1]);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取当前路径，可用空间
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getAvailableSize(String path) {
        try {
            File base = new File(path);
            StatFs stat = new StatFs(base.getPath());
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取 SD card 信息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static SDCardInfo getSDCardInfo() {
        SDCardInfo sd = new SDCardInfo();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            sd.isExist = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                StatFs sf = new StatFs(sdcardDir.getPath());

                sd.totalBlocks = sf.getBlockCountLong();
                sd.blockByteSize = sf.getBlockSizeLong();

                sd.availableBlocks = sf.getAvailableBlocksLong();
                sd.availableBytes = sf.getAvailableBytes();

                sd.freeBlocks = sf.getFreeBlocksLong();
                sd.freeBytes = sf.getFreeBytes();

                sd.totalBytes = sf.getTotalBytes();
            }
        }
        Log.i(TAG, sd.toString());
        return sd;
    }


    /**
     * see more {@link android.os.StatFs}
     */
    public static class SDCardInfo {
        public boolean isExist;
        public long totalBlocks;
        public long freeBlocks;
        public long availableBlocks;

        public long blockByteSize;

        public long totalBytes;
        public long freeBytes;
        public long availableBytes;

        @Override
        public String toString() {
            return "SDCardInfo{" +
                   "isExist=" + isExist +
                   ", totalBlocks=" + totalBlocks +
                   ", freeBlocks=" + freeBlocks +
                   ", availableBlocks=" + availableBlocks +
                   ", blockByteSize=" + blockByteSize +
                   ", totalBytes=" + totalBytes +
                   ", freeBytes=" + freeBytes +
                   ", availableBytes=" + availableBytes +
                   '}';
        }
    }
}