# 身高体重曲线图

纯代码自定义View画曲线图

### 效果图片

[效果图1](http://qn.super1288.com/sample_1.jpg)

[效果图2](http://qn.super1288.com/sample_2.jpg)

![image](https://github.com/zhangyi007/GraphDemo/blob/master/sample_1.jpg)

![image](https://github.com/zhangyi007/GraphDemo/blob/master/sample_2.jpg)

### 功能列表

> 1、展示标准身高或体重区间

> 2、以曲线图方式显示当前身高或者体重折线图

> 3、可左右滑动显示不同时间的身高或体重

> 4、滑动到有数据的日期，显示当前身高或体重是超出标准范围还是在标准范围内

### 对外接口(可按需求更改view暴露增加其他接口)


```
//设置x轴最小值，和y轴最小最大值
hWgraphView.setMaxMinXY(0,15,150);
//设置年龄，年月日
hWgraphView.setAge(2,1,1);
//设置身高体重数据
hWgraphView.setBaByHW(getTest());
//设置是身高曲线还是体重曲线，男孩还是女孩(标准不一样)
hWgraphView.setGraphTypeAndSex(HWgraphView.HEIGHT, HWgraphView.BOY);

//设置回调
hWgraphView.setOnHWGraphCallBack(new HWgraphView.OnHWGraphCallBack() {
    @Override
    public void onBackData(String time, float heightOrWeight, int type, float minHW, float maxHW) {
        /**
         * time 年龄
         * heightOrWeight 实际身高或体重
         * type 0正常，1偏高或偏重，-1偏低或偏轻
         * minHW-maxHW 正常标准身高或者体重
         */
    }
    
    @Override
    public void onNoData(String time, float minHW, float maxHW) {
        /**
         * 同上
         */
    }
});

```
