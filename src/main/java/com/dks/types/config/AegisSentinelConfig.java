package com.dks.types.config;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.config.types.Tag;
import com.github.manolo8.darkbot.config.types.TagDefault;

public class AegisSentinelConfig {
    @Option (value = "Master TAG", description = "Medium priority. He'll follow every ship with that tag")
    @Tag(TagDefault.ALL)
    public PlayerTag MASTER_TAG = null;

    @Option(value = "Stay in group", description = "Should the sentinel stay in group with master?")
    public boolean stayInGroup = true;

    @Option(value = "Send group requests", description = "Should the sentinel send group requests to master?")
    public boolean sendGroup = true;

    @Option(value = "Accept group requests", description = "Should the sentinel accept group requests from master?")
    public boolean acceptGroup = true;

    @Option(value = "Help master", description = "Should the sentinel help master with NPC?")
    public boolean shouldAttack = true;

    @Option(value = "HP Repair %", description = "% of master's HP when use HP Repair ability")
    @Num(min = 5, max = 95)
    public int hpRepairPercentage = 80;

    @Option(value = "Shield Repair %", description = "% of master's SHD when use Shield Repair ability")
    @Num(min = 5, max = 95)
    public int shdRepairPercentage = 80;

    @Option(value = "Repair Pod %", description = "% of master's HP when use Repair Pod ability")
    @Num(min = 5, max = 95)
    public int repairPodPercentage = 80;

    @Option(value = "Optimize Healing", description = "The bot will choose when repair")
    public boolean optimize = true;
}