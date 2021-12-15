package com.example.testrooted;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TestRoot {
    private Context context;
    private static final Set<String> mPath = new LinkedHashSet<>();

    public TestRoot(Context context) {
        this.context = context;
        constructPaths();
    }

    private static final String DEFAULT_ROM_BUILD_KEYS = bytesToString(new byte[] {116,101,115,116,45,107,101,121,115,44,106,105,100,101,44,114,101,109,105,120});  //  test-keys,jide,remix
    private static final String SU = bytesToString(new byte[] {115,117});
    private static final String[] DEFAULT_PATH = {
            bytesToString(new byte[] {47,115,98,105,110,47}),  //  /sbin/
            bytesToString(new byte[] {47,117,115,114,47,98,105,110,47}),   // /usr/bin/
            bytesToString(new byte[] {47,118,101,110,100,111,114,47,98,105,110,47}),   //  /vendor/bin/
            bytesToString(new byte[] {47,115,121,115,116,101,109,47,98,105,110,47}),   //  /system/bin/
            bytesToString(new byte[] {47,115,121,115,116,101,109,47,115,98,105,110,47}),   //  /system/sbin/
            bytesToString(new byte[] {47,115,121,115,116,101,109,47,120,98,105,110,47}),   //  /system/xbin/
            bytesToString(new byte[] {47,100,97,116,97,47,108,111,99,97,108,47,120,98,105,110,47}),//  /data/local/xbin/
            bytesToString(new byte[] {47,100,97,116,97,47,108,111,99,97,108,47,98,105,110,47}), //  /data/local/bin/
            bytesToString(new byte[] {47,115,121,115,116,101,109,47,115,100,47,120,98,105,110,47}), //  /system/sd/xbin/
            bytesToString(new byte[] {47,115,121,115,116,101,109,47,98,105,110,47,102,97,105,108,115,97,102,101,47}),    //  /system/bin/failsafe/
            bytesToString(new byte[] {47,100,97,116,97,47,108,111,99,97,108,47}), //  /data/local/
            bytesToString(new byte[] {47,109,97,103,105,115,107,47,112,104,104,47}), //  /magisk/phh/
            bytesToString(new byte[] {47,109,97,103,105,115,107,47,112,104,104,47,120,98,105,110,47}) //  /magisk/phh/xbin/
    };
    private static final String []DEFAULT_ROOTED_ROM_PROCESSES = {
            bytesToString(new byte[] {100,97,101,109,111,110,115,117}),     //daemonsu
            bytesToString(new byte[] {115,117,112,111,108,105,99,121})      //supolicy
    };
    private static final String []DEFAULT_SYSTEM_DIRS = {
            bytesToString(new byte[] {47,115,121,115,116,101,109})   //  /system
    };

    public boolean isRoot () {
        if(checkRootMethod1() || checkRootMethod2() || checkRootMethod4() || checkRootMethod5() || checkRootMethod7() || checkRootMethod8()
                || checkRootMethod9() || checkRootMethod10() || checkRootMethod11() || checkRootMethod12() || checkRootMethod13()
        ) {
            return true;
        }
        return false;
    }

    // METHOD 1
    private boolean checkRootMethod1() {
        String sRomKeys = context.getString(R.string.romKeys) + ", " + DEFAULT_ROM_BUILD_KEYS;
        String []keys = sRomKeys.split(",");
        String buildInfo = getBuildInfoFingerprint().toLowerCase();
        for(String k : keys) {
            String key = k.trim();
            if((!TextUtils.isEmpty(key)) && buildInfo.contains(key.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // METHOD 2
    private boolean checkRootMethod2() {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(0);
        String[] packageCandidate = new String[] {
                bytesToString(new byte[] {99,111,109,46,107,111,117,115,104,105,107,100,117,116,116,97,46,115,117,112,101,114,117,115,101,114}),   //com.koushikdutta.superuser
                bytesToString(new byte[] {99,111,109,46,110,111,115,104,117,102,111,117,46,97,110,100,114,111,105,100,46,115,117}),      //com.noshufou.android.su
                bytesToString(new byte[] {101,117,46,99,104,97,105,110,102,105,114,101,46,115,117,112,101,114,115,117}),     //eu.chainfire.supersu
                bytesToString(new byte[] {99,111,109,46,107,105,110,103,111,117,115,101,114,46,99,111,109}),        //com.kingouser.com
                bytesToString(new byte[] {99,111,109,46,113,117,105,110,110,121,56,57,56,46,97,112,112,46,97,117,116,111,109,97,103,105,115,107}),     //com.quinny898.app.automagisk
                bytesToString(new byte[] {99,111,109,46,116,111,112,106,111,104,110,119,117,46,109,97,103,105,115,107}),     //com.topjohnwu.magisk
                bytesToString(new byte[] {109,101,46,112,104,104,46,115,117,112,101,114,117,115,101,114})      //me.phh.superuser
        };
        for (PackageInfo pi : list) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pi.packageName, PackageManager.GET_META_DATA);
                for (String aPackageCandidate : packageCandidate) {
                    if (ai.packageName.contains(aPackageCandidate)) {
                        return true;
                    }
                }
            }
            catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // METHOD 4
    private boolean checkRootMethod4() {
        final String GENERIC = bytesToString(new byte[] {103,101,110,101,114,105,99});   //generic
        if(Build.BRAND.equalsIgnoreCase(GENERIC)) {
            return true;
        }
        return false;
    }

    // METHOD 5
    private boolean checkRootMethod5() {
        final String SDK = bytesToString(new byte[] {115,100,107});    //sdk
        if(Build.PRODUCT.equalsIgnoreCase(SDK) || Build.FINGERPRINT.contains(SDK)) {
            return true;
        }
        final String VBOX86P = bytesToString(new byte[] {118,98,111,120,56,54,112});   //vbox86p
        if(Build.PRODUCT.equalsIgnoreCase(VBOX86P)) {
            return true;
        }
        final String GOLDFISH = bytesToString(new byte[] {103,111,108,100,102,105,115,104});  //goldfish
        if(Build.HARDWARE.contains(GOLDFISH)) {
            return true;
        }
        return false;
    }

    // METHOD 7
    private boolean checkRootMethod7() {
        for (String where : mPath) {
            String binary = where + SU;
            if (new File(binary).exists()) {
               return true;
            }
        }
        return false;
    }

    // METHOD 8
    private boolean checkRootMethod8() {
        String binaryName = SU;
        final String STAT_COMMAND = bytesToString(new byte[] {115,116,97,116});     //stat
        for (String where : mPath) {
            BufferedReader in = null;
            try {
                Process p = Runtime.getRuntime().exec(STAT_COMMAND + " " + where + binaryName);
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("File: ") && line.contains(binaryName)) {
                        in.close();
                        return true;
                    }
                }
                in.close();
            }
            catch (IOException ignore) {
            }
        }
        return false;
    }

    // METHOD 9
    private boolean checkRootMethod9() {
        Set<String> rootedRomProcesses = new LinkedHashSet<>(Arrays.asList(DEFAULT_ROOTED_ROM_PROCESSES));
        rootedRomProcesses.add("daemonsu");
        rootedRomProcesses.add("supolicy");
        for (String where : mPath) {
            for(String process : rootedRomProcesses) {
                if (new File(where + process).exists()) {
                    return true;
                }
            }
        }
        return false;
    }

    // METHOD 10
    private boolean checkRootMethod10() {
        try {
            throw new Exception();
        } catch (Exception e) {
            int zygoteInitCallCount = 0;
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().equals(bytesToString(new byte[]{99, 111, 109, 46, 97, 110, 100, 114, 111, 105, 100, 46, 105, 110, 116, 101, 114, 110, 97, 108, 46, 111, 115, 46, 90, 121, 103, 111, 116, 101, 73, 110, 105, 116}))) {  //com.android.internal.os.ZygoteInit
                    zygoteInitCallCount++;
                    if (zygoteInitCallCount == 2) {
                        return true;
                    }
                }
                if (stackTraceElement.getClassName().equals(bytesToString(new byte[]{99, 111, 109, 46, 115, 97, 117, 114, 105, 107, 46, 115, 117, 98, 115, 116, 114, 97, 116, 101, 46, 77, 83, 36, 50})) //com.saurik.substrate.MS$2
                        && stackTraceElement.getMethodName().equals(bytesToString(new byte[]{105, 110, 118, 111, 107, 101, 100}))) {   //invoked
                    return true;
                }
                if (stackTraceElement.getClassName().equals(bytesToString(new byte[]{100, 101, 46, 114, 111, 98, 118, 46, 97, 110, 100, 114, 111, 105, 100, 46, 120, 112, 111, 115, 101, 100, 46, 88, 112, 111, 115, 101, 100, 66, 114, 105, 100, 103, 101}))   //de.robv.android.xposed.XposedBridge
                        && stackTraceElement.getMethodName().equals(bytesToString(new byte[]{109, 97, 105, 110}))) {  //main
                    return true;
                }
                if (stackTraceElement.getClassName().equals(bytesToString(new byte[]{100, 101, 46, 114, 111, 98, 118, 46, 97, 110, 100, 114, 111, 105, 100, 46, 120, 112, 111, 115, 101, 100, 46, 88, 112, 111, 115, 101, 100, 66, 114, 105, 100, 103, 101}))   //de.robv.android.xposed.XposedBridge
                        && stackTraceElement.getMethodName().equals(bytesToString(new byte[]{104, 97, 110, 100, 108, 101, 72, 111, 111, 107, 101, 100, 77, 101, 116, 104, 111, 100}))) {    //handleHookedMethod
                    return true;
                }
            }
        }

        BufferedReader reader = null;
        try {
            final String PROC = bytesToString(new byte[]{47, 112, 114, 111, 99, 47});      //  "/proc/"
            final String MAP = bytesToString(new byte[]{47, 109, 97, 112, 115});          //  "/maps"
            final String SO = bytesToString(new byte[]{46, 115, 111});                  //  ".so"
            final String JAR = bytesToString(new byte[]{46, 106, 97, 114});              //  ".jar"
            Set<String> libraries = new HashSet<>();
            String mapsFilename = PROC + android.os.Process.myPid() + MAP;
            reader = new BufferedReader(new FileReader(mapsFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(SO) || line.endsWith(JAR)) {
                    int n = line.lastIndexOf(" ");
                    libraries.add(line.substring(n + 1));
                }
            }
            for (String library : libraries) {
                if (library.contains(bytesToString(new byte[]{99, 111, 109, 46, 115, 97, 117, 114, 105, 107, 46, 115, 117, 98, 115, 116, 114, 97, 116, 101}))         //  "com.saurik.substrate"
                        || library.contains(bytesToString(new byte[]{88, 112, 111, 115, 101, 100, 66, 114, 105, 100, 103, 101, 46, 106, 97, 114}))) {  //  "XposedBridge.jar"
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (Exception ignore) {
        }
        return false;
    }

    // METHOD 11
    private boolean checkRootMethod11 () {
        List<String> foundDirs = checkBlueStacksExternalDirs(splitString("blue.utils,bstfolder", ","));
        if(foundDirs.size() > 0) {
            for(String dir : foundDirs) {
                if(dir.trim().length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    // METHOD 12
    private boolean checkRootMethod12() {
        // Scan memory for Frida artifacts.
        String artifactsStr = "frida,test";
        if (!TextUtils.isEmpty(artifactsStr)) {
            BufferedReader reader = null;
            try {
                String[] artifacts = artifactsStr.split(",");

                final String PROC = bytesToString(new byte[]{47, 112, 114, 111, 99, 47});      //  "/proc/"
                final String MAP = bytesToString(new byte[]{47, 109, 97, 112, 115});          //  "/maps"
                Set<String> libraries = new HashSet<>();
                String mapsFilename = PROC + android.os.Process.myPid() + MAP;
                reader = new BufferedReader(new FileReader(mapsFilename));
                String line;
                while ((line = reader.readLine()) != null) {
                    for (String artifact : artifacts) {
                        if (line.contains(artifact)) {
                            reader.close();
                            return true;
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    // METHOD 13
    private boolean checkRootMethod13() {
        String blockedXposedModulesString = "com.sudocode.sudohide";
        String xposedClassesRegex = "de.robv.android.xposed.XposedBridge";

        String[] blockedXposedModules = blockedXposedModulesString.split(",");
        PackageManager packageManager = context.getPackageManager();
        for (String hiddenPackage : blockedXposedModules) {
            try {
                packageManager.getApplicationInfo(hiddenPackage, PackageManager.GET_META_DATA);
            } catch (Exception e) {
                // Traverse the stack trace to find if xposed classes appear
                for(StackTraceElement stackTraceElement : e.getStackTrace()) {
                    if(stackTraceElement.getClassName().matches(xposedClassesRegex)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     *
     * Utils
     */
    private static String bytesToString(byte[] bytes) {
        return new String(bytes);
    }

    private static String getBuildInfoFingerprint() {
        return String.format(Locale.US, "%s__%s__%s__%s__%s__%s__%s__%s__%s__%s",
                Build.FINGERPRINT,
                Build.BOARD,
                Build.BOOTLOADER,
                Build.BRAND,
                Build.DEVICE,
                Build.HARDWARE,
                Build.MANUFACTURER,
                Build.MODEL,
                Build.PRODUCT,
                Build.TAGS);
    }

    //----------------------------------------------------------------------------------------------
    private static void appendPath(String path) {
        if(!path.endsWith(File.separator)) {
            path += File.separator;
        }
        mPath.add(path);
    }
    //----------------------------------------------------------------------------------------------
    private static void appendPath(String []paths) {
        for(String path : paths) {
            if(path != null) {
                appendPath(path.trim());
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    private static void constructPaths() {
        mPath.clear();
        appendPath(System.getenv("PATH").split(File.pathSeparator));
        appendPath(DEFAULT_PATH);
    }

    private static List<String> checkBlueStacksExternalDirs(String[] configuredExternalDirs) {
        List<String> foundDirs = new LinkedList<>();
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (configuredExternalDirs != null && configuredExternalDirs.length > 0) {
            for (String dir : configuredExternalDirs) {
                dir = dir.trim();
                String filePath = sdcardPath + '/' + dir;
                try {
                    boolean isExist = isFileExists(filePath);
                    if (isExist) {
                        foundDirs.add(dir);
                    }
                } catch (Exception e) {
                    File dirFile = new File(filePath);
                    if (dirFile.exists()) {
                        foundDirs.add(dir);
                    }
                }
            }
        }
        return foundDirs;
    }

    private static boolean isFileExists(String filePath) throws Exception {
        boolean isExist;
        BufferedReader in;
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        java.lang.Process proc = Runtime.getRuntime().exec("ls -l " + filePath);
        in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String firstLine = in.readLine();
        proc.waitFor();
        in.close();

        if (firstLine.startsWith("total") || firstLine.endsWith("Permission denied")) {
            isExist = true;
        } else if (firstLine.contains("No such file or directory")) {
            isExist = false;
        } else {
            throw new Exception("Unexpected case when checking file existence by ls command");
        }
        return isExist;
    }

    private static String[] splitString(String s, String delimiter) {
        if(s != null) {
            return s.trim().split(delimiter);
        }
        else {
            return new String[] {};
        }
    }
}
