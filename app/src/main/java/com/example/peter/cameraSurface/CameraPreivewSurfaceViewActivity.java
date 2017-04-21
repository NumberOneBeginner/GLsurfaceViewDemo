package com.example.peter.cameraSurface;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.peter.demoarf.R;

public class CameraPreivewSurfaceViewActivity extends Activity implements CameraInterface.CamOpenOverCallback {
    private static final String TAG = CameraPreivewSurfaceViewActivity.class.getName();
    CameraSurfaceView surfaceView = null;
    Button shutterBtn;
    float previewRate = -1f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread openThread = new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
//                CameraInterface.getInstance().doOpenCamera(CameraPreivewSurfaceViewActivity.this);
            }
        };
        openThread.start();
        setContentView(R.layout.activity_camera_preivew_surface_view);
        initUI();
        initViewParams();
        shutterBtn.setOnClickListener(new BtnListeners());

    }


    private void initUI(){
        surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
        shutterBtn = (Button)findViewById(R.id.btn_shutter);
    }
    private void initViewParams(){
        ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
        surfaceView.setLayoutParams(params);

        //手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
        ViewGroup.LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = DisplayUtil.dip2px(this, 80);
        p2.height = DisplayUtil.dip2px(this, 80);;
        shutterBtn.setLayoutParams(p2);

    }

    @Override
    public void cameraHasOpened() {
        // TODO Auto-generated method stub
        SurfaceHolder holder = surfaceView.getSurfaceHolder();
        if (holder!=null){
            CameraInterface.getInstance().doStartPreview(holder, previewRate);
        }

    }


    private class BtnListeners implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_shutter:
                    createPermissionDialog();
//                    CameraInterface.getInstance().doOpenCamera(CameraPreivewSurfaceViewActivity.this);
//                    CameraInterface.getInstance().doTakePicture();
                    break;
                default:break;
            }
        }

    }
    /**
     * Create a pop-up prompt after the camera permissions are not rejected
     */

    private void createPermissionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("shexiangtou ");
        builder.setMessage("qinggeiwo quanxian rangwo dakai");
        builder.setNegativeButton("seting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            }
        });
        builder.setPositiveButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
