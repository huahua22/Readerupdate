package com.xwr.readerupdate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.van.uart.LastError;
import com.van.uart.UartManager;
import com.xwr.ymoden.Constants;
import com.xwr.ymoden.UartUtil;
import com.xwr.ymoden.YModem;
import com.xwr.ymoden.eventbus.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


  @BindView(R.id.et_file_show)
  EditText mEtFileShow;
  @BindView(R.id.btn_file_select)
  Button mBtnFileSelect;
  @BindView(R.id.tv_progress)
  TextView mTvProgress;
  @BindView(R.id.pb_progress)
  ProgressBar mPbProgress;
  @BindView(R.id.btn_stat_download)
  Button mBtnStatDownload;
  @BindView(R.id.tv_result)
  TextView mTvResult;

  private String mName = "ttyHSL1";
  private String mBaud = "115200";
  private UartManager mUartManager;
  private Handler mHandler = new Handler();
  private File mFirmwareFile;

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MessageEvent event) {
    String message = event.getMessage();
    if (mTvResult != null && mPbProgress != null) {
      if (message.startsWith("pro_")) {
        int oneItem = (int) (Math.ceil((double) 100 / Constants.sCountPro));
        int Count = Constants.sCurrentPro * oneItem;
        if (Count >= 100) {
          Count = 100;
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              try {
                Runtime.getRuntime().exec("reboot");
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }, 2000);
        }
        mPbProgress.setProgress(Count);
        mTvProgress.setText(Count + "%");
      } else {
        if (message.equals("...")) {
          mTvResult.append(message);
        } else {
          mTvResult.append(message + "\n");
        }
      }
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    EventBus.getDefault().register(this);
    initData();
  }


  private void initData() {
    closetty();
    //文件读写权限判断
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mUartManager == null) {
      if (mName.isEmpty()) {
        showToast("Devices name is Empty");
        mBtnStatDownload.setEnabled(false);
      } else {
        if (mBaud.isEmpty()) {
          showToast("Baud rate is Empty");
          mBtnStatDownload.setEnabled(false);
        } else {
          mBtnStatDownload.setEnabled(true);
          try {
            mUartManager = new UartManager();
            mUartManager.open(mName, UartUtil.getBaudRate(Integer.valueOf(mBaud)));
          } catch (LastError lastError) {
            Toast.makeText(this, lastError.toString(), Toast.LENGTH_SHORT).show();
            lastError.printStackTrace();
          }
        }
      }
    }
  }

  public void showToast(String str) {
    Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
  }


  @OnClick({R.id.btn_file_select, R.id.btn_stat_download})
  public void onViewClicked(View view) {
    switch (view.getId()) {
      case R.id.btn_file_select://选择文件
        selectFileDialog();
        break;
      case R.id.btn_stat_download://开始烧写
        startFlash();
        break;
    }
  }


  private void startFlash() {
    writeData();
    mTvResult.setText("");
    mPbProgress.setVisibility(View.VISIBLE);
    Constants.sCountPro = 0;
    Constants.sCountPro = 0;
    if (mFirmwareFile != null && mFirmwareFile.exists()) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          if (mUartManager != null) {
            try {
              if (mUartManager.isOpen()) {
                new YModem(mUartManager).send(mFirmwareFile);
              }
            } catch (IOException e) {
              Log.e("xwr", e.toString());
            }
          }
        }
      }).start();
    } else {
      showToast(getString(R.string.valid_file));
    }

  }

  /**
   * 发送开始升级指令,与单片机约定
   */
  private void writeData() {
    try {
      byte[] data = {(byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0x96, 0x69, 0x00, 0x06, 0x55, 0x50, 0x1A, 0x2A, 0x3A, 0x09};
      int ret = mUartManager.write(data, data.length);
      Log.d("xwr", "write:" + ret);
    } catch (LastError lastError) {
      lastError.printStackTrace();
    }
  }

  private void selectFileDialog() {
    DialogUtils.select_file(this, new DialogUtils.DialogSelection() {
      @Override
      public void onSelectedFilePaths(String[] files) {
        if (files.length == 1) {
          mEtFileShow.setText(files[0]);
          mFirmwareFile = new File(files[0]);
        }
      }
    });
  }

  /**
   * 开始升级前关闭系统服务的串口 通信 ,防止串口占用
   */
  private void closetty() {
    Intent intent = new Intent();
    intent.setAction("unistrong.intent.action.SHUTDOWN");
    intent.putExtra("shutdown_value", "close_uart");
    sendBroadcast(intent);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mUartManager != null) {
      mUartManager.close();
    }
    EventBus.getDefault().unregister(this);
  }
}
