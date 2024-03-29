package eu.darkbot.ter.dks.api;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.HangarManager;
import com.github.manolo8.darkbot.backpage.hangar.EquippableItem;
import com.github.manolo8.darkbot.backpage.hangar.Hangar;
import com.github.manolo8.darkbot.backpage.hangar.HangarResponse;
import com.github.manolo8.darkbot.backpage.hangar.ItemInfo;
import eu.darkbot.ter.dks.api.hangar.HeroItem;
import eu.darkbot.ter.dks.api.hangar.LocalizationID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HangarAPI {
    protected final HangarManager hangar;
    protected HangarResponse currentHangar;

    protected List<Hangar> hangars;
    protected List<EquippableItem> equippableItems;
    protected List<ItemInfo> itemInfos;
    protected Map<String, HeroItem> items;
    protected long lastUpdate = 0L;
    protected long UPDATE_MINIMUM_SECONDS = 5000L;

    public HangarAPI (final @NotNull Main main) {
        this.hangar = main.backpage.hangarManager;
        this.items = new HashMap<>();
    }

    public HangarAPI (final @NotNull Main main, final long updateSeconds) {
        this.hangar = main.backpage.hangarManager;
        this.items = new HashMap<>();
        this.UPDATE_MINIMUM_SECONDS = updateSeconds;
    }

    public boolean shouldUpdate() {
        long now = System.currentTimeMillis();
        return now - this.lastUpdate > UPDATE_MINIMUM_SECONDS;
    }

    public boolean updateCurrentHangar() {
        try {
            if (!this.hasAnyData() || this.shouldUpdate()) {
                this.hangar.updateCurrentHangar();
                this.currentHangar = this.hangar.getCurrentHangar();
                this.hangars = this.currentHangar.getData().getRet().getHangars();
                this.equippableItems = this.currentHangar.getData().getRet().getItems();
                this.itemInfos = this.currentHangar.getData().getRet().getItemInfos();
                this.items.clear();
                for (ItemInfo item : this.itemInfos) {
                    this.items.put(item.getLocalizationId(), new HeroItem(this.getEquippableItemFromItemInfo(item), item));
                }
                this.lastUpdate = System.currentTimeMillis();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getLastUpdate() {
        return this.lastUpdate;
    }

    public boolean hasAnyData() {
        return this.currentHangar != null && !this.items.isEmpty();
    }

    public HeroItem getItem(String localization) {
        return this.items.get(localization);
    }

    public HeroItem getItem(@NotNull LocalizationID localization) {
        return this.getItem(localization.getLocalizationID());
    }

    public boolean hasItem(String localization) {
        return this.items.containsKey(localization);
    }

    public boolean hasItem(@NotNull LocalizationID localization) {
        return this.hasItem(localization.getLocalizationID());
    }

    private @Nullable EquippableItem getEquippableItemFromItemInfo(ItemInfo info) {
        if (this.equippableItems != null) {
            return this.equippableItems
                    .stream()
                    .filter(item -> item.getLootId() == info.getLootId())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
