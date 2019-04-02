package org.sr3u.vkDynCover;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class VKAPI {

    private final Configuration.Group group;
    public final String GET_UPLOAD_SERVER_URL;

    public VKAPI(Configuration.Group group) {
        this.group = group;
        GET_UPLOAD_SERVER_URL = "https://api.vk.com/method/photos.getOwnerCoverPhotoUploadServer?group_id=" + group.getId() + "&crop_x=0&crop_y=0&crop_x2=1590&crop_y2=400&access_token=" + group.getToken() + "&v=5.64";
    }

    public String getUploadServer() throws IOException {
        return REST.get(GET_UPLOAD_SERVER_URL);
    }

    public String setCover(File file, String uploadURL) throws IOException {
        System.out.println("Setting cover to: " + file);
        String postString = REST.post(uploadURL, file);
        JSONObject post = new JSONObject(postString);
        if (post.has("error")) {
            return postString;
        }
        String photoId = post.getString("photo");
        String hash = post.getString("hash");
        String get = REST.get(savePhotoURL(photoId, hash));
        return get;
    }

    private String savePhotoURL(String photoId, String hash) {
        return "https://api.vk.com/method/photos.saveOwnerCoverPhoto?hash=" + hash + "&photo=" + photoId + "&access_token=" + group.getToken() + "&v=5.65";
    }
}
