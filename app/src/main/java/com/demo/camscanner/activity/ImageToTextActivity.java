package com.demo.camscanner.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.camscanner.main_utils.Constant;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.demo.camscanner.R;
import com.demo.camscanner.utils.AdsUtils;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.bouncycastle.i18n.TextBundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class ImageToTextActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ImageToTextActivity";
    protected ImageView iv_back;
    protected ImageView iv_copy_txt;
    private ImageView iv_preview_img;
    protected ImageView iv_rescan_img;
    protected ImageView iv_share_txt;

    private TessBaseAPI m_tess;


    public ProgressDialog progressDialog;

    public TextView tv_ocr_txt;
    private TextView tv_title;
    private AdView adView;


    private void copyFile() throws IOException {
        // work with assets folder
        AssetManager assMng = getAssets();
        InputStream is = assMng.open("tessdata/vie.traineddata");
        OutputStream os = new FileOutputStream(getFilesDir() +
                "/tessdata/vie.traineddata");
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
        is.close();
        os.flush();
        os.close();
    }

    private void prepareLanguageDir() throws IOException {
        File dir = new File( getFilesDir() + "/tessdata" );
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File trainedData = new File(getFilesDir() + "/tessdata/vie.traineddata");
        if (!trainedData.exists()) {
            copyFile();
        }
    }




        @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView( R.layout.activity_img_to_text);

        init();

        bindView();

    }

    private void init() {
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_rescan_img = (ImageView) findViewById(R.id.iv_rescan_img);
        iv_preview_img = (ImageView) findViewById(R.id.iv_preview_img);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_share_txt = (ImageView) findViewById(R.id.iv_share_txt);
        iv_copy_txt = (ImageView) findViewById(R.id.iv_copy_txt);
        tv_ocr_txt = (TextView) findViewById(R.id.tv_ocr_txt);

        adView  = findViewById(R.id.adView);

    }

    private void bindView() {
        tv_title.setText(getIntent().getStringExtra("group_name"));
          iv_preview_img.setImageBitmap(Constant.original);
        try {
               prepareLanguageDir();
            m_tess = new TessBaseAPI();
            m_tess.init( String.valueOf( getFilesDir() ), "vie");
        } catch (Exception e) {
            // Logging here
        }
        doOCR(Constant.original);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                return;
            case R.id.iv_copy_txt:
                       ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText(TextBundle.TEXT_ENTRY, tv_ocr_txt.getText().toString()));
                Toast.makeText(this, "Đã copy vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
                return;
            case R.id.iv_rescan_img:
                tv_ocr_txt.setText("");
                doOCR(Constant.original);
                return;
            case R.id.iv_share_txt:
                   Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("text/*");
                 intent.putExtra("android.intent.extra.SUBJECT", "OCR Text");
                intent.putExtra("android.intent.extra.TEXT", tv_ocr_txt.getText().toString());
                  startActivity(Intent.createChooser(intent, "Share text using"));
                return;
            default:
                return;
        }
    }

    private void doOCR(final Bitmap bitmap) {
        // tạo một dialog progress và hiển thị nó
        if (progressDialog == null) {
           progressDialog = ProgressDialog.show(this, "Processing", "Doing OCR...", true);
        } else {
            //nó sẽ chỉ cần hiển thị lại dialog đó
            progressDialog.show();
        }

//        new Thread( () -> {
//            TextRecognizer build = new TextRecognizer.Builder(getApplicationContext()).build();
//            if (!build.isOperational()) {
//                Log.e(ImageToTextActivity.TAG, "Detector dependencies not loaded yet");
//                return;
//            }
//            final SparseArray<TextBlock> detect = build.detect(new Frame.Builder().setBitmap(bitmap).build());
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (detect.size() != 0) {
//                        StringBuilder sb = new StringBuilder();
//                        for (int i = 0; i < detect.size(); i++) {
//                            sb.append(((TextBlock) detect.valueAt(i)).getValue());
//                            sb.append(" ");
//                        }
//                        tv_ocr_txt.setText(sb.toString());
//                    } else {
//                        tv_ocr_txt.setText("No Text Found...");
//                    }
//                    progressDialog.dismiss();
//                }
//            });
//        } ).start();
        new Thread( () -> {

            m_tess.setImage(bitmap);
             String text = m_tess.getUTF8Text();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(text)) {
                        // nó sẽ được hiển thị trên TextView tv_ocr_txt.
                        tv_ocr_txt.setText(text);
                    } else {
                        tv_ocr_txt.setText("No Text Found...");
                    }
                    progressDialog.dismiss();
                    // cho biết rằng quá trình phân tích văn bản đã hoàn tất.
                }
            });
        } ).start();
    }

}












