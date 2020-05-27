package com.zyyl.graphdemo.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Abel on 2017/10/25.
 * 自定义view身高体重曲线图
 */

public class HWgraphView extends View {
    //身高
    public static final int HEIGHT=0;
    //体重
    public static final int WEIGHT=1;
    //男孩
    public static final int BOY=0;
    //女孩
    public static final int GIRL=1;

    //横竖平均分成9份
    private static float average=9f;
    //边距
    private static int left_gauge=40;
    private static int buttom_gauge=60;
    private static int top_gauge=80;
    //虚线距离右边的距离
    private static int imaginary_line_gauge=160;
    private static String TAG="baby_hw";

    //控件的总宽高
    float width=0;
    float height=0;

    //平分，每格的宽高
    float averageWidth=0;
    float averageHeight=0;

    //设置默认xy轴最大最小值
    int maxY=150;
    int minY=15;
    int minX=12;

    //虚线位置的年月日
    int year;
    int mouth;
    int day;
    float float_day;

    //今天
    int todayYear;
    int todayMouth;
    int todayDay;
    //最接近标准值
    float minHW;
    float maxHW;
    float minHW2;
    float maxHW2;
    float minHW_x;
    float maxHW_x;


    int graph_type=0;//0 身高，1体重
    int graph_sex=0;//0男 1女

    float leftExcursion=0;//偏移量，默认0

    //画笔，不同颜色的画笔
    private Paint black_paint;
    private Paint gray_paint;
    private Paint blue_paint;
    private Paint blue_paint2;
//    private Paint red_paint;
    private Paint yellow_paint;
    private Paint white_paint;
    private Paint orange_paint;

    //背景颜色
    int backgroundColor=-1;
    //记录上次滑动后的x坐标值
    private int lastX;
    //是否在节点位置
    boolean isNode=false;
    MPoint nodePoint=null;

    private float baby_age=-1;
    private boolean isCallBack=false;

    OnHWGraphCallBack onHWGraphCallBack=null;

    private String base_hw=null;

    //标准身高体重
    private List<MPoint> list=new LinkedList<>();
    //实际身高
    private List<MPoint> cmList=new LinkedList<>();

    public HWgraphView(Context context) {
        super(context);
        init();
    }

    public HWgraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HWgraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        //关闭硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //初始化边距
        left_gauge=dp2px(30);
        top_gauge=dp2px(25);
        buttom_gauge=dp2px(30);
        imaginary_line_gauge=dp2px(80);
        //黑色画笔，画坐标轴的
        black_paint = new Paint();
        black_paint.setColor(Color.parseColor("#333333"));
        black_paint.setStrokeWidth(2f);
        black_paint.setTextSize(dp2px(11));
        black_paint.setStyle(Paint.Style.FILL);
        black_paint.setAntiAlias(true);

        //灰色画笔，画背景参考线
        gray_paint = new Paint();
        gray_paint.setColor(Color.parseColor("#E1E1E1"));
        gray_paint.setStrokeWidth(1f);
        gray_paint.setStyle(Paint.Style.FILL);
        gray_paint.setAntiAlias(true);

        //蓝色画笔，画背标准区间
        blue_paint = new Paint();
        blue_paint.setColor(Color.parseColor("#E6F9FF"));
        blue_paint.setStrokeWidth(1f);
        blue_paint.setStyle(Paint.Style.FILL);
        blue_paint.setAntiAlias(true);
        //蓝色画笔，画背标准区间描边
        blue_paint2 = new Paint();
        blue_paint2.setColor(Color.parseColor("#48CEf9"));
        blue_paint2.setStrokeWidth(2f);
        blue_paint2.setStyle(Paint.Style.STROKE);
        blue_paint2.setAntiAlias(true);

        //橙色，画参考虚线
        yellow_paint =new Paint();
        yellow_paint.setColor(Color.parseColor("#FF9800"));
        yellow_paint.setStrokeWidth(3f);
        yellow_paint.setStyle(Paint.Style.STROKE);
        yellow_paint.setAntiAlias(true);
        //设置是虚线
        PathEffect effects = new DashPathEffect(new float[]{ dp2px(5), dp2px(3)},0);
        yellow_paint.setPathEffect(effects);

//        //红色，画参圆点和走势曲线
//        red_paint =new Paint();
//        red_paint.setColor(Color.parseColor("#FF9800"));
//        red_paint.setStrokeWidth(3f);
//        red_paint.setStyle(Paint.Style.FILL);
//        red_paint.setAntiAlias(true);


