package com.penguincrushers;

import lombok.Getter;

@Getter
public enum CrossingSoundEffect
{
    LOW_ALCHEMY("Low Alchemy", 98),
    ENCHANT_DIAMOND_AMULET("Enchant Diamond Amulet", 137),
    BONES_TO_BANANAS("Bones to Bananas", 114),
    SMITE("Smite", 2686),
    OOKS("Ooks", 1679),
    TELEBLOCK_IMPACT("Teleblock Impact", 203),
    BIND_CAST("Bind Cast", 101),
    GOBLIN_DEATH("Goblin Death", 471),
    NAILBEAST_ATTACK("Nailbeast Attack", 3482),
    ARMADYL_EYE("Armadyl Eye", 3892);

    private final String name;
    private final int soundEffectId;

    CrossingSoundEffect(String name, int soundEffectId)
    {
        this.name = name;
        this.soundEffectId = soundEffectId;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
