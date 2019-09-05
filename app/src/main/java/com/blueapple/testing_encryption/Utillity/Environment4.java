package com.blueapple.testing_encryption.Utillity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * <p>
 * Methoden zum Aufspüren von SD-Karten und USB-Geräten.
 * </p>
 * <ul>
 * <li>{@link #getDevices(Context)} liefert eine Liste aller
 * Geräte inklusive der internen Daten-Partition und ggf. nicht eingesteckter
 * Geräte.</li>
 * <li>{@link #getExternalStorage(Context)} liefert eine Liste
 * aller gerade nutzbaren Speicherorte außer /data, also Gerätespeicher, SDs
 * und USB.</li>
 * <li>{@link #getStorage(Context)} liefert diese Liste
 * inklusive dem internen Speicher /data</li>
 * </ul>
 * <p/>
 * <p>
 * Diese Methoden puffern die Listen und lauschen von selbst nach Änderungen
 * durch Einstecken und entnehmen von SD-Karten und USB-Geräten. Damit der dazu
 * nötige BroadcastReceiver am Ende freigegeben wird, muss
 * {@code setUseReceiver(android.content.Context, false)} aufgerufen werden,
 * sinnvollerweise in onDestroy() der Haupt-Activity. Will man keinen Receiver
 * haben, muss man vor dem ersten Aufruf einer der get-Methoden
 * {@code setUseReceiver(null, false)} aufrufen. Will man selbst einen Receiver
 * basteln, stellt dazu {@link #getRescanIntentFilter()} den passenden
 * IntentFilter bereit. Soll der eigene Receiver den dieser Klasse ablösen (hat
 * man also setUse...(...false) aufgerufen), führt
 * {@link #initDevices(Context)} einen Rescan der Geräte und
 * Neuaufbau der Listen durch.
 * </p>
 * <p>
 * Damit der Zugriff auf die Speichermedien klappt, muss im Manifest stehen:
 * </p>
 * <p/>
 * <pre>
 * <code>{@code
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18" />
 * }</code>
 * </pre>
 * <p>
 * Wenn das auch auf Geräten mit CyanogenMod laufen soll, darf man anders als
 * in der Android-Doku beschrieben allerdings das maxSdkVersion <b>nicht</b>
 * angeben.
 * <p/>
 * <h2>Hintergrund</h2>
 * <p>
 * Ab Android 4 verläuft die Verwaltung von SD-Karten etwas einfacher als in
 * alten Versionen, auch weil mit Android 3 die unified-Architektur eingeführt
 * wurde. Android selbst liefert allerdings erst ab 4.4 in
 *
 * Pfaden zurück, und die noch immer ohne USB-Geräte und mit den Pfaden in
 * /Android/data statt zum Root.
 * </p>
 * <p>
 * Ab Android 2 konnte man die Pfade über vold.fstab herausfinden, doch das ist
 * ab Android 4.3 nicht mehr implementiert. Die Suche über /proc/mounts
 * verläuft aufgrund der unübersichtlichen fuse-Einträge nicht gut.
 * </p>
 * <p>
 * Eine Lösung gibt es ab Android 4.0, nämlich eine versteckte Methode im
 * {@link StorageManager}, die hier per Reflection
 * herausgeführt ist.
 * </p>
 *
 * @author Jörg Wirtgen (jow@ct.de)
 * @version 0.9
 */
public class Environment4 {
    private static final String TAG = "Environment4";

    public final static String TYPE_PRIMARY = "primär";
    public final static String TYPE_INTERNAL = "intern";
    public final static String TYPE_SD = "MicroSD";
    public final static String TYPE_USB = "USB";
    public final static String TYPE_UNKNOWN = "unbekannt";

    public final static String WRITE_NONE = "none";
    public final static String WRITE_READONLY = "readonly";
    public final static String WRITE_APPONLY = "apponly";
    public final static String WRITE_FULL = "readwrite";

    private static Device[] devices, externalstorage, storage;
    private static BroadcastReceiver receiver;
    private static boolean useReceiver = true;
    private static String userDir;

    public static Device[] getDevices(Context context) {
        if (devices == null)
            initDevices(context);
        return devices;
    }


    public static Device[] getExternalStorage(Context context) {
        if (devices == null)
            initDevices(context);
        return externalstorage;
    }


    public static Device[] getStorage(Context context) {
        if (devices == null)
            initDevices(context);
        return storage;
    }


    public static IntentFilter getRescanIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL); // rausgenommen
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED); // wieder eingesetzt
        filter.addAction(Intent.ACTION_MEDIA_REMOVED); // entnommen
        filter.addAction(Intent.ACTION_MEDIA_SHARED); // per USB am PC
        filter.addDataScheme("file");
        return filter;
    }


    public static void setUseReceiver(Context context, boolean use) {
        if (use && receiver == null) {
            // receiver einschalten
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "Storage " + intent.getAction() + "-" + intent.getData());
                    initDevices(context);
                }
            };
            context.registerReceiver(receiver, getRescanIntentFilter());
        } else if (!use && receiver != null) {
            // receiver ausschalten
            context.unregisterReceiver(receiver);
            receiver = null;
        }
        useReceiver = use;
    }

    public static void initDevices(Context context) {
        // Userverzeichnis
        if (userDir == null)
            userDir = "/Android/data/" + context.getPackageName();

        // Broadcast-Receiver
        setUseReceiver(context, useReceiver);


        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class c = sm.getClass();
        Object[] vols;
        try {
            Method m = c.getMethod("getVolumeList", null);

            vols = (Object[]) m.invoke(sm, null); // android.os.Storage.StorageVolume
            Device[] temp = new Device[vols.length];
            for (int i = 0; i < vols.length; i++)
                temp[i] = new Device(vols[i]);


            Device primary = null;
            for (Device d : temp)
                if (d.mPrimary)
                    primary = d;
            if (primary == null)
                for (Device d : temp)
                    if (!d.mRemovable) {
                        d.mPrimary = true;
                        primary = d;
                        break;
                    }
            if (primary == null) {
                primary = temp[0];
                primary.mPrimary = true;
            }

            // ExternalDirs setzen (nur das erzeugt diese Pfade unter Android ab
            // 4.4)
            File[] files = ContextCompat.getExternalFilesDirs(context, null);
            File[] caches = ContextCompat.getExternalCacheDirs(context);
            for (Device d : temp) {
                if (files != null)
                    for (File f : files)
                        if (f != null && f.getAbsolutePath().startsWith(d.getAbsolutePath()))
                            d.mFiles = f;
                if (caches != null)
                    for (File f : caches)
                        if (f != null && f.getAbsolutePath().startsWith(d.getAbsolutePath()))
                            d.mCache = f;
            }


            ArrayList<Device> tempDev = new ArrayList<Device>(10);
            ArrayList<Device> tempStor = new ArrayList<Device>(10);
            ArrayList<Device> tempExt = new ArrayList<Device>(10);
            for (Device d : temp) {
                tempDev.add(d);
                if (d.isAvailable()) {
                    tempExt.add(d);
                    tempStor.add(d);
                }
            }


            Device internal = new Device(context);
            tempStor.add(0, internal); // bei Storage-Alternativen immer
            if (!primary.mEmulated)
                tempDev.add(0, internal); // bei Devices nur wenn zusätzlich

            // temp in devices-Tabelle übernehmen
            devices = tempDev.toArray(new Device[tempDev.size()]);
            storage = tempStor.toArray(new Device[tempStor.size()]);
            externalstorage = tempExt.toArray(new Device[tempExt.size()]);
        } catch (Exception e) {
            // Fallback auf normale Android-Funktionen
            Log.e(TAG, "getVolumeList not found, fallback");
            // TODO ist noch bei keinem Testgerät vorgekommen
        }

    }


    public static class Device extends File {
        String mUserLabel, mUuid, mState, mWriteState, mType;
        boolean mPrimary, mRemovable, mEmulated, mAllowMassStorage;
        long mMaxFileSize;
        File mFiles, mCache;
        Device(Context context) {
            super(Environment.getDataDirectory().getAbsolutePath());
            mState = Environment.MEDIA_MOUNTED;
            mFiles = context.getFilesDir();
            mCache = context.getCacheDir();
            mType = TYPE_INTERNAL;
            mWriteState = WRITE_APPONLY;
        }

        @SuppressWarnings("NullArgumentToVariableArgMethod")
        Device(Object storage) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            super((String) storage.getClass().getMethod("getPath", null).invoke(storage, null));
            for (Method m : storage.getClass().getMethods()) {
                if (m.getName().equals("getUserLabel") && m.getParameterTypes().length == 0
                        && m.getReturnType() == String.class)
                    mUserLabel = (String) m.invoke(storage, null); // ab Android
                // 4.4
                if (m.getName().equals("getUuid") && m.getParameterTypes().length == 0
                        && m.getReturnType() == String.class)
                    mUuid = (String) m.invoke(storage, null); // ab Android 4.4
                if (m.getName().equals("getState") && m.getParameterTypes().length == 0
                        && m.getReturnType() == String.class)
                    mState = (String) m.invoke(storage, null); // ab Android 4.4
                if (m.getName().equals("isRemovable") && m.getParameterTypes().length == 0
                        && m.getReturnType() == boolean.class)
                    mRemovable = (Boolean) m.invoke(storage, null); // ab
                // Android
                // 4.0
                if (m.getName().equals("isPrimary") && m.getParameterTypes().length == 0
                        && m.getReturnType() == boolean.class)
                    mPrimary = (Boolean) m.invoke(storage, null); // ab Android
                // 4.2
                if (m.getName().equals("isEmulated") && m.getParameterTypes().length == 0
                        && m.getReturnType() == boolean.class)
                    mEmulated = (Boolean) m.invoke(storage, null); // ab Android
                // 4.0
                if (m.getName().equals("allowMassStorage") && m.getParameterTypes().length == 0
                        && m.getReturnType() == boolean.class)
                    mAllowMassStorage = (Boolean) m.invoke(storage, null); // ab
                // Android
                // 4.0
                if (m.getName().equals("getMaxFileSize") && m.getParameterTypes().length == 0
                        && m.getReturnType() == long.class)
                    mMaxFileSize = (Long) m.invoke(storage, null); // ab Android
                // 4.0
                // getDescription (ab 4.1 mit context) liefert keine sinnvollen
                // Werte
                // getPathFile (ab 4.2) liefert keine sinnvollen Werte
                // getMtpReserveSpace (ab 4.0) für diese Zwecke unwichtig
                // getStorageId (ab 4.0) für diese Zwecke unwichtig
            }
            if (mState == null)
                mState = getState();

            if (mPrimary)
                mType = TYPE_PRIMARY;
            else {
                String n = getAbsolutePath().toLowerCase();
                if (n.indexOf("sd") > 0)
                    mType = TYPE_SD;
                else if (n.indexOf("usb") > 0)
                    mType = TYPE_USB;
                else
                    mType = TYPE_UNKNOWN + " " + getAbsolutePath();
            }
        }

        /**
         * Liefert den Typ des Geräts zurück
         *
         * @return ein String mit TYPE_PRIMARY = Festspeicher, TYPE_SD =
         * SD-Karte (auch im Akkuschacht sitzende), TYPE_USB =
         * USB-Speicher, TYPE_INTERNAL = die bis Android 2.x vorhandene
         * /data-Partition, TYPE_UNKNOWN bei sonstigen, wozu wohl auch
         * speziell verschlüsselte Bereiche zählen; habe ich bisher
         * nur bei einem Note4 und Galaxy S5 mal gesehen und noch nicht
         * weiter untersucht.
         */
        public String getType() {
            return mType;
        }

        /**
         * Liefert die Zugriffsart zurück. Bis Android 4.3 hatte man
         * Vollzugriff auf alles, doch ab 4.4 gilt das für SD-Karten und
         * USB-Speicher nicht mehr -- da dürfen Apps zwar alles lesen, aber nur
         * noch auf ihr privates Verzeichnis schreiben. Inkonsequenterweise
         * erlaubt Google weiterhin den vollen Schreibzugriff auf den internen
         * Gerätespeicher.
         *
         * @return ein String, der die Zugriffsart beschreibt:
         * <ul>
         * <li>WRITE_NONE = nichtmal Lesezugriff, dürfte nur bei nicht
         * vorhandenen Devices passieren.</li>
         * <li>WRITE_READONLY = Device als readonly gemounted, ist
         * ungetestet, weil mir das noch nicht untergekommen ist.</li>
         * <li>WRITE_APPONLY = Android-4.4-Einschränkung,
         * Schreibzugriff nur aufs eigene Verzeichnis. Das kriegt man
         * dann über {@link #getCacheDir()} und {@link #getFilesDir()}
         * geliefert. Achja, eine etwaige alte /data-Partition hat auch
         * diese Access-Art, bzw. ist man da sogar noch
         * eingeschränkter, da man die Verzeichnisse anderer Apps nicht
         * lesen darf. Das überprüfe ich hier allerdings nicht
         * differenziert.</li>
         * <li>WRITE_FULL = Schreibzugriff in allen Verzeichnissen</li>
         * </ul>
         */
        public String getAccess() {
            if (mWriteState == null) {
                try {
                    mWriteState = WRITE_NONE;
                    File[] root = listFiles();
                    if (root == null || root.length == 0)
                        throw new IOException("root empty/unreadable");
                    mWriteState = WRITE_READONLY;
                    File t = File.createTempFile("jow", null, getFilesDir());
                    // noinspection ResultOfMethodCallIgnored
                    t.delete();
                    mWriteState = WRITE_APPONLY;
                    t = File.createTempFile("jow", null, this);
                    // noinspection ResultOfMethodCallIgnored
                    t.delete();
                    mWriteState = WRITE_FULL;
                } catch (IOException ignore) {
                    Log.v(TAG, "test " + getAbsolutePath() + " ->" + mWriteState + "<- " + ignore.getMessage());
                }
            }
            return mWriteState;
        }

        /**
         * Überprüft anhand {@link #getState()}, ob das Device vorhanden ist.
         * Garantiert noch keinen Schreibzugriff, das kann man per
         * {@link #getAccess()} erfahren.
         *
         * @return true, wenn Device vorhanden ist, im Allgemeinen hat man dann
         * auch Lesezugriff. Schreibzugriff nicht unbedingt. false, wenn
         * Device fehlt.
         */
        public boolean isAvailable() {
            String s = getState();
            return (Environment.MEDIA_MOUNTED.equals(s) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(s));
            // MEDIA_SHARED: als USB freigegeben; bitte Handy auf MTP umstellen
        }

        /**
         * Überprüft, ob das Device vorhanden ist. Wenn nur ja/nein gefordert
         * ist, liefert das {@link #isAvailable()} einfacher.
         * <p/>
         * Ab Android 4.4 wird das an
         * {@link Environment#getStorageState(File)}}
         * deligiert, ansonsten wird überprüft, ob das Verzeichnis lesbar ist
         * und ob die Größe größer Null ist.
         *
         * @return ein String wie in {@link Environment} definiert.
         * Im Allgemeinen bedeuten nur MEDIA_MOUNTED und
         * MEDIA_MOUNTED_READ_ONLY, dass das Device wirklich
         * funktioniert. Die anderen sind mehr oder weniger nur
         * Fehlerhinweise, z.B. MEDIA_SHARED = ist per USB an einen PC
         * weitergereicht und kann daher nicht gelesen werden.
         */
        @SuppressLint("NewApi")
        public String getState() {
            if (mRemovable || mState == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    // Android 5.0? Da gibts was neues
                    mState = Environment.getExternalStorageState(this);
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    // Android 4.4? Dann dort nachfragen
                    mState = Environment.getStorageState(this);
                else if (canRead() && getTotalSpace() > 0)
                    // lesbar und Größe vorhanden => gibt es
                    mState = Environment.MEDIA_MOUNTED;
                else if (mState == null || Environment.MEDIA_MOUNTED.equals(mState))
                    // nicht lesbar, keine Größe aber noch MOUNTED || oder
                    // ungesetzt => UNKNOWN
                    mState = EnvironmentCompat.MEDIA_UNKNOWN;
            }
            return mState;
        }

        public File getFilesDir() {
            if (mFiles == null) {
                mFiles = new File(this, userDir + "/files");
                if (!mFiles.isDirectory())
                    // noinspection ResultOfMethodCallIgnored
                    mFiles.mkdirs();
            }
            return mFiles;
        }

        public File getCacheDir() {
            if (mCache == null) {
                mCache = new File(this, userDir + "/cache");
                if (!mCache.isDirectory())
                    // noinspection ResultOfMethodCallIgnored
                    mCache.mkdirs();
            }
            return mCache;
        }

        /**
         * Liefert zurück, ob das der primäre Speicher ist. Das ist der, den
         * man auch bei
         * {@link Context#getExternalFilesDir(String)} bekommt.
         *
         * @return true beim primären externen Speicher, false bei weiteren
         * externen Speichern und bei einer etwaigen /data-Partition
         */
        public boolean isPrimary() {
            return mPrimary;
        }

        /**
         * Untersucht, ob das Device im Betrieb entnommen werden kann.
         *
         * @return true, wenn der Nutzer das Device jederzeit herausnehmen kann.
         * False, falls fest eingebaut oder nur nach Ausschalten
         * entnehmbar. True bedeutet also nicht, dass das Device auch
         * wirklich vorhanden ist, sondern das bekommt man auch für
         * SD-Slots im Akkuschacht.
         */
        public boolean isRemovable() {
            return mRemovable;
        }

        /**
         * Liefert zurück, ob es sich um einen unified Memory handelt. Das war
         * eine mit Android 3 eingeführte Speichertechnik. Hat nur interne
         * Bedeutung.
         *
         * @return true für moderne Geräte mit unified Memory, bei denen sich
         * die /data- Partition und der primäre Speicher eine echte
         * Partition teilen. False bei älteren Geräten; bedeutet, dass
         * die /data-Partition auch noch vorhanden ist und von
         * {@link #getDevices(Context)} am Index 1
         * geliefert wird.
         */
        public boolean isEmulated() {
            return mEmulated;
        }

        public boolean isAllowMassStorage() {
            return mAllowMassStorage;
        }

        public long getMaxFileSize() {
            return mMaxFileSize;
        }

        public String getUserLabel() {
            return mUserLabel;
        }

        public String getUuid() {
            return mUuid;
        }
    }
}