        //白色，画Y轴矩形
        white_paint =new Paint();
        white_paint.setColor(Color.WHITE);
        white_paint.setStyle(Paint.Style.FILL);
        white_paint.setAntiAlias(true);

        //橙色画笔
        orange_paint=new Paint();
        orange_paint.setStrokeWidth(3f);
        orange_paint.setColor(Color.parseColor("#FF9800"));
        orange_paint.setStyle(Paint.Style.FILL);
        orange_paint.setAntiAlias(true);
        base_hw=getFromAssets("basic_height_weight.json");
    }
    public String getFromAssets(String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += line;
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        //获取空间的宽高
        width=getWidth();
        height=getHeight();
        if(width==0 || height==0){
            width=500;
            height=500;
        }
        averageWidth=(int)((width-left_gauge)/average);
        averageHeight=(int)((height-buttom_gauge-top_gauge)/average);
        getBackgroundColor();
        skipToday();
    }

    /**
     * 跳转到今天
     */
    public void skipToday(){
        if(this.baby_age!=-1){

            this.year=this.todayYear;
            this.mouth=this.todayMouth;
            this.day=this.todayDay;
            isCallBack=false;
            float dx=width-imaginary_line_gauge-left_gauge;
            float dm=dx/averageWidth;
            Log.d(TAG,"dx="+dx+",dm="+dm);
            float mdx=this.baby_age-dm;
            minX=(int) Math.ceil(mdx);
//            if(minX>90){
//                minX=85;
//            }
            if(minX>85){
                minX=85;
            }
            if(minX<1 || minX==1 && leftExcursion>0){
                minX+=1;
            }
            leftExcursion=(int)((1-(mdx-(int)mdx))*averageWidth);
            Log.d(TAG,"mdx="+mdx+",minX="+minX+",leftExcursion="+leftExcursion);
        }
        postInvalidate();
    }

    private void getBackgroundColor(){
        //获取背景颜色
        Drawable background=getBackground();
        if (background==null){
            backgroundColor= Color.WHITE;
            return;
        }
        if (background instanceof ColorDrawable) {
            ColorDrawable colordDrawable = (ColorDrawable) background;
            backgroundColor = colordDrawable.getColor();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取view相对于手机屏幕的xy值
        isCallBack=true;
        int x = (int) event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastX;
                //防止右滑过界
                if(minX<-10 && deltaX>0){
                    lastX = x;
                    return false;
                }
                //防止左滑超过极限90个月
                if(minX>85 && deltaX<0){
                    lastX = x;
                    return false;
                }
                leftExcursion+=deltaX;
                if(leftExcursion>0 && leftExcursion>=averageWidth){
                    leftExcursion-=averageWidth;
                    minX--;
                }else if(leftExcursion<0 && Math.abs(leftExcursion)>=averageWidth){
                    leftExcursion+=averageWidth;
                    minX++;
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        lastX = x;
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        drawAssist(canvas);
        drawXY(canvas);
        drawCircle(canvas);
        drawImaginaryLine(canvas);
        drawHint(canvas);
        drawY(canvas);
        super.onDraw(canvas);
    }

    private void drawHint(Canvas canvas) {
        int ym=year*12+mouth;
        if(ym>81 || ym==81 && day>0 || (year<=0 && mouth<=0 && day<0)){
            minHW=-1;
            maxHW=-1;
        }
        float mIHW=-1;
        float maHW=-1;
        if(minHW!=-1 && maxHW!=-1){
            mIHW=decimalTwo(minHW2,minHW,maxHW_x,minHW_x);
            maHW=decimalTwo(maxHW2,maxHW,maxHW_x,minHW_x);
//            mIHW+=2;
//            maHW+=2;
        }
        if(isNode && nodePoint!=null){
            float dx=getDX(nodePoint.x)+leftExcursion;
            float dy=getDY(nodePoint.y);
            canvas.drawCircle(dx,dy,12,orange_paint);
            canvas.drawCircle(dx,dy,8,white_paint);
            Path p=new Path();
            p.moveTo(dx,dy-12);
            p.lineTo(dx+10,dy-22);
            p.lineTo(dx-10,dy-22);
            p.close();
            canvas.drawPath(p,orange_paint);
            white_paint.setTextAlign(Paint.Align.CENTER);
            white_paint.setTextSize(dp2px(13));
            int textw=getTextWidth(nodePoint.y+"");
            RectF r1 = new RectF();
            r1.left = dx-(textw/2f)-15;
            r1.right = dx+(textw/2f)+15;
            r1.top = dy-22-dp2px(18);
            r1.bottom = dy-21;
            canvas.drawRoundRect(r1,dp2px(15),dp2px(15),orange_paint);
            canvas.drawText(nodePoint.y+"",dx,dy-21-dp2px(4),white_paint);
            if(onHWGraphCallBack!=null){
                int type;
                if(nodePoint.y<mIHW){
                    type=-1;
                }else if(nodePoint.y>maHW){
                    type=1;
                }else{
                    type=0;
                }
                onHWGraphCallBack.onBackData(getTime(),nodePoint.y,type,mIHW,maHW);
            }
        }else{
            //无节点-给出无节点回调
            if(onHWGraphCallBack!=null){
                onHWGraphCallBack.onNoData(getTime(),mIHW,maHW);
            }
        }
    }

    /**
     * 保留1为小数
     * @return
     */
    private float decimalTwo(float y2,float y1,float x2,float x1){
        Log.d(TAG,"y2="+y2+",x2="+x2+",y1="+y1+",x1="+x1+",x="+float_day);
        return (float) Math.round(((float_day-x1)*(y2-y1)/(x2-x1)+y1)*10)/10;
//        return (float) Math.round((float_day*(y2-y1)/(x2-x1)+(y1*x2-y2*x1)/(x2-x1))*10)/10;
    }


    public int getTextWidth(String content){
        int width = 0;
        if(content!=null&&content.length()>0){
            int length = content.length();
            float[] widths = new float[length];
            white_paint.getTextWidths(content,widths);
            for (int i = 0; i < length; i++) {
                width += (int) Math.ceil(widths[i]);
            }
        }
        return width;
    }

    private String getTime(){
        String time="";
        if(year>0){
            time+=year+"岁";
        }
        if(mouth>0){
            time+=mouth+"个月";
        }
        if (day>0){
            time+=day+"天";
        }
        if(year<=0 && mouth<=0){
            if (day==0){
                return "出生";
            }else if(day<0){
                return "未出生";
            }
        }
        return time;
    }

    private void drawY(Canvas canvas) {
        if(backgroundColor!=-1){
            white_paint.setColor(backgroundColor);
        }
        canvas.drawRect(0 ,0,left_gauge,height,white_paint);
        //y轴
        canvas.drawLine(left_gauge, top_gauge, left_gauge, height-buttom_gauge,black_paint);
        //画体重或身高单位
        black_paint.setTextSize(dp2px(11));
        black_paint.setTextAlign(Paint.Align.LEFT);
        if(graph_type==0){
            canvas.drawText("cm",left_gauge+20,top_gauge-20,black_paint);
        }else{
            canvas.drawText("kg",left_gauge+20,top_gauge-20,black_paint);
        }

        int num=0;
        black_paint.setTextAlign(Paint.Align.RIGHT);
        for(float i=height-top_gauge-buttom_gauge-averageHeight;i>=0;i-=averageHeight){
            //画竖轴数字
            canvas.drawText((int)(num*((maxY-minY)*1.0/average)+minY)+"",left_gauge-10,i+averageHeight+top_gauge,black_paint);
            num++;
        }
        if(num<=(int)average){
            canvas.drawText(maxY+"",left_gauge-10,top_gauge,black_paint);
        }
    }

    private void drawCircle(Canvas canvas) {
        isNode=false;
        MPoint t=null;
        for (int i=0;i<cmList.size();i++){
            float dx=getDX(cmList.get(i).x)+leftExcursion;
            canvas.drawCircle(dx, getDY(cmList.get(i).y), dp2px(4), orange_paint);
            if((width-imaginary_line_gauge)>dx-dp2px(2) && (width-imaginary_line_gauge)<dx+dp2px(2)){
                isNode=true;
                nodePoint=cmList.get(i);
            }
            if(t!=null){
                canvas.drawLine(getDX(t.x)+leftExcursion,getDY(t.y),dx, getDY(cmList.get(i).y),orange_paint);
            }
            t=cmList.get(i);
        }
    }

    private float getDX(float x) {
        return (x-minX)*averageWidth+left_gauge;
    }

    /**
     * 画参考虚线
     * @param canvas
     */
    private void drawImaginaryLine(Canvas canvas) {
        Path path = new Path();
        path.moveTo(width-imaginary_line_gauge, top_gauge);
        path.lineTo(width-imaginary_line_gauge,height-buttom_gauge-2);
        canvas.drawPath(path , yellow_paint);
    }

    /**
     * 画标准身高体重辅助线
     * @param canvas
     */
    private void drawAssist(Canvas canvas) {
        Path path = new Path();
        maxHW= Float.MIN_VALUE;
        minHW= Float.MIN_VALUE;
        maxHW2= Float.MAX_VALUE;
        minHW2= Float.MAX_VALUE;
        boolean isf=true;
        path.reset();
        for (int i=0;i<list.size();i++){
            if(isf){
                path.moveTo((list.get(i).x-minX)*averageWidth+left_gauge+leftExcursion, getDY(list.get(i).maxY));
                isf=false;
            }else{
                path.lineTo((list.get(i).x-minX)*averageWidth+left_gauge+leftExcursion, getDY(list.get(i).maxY));
            }
            //计算最接近虚线的标准值
            float max_hw=((list.get(i).x-minX)*averageWidth+left_gauge+leftExcursion)-(width-imaginary_line_gauge);
//            Log.d(TAG,"max_hw="+max_hw);
            if(max_hw<=0){
                if(maxHW<list.get(i).maxY){
                    maxHW=list.get(i).maxY;
                    minHW=list.get(i).minY;
                    minHW_x=list.get(i).x;
                }
            }
            if(max_hw>0){
                if(maxHW2>list.get(i).maxY){
                    maxHW2=list.get(i).maxY;
                    minHW2=list.get(i).minY;
                    maxHW_x=list.get(i).x;
                }
            }
        }
        for (int i=list.size()-1;i>=0;i--){
            path.lineTo((list.get(i).x-minX)*averageWidth+left_gauge+leftExcursion, getDY(list.get(i).minY));
        }
        path.close();
        canvas.drawPath(path, blue_paint);
        canvas.drawPath(path, blue_paint2);
    }

    private float getDY(float y) {
        return  (maxY-y)/(maxY-minY)*(height-top_gauge-buttom_gauge)+top_gauge;
    }

    private void drawXY(Canvas canvas) {
        //x轴
        canvas.drawLine(left_gauge, height-buttom_gauge, width, height-buttom_gauge,black_paint);

        black_paint.setTextSize(dp2px(12));
        black_paint.setTextAlign(Paint.Align.RIGHT);
        for(float i=height-top_gauge-buttom_gauge-averageHeight;i>=0;i-=averageHeight){
            //画横轴参考线
            canvas.drawLine(left_gauge, i+top_gauge, width, i+top_gauge,gray_paint);
        }
        //画竖轴参考线
        black_paint.setTextAlign(Paint.Align.LEFT);
        for(float i=0;i<=width*2;i+=averageWidth){
            canvas.drawLine(i+left_gauge+leftExcursion, top_gauge, i+left_gauge+leftExcursion, height-buttom_gauge,gray_paint);
        }
        //预加载
        for (float i=left_gauge+leftExcursion-averageWidth;i>=width*-1;i-=averageWidth){
            canvas.drawLine(i, top_gauge, i, height-buttom_gauge,gray_paint);
        }
        //画横轴文字
        black_paint.setTextAlign(Paint.Align.LEFT);
        for (int i=0;i<=average+average;i++){
            String xText;
            if(i+minX==0){
                xText="出生";
            }else if (i+minX>0){
                xText=i+minX+"月";
            }else {
                xText="";
            }
            //X轴极限90个月
            if(i+minX<=90){
                canvas.drawText(xText,i*averageWidth+left_gauge+leftExcursion,height-buttom_gauge+dp2px(12)+5,black_paint);
            }else{
                break;
            }
        }
        //预加载
        for (int i=-1;i>average*-1;i--){
            String xText;
            if(i+minX==0){
                xText="出生";
            }else if (i+minX>0){
                xText=i+minX+"月";
            }else {
                xText="";
            }
            canvas.drawText(xText,i*averageWidth+left_gauge+leftExcursion,height-buttom_gauge+dp2px(12)+5,black_paint);
        }
        if(isCallBack){//第一次不记录
            //获取虚线参考线位置时间比例
            float b=(width-imaginary_line_gauge-left_gauge-leftExcursion)/averageWidth;
            float_day=minX+b;
            int m=minX+(int)b;
            year=m/12;
            mouth=m%12;
            //天
            if(minX+b<0){
                day=-1;
            }else{
                day=(int)((b-(int)b)*31);
            }
        }else{
            float_day=todayYear*12+todayMouth+todayDay/31f;
        }
    }

    /**
     * 设置最大x,y
     * @param minX
     * @param minY
     * @param maxY
     */
    public void setMaxMinXY(int minX,int minY,int maxY){
        this.minX=minX;
        this.minY=minY;
        this.maxY=maxY;
    }

    /**
     * 设置宝宝当前年龄
     * @param m
     */
    public void setAge(int y,int m,int d){
        Log.d("baby_hw","y="+y+",m="+m+",d="+d);
        this.todayYear=y;
        this.todayMouth=m;
        this.todayDay=d;
        this.baby_age=y*12+m+d/31F;
        Log.d("baby_hw","baby_age="+baby_age);
    }

    //设置是身高曲线还是体重曲线，默认是身高
    public void setGraphTypeAndSex(int type,int sex) {
        this.graph_type = type;
        this.graph_sex = sex;
        baseJson();
//        postInvalidate();
    }

    /**
     * 刷新view
     */
    public  void refreshView(){
        skipToday();
    }

    public void setBaByHW(@Nullable List<MPoint> list){
        this.cmList.clear();
        this.cmList.addAll(list);
    }

    private void baseJson() {
        if(base_hw!=null && base_hw.length()>0){
            try {
                JSONObject jsono=new JSONObject(base_hw);
                JSONArray baseJson=null;
                if(this.graph_type==HEIGHT){
                    baseJson=jsono.optJSONArray("height");
                }else if(this.graph_type==WEIGHT){
                    baseJson=jsono.optJSONArray("weight");
                }
                if(baseJson==null || baseJson.length()==0){
                    return;
                }
                list.clear();
                for (int i=0;i<baseJson.length();i++){
                    MPoint mPoint;
                    if(this.graph_sex==BOY){
                        float my= Float.parseFloat(baseJson.optJSONObject(i).optString("bl"));
                        float hy= Float.parseFloat(baseJson.optJSONObject(i).optString("bh"));
                        float x= Float.parseFloat(baseJson.optJSONObject(i).optString("mth"));
//                        Log.e(TAG,"my="+my+",hy="+hy+",x="+x);
                        mPoint=new MPoint(my,hy,x);
                    }else {
                        float my= Float.parseFloat(baseJson.optJSONObject(i).optString("gl"));
                        float hy= Float.parseFloat(baseJson.optJSONObject(i).optString("gh"));
                        float x= Float.parseFloat(baseJson.optJSONObject(i).optString("mth"));
                        mPoint=new MPoint(my,hy,x);
                    }
                    list.add(mPoint);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private int dp2px(float dpValue) {
        return (int) (dpValue * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    public static  class MPoint{
        float minY;
        float maxY;
        float x;
        float y;

        /**
         * 标准身高
         * @param minY
         * @param maxY
         * @param x
         */
        public MPoint(float minY,float maxY,float x){
            this.minY=minY;
            this.maxY=maxY;
            this.x=x;
        }

        /**
         * 设置宝宝身高
         * @param y
         * @param x
         */
        public MPoint(float y,float x){
            this.x=x;
            this.y=y;
        }
    }

    /**
     * 设置回调
     * @param onHWGraphCallBack
     */
    public void setOnHWGraphCallBack(OnHWGraphCallBack onHWGraphCallBack){
        this.onHWGraphCallBack=onHWGraphCallBack;
    }

    public interface OnHWGraphCallBack{
        void onBackData(String time, float heightOrWeight, int type, float minHW, float maxHW);//type=0正常，1偏高，-1偏低
        void onNoData(String time, float minHW, float maxHW);
    }
}
