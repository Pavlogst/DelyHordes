package com.sweityuo.delyHordes.dates;

public class WaveData {
    private final String mobId;
    private final int amount;

    public WaveData(String mobId, int amount) {
        this.mobId = mobId;
        this.amount = amount;
    }

    public String getMobId() {
        return mobId;
    }

    public int getAmount() {
        return amount;
    }
}
