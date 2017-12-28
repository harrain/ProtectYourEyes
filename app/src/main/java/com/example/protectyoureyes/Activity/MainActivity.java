package com.example.protectyoureyes.Activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.protectyoureyes.Constants;
import com.example.protectyoureyes.MyActivity;
import com.example.protectyoureyes.R;
import com.example.protectyoureyes.Service.LongRunningService;
import com.example.protectyoureyes.ServiceUtil;
import com.example.protectyoureyes.bean.GlobalData;
import com.example.protectyoureyes.card.Card;
import com.example.protectyoureyes.card.CardLayout;
import com.example.protectyoureyes.card.CardProvider;
import com.example.protectyoureyes.util.SharedPreferenceUtil;
import com.example.protectyoureyes.util.UiUtil;

import java.io.File;
import java.io.IOException;


public class MainActivity extends MyActivity implements View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private FloatingActionButton bt_start_inform;
    private FloatingActionButton bt_stop_inform;
    private TextView tv_inform_time;
    private TextView tv_interval_time;
    private TextView tv_inform_vibrate_type;
    private TextView tv_inform_title;
    private TextView tv_inform_content;
    private SharedPreferences prf;

    private boolean isBindedService = false;
    private final String TAG = "MainActivity";
    private Context mContext;
    private TextView formatTimeTv;
    private Intent intent;
    private CardLayout cardLayoutWorkTime;
    private CardLayout cardLayoutIntervalTime;
    private CardLayout cardLayoutVibrateType;
    private CardLayout cardLayoutInformTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        initToolbar(R.id.tb_main);
        //初始化界面
        //初始化drawerlayout
        //从系统获取信息存储到GlobalData的全局变量中
        InitView();
        initDrawerLayout();
        InitGlobalData();

        addWorkTimeCardLayout();
        addIntervalCardLayout();
        addVibrateCardLayout();
        addInformCardLayout();

        intent = new Intent(MainActivity.this, LongRunningService.class);


    }

    @Override
    protected void initToolbar(int id) {
        super.initToolbar(id);
        toolbar.setTitle(R.string.app_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_setting);
        }
    }


    private void InitView() {
        bt_start_inform = (FloatingActionButton) findViewById(R.id.bt_start_inform);
        bt_stop_inform = (FloatingActionButton) findViewById(R.id.bt_stop_inform);
        formatTimeTv = (TextView) findViewById(R.id.timeformat_tv);
        /*tv_inform_time=(TextView)findViewById(R.id.tv_inform_time);
        tv_interval_time = (TextView) findViewById(R.id.tv_interval_time);
        tv_inform_vibrate_type=(TextView)findViewById(R.id.tv_inform_vibrate_type);
        tv_inform_title=(TextView)findViewById(R.id.tv_inform_title);
        tv_inform_content=(TextView)findViewById(R.id.tv_inform_content);*/
        prf = getSharedPreferences("GlobalData", MODE_PRIVATE);

        bt_start_inform.setOnClickListener(this);
        bt_stop_inform.setOnClickListener(this);
        //设置不允许点击“关闭提示”按钮
        bt_stop_inform.setEnabled(false);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        refreshCards();
        Log.d(TAG, "onWindowFocusChanged: service running " + ServiceUtil.isServiceRunning(this, Constants.LONG_RUN_SERVICE_NAME));
        if (SharedPreferenceUtil.getInstance().isTiming() && ServiceUtil.isServiceRunning(this, Constants.LONG_RUN_SERVICE_NAME)) {
            /** 绑定已经运行的service，button状态跟随alarm状态 */
            startAlarmViewChanged();
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        if (Constants.TIMESET_CHANGED) {
            stopAlarmingMethod();
        }
    }

    private void refreshCards() {
        Card card = cardLayoutWorkTime.getCard();
        card.getProvider().setDescription(GlobalData.inform_time + getResources().getString(R.string.time_unit));
        cardLayoutWorkTime.build(card);

        Card interval = cardLayoutIntervalTime.getCard();
        interval.getProvider().setDescription(GlobalData.interval_time + getResources().getString(R.string.time_unit));
        cardLayoutIntervalTime.build(interval);

        Card vibrateType = cardLayoutVibrateType.getCard();
        vibrateType.getProvider().setDescription(GlobalData.vibrate_type_name[GlobalData.vibrate_type_number]);
        cardLayoutVibrateType.build(vibrateType);

        Card informTitle = cardLayoutInformTitle.getCard();
        informTitle.getProvider().setDescription(GlobalData.inform_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.bt_start_inform:
                Constants.TIMESET_CHANGED = false;//重置，时间设置触发的flag被解开，alarm相关的button显示恢复正常
                startAlarmingMethod();
                Toast.makeText(MainActivity.this, "提醒功能已经开启。\nAPP关闭了仍然能够提醒哦！", Toast.LENGTH_LONG).show();
                break;
            case R.id.bt_stop_inform:
                stopAlarmingMethod();
                Toast.makeText(MainActivity.this, "提醒功能已经关闭！", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void startAlarmingMethod() {
        SharedPreferenceUtil.getInstance().setTiming(true);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_WAIVE_PRIORITY);

        //当提示开启后 “开启提示”不可点击，“关闭提示”可以点击
        startAlarmViewChanged();
    }

    private void stopAlarmingMethod() {
        if (longRunningService != null && longRunningService.getCountDownTimer() != null) {
            longRunningService.getCountDownTimer().cancel();
            formatTimeTv.setText("");
        }

        SharedPreferenceUtil.getInstance().setTiming(false);
        if (isBindedService)   unbindService(serviceConnection);

        stopService(intent);
        bt_start_inform.setVisibility(View.VISIBLE);
        bt_start_inform.setEnabled(true);
        bt_stop_inform.setVisibility(View.GONE);
        bt_stop_inform.setEnabled(false);
    }

    private void startAlarmViewChanged() {
        bt_start_inform.setEnabled(false);
        bt_start_inform.setVisibility(View.GONE);
        bt_stop_inform.setVisibility(View.VISIBLE);
        bt_stop_inform.setEnabled(true);
    }

    private void addWorkTimeCardLayout() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        cardLayoutWorkTime = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card, null, false);
        cardLayoutWorkTime.build(createWorkTimeCard());
        ll.addView(cardLayoutWorkTime);
    }

    private Card createWorkTimeCard() {

        return new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_work)
                .setDescription(GlobalData.inform_time + getResources().getString(R.string.time_unit))
                .endConfig()
                .build();
    }

    private void addIntervalCardLayout() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.interval);

        cardLayoutIntervalTime = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card, null, false);
        cardLayoutIntervalTime.build(createIntervalCard());
        ll.addView(cardLayoutIntervalTime);
    }

    private Card createIntervalCard() {

        return new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_interval)
                .setDescription(GlobalData.interval_time + getResources().getString(R.string.time_unit))
                .endConfig()
                .build();
    }

    private void addVibrateCardLayout() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.vibrate);

        cardLayoutVibrateType = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card, null, false);
        cardLayoutVibrateType.build(createVibrateCard());

        ll.addView(cardLayoutVibrateType);
    }

    private Card createVibrateCard() {

        return new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_vibrate_type)
                .setDescription(GlobalData.vibrate_type_name[GlobalData.vibrate_type_number])
                .endConfig()
                .build();
    }

    private void addInformCardLayout() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.inform);

        cardLayoutInformTitle = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card, null, false);
        cardLayoutInformTitle.build(createInformCard());

        ll.addView(cardLayoutInformTitle);
    }

    private Card createInformCard() {

        return new Card.Builder(this)
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_text_card)
                .setTitle("提示内容：")
                .setDescription(GlobalData.inform_title)
                .endConfig()
                .build();
    }

    private void initDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawerLayout);

        mDrawerLayout.setDrawerListener(new DrawerListener() {//设置DrawerListener
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            //当产生抽屉滑动时slide
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //获取mDrawerLayout中的第一个子布局，也就是布局中的Relativelayt
                //获取抽屉的view
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;// [1,0]
//                Log.i(TAG,"slideOffset:"+slideOffset+"");
//                Log.i(TAG,"scale:"+scale+"");

                float rightScale = 0.8f + scale * 0.2f;//= 1-0.2*slideOffset ; 0.8*slideOffset也可以 [1,0.8]
//                Log.i(TAG,"rightScale:"+rightScale+"");
                if (drawerView.getTag().equals("LEFT")) {

                    float leftScale = 1 - 0.3f * scale;//[0.7,1]  = 0.7+0.3*slideOffset
//                    Log.i(TAG,"leftScale:"+leftScale+"");
                    //设置左边菜单滑动后的占据屏幕大小
                    mMenu.setScaleX(leftScale);
                    mMenu.setScaleY(leftScale);
                    //设置菜单透明度
                    float menuAlpha = 0.6f + 0.4f * (1 - scale);//= 0.6+0.4*slideOffset
//                    Log.i(TAG,"menuAlpha:"+menuAlpha+"");
                    mMenu.setAlpha(menuAlpha);

                    //设置内容界面水平和垂直方向偏转量
                    //在滑动时内容界面的宽度为 屏幕宽度减去菜单界面所占宽度
                    float translationX = mMenu.getMeasuredWidth() * (1 - scale);//= mMenu.getMeasuredWidth() * slideOffset
//                    Log.i(TAG,"translationX:"+translationX+"");
                    mContent.setTranslationX(translationX);
                    //设置内容界面操作无效（比如有button就会点击无效）
                    mContent.invalidate();
                    //设置右边菜单滑动后的占据屏幕大小
                    mContent.setScaleX(rightScale);
                    mContent.setScaleY(rightScale);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }
        });
    }

    //使用sharepreference读取信息并且设置到全局变量中
    private void InitGlobalData() {
        int inform_time = prf.getInt("inform_time", 55);
        int interval_time = prf.getInt("interval_time", 10);
        int vibrate_type_number = prf.getInt("vibrate_type_number", 0);
        String inform_title = prf.getString("inform_title", UiUtil.getString(R.string.inform_title));
        String inform_content = prf.getString("inform_content", UiUtil.getString(R.string.inform_content));
        String interval_title = prf.getString("interval_title", UiUtil.getString(R.string.interval_title));
        String interval_content = prf.getString("interval_content", UiUtil.getString(R.string.interval_content));
        GlobalData.inform_time = inform_time;
        GlobalData.interval_time = interval_time;
        GlobalData.vibrate_type_number = vibrate_type_number;
        GlobalData.inform_title = inform_title;
        GlobalData.inform_content = inform_content;
        GlobalData.interval_title = interval_title;
        GlobalData.interval_content = interval_content;

        //查看SD卡中是否有已存的提示图片，如果有就把它设为全局变量，如果没有就把默认的老鼠图片设为全局变量
        File outputImage = new File(Environment.getExternalStorageDirectory() + File.separator + "ProtectYourEyes",
                "output_image.jpg");
        try {
            if (outputImage.exists()) {
                GlobalData.imageUri = Uri.fromFile(outputImage);
                GlobalData.inform_bitmap = BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(GlobalData.imageUri));
            } else {
                Resources res = getResources();
                GlobalData.inform_bitmap = BitmapFactory.decodeResource(res, R.mipmap.little_robot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bindMyService(Context context) {
        Intent intent = new Intent(MainActivity.this, LongRunningService.class);
        context.bindService(intent, serviceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    private AlertDialog alertDialog;

    public void createAlertDialog() {
        if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            if (GlobalData.alarmType) {
                builder.setMessage(GlobalData.inform_title);
            } else {
                builder.setMessage(GlobalData.interval_title);
            }
            builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog = builder.create();
//            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设置了这句，在没获得悬浮窗权限之前无法显示窗体。不设置，在Activity中可以正常显示。
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private LongRunningService longRunningService;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "ServiceConnected");
            isBindedService = true;
            longRunningService = ((LongRunningService.MyBinder) iBinder).getService();
            longRunningService.setCountTimeShowListener(new LongRunningService.CountTimeShowListener() {
                @Override
                public void showFormatTime(String formatTime) {
                    formatTimeTv.setText(formatTime);
                }
            });
            longRunningService.setShowDialogListener(new LongRunningService.ShowInformDialogListener() {
                @Override
                public void onShowDialog() {
                    createAlertDialog();
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "ServiceDisconnected");
            isBindedService = false;
            longRunningService = null;
        }
    };

    @Override
    protected void onDestroy() {
        if (isBindedService) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
        Log.i(TAG, "destroy");
    }
}
