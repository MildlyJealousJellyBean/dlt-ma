package com.keye.karthiksubraveti.keye;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static com.keye.karthiksubraveti.keye.MainActivity.TAG;

class FaceDetectionServiceReceiver extends ResultReceiver {
    //...
    private MainActivity mReceiver;
    public FaceDetectionServiceReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(MainActivity receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "Received results");
        if (mReceiver != null) {
            if(resultCode == FaceDetectionService.RESULT_CODE_FACE_RECOG_COMPLETE){
                ArrayList<String> labelArr = resultData.getStringArrayList("labels");
                Log.d(TAG, labelArr.toString());
                mReceiver.handleFaceRecognitionResults(labelArr);
            } else if(resultCode == FaceDetectionService.RESULT_CODE_ADD_PERSON_COMPLETE) {
                FaceDetectionService.startActionTrainClassifier(mReceiver, this);
            } else if(resultCode == FaceDetectionService.RESULT_CODE_BUILD_CLASSIFIER_COMPLETE) {
                Log.d(TAG, "New classifier built");
                mReceiver.handleClassifierBuildComplete();
                //FaceDetectionService.startActionStoreModel(mReceiver, this);
            } else if(resultCode == FaceDetectionService.RESULT_CODE_STORE_CLASSIFIER_COMPLETE) { ;
                Log.d(TAG, "New classifier built and stored successfully");

            } else if(resultCode == FaceDetectionService.RESULT_CODE_ERR) {
                Log.d(TAG, "erro");
                mReceiver.handleFaceRecognitionResults(null);
            }
        } else {
            Log.d(TAG, "mReceiver is null");
        }
    }
}

public class MainActivity extends AppCompatActivity {
    private CameraDevice mCamera;
    protected CameraCaptureSession mCaptureSession;
    protected CaptureRequest.Builder mPreviewCaptureRequestBuilder;

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int REQUEST_AUDIO_PERMISSION = 300;
    private HandlerThread mBackgroundThread, mFaceDetectThread;

