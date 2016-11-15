package im.tiki.sample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.RendererCommon;

import im.facechat.Rtc;
import im.facechat.view.FCSurfaceView;
import im.facechat.view.PercentFrameLayout;

public class MainActivity extends AppCompatActivity implements Rtc.FCRoomEvent {

    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 10;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    private PercentFrameLayout mLocalSurfaceLayout;
    private FCSurfaceView mLocalSurface;
    private PercentFrameLayout mRemoteSurfaceLayout;
    private FCSurfaceView mRemoteSurface;
    private ImageView mActionRoom;
    private ImageView mActionCamera;
    private TextView mExitRoom;
    private TextView mActionSwitch;
    private View mRemoteMask;

    private boolean mOpenCamera;
    private String mRoomId;
    private boolean mCallConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocalSurfaceLayout = (PercentFrameLayout) findViewById(R.id.local_surface_layout);
        mLocalSurface = (FCSurfaceView) findViewById(R.id.local_surface);
        mRemoteSurfaceLayout = (PercentFrameLayout) findViewById(R.id.remote_surface_layout);
        mRemoteSurface = (FCSurfaceView) findViewById(R.id.remote_surface);
        mActionRoom = (ImageView) findViewById(R.id.action_room);
        mActionCamera = (ImageView) findViewById(R.id.action_camera);
        mExitRoom = (TextView) findViewById(R.id.exit_room);
        mActionSwitch = (TextView) findViewById(R.id.action_switch);
        mRemoteMask = findViewById(R.id.mask);

        initView();
        bindListener();

    }

    private void initView() {
        mLocalSurface.setZOrderMediaOverlay(true);

        updateVideoView();

    }


    //绑定控件事件
    private void bindListener() {
        Rtc.initialize(getApplication());
        Rtc.registerTokenCallback(new Rtc.FCInitializeEvent() {
            @Override
            public void onToken(final String token) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "获得token=" + token, Toast.LENGTH_SHORT).show();
                        Rtc.openCamera(MainActivity.this, mLocalSurface, mRemoteSurface);
                    }
                });
            }
        });

        mActionRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRoomDialog();
            }
        });

        mActionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rtc.switchCamera(mLocalSurface);
            }
        });

        mActionCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitRoom();
            }
        });

        Rtc.registerRoomEvent(this);
    }

    private void showRoomDialog() {
        final EditText editText = new EditText(this);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setMaxLines(1);
        editText.setSingleLine(true);
        new AlertDialog.Builder(this)
                .setTitle("输入Room ID")
                .setView(editText)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString().replace(" ", "").replace("\n", "");
                        enterRoom(text);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void showExitRoomDialog() {
        new AlertDialog.Builder(this)
                .setMessage("退出当前Room ID？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exitRoom();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void enterRoom(String roomId) {
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(MainActivity.this, "请输入合法的Room ID", Toast.LENGTH_LONG).show();
            return;
        }
        mRoomId = roomId;
        Rtc.joinRoom(roomId, "");

        mExitRoom.setText(String.valueOf(roomId));
        mExitRoom.setVisibility(View.VISIBLE);
        mActionRoom.setVisibility(View.GONE);

        mActionCamera.setVisibility(View.GONE);

    }

    private void exitRoom() {
        Rtc.leaveRoom(mRemoteSurface, mRoomId, "");

        mExitRoom.setVisibility(View.GONE);
        mActionRoom.setVisibility(View.VISIBLE);

        mCallConnected = false;
        updateVideoView();
    }

    /**
     * 收到加入房间消息
     *
     * @param roomId  房间ID
     * @param payload 额外信息
     */
    @Override
    public void onJoinRoom(final String roomId, String payload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "xxx加入房间 roomId=" + roomId, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 收到离开房间消息
     *
     * @param roomId  房间ID
     * @param payload 额外信息
     */
    @Override
    public void onLeaveRoom(final String roomId, String payload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exitRoom();
                Toast.makeText(MainActivity.this, "对方已离开房间 roomId=" + roomId, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 收到房间消息
     *
     * @param roomId  房间ID
     * @param payload 额外信息
     */
    @Override
    public void onRoomMessage(final String roomId, final String payload) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "roomId=" + roomId + " 收到消息:" + payload, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 房间会话消息
     *
     * @param roomId  房间Id
     * @param session 会话Id
     */
    @Override
    public void onRoomSession(String roomId, String session) {

    }

    /**
     * 网络状况不稳定
     */
    @Override
    public void onNetworkUnstable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "网络不稳定", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 错误回调
     *
     * @param code     错误代码
     * @param errorMsg 错误信息
     */
    @Override
    public void onError(final int code, final String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "错误码:" + code + " 错误信息:" + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 收到视频讯号
     */
    @Override
    public void onCallConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallConnected = true;
                updateVideoView();
            }
        });

    }

    /**
     * 视频讯号断开
     */
    @Override
    public void onCallDisconnected() {

    }

    private void updateVideoView() {
        if (mCallConnected) {
            mRemoteMask.setVisibility(View.GONE);
            mActionCamera.setVisibility(View.VISIBLE);
        } else {
            mRemoteMask.setVisibility(View.VISIBLE);
            mActionCamera.setVisibility(View.GONE);
        }

        mRemoteSurfaceLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        mRemoteSurface.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteSurface.setMirror(false);

        mLocalSurfaceLayout.setPosition(LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
        mLocalSurface.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        mLocalSurface.requestLayout();
        mRemoteSurface.requestLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Rtc.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Rtc.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Rtc.unregisterRoomEvent(this);
        Rtc.shutdown();
    }
}
