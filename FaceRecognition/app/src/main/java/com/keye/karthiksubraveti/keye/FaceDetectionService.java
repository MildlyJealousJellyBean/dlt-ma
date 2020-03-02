package com.keye.karthiksubraveti.keye;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.provider.BaseColumns;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class TrainingDataContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private TrainingDataContract() {}

    /* Inner class that defines the table contents */
    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_KEY = "name";
        public static final String COLUMN_AUDIO_LABEL = "audio_label";
        public static final String COLUMN_IMAGE_LIST_JSON = "image_list";
    }
}

class TrainingDataDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "TrainingData.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TrainingDataContract.Entry.TABLE_NAME + " (" +
                    TrainingDataContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    TrainingDataContract.Entry.COLUMN_NAME_KEY + " TEXT UNIQUE," +
                    TrainingDataContract.Entry.COLUMN_AUDIO_LABEL + " TEXT," +
                    TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TrainingDataContract.Entry.TABLE_NAME;
    

    public TrainingDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FaceDetectionService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_ADD_PERSON = "com.keye.karthiksubraveti.keye.action.ADDPERSON";
    private static final String ACTION_TRAIN_CLASSIFIER = "com.keye.karthiksubraveti.keye.action.TRAINCLASSIFIER";
    private static final String ACTION_STORE_MODEL = "com.keye.karthiksubraveti.keye.action.STOREMODEL";
    private static final String ACTION_RECOGNIZE_FACE = "com.keye.karthiksubraveti.keye.action.RECOGNIZEFACE";
    private static final String ACTION_RECOGNIZE_FACE_BITMAP = "com.keye.karthiksubraveti.keye.action.RECOGNIZEFACEBMP";


    // TODO: Rename parameters
    static final String TAG = "FaceDetectionService";
    static FaceDet sfaceDet;
    static File sAppDirectory;
    public static int RESULT_CODE_ERR = 0;
    public static int RESULT_CODE_ADD_PERSON_COMPLETE = 1;
    public static int RESULT_CODE_BUILD_CLASSIFIER_COMPLETE = 2;
    public static int RESULT_CODE_STORE_CLASSIFIER_COMPLETE = 3;
    public static int RESULT_CODE_FACE_RECOG_COMPLETE = 4;

    TrainingDataDbHelper mDbHelper;

    public FaceDetectionService() {
        super("FaceDetectionService");
    }
    static boolean modelsInitialized;
    boolean initialized;
    public static String face_recognition_model_filename = "dlib_face_recognition_resnet_model_v1.dat";
    public static String predictor_model_filename = "shape_predictor_68_face_landmarks.dat";

    public static void copyFileFromRawToAppDir(Context context) {
        InputStream in = null;
        File f = new File(getFaceShapeModelPath());
        if (!f.exists()) {
            try {
                in = context.getAssets().open(predictor_model_filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(getFaceShapeModelPath());
                byte[] buff = new byte[1024];
                int read = 0;
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        in = null;
        f = new File(getFaceRecogModelPath());
        if(f.exists()) {
            try {
                in = context.getAssets().open(face_recognition_model_filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileOutputStream out = null;
                out = new FileOutputStream(getFaceRecogModelPath());
                byte[] buff = new byte[1024];
                int read = 0;
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    static String getFaceRecogModelPath() {
        return sAppDirectory + "/models/" + face_recognition_model_filename;

    }

    static String getFaceShapeModelPath() {
        return sAppDirectory + "/models/" + predictor_model_filename;
    }

    public static void doSetup(Context context) {
        if(modelsInitialized) {
           return;
        }

        Log.d(TAG, "XXXX FACE DETECTION");
        sAppDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Keye");
        sAppDirectory.mkdirs();

        new File(sAppDirectory.getPath() + "/models").mkdirs();

        copyFileFromRawToAppDir(context);
        sfaceDet = new FaceDet(getFaceShapeModelPath(), getFaceRecogModelPath());
        modelsInitialized = true;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionAddPerson(Context context, TrainingDataModel model,
                                            ResultReceiver receiver) {
        Intent intent = new Intent(context, FaceDetectionService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("value", model);
        bundle.putParcelable("receiverTag", receiver);
        intent.putExtras(bundle);
        intent.setAction(ACTION_ADD_PERSON);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionTrainClassifier(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, FaceDetectionService.class);
        intent.setAction(ACTION_TRAIN_CLASSIFIER);
        Bundle bundle = new Bundle();
        bundle.putParcelable("receiverTag", receiver);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void startActionStoreModel(Context context, ResultReceiver receiver) {
        Intent intent = new Intent(context, FaceDetectionService.class);
        intent.setAction(ACTION_STORE_MODEL);
        Bundle bundle = new Bundle();
        bundle.putParcelable("receiverTag", receiver);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    public static void startActionRecognizeFace(Context context, String srcPath, String dstPath,
                                                ResultReceiver receiver) {
        Intent intent = new Intent(context, FaceDetectionService.class);
        Bundle bundle = new Bundle();
        bundle.putString("srcPath", srcPath);
        bundle.putString("dstPath", dstPath);
        bundle.putParcelable("receiverTag", receiver);
        intent.putExtras(bundle);
        intent.setAction(ACTION_RECOGNIZE_FACE);
        context.startService(intent);
    }

    public static void startActionRecognizeFaceBitmap(Context context, byte[] imageBytes,
                                                      ResultReceiver receiver) {
        Intent intent = new Intent(context, FaceDetectionService.class);
        Bundle bundle = new Bundle();
        bundle.putByteArray("imageBytes", imageBytes);
        bundle.putParcelable("receiverTag", receiver);
        intent.putExtras(bundle);
        intent.setAction(ACTION_RECOGNIZE_FACE_BITMAP);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if(!initialized) {
            mDbHelper = new TrainingDataDbHelper(this);
            initialized = true;
        }
        doSetup(this);
        if (intent != null) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            if(extras == null) {
                Log.e(TAG, "no bundle extras found for " + intent.getAction());
                return;
            }
            ResultReceiver receiver = extras.getParcelable("receiverTag");
            if (ACTION_ADD_PERSON.equals(action)) {
                TrainingDataModel model =  (TrainingDataModel)extras.getSerializable("value");
                handleActionAddPerson(model);
                receiver.send(RESULT_CODE_ADD_PERSON_COMPLETE, null);
            } else if (ACTION_TRAIN_CLASSIFIER.equals(action)) {
                handleActionTrainClassifier();
                receiver.send(RESULT_CODE_BUILD_CLASSIFIER_COMPLETE, null);
            } else if (ACTION_STORE_MODEL.equals(action)) {
                handleActionStoreModel();
                receiver.send(RESULT_CODE_STORE_CLASSIFIER_COMPLETE, null);
            } else if(ACTION_RECOGNIZE_FACE.equals(action)) {
                String srcPath = extras.getString("srcPath");
                int[] labelsArr = sfaceDet.recognizeFaceChips(srcPath);
                ArrayList<String> labelList = new ArrayList<>();
                for(int i = 0; i < labelsArr.length; ++i) {
                    TrainingDataModel model = getModelForID(labelsArr[i]);
                    Log.d(TAG, "Adding label " + labelsArr[i]);
                    labelList.add(new Gson().toJson(model));
                }
                Bundle b = new Bundle();
                b.putStringArrayList("labels", labelList);
                receiver.send(RESULT_CODE_FACE_RECOG_COMPLETE, b);
            } else if(ACTION_RECOGNIZE_FACE_BITMAP.equals(action)) {
                Log.d(TAG, "Face Recognition Bitmap");
                byte[] imageBytes = extras.getByteArray("imageBytes");
                if(imageBytes == null) {
                    Log.d(TAG, "compressed bytes is null");
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0,
                        imageBytes.length);

                String dstPath = extras.getString("dstPath");
                int[] labelsArr = sfaceDet.recognizeFaceChipsBitmap(bmp);
                ArrayList<String> labelList = new ArrayList<>();
                for(int i = 0; i < labelsArr.length; ++i) {
                    TrainingDataModel model = getModelForID(labelsArr[i]);
                    Log.d(TAG, "Adding label " + labelsArr[i]);
                    labelList.add(new Gson().toJson(model));
                }
                Bundle b = new Bundle();
                b.putStringArrayList("labels", labelList);
                receiver.send(RESULT_CODE_FACE_RECOG_COMPLETE, b);

            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
    private TrainingDataModel getModelForID(long id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                TrainingDataContract.Entry.COLUMN_NAME_KEY,
                TrainingDataContract.Entry.COLUMN_AUDIO_LABEL,
                TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON,
        };

        String selection = TrainingDataContract.Entry._ID + " = ?";
        String[] selectionArgs = { Long.toString(id) };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TrainingDataContract.Entry.COLUMN_NAME_KEY + " DESC";

        Cursor cursor = db.query(
                TrainingDataContract.Entry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        List itemIds = new ArrayList<>();
        String name = "";
        String audioLabel = "";
        while(cursor.moveToNext()) {
            name = cursor.getString(
                    cursor.getColumnIndexOrThrow(TrainingDataContract.Entry.COLUMN_NAME_KEY));
            audioLabel = cursor.getString(
                    cursor.getColumnIndexOrThrow(TrainingDataContract.Entry.COLUMN_AUDIO_LABEL));
            break;
        }
        cursor.close();
        db.close();
        TrainingDataModel m = new TrainingDataModel();
        m.setAudioLabel(audioLabel);
        m.setName(name);
        Log.d(TAG,"Training Model is " + m.name);
        Log.d(TAG,"Training Model is " + m.audioLabel);
        return m;
    }

    private TrainingDataModel getModelForName(String name) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                TrainingDataContract.Entry.COLUMN_NAME_KEY,
                TrainingDataContract.Entry.COLUMN_AUDIO_LABEL,
                TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON,
        };

        String selection = TrainingDataContract.Entry.COLUMN_NAME_KEY + " = ?";
        String[] selectionArgs = { name };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                TrainingDataContract.Entry.COLUMN_NAME_KEY + " DESC";

        Cursor cursor = db.query(
                TrainingDataContract.Entry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        List itemIds = new ArrayList<>();
        long id = -1;
        String audioLabel = "";
        String imageListJson = "";
        while(cursor.moveToNext()) {
            id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(TrainingDataContract.Entry._ID));
            audioLabel = cursor.getString(
                    cursor.getColumnIndexOrThrow(TrainingDataContract.Entry.COLUMN_AUDIO_LABEL));
            imageListJson = cursor.getString(
                    cursor.getColumnIndexOrThrow(TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON));
            break;
        }
        cursor.close();
        db.close();
        TrainingDataModel m = new TrainingDataModel();
        m.setAudioLabel(audioLabel);
        m.setName(name);
        m.setID(id);
        return m;
    }

    private long createPersonDbEntry(String name, String audioLabel, String imageList) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrainingDataContract.Entry.COLUMN_NAME_KEY, name);
        values.put(TrainingDataContract.Entry.COLUMN_AUDIO_LABEL, audioLabel);
        values.put(TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON, imageList);
        long modelID = db.insert(TrainingDataContract.Entry.TABLE_NAME, null, values);
        mDbHelper.close();
        return modelID;
    }

    private long updatePersonDbEntry(String name, String audioLabel, String imageList) {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TrainingDataContract.Entry.COLUMN_NAME_KEY, name);
        values.put(TrainingDataContract.Entry.COLUMN_AUDIO_LABEL, audioLabel);
        values.put(TrainingDataContract.Entry.COLUMN_IMAGE_LIST_JSON, imageList);
        long modelID = db.update(TrainingDataContract.Entry.TABLE_NAME, values,
                TrainingDataContract.Entry.COLUMN_NAME_KEY + "=" + name, null);
        mDbHelper.close();
        return modelID;
    }

    private void handleActionAddPerson(TrainingDataModel model) {
        Log.d(TAG, "handleActionAddPerson: " + model.imageList.toString());
        File audioFile = null;
        String audioLabel = "";
        model.name = model.name.toLowerCase();
        if (!model.audioLabel.equals("")){
            File audioCacheFile = new File(model.audioLabel);
            File audioDir = new File(sAppDirectory.getAbsolutePath() + "/Label/" + model.name);
            audioDir.mkdirs();
            String fn = audioCacheFile.getName();
            int idx = fn.lastIndexOf('.');
            audioFile = new File(audioDir.getAbsolutePath() + "/audioLabel" + fn.substring(idx));
            audioLabel = audioFile.getAbsolutePath();
            try {
                copy(audioCacheFile, audioFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TrainingDataModel existingModel = getModelForName(model.name);
        long modelID = existingModel.ID;
        ArrayList<String> imgList = new ArrayList<String>();
        if(modelID == -1) {
            // Insert the new row, returning the primary key value of the new row
            modelID = createPersonDbEntry(model.name, audioLabel,
                    new Gson().toJson(model.imageList));
            imgList.addAll(model.imageList);
        } else {
            Set<String> imageListHashSet = new HashSet<String>(existingModel.imageList);
            for(int i =0; i < model.imageList.size(); i++) {
                if(!imageListHashSet.contains(model.imageList.get(i))) {
                    imgList.add(model.imageList.get(i));
                }
            }
            existingModel.imageList.addAll(imgList);
            updatePersonDbEntry(model.name, audioLabel, new Gson().toJson(existingModel.imageList));
        }

        // save face chips
        File personDirectory = new File(sAppDirectory.getAbsolutePath() + "/Train/" +
                Long.toString(modelID));
        personDirectory.mkdirs();
        for(String imgFn : imgList) {
            String imgDst = personDirectory.getAbsolutePath() + "/" +
                imgFn.substring(imgFn.lastIndexOf('/') + 1, imgFn.lastIndexOf('.'));
            sfaceDet.saveFaceChips(imgFn, imgDst);
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionTrainClassifier() {
        Log.d(TAG, "handleActionTrainClassifier");
        File trainDir = new File(sAppDirectory.getAbsolutePath() + "/Train");
        sfaceDet.trainClassifier(trainDir.getPath());
    }

    private void handleActionStoreModel() {
        Log.d(TAG, "handleActionStoreModel");
        File trainDir = new File(sAppDirectory.getAbsolutePath() + "/Train");
        sfaceDet.storeModel(trainDir.getPath());
    }
}
