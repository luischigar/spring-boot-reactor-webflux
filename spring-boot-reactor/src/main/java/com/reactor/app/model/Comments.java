package com.reactor.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comments {
    private List<String> comments =new ArrayList<>();
    public void addComments(String comment){
        this.comments.add(comment);
    }
}
