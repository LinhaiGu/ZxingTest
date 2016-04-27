package com.example.zxingtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zxingtest.utils.DensityUtil;
import com.example.zxingtest.zxing.activity.CaptureActivity;
import com.example.zxingtest.zxing.encoding.EncodingUtils;

public class MainActivity extends Activity {

	private Button btn_open;
	private Button btn_create;

	private ImageView iv_zxing;

	private TextView tv_result;

	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initEvent();

	}

	private void initViews() {
		btn_open = (Button) findViewById(R.id.btn_open);
		btn_create = (Button) findViewById(R.id.btn_create);

		iv_zxing = (ImageView) findViewById(R.id.iv_zxing);

		tv_result = (TextView) findViewById(R.id.tv_result);
	}

	private void initEvent() {
		btn_open.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				open();
			}
		});

		btn_create.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						create();
					}
				});

			}
		});
	}

	/**
	 * 打开二维码扫描
	 */
	private void open() {
		config();
		startActivityForResult(new Intent(MainActivity.this,
				CaptureActivity.class), 0);
	}

	/**
	 * 创建二维码并将图片保存在本地
	 */
	private void create() {
		int width = DensityUtil.dip2px(this, 200);
		Bitmap bitmap = EncodingUtils.createQRCode("http://www.baidu.com",
				width, width, BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_launcher));
		iv_zxing.setImageBitmap(bitmap);
		saveBitmap(bitmap);
	}

	/**
	 * 将Bitmap保存在本地
	 * 
	 * @param bitmap
	 */
	public void saveBitmap(Bitmap bitmap) {
		// 首先保存图片
		File appDir = new File(Environment.getExternalStorageDirectory(),
				"zxing_image");
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		String fileName = "zxing_image" + ".jpg";
		File file = new File(appDir, fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 把文件插入到系统图库
		try {
			MediaStore.Images.Media.insertImage(this.getContentResolver(),
					file.getAbsolutePath(), fileName, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 通知图库更新
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				Uri.parse("file://" + "/sdcard/namecard/")));
	}

	/**
	 * 提高屏幕亮度
	 */
	private void config() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 1.0f;
		getWindow().setAttributes(lp);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			String result = bundle.getString("result");
			tv_result.setText(result);
		}
	}

}
