package com.example.protectyoureyes.Activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.protectyoureyes.MyActivity;
import com.example.protectyoureyes.R;
import com.example.protectyoureyes.My_View.TitleLayout;
import com.example.protectyoureyes.bean.GlobalData;



public class Activity_time extends MyActivity {
    private Spinner sp_vibrate_control;
    private EditText et_inform_time;
    private EditText et_interval_time;
    private Button bt_time_confirm;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_control);
        initToolbar(R.id.tb_timeset);
        InitView();
    }

    @Override
    protected void initToolbar(int id) {
        super.initToolbar(id);
        toolbar.setTitle(R.string.tb_timeset);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    private void InitView() {
        sp_vibrate_control=(Spinner)findViewById(R.id.sp_vibrate_control);
        et_inform_time=(EditText)findViewById(R.id.et_inform_time);
        et_interval_time = (EditText) findViewById(R.id.et_interval_time);
        bt_time_confirm=(Button)findViewById(R.id.bt_time_confirm);
        editor=getSharedPreferences("GlobalData",MODE_PRIVATE).edit();

        //设置界面中的spinner
        setSpinner();
        et_inform_time.setText(GlobalData.inform_time+"");
        et_interval_time.setText(GlobalData.interval_time+"");
        //设置确认button的点击效果
        bt_time_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置提醒时间
                //设置振动类型
                setMyTime();
                setMyVibrateType();
                Toast.makeText(Activity_time.this, "设置成功！", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },1000);
            }
        });

    }



    private void setSpinner() {
        //给单选spinner设置adapter，内容为GlobalData.vibrate_type_name
        ArrayAdapter<String> sp_adapter=new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_singlechoice, GlobalData.vibrate_type_name);
        sp_vibrate_control.setAdapter(sp_adapter);
        //setSelection这句话一定要放在setAdapter之后，否则无效，并且不会提示错误
        sp_vibrate_control.setSelection(GlobalData.vibrate_type_number,true);
    }

    private void setMyTime() {
        String time=et_inform_time.getText().toString();
        //由于GlobalData.inform_time是int类型的，此处有要转换一下
        GlobalData.inform_time=Integer.parseInt(time);
        String intervalTime = et_interval_time.getText().toString();
        GlobalData.interval_time = Integer.parseInt(intervalTime);
        editor.putInt("inform_time",Integer.parseInt(time));
        editor.putInt("interval_time",Integer.parseInt(intervalTime));
        //提交数据
        editor.commit();
    }

    private void setMyVibrateType() {
        int position=sp_vibrate_control.getSelectedItemPosition();
        GlobalData.vibrate_type_number=position;
        editor.putInt("vibrate_type_number",position);
        //提交数据
        editor.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
