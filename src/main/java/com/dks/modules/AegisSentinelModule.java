package com.dks.modules;

import com.dks.types.VerifierChecker;
import com.dks.types.config.AegisSentinelConfig;
import com.github.manolo8.darkbot.Main;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Module;
import eu.darkbot.api.extensions.InstructionProvider;
import eu.darkbot.api.game.entities.Entity;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.group.MemberInfo;
import eu.darkbot.api.game.items.Item;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.other.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.api.utils.Inject;
import eu.darkbot.shared.modules.MapModule;
import eu.darkbot.shared.utils.SafetyFinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Feature(name = "Aegis Sentinel", description = "Follow, help and heal the master")
public class AegisSentinelModule implements Module, Configurable<AegisSentinelConfig>, InstructionProvider {
    protected PluginAPI api;
    protected HeroAPI heroapi;
    protected BotAPI bot;
    protected MovementAPI movement;
    protected AttackAPI attacker;
    protected StarSystemAPI starSystem;
    protected PetAPI pet;
    protected GroupAPI group;
    protected HeroItemsAPI items;
    protected ConfigSetting<Integer> workingMap;

    protected Collection<? extends Portal> portals;
    protected Collection<? extends Ship> ships;
    protected Collection<? extends Player> players;
    protected Collection<? extends Npc> npcs;

    private AegisSentinelConfig sConfig;
    private final Main main;
    private final SafetyFinder safety;
    private State currentStatus;

    private Player master;
    private int hpRepairTotal = 280000;
    private int hpRepairPodTotal = 180000;
    private int shdRepairTotal = 125000;
    private int rangeToUseAbility = 500;

    private enum State {
        INIT("Init"),
        WAIT("Waiting for group invitation"),
        WAIT_GROUP_LOADING("Waiting while loading the group"),
        WAIT_MASTER_ATTACK("Waiting for master to attack"),
        TRAVELLING_TO_MASTER("Travelling to the master's map"),
        FOLLOWING_MASTER("Following the master"),
        NEAR_MASTER("Near master"),
        HELPING_MASTER("Helping the master");

        private final String message;

        State(String message) {
            this.message = message;
        }
    }

    @Override
    public void setConfig(ConfigSetting<AegisSentinelConfig> arg0) {
        this.sConfig = arg0.getValue();
    }

    public AegisSentinelModule(Main main, PluginAPI api) {
        this(main, api, api.requireAPI(AuthAPI.class), api.requireInstance(SafetyFinder.class));
    }

    @Inject
    public AegisSentinelModule(Main main, PluginAPI api, AuthAPI auth, SafetyFinder safety) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.main = main;
        this.currentStatus = State.INIT;
        this.api = api;
        this.bot = api.getAPI(BotAPI.class);
        this.heroapi = api.getAPI(HeroAPI.class);
        this.movement = api.getAPI(MovementAPI.class);
        this.attacker = api.getAPI(AttackAPI.class);
        this.starSystem = api.getAPI(StarSystemAPI.class);
        this.pet = api.getAPI(PetAPI.class);
        this.group = api.getAPI(GroupAPI.class);
        this.safety = safety;
        this.items = api.getAPI(HeroItemsAPI.class);

