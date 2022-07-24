package eu.darkbot.ter.dks.types.remotestats;

import com.github.manolo8.darkbot.Main;
import com.google.gson.Gson;
import eu.darkbot.api.managers.BackpageAPI;

public class UserDataDTO {
    private String instance;
    private String sid;

    public UserDataDTO(BackpageAPI backpage) {
        this.instance = backpage.getInstanceURI().toString();
        this.sid = backpage.getSid();
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
