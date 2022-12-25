package at.toastiii.audioshutdown.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {

    @Unique
    private boolean hasStoppedBecauseOfAudioShutdown = false;
    private long lastAudioStartTime = -1;

    @Shadow private boolean started;

    @Shadow protected abstract void start();

    @Shadow public abstract void stop();

    @Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

    @Inject(method = "tick(Z)V", at = @At("HEAD"), cancellable = true)
    public void onTick(boolean b, CallbackInfo ci) {
        //When sound engine is started, and there are no sounds playing and the last started sound was 5 seconds ago OR if the minecraft window is not focused, and the game is paused SHUT THE THING DOWN
        if(started && ((System.currentTimeMillis()-lastAudioStartTime > 5000 && sources.isEmpty()) || !MinecraftClient.getInstance().isWindowFocused()) && isGamePaused()) {
            hasStoppedBecauseOfAudioShutdown = true;
            stop();
            LogManager.getLogger().info("Shutting down audio because of inactivity");
            ci.cancel();
        }
    }

    @Unique
    private boolean isGamePaused() {
        return MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().isPaused();
    }

    @Inject(method = "shouldReloadSounds", at = @At("HEAD"), cancellable = true)
    public void shouldReloadSounds(CallbackInfoReturnable<Boolean> ci) {
        if(hasStoppedBecauseOfAudioShutdown) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    public void onPlay(SoundInstance instance, CallbackInfo ci) {
        if((hasStoppedBecauseOfAudioShutdown && !started) && (MinecraftClient.getInstance().isWindowFocused() || !isGamePaused())) {
            hasStoppedBecauseOfAudioShutdown = false;
            LogManager.getLogger().info("Enabling sound system because of audio activity(and the game was either unpaused or focused)");
            start();
        }
        lastAudioStartTime = System.currentTimeMillis();
    }


}
