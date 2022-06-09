package entities;

import services.Constants;
import services.MonsterLoader;

import java.util.Arrays;

public class Monster {
    
    int id;
    int offset;
    
    int attack;
    int defense;
    int speed;
    int magic;
    int hp;
    String attackMagicValue;
    String defenseMagicValue;
    int xp;
    int gold;
    
    byte[] bytes;

    String[] attackMagic;
    String[] defenseMagic;
    
    public Monster(byte[] data) {
        int i=0;
        attack = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_ATTACK);
        defense = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_DEFENSE);
        speed = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_SPEED);
        magic = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_MAGIC);
        hp = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_HP);
        attackMagicValue = MonsterLoader.getStat(data, Constants.MONSTER_STATS_INDEX_M_ATTACK);
        defenseMagicValue = MonsterLoader.getStat(data, Constants.MONSTER_STATS_INDEX_M_DEFENSE);
        xp = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_XP);
        gold = MonsterLoader.getIntStat(data, Constants.MONSTER_STATS_INDEX_GOLD);
        bytes = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public String getAttackMagicValue() {
        return attackMagicValue;
    }

    public void setAttackMagicValue(String attackMagicValue) {
        this.attackMagicValue = attackMagicValue;
    }

    public String getDefenseMagicValue() {
        return defenseMagicValue;
    }

    public void setDefenseMagicValue(String defenseMagicValue) {
        this.defenseMagicValue = defenseMagicValue;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public String[] getAttackMagic() {
        return attackMagic;
    }

    public void setAttackMagic(String[] attackMagic) {
        this.attackMagic = attackMagic;
    }

    public String[] getDefenseMagic() {
        return defenseMagic;
    }

    public void setDefenseMagic(String[] defenseMagic) {
        this.defenseMagic = defenseMagic;
    }

    @Override
    public String toString() {
        return "Monster{" +
                "id=" + id +
                ", offset=" + offset +
                ", attack=" + attack +
                ", defense=" + defense +
                ", speed=" + speed +
                ", magic=" + magic +
                ", hp=" + hp +
                ", attackMagic='" + attackMagicValue + '\'' +
                ", defenseMagic='" + defenseMagicValue + '\'' +
                ", xp=" + xp +
                ", gold=" + gold +
                ", attackMagic=" + Arrays.toString(attackMagic) +
                ", defenseMagic=" + Arrays.toString(defenseMagic) +
                '}';
    }
}
