package com.example.protectyoureyes.Activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.protectyoureyes.MyActivity;
import com.example.protectyoureyes.R;
import com.example.protectyoureyes.Service.LongRunningService;
import com.example.protectyoureyes.bean.GlobalData;
import com.example.protectyoureyes.card.Card;
import com.example.protectyoureyes.card.CardLayout;
import com.example.protectyoureyes.card.CardProvider;
import com.example.protectyoureyes.card.OnActionClickListener;
import com.example.protectyoureyes.card.action.TextViewAction;
import com.example.protectyoureyes.util.UiUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.protectyoureyes.R.id.ll;


public class MainActivity extends MyActivity implements View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private FloatingActionButton bt_start_inform;
    private FloatingActionButton bt_stop_inform;
    private TextView tv_inform_time;
    private TextView tv_interval_time;
    private TextView tv_inform_vibrate_type;
    private TextView tv_inform_title;
    private TextView tv_inform_content;
    private SharedPreferences prf;

    private FrameLayout mainFrame, frontFrame, backFrame;
    private AnimatorSet mRightOutSet;
    private AnimatorSet mLeftInSet;
    private boolean mIsShowBack = false;//判断哪面朝上的标志

    private boolean isBindedService = false;
    private final String TAG = "MainActivity" ;
    private Context mContext;
    private CardProvider workTimeProvider;
    private CardProvider vibrateProvider;
    private CardProvider workContentProvider;
    private CardProvider breakTimeProvider;
    private CardProvider breakContentProvider;

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
    }

    @Override
    protected void initToolbar(int id) {
        super.initToolbar(id);
        toolbar.setTitle(R.string.app_name);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_setting);
        }
    }


    private void InitView() {
        bt_start_inform=(FloatingActionButton) findViewById(R.id.bt_start_inform);
        bt_stop_inform=(FloatingActionButton)findViewById(R.id.bt_stop_inform);

        prf= getSharedPreferences("GlobalData",MODE_PRIVATE);

        bt_start_inform.setOnClickListener(this);
        bt_stop_inform.setOnClickListener(this);
        //设置不允许点击“关闭提示”按钮
        bt_stop_inform.setEnabled(false);

        mainFrame = (FrameLayout) findViewById(R.id.mainFrame);
        frontFrame = (FrameLayout) findViewById(R.id.frontFrame);
        backFrame = (FrameLayout) findViewById(R.id.backFrame);
        informContentFrame();

        addWorkTimeCardLayout();
        addBreakTimeCardLayout();
        addVibrateCardLayout();
        addWorkContentCardLayout();
        addBreakContentCardLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent(MainActivity.this, LongRunningService.class);

        switch(v.getId()){

            case R.id.bt_start_inform:
                startService(intent);
                bindService(intent,serviceConnection,Context.BIND_WAIVE_PRIORITY);
                isBindedService = true;
                //当提示开启后 “开启提示”不可点击，“关闭提示”可以点击
                bt_start_inform.setEnabled(false);
                bt_start_inform.setVisibility(View.GONE);
                bt_stop_inform.setVisibility(View.VISIBLE);
                bt_stop_inform.setEnabled(true);
                Toast.makeText(MainActivity.this, "提醒功能已经开启。\nAPP关闭了仍然能够提醒哦！", Toast.LENGTH_LONG).show();
                break;
            case R.id.bt_stop_inform:
                unbindService(serviceConnection);
                isBindedService = false;
                stopService(intent);
                bt_start_inform.setVisibility(View.VISIBLE);
                bt_start_inform.setEnabled(true);
                bt_stop_inform.setVisibility(View.GONE);
                bt_stop_inform.setEnabled(false);
                Toast.makeText(MainActivity.this, "提醒功能已经关闭！", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void informContentFrame(){
        backFrame.setVisibility(View.GONE);
        initAnimator();
        mainFrame.setClickable(true);
        mainFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //正面朝上
                if (!mIsShowBack) {
                    mRightOutSet.setTarget(frontFrame);
                    mLeftInSet.setTarget(backFrame);
                    mRightOutSet.start();
                    mLeftInSet.start();
                    mIsShowBack = true;

                } else { //背面朝上
                    mRightOutSet.setTarget(backFrame);
                    mLeftInSet.setTarget(frontFrame);
                    mRightOutSet.start();
                    mLeftInSet.start();
                    mIsShowBack = false;

                }
            }
        });
    }

    private void addWorkTimeCardLayout(){
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        CardLayout cardLayout = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card,null,false);
        cardLayout.build(createWorkTimeCard());

        ll.addView(cardLayout);
    }

    private Card createWorkTimeCard(){

        workTimeProvider = new Card.Builder(this).withProvider(new CardProvider());
        return workTimeProvider.setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_work)
                .setDescription(""+GlobalData.inform_time+getResources().getString(R.string.time_unit))
                .endConfig()
                .build();
    }

    private void addBreakTimeCardLayout(){
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll_break);

        CardLayout cardLayout = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card,null,false);
        cardLayout.build(createBreakTimeCard());

        ll.addView(cardLayout);
    }

    private Card createBreakTimeCard(){

        breakTimeProvider = new Card.Builder(this).withProvider(new CardProvider());
        return breakTimeProvider.setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_interval)
                .setDescription(""+GlobalData.interval_time+getResources().getString(R.string.time_unit))
                .endConfig()
                .build();
    }

    private void addVibrateCardLayout(){
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll_vibrate);

        CardLayout cardLayout = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card,null,false);
        cardLayout.build(createVibrateCard());

        ll.addView(cardLayout);
    }

    private Card createVibrateCard(){

        vibrateProvider = new Card.Builder(this).withProvider(new CardProvider());
        return vibrateProvider.setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_vibrate_type)
                .setDescription(""+getResources().getString(R.string.vibrate)+GlobalData.vibrate_type_number)
                .endConfig()
                .build();
    }

    private void addWorkContentCardLayout(){

        CardLayout cardLayout = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card,null,false);
        cardLayout.build(createWorkContentCard());

        frontFrame.addView(cardLayout);
    }

    private Card createWorkContentCard(){

        workContentProvider = new Card.Builder(this).withProvider(new CardProvider());
        return workContentProvider.setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_inform_break)
                .setSubtitle(R.string.inform_title)
                .setDescription(R.string.inform_content)
                .endConfig()
                .build();
    }

    private void addBreakContentCardLayout(){

        CardLayout cardLayout = (CardLayout) LayoutInflater.from(mContext).inflate(R.layout.material_text_card,null,false);
        cardLayout.build(createBreakContentCard());

        backFrame.addView(cardLayout);
    }

    private Card createBreakContentCard(){

        breakContentProvider = new Card.Builder(this).withProvider(new CardProvider());
        return breakContentProvider.setLayout(R.layout.material_text_card)
                .setTitle(R.string.title_inform_work)
                .setSubtitle(R.string.interval_title)
                .setDescription(R.string.interval_content)
                .endConfig()
                .build();
    }

    private void initAnimator() {
        mRightOutSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.anim_out);
        mLeftInSet = (AnimatorSet) AnimatorInflater.loadAnimator(mContext, R.animator.anim_in);

        //设置点击事件
        mRightOutSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                //itemView.setClickable(false);
                if (!mIsShowBack) {
                    backFrame.setVisibility(View.VISIBLE);
                }else {
                    frontFrame.setVisibility(View.VISIBLE);
                }
                mainFrame.setClickable(false);
                super.onAnimationStart(animation);
            }
        });
        mLeftInSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainFrame.setClickable(true);
                //itemView.setClickable(true);
                if (!mIsShowBack) {
                    backFrame.setVisibility(View.GONE);
                }else {
                    frontFrame.setVisibility(View.GONE);
                }
                super.onAnimationEnd(animation);
            }
        });
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
                Log.i(TAG,"slideOffset:"+slideOffset+"");
                Log.i(TAG,"scale:"+scale+"");

                float rightScale = 0.8f + scale * 0.2f;//= 1-0.2*slideOffset ; 0.8*slideOffset也可以 [1,0.8]
                Log.i(TAG,"rightScale:"+rightScale+"");
                if (drawerView.getTag().equals("LEFT")) {

                    float leftScale = 1 - 0.3f * scale;//[0.7,1]  = 0.7+0.3*slideOffset
                    Log.i(TAG,"leftScale:"+leftScale+"");
                    //设置左边菜单滑动后的占据屏幕大小
                    mMenu.setScaleX(leftScale);
                    mMenu.setScaleY(leftScale);
                    //设置菜单透明度
                    float menuAlpha = 0.6f + 0.4f * (1 - scale);//= 0.6+0.4*slideOffset
                    Log.i(TAG,"menuAlpha:"+menuAlpha+"");
                    mMenu.setAlpha(menuAlpha);

                    //设置内容界面水平和垂直方向偏转量
                    //在滑动时内容界面的宽度为 屏幕宽度减去菜单界面所占宽度
                    float translationX = mMenu.getMeasuredWidth() * (1 - scale);//= mMenu.getMeasuredWidth() * slideOffset
                    Log.i(TAG,"translationX:"+translationX+"");
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
        int inform_time=prf.getInt("inform_time",55);
        int interval_time = prf.getInt("interval_time",10);
        int vibrate_type_number=prf.getInt("vibrate_type_number",0);
        String inform_title=prf.getString("inform_title", UiUtil.getString(R.string.inform_title));
        String inform_content=prf.getString("inform_content",UiUtil.getString(R.string.inform_content));
        String interval_title = prf.getString("interval_title",UiUtil.getString(R.string.interval_title));
        String interval_content = prf.getString("interval_content",UiUtil.getString(R.string.interval_content));
        GlobalData.inform_time=inform_time;
        GlobalData.interval_time = interval_time;
        GlobalData.vibrate_type_number=vibrate_type_number;
        GlobalData.inform_title=inform_title;
        GlobalData.inform_content=inform_content;
        GlobalData.interval_title = interval_title;
        GlobalData.interval_content = interval_content;

        //查看SD卡中是否有已存的提示图片，如果有就把它设为全局变量，如果没有就把默认的老鼠图片设为全局变量
        File outputImage = new File(Environment.getExternalStorageDirectory()+File.separator+"ProtectYourEyes",
                "output_image.jpg");
        try {
            if (outputImage.exists()) {
                GlobalData.imageUri = Uri.fromFile(outputImage);
                GlobalData.inform_bitmap=BitmapFactory.decodeStream(getContentResolver()
                        .openInputStream(GlobalData.imageUri));
            }else{
                Resources res=getResources();
                GlobalData.inform_bitmap=BitmapFactory.decodeResource(res, R.mipmap.eye);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //在每次返回主界面的时候刷新主界面中的内容
    @Override
    protected void onStart() {
        super.onStart();

        workTimeProvider.setDescription(""+GlobalData.inform_time+getResources().getString(R.string.time_unit));
        String btDes = ""+GlobalData.interval_time+getResources().getString(R.string.time_unit);
        breakTimeProvider.setDescription(btDes);
        String vibDes = ""+getResources().getString(R.string.vibrate)+GlobalData.vibrate_type_number;
        vibrateProvider.setDescription(vibDes);
        workContentProvider.setSubtitle(GlobalData.inform_title);
        workContentProvider.setDescription(GlobalData.inform_content);
        breakContentProvider.setSubtitle(GlobalData.interval_title);
        breakContentProvider.setDescription(GlobalData.interval_content);

        if (isServiceRunning(this,"com.example.protectyoureyes.Service.LongRunningService")){

            bt_start_inform.setVisibility(View.GONE);
            bt_start_inform.setEnabled(false);
            bt_stop_inform.setVisibility(View.VISIBLE);
            bt_stop_inform.setEnabled(true);
            bindMyService(this);
        }

    }

    public boolean isServiceRunning(Context mContext,String className){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //用不了ActivityManagerCompat managerCompat = (ActivityManagerCompat) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(100);
        if (!(serviceInfos.size() > 0)){
            Log.i(TAG,"serviceInfos.size() <= 0");
            return false;
        }
        for (int i = 0; i < serviceInfos.size(); i++) {
            //Log.i(TAG,"service"+i+":"+serviceInfos.get(i).service.getClassName());
            if (serviceInfos.get(i).service.getClassName().equals(className)){
                return true;
            }
        }
        return false;
    }

    private void bindMyService(Context context){
        Intent intent=new Intent(MainActivity.this, LongRunningService.class);
        context.bindService(intent,serviceConnection,Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    private LongRunningService longRunningService;

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG,"ServiceConnected");
            longRunningService = ((LongRunningService.MyBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG,"ServiceDisconnected");
            longRunningService = null;
        }
    };

    @Override
    protected void onDestroy() {
        if (isBindedService) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
        Log.i(TAG,"destroy");
    }
}
