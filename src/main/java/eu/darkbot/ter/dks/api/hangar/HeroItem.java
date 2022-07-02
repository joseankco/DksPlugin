package eu.darkbot.ter.dks.api.hangar;

import com.github.manolo8.darkbot.backpage.hangar.EquippableItem;
import com.github.manolo8.darkbot.backpage.hangar.ItemInfo;
import com.google.gson.annotations.SerializedName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HeroItem {

    private EquippableItem item;
    private ItemInfo info;

    public HeroItem(EquippableItem item, ItemInfo info) {
        this.item = item;
        this.info = info;
    }

    // ItemInfo
    public String getName() {
        return this.info.getName();
    }

    public String getLocalizationId() {
        return this.info.getLocalizationId();
    }

    public void setLocalizationId(String localizationId) {
        this.info.setLocalizationId(localizationId);
    }

    public String getCategory() {
        return this.info.getCategory();
    }

    public List<Map<String, Object>> getLevels() {
        return this.info.getLevels();
    }

    // EquippableItem
    public Map<String, String> getProperties() {
        return this.item.getProperties();
    }

    public Integer getQuantity() {
        return this.item.getQuantity();
    }

    public String getEquippedHangar() {
        return this.item.getEquippedHangar();
    }

    public String getEquippedConfig() {
        return this.item.getEquippedConfig();
    }

    public String getEquippedTarget() {
        return this.item.getEquippedTarget();
    }

    public int getShieldLevel() {
        return this.item.getShieldLevel();
    }

    public int getDamageLevel() {
        return this.item.getDamageLevel();
    }
}
