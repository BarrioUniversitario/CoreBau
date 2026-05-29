package me.davidml16.baul.effects;

import me.davidml16.baul.effects.implementations.*;
import org.jetbrains.annotations.NotNull;

public enum MachineEffectModel {

    HELIX(new MachineHelixEffect()),
    SPIRAL(new MachineSpiralEffect()),
    SPHERE(new MachineSphereEffect()),
    HEART(new MachineHeartEffect()),
    PULSAR(new MachinePulsarEffect()),
    BEACON(new MachineBeaconEffect()),
    VORTEX(new MachineVortexEffect()),
    RINGS(new MachineRingsEffect()),
    SIMPLE(new MachineSimpleEffect()),
    NONE(new MachineSimpleEffect());

    private final MachineEffect effect;

    MachineEffectModel(@NotNull MachineEffect effect) {
        this.effect = effect;
    }

    @NotNull
    public MachineEffect getEffect() {
        return effect;
    }
}
