package com.zyyl.graphdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.zyyl.graphdemo.view.HWgraphView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 数据以小朋友身高体重曲线为例
 */
public class MainActivity extends AppCompatActivity {

    //自定义View，曲线图
    private HWgraphView hWgraphView=null;
    private TextView mTvDateFm,mTvHeightHeightfm,mTvHresultHeightfm,mTvHstandardHeightfm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hWgraphView=findViewById(R.id.hwgraph);
        mTvDateFm=findViewById(R.id.tv_date_heightfm);
        mTvHeightHeightfm=findViewById(R.id.tv_height_heightfm);
        mTvHresultHeightfm=findViewById(R.id.tv_hresult_heightfm);
        mTvHstandardHeightfm=findViewById(R.id.tv_hstandard_heightfm);
        //点击回到今天数据
        findViewById(R.id.iv_today_hfm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hWgraphView.setAge(2,1,1);
                hWgraphView.skipToday();
            }
        });


        //设置x轴最小值，和y轴最小最大值
        hWgraphView.setMaxMinXY(0,15,150);
        //设置年龄，年月日
        hWgraphView.setAge(2,1,1);
        hWgraphView.setBaByHW(getTest());
        //设置是身高曲线还是体重曲线，男孩还是女孩(标准不一样)
        hWgraphView.setGraphTypeAndSex(HWgraphView.HEIGHT, HWgraphView.BOY);
        hWgraphView.setOnHWGraphCallBack(new HWgraphView.OnHWGraphCallBack() {
            @Override
            public void onBackData(String time, float heightOrWeight, int type, float minHW, float maxHW) {
                String te = "";
                switch (type) {
                    case 0:
                        te = "正常";
                        break;
                    case 1:
                        te = "偏高了";
                        break;
                    case -1:
                        te = "偏低了";
                        break;
                }
                mTvDateFm.setText(time);
                SpannableString spannableString = new SpannableString("身高 " + heightOrWeight + " cm");
                spannableString.setSpan(new ForegroundColorSpan(type==0? Color.parseColor("#07c2ff"):Color.parseColor("#ffc107")),3,3+(heightOrWeight+"").length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                mTvHeightHeightfm.setText(spannableString);
                mTvHresultHeightfm.setTextColor(type==0?Color.parseColor("#07c2ff"):Color.parseColor("#ffc107"));
                if(minHW<0 && maxHW<0){
                    mTvHstandardHeightfm.setText("标准身高: 无数据");
                }else {
                    mTvHresultHeightfm.setText("宝宝身高"+te);
                    mTvHstandardHeightfm.setText("标准身高: "+minHW + "cm~" + maxHW + "cm");
                }
            }

            @Override
            public void onNoData(String time, float minHW, float maxHW) {
                //mTvDateFm.setText(time + "\n身高:暂无记录\n标准：" + minHW + "cm~" + maxHW + "cm");
                mTvDateFm.setText(time);
                mTvHeightHeightfm.setText("暂无记录");
                mTvHresultHeightfm.setText("");
                if(minHW<0 && maxHW<0){
                    mTvHstandardHeightfm.setText("标准身高: 无数据");
                }else {
                    mTvHstandardHeightfm.setText("标准身高: "+minHW + "cm~" + maxHW + "cm");
                }
            }
        });

        hWgraphView.refreshView();


    }

    public List<HWgraphView.MPoint> getTest() {
        List<HWgraphView.MPoint> cmList = new LinkedList<>();
        for (int i = 1; i < 30; i++) {
            try {
                float height = 73+new Random().nextInt(30);
                HWgraphView.MPoint mp = new HWgraphView.MPoint(height,
                        (i/12+1) * 12 + (i%12+1) + 15 / 30.0f);
                cmList.add(mp);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return cmList;
    }
}
