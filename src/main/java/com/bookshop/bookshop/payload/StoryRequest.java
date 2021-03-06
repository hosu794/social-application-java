package com.bookshop.bookshop.payload;

import com.bookshop.bookshop.model.Love;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class StoryRequest {

    private String title;

    private String body;

    private String description;

    private boolean premiumContent;

    public StoryRequest(String title, String body, String description) {
        this.title = title;
        this.body = body;
        this.description = description;
    }

    public StoryRequest() {}

    public StoryRequest(String title, String body, String description, boolean premiumContent) {
        this.title = title;
        this.body = body;
        this.description = description;
        this.premiumContent = premiumContent;
    }

    public boolean isPremiumContent() {
        return premiumContent;
    }

    public void setPremiumContent(boolean premiumContent) {
        this.premiumContent = premiumContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
