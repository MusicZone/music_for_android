package com.weshi.imusic.imusicapp.main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.weshi.imusic.imusicapp.R;
import com.weshi.imusic.imusicapp.update.UpdateManager;

public class MainActivity extends TabActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//这里使用了上面创建的xml文件（Tab页面的布局）


        //UpdateManager manager = new UpdateManager(this);
        // 检查软件更新
        //manager.checkUpdate();

        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabSpec spec;
        Intent intent;  // Reusable Intent for each tab

        //第一个Tab
        intent = new Intent(this,ImusicActivity.class);//新建一个Intent用作Tab1显示的内容
        spec = tabHost.newTabSpec("tab1")//新建一个 Tab
                //.setIndicator("Tab1", res.getDrawable(android.R.drawable.ic_media_play))//设置名称以及图标
                .setIndicator("iMusic", res.getDrawable(R.drawable.radio))//设置名称以及图标
                .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
        tabHost.addTab(spec);//添加进tabHost

        //第二个Tab
        intent = new Intent(this,LocalActivity.class);//第二个Intent用作Tab1显示的内容
        spec = tabHost.newTabSpec("tab2")//新建一个 Tab
               //.setIndicator("Tab2", res.getDrawable(android.R.drawable.ic_menu_edit))//设置名称以及图标
                .setIndicator("本地音乐",res.getDrawable(R.drawable.local))//设置名称以及图标
                .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
        tabHost.addTab(spec);//添加进tabHost

        //第三个Tab
        intent = new Intent(this,AboutActivity.class);//第二个Intent用作Tab1显示的内容
        spec = tabHost.newTabSpec("tab3")//新建一个 Tab
                //.setIndicator("Tab2", res.getDrawable(android.R.drawable.ic_menu_edit))//设置名称以及图标
                .setIndicator("关于",res.getDrawable(R.drawable.settings))//设置名称以及图标
                .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
        tabHost.addTab(spec);//添加进tabHost

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Activity current = getCurrentActivity();
                if(current instanceof ImusicActivity){
                    Activity lastone = getLocalActivityManager().getActivity("tab2");
                    if(lastone instanceof LocalActivity){
                        ((LocalActivity)lastone).stopMusic();
                    }

                }else if(current instanceof LocalActivity){
                    Activity lastone = getLocalActivityManager().getActivity("tab1");
                    if(lastone instanceof ImusicActivity){
                        ((ImusicActivity)lastone).stopMusic();
                    }

                }else{
                    Activity lastone = getLocalActivityManager().getActivity("tab2");
                    if(lastone instanceof LocalActivity){
                        ((LocalActivity)lastone).stopMusic();
                    }
                    lastone = getLocalActivityManager().getActivity("tab1");
                    if(lastone instanceof ImusicActivity){
                        ((ImusicActivity)lastone).stopMusic();
                    }
                }
            }
        });



        TabWidget tw = tabHost.getTabWidget();
        setStyle(tw.getChildAt(0));
        setStyle(tw.getChildAt(1));
        setStyle(tw.getChildAt(2));
        tw.getChildAt(1).setClickable(false);
        tw.getChildAt(2).setClickable(false);//.getTabWidget().setEnabled(en); //.getChildAt(1).setClickable(en);




    }
    private void setStyle(View v){
        //final TextView tv = (TextView)v.findViewById(android.R.id.title);
        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)tv.getLayoutParams();
        //params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
        //params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        //params.height=LinearLayout.LayoutParams.WRAP_CONTENT;
        //v.getLayoutParams().height=100;//LinearLayout.LayoutParams.WRAP_CONTENT;
        //v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
