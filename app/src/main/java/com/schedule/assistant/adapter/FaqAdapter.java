package com.schedule.assistant.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.schedule.assistant.R;
import com.schedule.assistant.model.FaqItem;

import java.util.List;

/**
 * FAQ列表适配器
 */
public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {
    private final List<FaqItem> faqList;
    private static final int ROTATE_DURATION = 200;

    public FaqAdapter(List<FaqItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        FaqItem item = faqList.get(position);
        holder.questionText.setText(item.getQuestion());
        holder.answerText.setText(item.getAnswer());

        // 设置答案文本的可见性
        holder.answerText.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);

        // 设置展开图标的旋转状态
        holder.expandIcon.setRotation(item.isExpanded() ? 180 : 0);

        // 设置点击事件
        holder.questionContainer.setOnClickListener(v -> {
            // 切换展开状态
            boolean expanded = !item.isExpanded();
            item.setExpanded(expanded);

            // 显示/隐藏答案文本
            holder.answerText.setVisibility(expanded ? View.VISIBLE : View.GONE);

            // 旋转展开图标
            float startRotation = expanded ? 0 : 180;
            float endRotation = expanded ? 180 : 0;

            Animation rotation = new RotateAnimation(startRotation, endRotation,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotation.setDuration(ROTATE_DURATION);
            rotation.setFillAfter(true);
            holder.expandIcon.startAnimation(rotation);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        final View questionContainer;
        final TextView questionText;
        final TextView answerText;
        final ImageView expandIcon;

        FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            questionContainer = itemView.findViewById(R.id.question_container);
            questionText = itemView.findViewById(R.id.question_text);
            answerText = itemView.findViewById(R.id.answer_text);
            expandIcon = itemView.findViewById(R.id.expand_icon);
        }
    }
}