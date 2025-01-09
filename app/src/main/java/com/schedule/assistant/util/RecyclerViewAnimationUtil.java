package com.schedule.assistant.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.recyclerview.widget.RecyclerView;

import com.schedule.assistant.R;

public class RecyclerViewAnimationUtil {
    
    @SuppressLint("NotifyDataSetChanged")
    public static void runLayoutAnimation(final RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getAdapter() == null) return;
        
        final Context context = recyclerView.getContext();
        if (context == null) return;

        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        if (controller == null) return;

        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }
} 