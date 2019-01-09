package com.espressif.iot.esptouch.demo_activity.Utils;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.List;

public class PickerUtilClass {

    /**
     * 调整FrameLayout的大小
     * */
    public static void resizePicker(FrameLayout tp){        //DatePicker和TimePicker继承自FrameLayout
        List<NumberPicker> npList = findNumberPicker(tp);  //找到组成的NumberPicker
        for(NumberPicker np:npList){
            resizeNumberPicker(np);      //调整每个NumberPicker的宽度
        }
    }
    /**
     * 得到viewGroup 里面的numberpicker组件
     * */
    public static List<NumberPicker> findNumberPicker(ViewGroup viewGroup) {
        List<NumberPicker> npList = new ArrayList<NumberPicker>();
        View child = null;
        if(null != viewGroup){
            for(int i=0;i<viewGroup.getChildCount();i++){
                child = viewGroup.getChildAt(i);
                if(child instanceof NumberPicker){
                    npList.add((NumberPicker)child);
                }else if(child instanceof LinearLayout){
                    List<NumberPicker> result = findNumberPicker((ViewGroup) child);
                    if(result.size()>0){
                        return result;
                    }
                }
            }
        }
        return npList;
    }

    /**
     * 调整numberpicker大小
     * */
    public static void resizeNumberPicker(NumberPicker np){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,5,10,5);
        np.setLayoutParams(params);
    }
}