    private Handler mBackgroundHandler;
    private TextureView textureView;
    private Size imageDimension;
    Size mJpegSize;
    static final String TAG = "KeyeMainActivity";

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 270);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 90);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);

    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            mCamera = camera;

            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            mCamera.close();
            mCamera = null;
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            mCamera.close();
            mCamera = null;
        }
    };

    private Button mClickButton;
    private String mCameraId;
    private ImageView mImageView;
    //private TextView mModeText;
    private File mAppDirectory;
    private Handler mFaceDetHandler;
    private File mWorkingDir;
    private FaceDetectionServiceReceiver mReceiver;
    private TextView mNotificationBox;


    private static final int PICK_IMAGE_MULTIPLE = 1;


    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    TrainingDataModel mTrainingModel;
    private TextView mRecordStatusTv, mImagePickerStatusTv;
    private Handler mAudioTimerHandler;
    private TextView mBackendStatusText;


    //    /** A safe way to get an instance of the Camera object. */
    public void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // get back facing camera
            for(final String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                //if(cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    mCameraId = cameraId;
                    break;
                }
            }
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            if (characteristics != null) {
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;

                Size[] jpegSizes = null;
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
                int width = 640;
                int height = 480;
                if (jpegSizes != null && 0 < jpegSizes.length) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                mJpegSize = new Size(width, height);
            } else {
                Log.d(TAG, "Couldn't determine camera characteristics ..exiting..");
                finish();
            }
            manager.openCamera(mCameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission",
                        Toast.LENGTH_LONG).show();
                finish();
            }


        }

    }

    protected void startBackgroundThreads() {
        mBackgroundThread = new HandlerThread("Background");
        mFaceDetectThread = new HandlerThread("Face_Detection_Background");
        mFaceDetectThread.start();
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mAudioTimerHandler = new Handler();
        mFaceDetHandler = new Handler(mFaceDetectThread.getLooper());
    }

    protected void stopBackgroundThreads() {
        Log.d(TAG, "Stopping Background thread");

        mBackgroundThread.quitSafely();
        mFaceDetectThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            if(mBackgroundHandler != null) {
                mBackgroundHandler.removeCallbacks(null);
                mBackgroundHandler = null;
            }
            if (mAudioTimerHandler != null) {
                mAudioTimerHandler.removeCallbacks(null);
                mAudioTimerHandler = null;
            }

            mFaceDetectThread.join();
            mFaceDetectThread = null;
            if(mFaceDetHandler != null) {
                mFaceDetHandler.removeCallbacks(null);
                mFaceDetHandler = null;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mCaptureSession.abortCaptures();
            mCaptureSession.close();
        } catch (CameraAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if(mCamera != null) {
            mCamera.close();
            mCamera = null;
        }
        stopBackgroundThreads();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThreads();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    protected void updatePreview() {
        Log.d(TAG, "setting repeating request");
        if(null == mCamera) {
            Log.e(TAG, "updatePreview error, return");
        }

        if(mBackgroundHandler == null) {
            Log.e(TAG, "ERROR BACKGROUND HANDLER IS NULL");
        }
        mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            mCaptureSession.capture(mPreviewCaptureRequestBuilder.build(), null,
                    mBackgroundHandler);
            mCaptureSession.setRepeatingRequest(mPreviewCaptureRequestBuilder.build(),
                    null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            mPreviewCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(surface);
            mCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    //The cameracreateCameraPreview is already closed
                    if (null == mCamera) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    mCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            System.exit(1);

        }
    }

    protected void imageCapture() {
        if(mCamera == null) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        Log.d( TAG, "Image Capture");
        try {
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    Log.d( TAG, "Saving Image");
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };

            ImageReader imageReader = ImageReader.newInstance(mJpegSize.getWidth(), mJpegSize.getHeight(), ImageFormat.JPEG, 1);
            
            imageReader.setOnImageAvailableListener( new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d(TAG, "New Image is Available");
                    Image image = null;
                    image = reader.acquireNextImage();

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[buffer.capacity()];
                    buffer.get(imageBytes);
                    File mSessionDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Keye/working/" +
                            Calendar.getInstance().getTime().hashCode());
                    mSessionDir.mkdirs();
                    
                    final File file = new File(mSessionDir.getAbsolutePath() +"/pic.jpg");
                    mFaceDetHandler.post(new Runnable() {
                        private void save(byte[] bytes) throws IOException {
                            OutputStream output = null;
                            try {
                                output = new FileOutputStream(file);
                                output.write(bytes);
                            } finally {
                                if (null != output) {
                                    output.close();
                                }
                            }
                        }

                        @Override
                        public void run() {
                            //try {
                            try {
                                Log.d(TAG, "Sending Image to face bitmap recognition");
//                                Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes,0, imageBytes.length);
//                                Matrix matrix = new Matrix();
//                                matrix.postRotate(270);
//                                Bitmap rotatedImg = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                                rotatedImg.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                                byte[] compressedByteArray = stream.toByteArray();
//                                stream.close();
//                                bmp.recycle();
//                                rotatedImg.recycle();
                                //save(compressedByteArray);
                                String fn = file.getAbsolutePath();
                                fn = fn.substring(0, fn.lastIndexOf('.'));
                                save(imageBytes);
//                                FaceDetectionService.startActionRecognizeFaceBitmap(
//                                MainActivity.this, compressedByteArray, mReceiver);

                                FaceDetectionService.startActionRecognizeFace(
                                    MainActivity.this, file.getAbsolutePath(), fn, mReceiver);
                                Log.d(TAG, "Finish sending Image to face bitmap recognition");


                            } catch (IOException e) {
                               e.printStackTrace();
                            }
                        }
                    });
                    buffer.clear();
                    image.close();
                    reader.close();
                }
            }, mBackgroundHandler);



            ArrayList<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imageReader.getSurface());
            //outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder stillCaptureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            stillCaptureRequestBuilder.addTarget(imageReader.getSurface());
            stillCaptureRequestBuilder.set(CaptureRequest.JPEG_QUALITY, (byte)1);
            stillCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            stillCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


            mCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        Log.d( TAG, "Image Capture Session Configured");
                        session.capture(stillCaptureRequestBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.d( TAG, "Image Capture Session Configured failed");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    void doSetup() {
        mAppDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Keye");
        mAppDirectory.mkdirs();
        mWorkingDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Keye/working");
        mWorkingDir.mkdirs();
        mReceiver = new FaceDetectionServiceReceiver(new Handler());
        mReceiver.setReceiver(this);
        mTrainingModel = new TrainingDataModel();
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_activity_menu, menu);
//        return true;
//    }
//    public boolean onOptionsItemSelected(MenuItem item) {
//        //respond to menu item selection
//        switch (item.getItemId()) {
//            case R.id.settings:
//                // Start NewActivity.class
//                Intent myIntent = new Intent(MainActivity.this,
//                        StatusActivity.class);
//                startActivity(myIntent);
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//
//    }
    Button mRecordButton, mPlayButton, mImagePickButton, mSubmitButton;
    EditText mNameEdit;

    void initializeAddTrainingDataComponents() {
        // add a training classifier
        FaceDetectionService.startActionTrainClassifier(this, mReceiver);
        mNameEdit = (EditText)findViewById(R.id.NAME_ID);
        mRecordStatusTv = (TextView)findViewById(R.id.RECORD_BUTTON_STATUS_ID);
        mImagePickerStatusTv = (TextView)findViewById(R.id.IMAGE_PICKER_STATUS_ID);
        mImagePickButton = findViewById(R.id.IMAGE_PICK_BUTTON_ID);
        mRecordButton = findViewById(R.id.RECORD_NAME_ID);
        mPlayButton = findViewById(R.id.PLAY_NAME_ID);
        mSubmitButton  = findViewById(R.id.SUBMIT_ID);
        mImagePickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/jpg");
                //intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // TODO - select only image gallery
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Pictures"), PICK_IMAGE_MULTIPLE);

            }
        });

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecordButton.setClickable(false);
                recordAudio();
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayButton.setClickable(false);
                playAudio();
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrainingData();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        doSetup();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(textureListener);
        mNotificationBox = findViewById(R.id.OUTPUT_TEXT_ID);
        mNotificationBox.setText(R.string.imagePreview);
        mBackendStatusText = findViewById(R.id.BACKEND_STATUS_ID);
        initializeAddTrainingDataComponents();


    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    void handleDetectButton() {
        Log.d(TAG, "handleDetectMode");
        mNotificationBox.setVisibility(View.VISIBLE);
        mNotificationBox.setText(R.string.waiting_for_results);
        if(!mClassifierAvailable) {
            Toast.makeText(this, "Classifier not built yet", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Capturing Image", Toast.LENGTH_SHORT).show();
        }
        imageCapture();
    }
    boolean mClassifierAvailable;
    public void handleClassifierBuildComplete() {
        mClassifierAvailable = true;
        mBackendStatusText.setText("New Classifier Built at " + Calendar.getInstance().getTime());
    }

    public void handleFaceRecognitionResults(ArrayList<String> labelArr) {
        Toast.makeText(this, "Recognition Finished", Toast.LENGTH_SHORT).show();
        if(labelArr == null) {
            mNotificationBox.setVisibility(View.VISIBLE);
            mNotificationBox.setText(R.string.label_not_found);
        } else {
            StringBuilder nameList = new StringBuilder();
            for(int i = 0; i < labelArr.size(); i++) {
                TrainingDataModel m = (new Gson()).fromJson(labelArr.get(i), TrainingDataModel.class);
                if(i == labelArr.size()-1) {
                    nameList.append(m.name);
                } else {
                    nameList.append(m.name);
                    nameList.append(" , ");
                }

                if(!m.audioLabel.equals("")){
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(m.audioLabel);
                        mPlayer.prepare();
                        if (!mPlayer.isPlaying()) {
                            mPlayer.start();
                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                public void onCompletion(MediaPlayer mp) {
                                    mp.release();

                                };
                            });
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "prepare() failed");
                    }
                }
            }

            mNotificationBox.setVisibility(View.VISIBLE);
            mNotificationBox.setText(nameList);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handleDetectButton();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /***** SAVE TRAINING DATA *********/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                if(data.getData()!=null){
                    mTrainingModel.imageList.clear();
                    Uri mImageUri=data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imageEncoded  = cursor.getString(columnIndex);
                    Log.d(TAG, "Image Encoded :" + imageEncoded);
                    mTrainingModel.imageList.add(imageEncoded);
                    cursor.close();
                } else {
                    mTrainingModel.imageList.clear();
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String imageEncoded  = cursor.getString(columnIndex);
                            mTrainingModel.imageList.add(imageEncoded);
                            Log.d(TAG, "Clip Image Encoded :"+ imageEncoded);
                            cursor.close();

                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
                mImagePickerStatusTv.setText(mTrainingModel.imageList.size() + " pictures selected");
            } else {
                mImagePickerStatusTv.setText("no images selected");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    void playAudio() {
        mAudioTimerHandler.post(new Runnable() {
            private boolean playMode;

            @Override
            public void run() {
                if (!playMode) {
                    playMode = true;
                    Toast.makeText(MainActivity.this, "playing", Toast.LENGTH_SHORT).show();
                    onPlay(true);
                    mAudioTimerHandler.postDelayed(this, 2000);
                } else {
                    onPlay(false);
                    playMode = false;
                    mPlayButton.setClickable(true);
                }
            }
        });
    }

    void recordAudio() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
            return;
        }

        mTrainingModel.setAudioLabel(getCacheDir().getAbsolutePath() + "/label" +
                Calendar.getInstance().getTime().hashCode() + ".3gp");
        mAudioTimerHandler.post(new Runnable() {
            private boolean recordMode;
            @Override
            public void run() {
                if (!recordMode) {
                    recordMode = true;
                    Toast.makeText(MainActivity.this, "recording", Toast.LENGTH_SHORT).show();
                    onRecord(true);
                    mAudioTimerHandler.postDelayed(this, 2000);
                } else {
                    mRecordStatusTv.setText("new audio label stored at " + Calendar.getInstance().getTime());
                    mRecordButton.setClickable(true);
                    onRecord(false);
                    recordMode = false;
                }
            }
        });
    }

    void saveTrainingData() {
        if(mNameEdit.getText().toString().equals("") || mTrainingModel.imageList.size() == 0) {
            if(mNameEdit.getText().equals("")) {
                Toast.makeText(this, "Name field cannot be empty",
                        Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Images selected cannot be empty",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }
        String personName = mNameEdit.getText().toString();
        mTrainingModel.setName(personName);
        mTrainingModel.setAudioLabel(mTrainingModel.audioLabel);
        FaceDetectionService.startActionAddPerson(this, mTrainingModel, mReceiver);
        Toast.makeText(this, "Saving Training Information", Toast.LENGTH_LONG).show();
        clearTrainingData();
    }

    void clearTrainingData() {
        mTrainingModel.clear();
        mNameEdit.setText("");
        mImagePickerStatusTv.setText(R.string.no_images_selected);
        mRecordStatusTv.setText(R.string.no_audio_label_recorded_yet);
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            if(mTrainingModel.audioLabel.equals("")) {
                Log.d(TAG, "audio label empty");
                return;
            }
            mPlayer.setDataSource(mTrainingModel.audioLabel);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mTrainingModel.audioLabel);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }
}