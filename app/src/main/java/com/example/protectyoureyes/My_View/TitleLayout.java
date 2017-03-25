package com.example.protectyoureyes.My_View;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.protectyoureyes.R;

//自定义标题布局
public class TitleLayout extends LinearLayout {

    private Button titleBack;
    private TextView titleText;

    public TitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //加载布局文件，与setContentView()效果一样
        LayoutInflater.from(context).inflate(R.layout.title_layout, this);
        titleBack = (Button) findViewById(R.id.title_back);
        titleText = (TextView) findViewById(R.id.title_text);

        //设置返回键的点击效果
        titleBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).finish();
            }
        });

    }

    //创建一个方法来改变title中text的内容
    public void setTitleText(String text) {
        titleText.setText(text);
    }
}