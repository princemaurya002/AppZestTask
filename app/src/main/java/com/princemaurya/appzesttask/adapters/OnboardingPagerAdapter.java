package com.princemaurya.appzesttask.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.princemaurya.appzesttask.R;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {
    private static final String TAG = "OnboardingPagerAdapter";
    private Context context;
    private OnButtonClickListener buttonClickListener;
    private int[] layouts = {
        R.layout.activity_info_screen1,
        R.layout.activity_info_screen2,
        R.layout.activity_info_screen3
    };

    public interface OnButtonClickListener {
        void onButtonClick(int position);
    }

    public OnboardingPagerAdapter(Context context, OnButtonClickListener listener) {
        this.context = context;
        this.buttonClickListener = listener;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        ImageButton bottomButton = holder.itemView.findViewById(R.id.imageButton);
        if (bottomButton != null) {
            bottomButton.setOnClickListener(v -> {
                Log.d(TAG, "Button clicked at position: " + position);
                if (buttonClickListener != null) {
                    buttonClickListener.onButtonClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}