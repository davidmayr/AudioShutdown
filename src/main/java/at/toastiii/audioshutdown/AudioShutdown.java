package at.toastiii.audioshutdown;

import at.toastiii.audioshutdown.mixin.accessor.SoundEngineAccessor;
import at.toastiii.audioshutdown.mixin.accessor.SoundSystemAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundSystem;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.SOFTPauseDevice;

public class AudioShutdown {

    public boolean hasStoppedBecauseOfAudioShutdown = false;
    public long lastAudioStartTime = -1;

    private boolean isEnginePaused = false;
    private final SoundSystem system;

    public boolean shutdownBecauseOfLowAudioVolume = false;

    private long capabilityCheckHandle = -1;
    private boolean isCapableOfAudioPause = false;

    public AudioShutdown(SoundSystem system) {
        this.system = system;
    }


    public void stopOrPauseEngine() {
        hasStoppedBecauseOfAudioShutdown = true;
        if(shutdownBecauseOfLowAudioVolume) {
            LogManager.getLogger().info("Shutting down audio because of silent audio volume");
        } else {
            LogManager.getLogger().info("Shutting down audio because of inactivity");
        }

        SoundEngine engine = ((SoundSystemAccessor)system).getSoundEngine();
        SoundEngineAccessor accessor = (SoundEngineAccessor)engine;
        if (capabilityCheckHandle != accessor.getDevicePointer()) {
            capabilityCheckHandle = accessor.getDevicePointer();
            isCapableOfAudioPause = ALC.createCapabilities(accessor.getDevicePointer()).ALC_SOFT_pause_device;

            LogManager.getLogger().info("Audio pause capability: {}", isCapableOfAudioPause);
        }


        if(isCapableOfAudioPause) {
            SOFTPauseDevice.alcDevicePauseSOFT(accessor.getDevicePointer());
            this.isEnginePaused = true;
        } else {
            system.stop();
        }
    }

    public void startOrResumeEngine() {
        hasStoppedBecauseOfAudioShutdown = false;
        LogManager.getLogger().info("Enabling sound system because of audio activity(and the game was either unpaused or focused)");

        SoundEngine engine = ((SoundSystemAccessor)system).getSoundEngine();
        SoundEngineAccessor accessor = (SoundEngineAccessor)engine;


        if (isCapableOfAudioPause && isEnginePaused) {
            SOFTPauseDevice.alcDeviceResumeSOFT(accessor.getDevicePointer());
            this.isEnginePaused = false;
        } else if(!((SoundSystemAccessor) system).getStarted()) {
            system.stop();
        }

    }

    public boolean isEngineRunning() {
        return ((SoundSystemAccessor)system).getStarted() && !isEnginePaused;
    }

    public boolean isGamePaused() {
        return MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().isPaused();
    }


}
