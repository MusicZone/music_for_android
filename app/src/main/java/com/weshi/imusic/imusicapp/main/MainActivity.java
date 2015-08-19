package com.weshi.imusic.imusicapp.main;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

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
                .setIndicator("Imusic")//设置名称以及图标
                .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
        tabHost.addTab(spec);//添加进tabHost

        //第二个Tab
        intent = new Intent(this,LocalActivity.class);//第二个Intent用作Tab1显示的内容
        spec = tabHost.newTabSpec("tab2")//新建一个 Tab
               //.setIndicator("Tab2", res.getDrawable(android.R.drawable.ic_menu_edit))//设置名称以及图标
                .setIndicator("本地音乐")//设置名称以及图标
                .setContent(intent);//设置显示的intent，这里的参数也可以是R.id.xxx
        tabHost.addTab(spec);//添加进tabHost

    }
}
