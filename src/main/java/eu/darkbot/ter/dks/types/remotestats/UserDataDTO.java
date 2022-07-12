package eu.darkbot.ter.dks.types.remotestats;

import com.github.manolo8.darkbot.Main;
import com.google.gson.Gson;

public class UserDataDTO {
    private String instance;
    private String sid;

    public UserDataDTO(Main main) {
        this.instance = main.backpage.getInstanceURI().toString();
        this.sid = main.backpage.getSid();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
