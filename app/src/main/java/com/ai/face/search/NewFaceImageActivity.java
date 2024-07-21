package com.ai.face.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ai.face.base.baseImage.BaseImageCallBack;
import com.ai.face.base.baseImage.BaseImageDispose;
import com.ai.face.base.utils.DataConvertUtils;
import com.ai.face.base.view.CameraXFragment;
import com.ai.facesearch.demo.R;
import com.ai.facesearch.demo.databinding.ActivityNewFaceImageBinding;

import java.io.ByteArrayOutputStream;

/**
 * 1.人脸角度提示
 * 2.人脸完整度提示
 * 3.闭眼提示
 * 4.特征点遮挡提示（待开发）
 * 5.活体检测
 */
public class NewFaceImageActivity extends AppCompatActivity {
    private TextView tipsTextView;
    private BaseImageDispose baseImageDispose;
    private ActivityNewFaceImageBinding binding;

    private long index = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewFaceImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle("录入人脸照片");

        binding.back.setOnClickListener(v -> {
            this.finish();
        });


        baseImageDispose = new BaseImageDispose(getBaseContext(), new BaseImageCallBack() {
            @Override
            public void onCompleted(Bitmap bitmap) {
                countDownTimer.onFinish();
                runOnUiThread(() -> showConfirmDialog(bitmap));
            }

            @Override
            public void onProcessTips(int actionCode) {
                runOnUiThread(() -> {
                    switch (actionCode) {
                        case NO_FACE:
                            showTempTips("未检测到人脸");
                            break;
                        case MANY_FACE:
                            showTempTips("多张人脸出现");
                            break;
                        case SMALL_FACE:
                            showTempTips("请靠近一点");
                            break;
                        case AlIGN_FAILED:
                            showTempTips("图像校准失败");
                            break;
                    }
                });
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);

        // 1. Camera 的初始化。第一个参数0/1 指定前后摄像头； 第二个参数linearZoom [0.001f,1.0f] 指定焦距，默认0.1
        int cameraLens = sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 0));
        CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLens, 0.001f);

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            index++;
            if (index % 15 == 0) { //% 值调大一点 让流程慢一点
                baseImageDispose.dispose(DataConvertUtils.imageProxy2Bitmap(imageProxy, 10, false));
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_camerax, cameraXFragment).commit();

        tipsTextView=binding.tipsView;

    }


    private void showTempTips(String tips) {
        countDownTimer.cancel();
        tipsTextView.setText(tips);
        countDownTimer.start();
    }


    /**
     * 封装成一个Lib ，倒计时显示库
     */
    CountDownTimer countDownTimer = new CountDownTimer(1000L * 3, 1000L) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.e("countDownTimer", "onTick()" + millisUntilFinished);
        }

        @Override
        public void onFinish() {
            Log.e("countDownTimer", "onFinish()");
            runOnUiThread(() -> tipsTextView.setText(""));
        }
    };


    /**
     * 确认是否保存底图
     *
     * @param bitmap
     */
    private void showConfirmDialog(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(this, R.layout.dialog_confirm_base, null);

        //设置对话框布局
        dialog.setView(dialogView);
        dialog.setCanceledOnTouchOutside(false);
        ImageView basePreView = dialogView.findViewById(R.id.preview);

        basePreView.setImageBitmap(bitmap);

        Button btnOK = dialogView.findViewById(R.id.btn_ok);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.requestFocus();
        editText.setVisibility(View.VISIBLE);

        btnOK.setOnClickListener(v -> {

           if (!TextUtils.isEmpty(editText.getText().toString())) {
                dialog.dismiss();
                Intent intent = new Intent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapByte = baos.toByteArray();
                intent.putExtra("picture_data", bitmapByte);
                intent.putExtra("picture_name", editText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }else {
                Toast.makeText(getBaseContext(), "请输入人脸名称", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            index = 1;
            dialog.dismiss();
            //太快了，可以延迟一点重试
            baseImageDispose.retry();
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }
}
