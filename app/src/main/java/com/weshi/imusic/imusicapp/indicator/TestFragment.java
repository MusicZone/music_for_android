package com.weshi.imusic.imusicapp.indicator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.weshi.imusic.imusicapp.R;
import com.weshi.imusic.imusicapp.main.MainActivity;

public final class TestFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";
    private static final String KEY_LAST = "TestFragment:Last";

    private int pic;
    private boolean isLastFrag;
    private Context parent;
    public static TestFragment newInstance(int content,boolean isLast,Context ctx) {
        TestFragment fragment = new TestFragment();
/*
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            builder.append(content).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        fragment.mContent = builder.toString();*/
        fragment.pic = content;
        fragment.isLastFrag = isLast;
        fragment.parent = ctx;
        return fragment;
    }

    //private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT) && savedInstanceState.containsKey(KEY_LAST)) {
            pic = savedInstanceState.getInt(KEY_CONTENT);
            isLastFrag = savedInstanceState.getBoolean(KEY_LAST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
        text.setText(mContent);
        text.setTextSize(20 * getResources().getDisplayMetrics().density);
        text.setPadding(20, 20, 20, 20);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.setGravity(Gravity.CENTER);
        layout.addView(text);

        return layout;*/
        View v = inflater.inflate(R.layout.fragment_guide,container,false);
        ImageView img = (ImageView)v.findViewById(R.id.image);
        img.setBackgroundResource(pic);


        Button btn = (Button)v.findViewById(R.id.button);
        if(isLastFrag){

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences pre = parent.getSharedPreferences("first_pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edt = pre.edit();
                    edt.putBoolean("isFirstTime", false);
                    edt.commit();
                    Activity me = (Activity)parent;

                    Intent it = new Intent(me, MainActivity.class);
                    startActivity(it);
                    me.finish();
                }
            });
            btn.setVisibility(View.VISIBLE);
        }else{
            btn.setVisibility(View.GONE);
        }
        /*
        * SharedPreferences pre = getSharedPreferences("first_pref",MODE_PRIVATE);
        Boolean isFirstTime = pre.getBoolean("isFirstTime",true);
        if(isFirstTime){
            Intent it = new Intent(MainActivity.this, SampleCirclesDefault.class);
            startActivity(it);
            finish();
        }
        *
        * */
        return v;

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, pic);
        outState.putBoolean(KEY_LAST, isLastFrag);
    }
}
