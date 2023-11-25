package at.toastiii.audioshutdown.mixin.accessor;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SoundSystem.class)
public interface SoundSystemAccessor {

    @Accessor
    boolean getStarted();

    @Accessor
    SoundEngine getSoundEngine();

    @Invoker
    void invokeStart();
}
