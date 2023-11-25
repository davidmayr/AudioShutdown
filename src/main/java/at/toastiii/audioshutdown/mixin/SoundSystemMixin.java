package at.toastiii.audioshutdown.mixin;

import at.toastiii.audioshutdown.AudioShutdown;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {

    @Unique
    private final AudioShutdown audioShutdown = new AudioShutdown((SoundSystem) (Object) this);

    @Shadow @Final private Map<SoundInstance, Channel.SourceManager> sources;

    @Inject(method = "tick(Z)V", at = @At("HEAD"), cancellable = true)
    public void onTick(boolean b, CallbackInfo ci) {
        if(MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER) <= 0) {
            audioShutdown.shutdownBecauseOfLowAudioVolume = true;
            if(audioShutdown.isEngineRunning()) {
                audioShutdown.stopOrPauseEngine();
            }
            ci.cancel();
            return;
        } else if(audioShutdown.shutdownBecauseOfLowAudioVolume) {
            audioShutdown.shutdownBecauseOfLowAudioVolume = false;
            audioShutdown.startOrResumeEngine();
            ci.cancel();
            return;
        }

        //When sound engine is started, and there are no sounds playing and the last started sound was 5 seconds ago OR if the minecraft window is not focused, and the game is paused SHUT THE THING DOWN
        if(audioShutdown.isEngineRunning() && ((System.currentTimeMillis()-audioShutdown.lastAudioStartTime > 5000 && sources.isEmpty()) || !MinecraftClient.getInstance().isWindowFocused()) && audioShutdown.isGamePaused()) {
            audioShutdown.stopOrPauseEngine();

            ci.cancel();
            return;
        }

        //If sounds should be playing and the engine was paused because of window un-focus, restart it if focused again
        if(!sources.isEmpty() && !audioShutdown.isEngineRunning() && MinecraftClient.getInstance().isWindowFocused() && !audioShutdown.shutdownBecauseOfLowAudioVolume) {
            audioShutdown.startOrResumeEngine();
            ci.cancel();
        }
    }


    @Inject(method = "shouldReloadSounds", at = @At("HEAD"), cancellable = true)
    public void shouldReloadSounds(CallbackInfoReturnable<Boolean> ci) {
        if(audioShutdown.hasStoppedBecauseOfAudioShutdown) {
            ci.setReturnValue(false);
        }
    }

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    public void onPlay(SoundInstance instance, CallbackInfo ci) {
        if((audioShutdown.hasStoppedBecauseOfAudioShutdown && !audioShutdown.isEngineRunning()) && (MinecraftClient.getInstance().isWindowFocused() || !audioShutdown.isGamePaused())) {
            if(!audioShutdown.hasStoppedBecauseOfAudioShutdown) {
                audioShutdown.startOrResumeEngine();
            }

        }
        audioShutdown.lastAudioStartTime = System.currentTimeMillis();
    }


}
