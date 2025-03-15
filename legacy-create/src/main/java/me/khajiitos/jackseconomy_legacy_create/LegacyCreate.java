package me.khajiitos.jackseconomy_legacy_create;

import me.khajiitos.jackseconomy.create.CreateCheck;
import me.khajiitos.jackseconomy.create.CreateHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(LegacyCreate.MOD_ID)
public class LegacyCreate {
    public static final String MOD_ID = "jackseconomy_legacy_create";

    public LegacyCreate() {
        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> LegacyCreateClient::init);

        if (CreateCheck.isLegacyInstalled()) {
            CreateStressProvider.init();

            CreateHelper.addProvider(LegacyCreateHelper::provideValues);
        }
    }
}
