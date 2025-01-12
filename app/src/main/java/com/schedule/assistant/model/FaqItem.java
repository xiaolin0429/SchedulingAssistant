package com.schedule.assistant.model;

/**
 * FAQ项目数据模型
 */
public class FaqItem {
    private final String question;
    private final String answer;
    private boolean expanded;

    public FaqItem(String question, String answer) {
        this.question = question;
        this.answer = answer;
        this.expanded = false;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}