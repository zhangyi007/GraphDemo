package com.zyyl.graphdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zyyl.graphdemo.view.HWgraphView;

import java.util.LinkedList;
import java.util.List;

/**
 * 数据以小朋友身高体重曲线为例
 */
public class MainActivity extends AppCompatActivity {

    //自定义View，曲线图
    private HWgraphView hWgraphView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hWgraphView=findViewById(R.id.hwgraph);

        //设置x轴最小值，和y轴最小最大值
        hWgraphView.setMaxMinXY(0,15,150);
        //设置年龄，年月日
        hWgraphView.setAge(1,1,1);
        hWgraphView.setBaByHW(getTest());

    }

    public List<HWgraphView.MPoint> getTest() {
        List<HWgraphView.MPoint> cmList = new LinkedList<>();
        for (int i = 1; i < mData.size(); i++) {
            try {
                float height = Float.parseFloat(mData.get(i).getHt());
                HWgraphView.MPoint mp = new HWgraphView.MPoint(height,
                        mData.get(i).getAy() * 12 + mData.get(i).getAm() + mData.get(i).getAd() / 30.0f);
                cmList.add(mp);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return cmList;
    }
}
