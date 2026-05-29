package dev.blancocl.services;

import dev.blancocl.ServiceContainer;
import dev.blancocl.api.NpcAPI;
import dev.blancocl.api.manager.HologramManager;
import dev.blancocl.api.manager.NpcManager;
import dev.blancocl.api.manager.SkinManager;
import dev.blancocl.api.manager.VelocityManager;

/** {@link NpcAPI} that simply forwards to the {@link ServiceContainer}. */
public final class NpcApiImpl implements NpcAPI {

    private final ServiceContainer services;

    public NpcApiImpl(ServiceContainer services) { this.services = services; }

    @Override public NpcManager       npcs()      { return services.npcs(); }
    @Override public HologramManager  holograms() { return services.holograms(); }
    @Override public SkinManager      skins()     { return services.skins(); }
    @Override public VelocityManager  velocity()  { return services.velocity(); }
}
