package stark.a.is.zhang.zxingtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import net.people.zxing.camera.CameraManager;
import net.people.zxing.decoding.CaptureActivityHandler;
import net.people.zxing.decoding.InactivityTimer;
import net.people.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;


/**
 * 扫一扫
 * Created by zh_xu on 2016/6/8.
 */
public class CaptureActivity extends AppCompatActivity implements Callback {

    private MediaPlayer mediaPlayer;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private CaptureActivityHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        initView();
    }

    private void initView() {
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        hasSurface = false;
        //初始化inactivityTimer(活动监控器)对象
        inactivityTimer = new InactivityTimer(this);
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder sufaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(sufaceHolder);
        } else {
            sufaceHolder.addCallback(this);
            //防止sdk8的设备初始化预览异常
            sufaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();//超时后重新操作
            handler = null;
        }
        CameraManager.get().closeDriver();//关闭摄像头
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initBeepSound() {
        // TODO Auto-generated method stub
        if (playBeep && mediaPlayer == null) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);//音乐回放即媒体音量
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);
            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                //装载指定音频资源，使用此方法不论程序调用openFd(String name)方法时指定打开哪个原始资源，mediaplayer将总是播放第一个原始资源
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                //准备音频
                mediaPlayer.prepare();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                mediaPlayer = null;
            }
        }
    }

    /**
     * 处理扫描效果
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        String resultStr = result.getText();
        playBeepSoundAndVibrate();
        Toast.makeText(this, "扫描成功: " + resultStr, Toast.LENGTH_SHORT).show();
    }


    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            //获取手机振动器
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);//指定振动的毫秒数
            }
        }
    }

    private void initCamera(SurfaceHolder sufaceHolder) {
        // TODO Auto-generated method stub
        try {
            CameraManager.get().openDriver(sufaceHolder); //二维码中相机处理功能
        } catch (IOException e) {
            displayFrameworkBugMessageAndExit();
            return;
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();//在调用系统相机‘拒绝’时提示
            return;
        }

        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("sorry");//当系统提示‘拒绝’后的提示信息
        builder.setPositiveButton("确认", new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        hasSurface = false;

    }

    //播放完成事件绑定事件监听器
    private final OnCompletionListener beepListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // TODO Auto-generated method stub
            mediaPlayer.seekTo(0);
        }
    };

    /**
     * Simple listener used to exit the app in a few cases.
     *
     * @author Sean Owen
     */
    public final class FinishListener
            implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener, Runnable {

        private final Activity activityToFinish;

        public FinishListener(Activity activityToFinish) {
            this.activityToFinish = activityToFinish;
        }

        public void onCancel(DialogInterface dialogInterface) {
            run();
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            run();
        }

        public void run() {
            activityToFinish.finish();
        }
    }

}

