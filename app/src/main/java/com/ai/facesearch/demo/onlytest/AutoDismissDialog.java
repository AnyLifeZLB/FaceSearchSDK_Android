package com.ai.facesearch.demo.onlytest;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ai.facesearch.demo.R;

//自定义一个dialog类
public class AutoDismissDialog extends Dialog {
   //handler 用来更新UI的一套机制也是消息机制
   private final Handler handler = new Handler();

   public AutoDismissDialog(@NonNull Context context) {
      this(context,0);
   }
   //在构造的时候 我们约定一个style 和一个上下文
   public AutoDismissDialog(@NonNull Context context, int themeResId) {
      super(context, themeResId);
   }

   @Override
   protected void onStart() {
      super.onStart();
      if (handler != null) {
         //这里用到了handler的定时器效果 延迟2秒执行dismiss();
         handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               dismiss();
            }
         }, 2000);
      }
   }





}
