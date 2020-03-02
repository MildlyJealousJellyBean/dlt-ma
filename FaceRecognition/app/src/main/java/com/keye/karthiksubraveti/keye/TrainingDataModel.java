package com.keye.karthiksubraveti.keye;

import java.io.Serializable;
import java.util.ArrayList;

public class TrainingDataModel implements Serializable {
    long ID;
    String name;
    String audioLabel;
    ArrayList<String> imageList;

    TrainingDataModel() {
        ID = -1;
        name = "";
        audioLabel = "";
        imageList = new ArrayList<>();
    }
    void setID(long id_) {
        ID = id_;
    }
    void setName(String name_) {
        name = name_;
    }
    void setAudioLabel(String audioLabel_) {
        audioLabel = audioLabel_;
    }
    void setImageList(ArrayList<String> imageList_) {
        imageList = imageList_;
    }

    void clear() {
        name = "";
        audioLabel = "";
        imageList.clear();
    }
}
