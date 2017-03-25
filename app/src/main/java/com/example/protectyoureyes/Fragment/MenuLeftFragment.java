package com.example.protectyoureyes.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.protectyoureyes.Activity.Activity_inform;
import com.example.protectyoureyes.Activity.Activity_interval;
import com.example.protectyoureyes.Activity.Activity_time;
import com.example.protectyoureyes.R;

public class MenuLeftFragment extends Fragment implements View.OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState)
	{
		View view =inflater.inflate(R.layout.layout_menu, container, false);
		//绑定两个button，并且设置点击效果
		Button bt_time_control=(Button)view.findViewById(R.id.bt_time_control);
		Button bt_inform_control=(Button)view.findViewById(R.id.bt_inform_control);

		bt_time_control.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(getActivity().getApplicationContext(),Activity_time.class);
				startActivity(intent);
			}
		});
		bt_inform_control.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(getActivity().getApplicationContext(),Activity_inform.class);
				startActivity(intent);
			}
		});

		Button bt_interval = (Button) view.findViewById(R.id.bt_interval_control);
		bt_interval.setOnClickListener(this);
		return view;
	}


	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.bt_interval_control:
				Intent intent=new Intent(getActivity().getApplicationContext(),Activity_interval.class);
				startActivity(intent);
				break;
		}
	}
}
