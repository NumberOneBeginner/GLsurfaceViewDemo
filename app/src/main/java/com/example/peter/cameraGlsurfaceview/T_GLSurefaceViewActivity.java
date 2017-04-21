package com.example.peter.cameraGlsurfaceview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Base64;
import android.view.View;

import com.example.peter.demoarf.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class T_GLSurefaceViewActivity extends Activity implements Camera.PreviewCallback,
    GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
        public static final String TAG = T_GLSurefaceViewActivity.class.getName();
        public static T_GLSurefaceViewActivity instance;
        private boolean isDebug, isBackCamera, isTiming, isROIDetect,
                isFaceProperty;
        private int printTime = 33;
        private GLSurfaceView mGlSurfaceView;
        private ICamera mICamera;
        private Camera mCamera;
        private DialogUtil mDialogUtil;
        // private TextView debugInfoText, debugPrinttext;
        HandlerThread mHandlerThread = new HandlerThread("s");
        Handler mHandler;
        private MediaRecorderUtil mediaRecorderUtil;
        private boolean isStartRecorder = false;
        private int min_face_size = 200;
        private int detection_interval = 25;
        private HashMap<String, Integer> resolutionMap;
    private SensorEventUtil sensorUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Screen.initialize(this);
        setContentView(R.layout.activity_t__glsureface_view);
        init();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startRecorder();
            }
        }, 1000);
        instance = this;
    }

    public Context getContext() {
        return this;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        if (android.os.Build.MODEL.equals("PLK-AL10"))
            printTime = 50;
//		rv_dialog=  (RelativeLayout) findViewById(R.id.rv_dialog);
        isDebug = getIntent().getBooleanExtra("isdebug", false);
        isFaceProperty = getIntent().getBooleanExtra("isFaceProperty", false);
//        isBackCamera = getIntent().getBooleanExtra("isBackCamera", false);
        isBackCamera = true;
        isStartRecorder = getIntent().getBooleanExtra("isStartRecorder", false);
        isROIDetect = getIntent().getBooleanExtra("ROIDetect", false);
        isTiming = getIntent().getBooleanExtra("isTiming", false);
        isTiming = true;
        min_face_size = getIntent().getIntExtra("faceSize", min_face_size);
        detection_interval = getIntent().getIntExtra("interval",
                detection_interval);
//        resolutionMap = (HashMap<String, Integer>) getIntent()
//                .getSerializableExtra("resolution");
        sensorUtil = new SensorEventUtil(this);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.opengl_layout_surfaceview);
        mGlSurfaceView.setEGLContextClientVersion(2);// 创建一个OpenGL ES 2.0
        // context
        mGlSurfaceView.setRenderer(this);// 设置渲染器进入gl
        // RENDERMODE_CONTINUOUSLY不停渲染
        // RENDERMODE_WHEN_DIRTY懒惰渲染，需要手动调用 glSurfaceView.requestRender() 才会进行更新
        mGlSurfaceView.setRenderMode(mGlSurfaceView.RENDERMODE_WHEN_DIRTY);// 设置渲染器模式
        mGlSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });
        mICamera = new ICamera();
        mDialogUtil = new DialogUtil(this);

    }

    /**
     * 开始录制
     */
    private void startRecorder() {
        if (isStartRecorder) {
            int Angle = 360 - mICamera.Angle;
            if (isBackCamera)
                Angle = 180 - mICamera.Angle;
            mediaRecorderUtil = new MediaRecorderUtil(this, mCamera,
                    mICamera.cameraWidth, mICamera.cameraHeight);
            isStartRecorder = mediaRecorderUtil.prepareVideoRecorder(Angle);
            if (isStartRecorder) {
                mediaRecorderUtil.start();
                mICamera.actionDetect(this);
            }
        }
    }

    /**
     * 自动对焦
     */
    private void autoFocus() {
        if (mCamera != null && isBackCamera) {
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            // parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(parameters);
            // mCamera.autoFocus(null);
        }
    }

    private int Angle;

    @Override
    protected void onResume() {
        super.onResume();
        ConUtil.acquireWakeLock(this);
        startTime = System.currentTimeMillis();

        mCamera = mICamera.openCamera(isBackCamera, this, resolutionMap);

        if (mCamera != null) {
            Angle = 360 - mICamera.Angle;
            if (isBackCamera)
                Angle = 360 - mICamera.Angle - 180;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mICamera.cameraId, cameraInfo);
        } else {
            mDialogUtil.showDialog("打开相机失败");
        }
    }

    public Bitmap decodeToBitMap(byte[] data, Camera _camera) {
        Camera.Size size = _camera.getParameters().getPreviewSize();
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width,
                    size.height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height),
                        80, stream);
                Bitmap bmp = BitmapFactory.decodeByteArray(
                        stream.toByteArray(), 0, stream.size());
                stream.close();
                return bmp;
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 画绿色框
     */
    private void drawShowRect() {
//        mPointsMatrix.vertexBuffers = OpenGLDrawRect.drawCenterShowRect(
//                isBackCamera, mICamera.cameraWidth, mICamera.cameraHeight,
//                roi_ratio);
    }

    public void getSensor(float x, float y, float z) {
        // if (mPointsMatrix != null) {
        // mPointsMatrix.bottomVertexBuffer =
        // OpenGLDrawRect.drawBottomShowRect(0.15f, 0, 0f, x, y, z,180);
        // }
    }

    boolean isSuccess = false;
    float confidence;
    float pitch, yaw, roll;
    long startTime;
    long get3DPosefaceTime_end = 0;

    @Override
    public void onPreviewFrame(final byte[] imgData, final Camera camera) {


    }

    @Override
    protected void onPause() {
        super.onPause();
        ConUtil.releaseWakeLock();
        if (mediaRecorderUtil != null) {
            mediaRecorderUtil.releaseMediaRecorder();
        }
        mICamera.closeCamera();
        mCamera = null;

        timeHandle.removeMessages(0);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        facepp.release();
    }

    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
           //在这里获取摄像头数据


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTextureID = OpenGLUtil.createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        // 这个接口就干了这么一件事，当有数据上来后会进到onFrameAvailable方法
        mSurface.setOnFrameAvailableListener(this);// 设置照相机有数据时进入
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix();
        mICamera.startPreview(mSurface);// 设置预览容器
        mICamera.actionDetect(this);
        if (isTiming) {
            timeHandle.sendEmptyMessageDelayed(0, printTime);
        }
        if (isROIDetect)
            drawShowRect();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mCamera != null) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // TODO Auto-generated method stub
                    autoFocus();
                    // mCamera.cancelAutoFocus();
                }
            });
        }

        // 设置画面的大小
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // Matrix.perspectiveM(mProjMatrix, 0, 0.382f, ratio, 3, 700);

    }

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        final long actionTime = System.currentTimeMillis();
        // Log.w("ceshi", "onDrawFrame===");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mCameraMatrix.draw(mtx);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        mPointsMatrix.draw(mMVPMatrix);
        if (isDebug) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long endTime = System.currentTimeMillis()
                            - actionTime;
                    // debugPrinttext.setText("printTime: " + endTime);
                }
            });
        }
        mSurface.updateTexImage();// 更新image，会调用onFrameAvailable方法
    }

    Handler timeHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    timeHandle.sendEmptyMessageDelayed(0, printTime);
                    break;
                case 1:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    break;
                case 2:
//                    mImageGo.setVisibility(View.GONE);
                    break;
            }
        }
    };
    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
