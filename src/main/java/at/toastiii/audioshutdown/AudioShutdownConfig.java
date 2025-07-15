package at.toastiii.audioshutdown;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioShutdownConfig {

    public boolean pauseOnServers = false;

    public static AudioShutdownConfig load() {
        Path resolve = FabricLoader.getInstance().getConfigDir().resolve("audio-shutdown.json");

        Gson gson = new Gson();

        if(!Files.exists(resolve)) {
            try {
                Files.createDirectories(FabricLoader.getInstance().getConfigDir());
                AudioShutdownConfig config = new AudioShutdownConfig();
                Files.createFile(resolve);
                Files.writeString(resolve, gson.toJson(config));
                return config;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            String json = Files.readString(resolve);
            return gson.fromJson(json, AudioShutdownConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read audio shutdown config", e);
        }
    }

}