        EntitiesAPI entities = api.getAPI(EntitiesAPI.class);
        this.portals = entities.getPortals();
        this.ships = entities.getShips();
        this.players = entities.getPlayers();
        this.npcs = entities.getNpcs();
    }

    @Override
    public boolean canRefresh() {
        return safety.tick();
    }

    @Override
    public String getStatus() {
        return safety.state() != SafetyFinder.Escaping.NONE ? safety.status() : currentStatus.message;
    }

    @Override
    public String instructions() {
        return "Aegis Sentinel Module: \n" +
            "Master Ship should invite its Sentinel \n" +
            "Sentinel will follow the master \n" +
            "Sentinel will heal the master with its abilities \n";
    }

    // TODO always lock master (no need to group)
    // TODO check master every X seconds if not in group
    // TODO check group accept
    @Override
    public void onTickModule() {
        if (!group.hasGroup()) {
            currentStatus = State.WAIT;
            acceptOrInviteMaster();
        }
        if (isMasterInRange()) {
            currentStatus = State.NEAR_MASTER;
            if (group.hasGroup() && !sConfig.stayInGroup) {
                leaveGroup();
            }
            goToLocation(master.getLocationInfo().getCurrent());
            boolean isHealing = checkAndHealMaster();
            if (sConfig.shouldAttack) {
                if (isMasterAttacking()) {
                    attackMasterTarget();
                    currentStatus = State.HELPING_MASTER;
                } else {
                    heroapi.setRoamMode();
                    currentStatus = State.WAIT_MASTER_ATTACK;
                }
            }
        } else if (group.hasGroup()) {
            heroapi.setRunMode();
            goToMasterInGroup();
        }
    }

    private GroupMember getMasterFromGroupMembers() {
        for (GroupMember member : group.getMembers()) {
            if (isNotMe(member.getId()) && master.getId() == member.getId()) {
                return member;
            }
        }
        return null;
    }

    private boolean checkAndDoShdRepair() {
        GroupMember gmaster = getMasterFromGroupMembers();
        if (gmaster != null) {
            MemberInfo masterInfo = gmaster.getMemberInfo();
            double percentage = masterInfo.shieldPercent();
            int actual = masterInfo.getShield();
            int max = masterInfo.getMaxShield();

            boolean shouldHeal = sConfig.optimize ?
                    (max - actual) >= shdRepairTotal :
                    (percentage * 100) <= sConfig.shdRepairPercentage;

            if (shouldHeal) {
                return useSentinelAbilityOnMaster(SelectableItem.Ability.AEGIS_SHIELD_REPAIR, true);
            }
        }
        return false;
    }

    private boolean checkAndDoHpRepair() {
        GroupMember gmaster = getMasterFromGroupMembers();
        if (gmaster != null) {
            MemberInfo masterInfo = gmaster.getMemberInfo();
            double percentage = masterInfo.hpPercent();
            int actual = masterInfo.getHp();
            int max = masterInfo.getMaxHp();

            boolean shouldHeal = sConfig.optimize ?
                    (max - actual) >= hpRepairTotal :
                    (percentage * 100) <= sConfig.hpRepairPercentage;

            if (shouldHeal) {
                return useSentinelAbilityOnMaster(SelectableItem.Ability.AEGIS_HP_REPAIR, true);
            }
        }
        return false;
    }

    private boolean checkAndDoHpPodRepair() {
        GroupMember gmaster = getMasterFromGroupMembers();
        if (gmaster != null) {
            MemberInfo masterInfo = gmaster.getMemberInfo();
            double percentage = masterInfo.hpPercent();
            int actual = masterInfo.getHp();
            int max = masterInfo.getMaxHp();

            boolean shouldHeal = sConfig.optimize ?
                    (max - actual) >= hpRepairPodTotal :
                    (percentage * 100) <= sConfig.repairPodPercentage;

            if (shouldHeal) {
                return useSentinelAbilityOnMaster(SelectableItem.Ability.AEGIS_REPAIR_POD, false);
            }
        }
        return false;
    }

    private boolean tryLockMaster() {
        if (master != null) {
            heroapi.setLocalTarget(master);
            return master.trySelect(false);
        }
        return false;
    }

    private boolean useSentinelAbilityOnMaster(SelectableItem item, boolean lockMaster) {
        Optional<Item> hpRepairItem = items.getItem(item, ItemFlag.READY, ItemFlag.USABLE);
        if (hpRepairItem.isPresent()) {
            if (lockMaster && tryLockMaster()) {
                return items.useItem(item).isSuccessful();
            }
        }
        return false;
    }

    private boolean checkAndHealMaster() {
        if (master != null) {
            boolean healed = checkAndDoShdRepair();
            if (checkAndDoHpRepair()) {
                healed = true;
            }
            if (checkAndDoHpPodRepair()) {
                healed = true;
            }
            return healed;
        }
        return false;
    }

    private boolean isMasterAttacking() {
        return master != null && master.isAttacking() && master.getTarget() != null;
    }

    private boolean isSentinelAttacking() {
        return heroapi.isAttacking() && heroapi.getTarget() != null;
    }

    private void attackNpcTarget(Entity target) {
        if (target instanceof Npc) {
            attacker.setTarget((Npc) target);
            heroapi.setAttackMode((Npc) target);
            attacker.tryLockAndAttack();
        }
    }

    private Npc getMasterTargetAsNPC() {
        return npcs.stream()
            .filter(s -> master.isAttacking(s))
            .findFirst().orElse(null);
    }

    private Npc getSentinelTargetAsNPC() {
        return npcs.stream()
            .filter(s -> heroapi.isAttacking(s))
            .findFirst().orElse(null);
    }

    private void attackMasterTarget() {
        Npc masterTarget = getMasterTargetAsNPC();
        /*
        if (false && isSentinelAttacking()) {
            Npc sentinelTarget = getSentinelTargetAsNPC();
            if (masterTarget.getId() != sentinelTarget.getId()) {
                attackNpcTarget(masterTarget);
            }
        } else {
            attackNpcTarget(masterTarget);
        }
        */
        attackNpcTarget(masterTarget);
    }

    private void acceptOrInviteMaster() {
        if (sConfig.acceptGroup) {
            main.guiManager.group.invites.stream()
                .filter(in -> in.isIncoming() && hasMasterTag(in.getInviter().getId()))
                .findFirst()
                .ifPresent(inv -> main.guiManager.group.acceptInvite(inv));
        }
    }

    private void leaveGroup() {
        // TODO
    }

    private boolean isMasterInRange() {
        if (!players.isEmpty()) {
            master = players.stream()
                .filter(ship -> ship.isValid() && isNotMe(ship.getId()))
                .filter(ship -> hasMasterTag(ship.getId()))
                .findAny().orElse(null);
            return master != null;
        }
        return false;
    }

    protected GameMap getWorkingMap() {
        return starSystem.getOrCreateMapById(workingMap.getValue());
    }

    private boolean isNotMe(Integer shipId) {
        return shipId != heroapi.getId();
    }

    private boolean isInSameMap(int mapId) {
        return mapId == heroapi.getMap().getId();
    }

    private boolean hasMasterTag(Integer shipId) {
        return sConfig.MASTER_TAG.has(main.config.PLAYER_INFOS.get(shipId));
    }

    private void goToMap(int mapId) {
        this.bot.setModule(api.requireInstance(MapModule.class)).setTarget(starSystem.getOrCreateMapById(mapId));
    }

    private void goToLocation(Location location) {
        movement.moveTo(location);
    }

    private void goToMasterInGroup() {
        currentStatus = State.WAIT_GROUP_LOADING;
        for (GroupMember member : group.getMembers()) {
            if (isNotMe(member.getId()) && hasMasterTag(member.getId())) {
                if (isInSameMap(member.getMapId())) {
                    goToLocation(member.getLocation());
                    currentStatus = State.FOLLOWING_MASTER;
                } else {
                    goToMap(member.getMapId());
                    currentStatus = State.TRAVELLING_TO_MASTER;
                }
                break;
            }
        }
    }
}
