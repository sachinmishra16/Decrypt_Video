package com.blueapple.testing_encryption.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VideoDetails
{
    public static String token="blOe&^8Hall^wTue";

    private String concept_sub_path;

    public String getConcept_sub_path() {
        return concept_sub_path;
    }

    public void setConcept_sub_path(String concept_sub_path) {
        this.concept_sub_path = concept_sub_path;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        VideoDetails.token = token;
    }

    @SerializedName("video")
    @Expose
    private String video;
    @SerializedName("pic")
    @Expose
    private String pic;

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
