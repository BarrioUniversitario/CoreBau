package dev.blancocl.skin;

import dev.blancocl.api.skin.Skin;
import dev.blancocl.api.skin.SkinSource;
import dev.blancocl.config.PluginConfig;
import dev.blancocl.util.Threading;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal, zero-dependency Mojang lookup client. Uses JDK {@link HttpClient}
 * driven from the virtual-thread pool. Never blocks the main thread.
 */
public final class MojangClient {

    private static final Pattern UUID_FIELD =
            Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
    private static final Pattern TEXTURE_VALUE =
            Pattern.compile("\"name\"\\s*:\\s*\"textures\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"signature\"\\s*:\\s*\"([^\"]+)\"");

    private final Threading threading;
    private final PluginConfig cfg;
    private final HttpClient http;

    public MojangClient(Threading threading, PluginConfig cfg) {
        this.threading = threading;
        this.cfg = cfg;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(cfg.requestTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<Optional<Skin>> fetchByName(String username) {
        return CompletableFuture.supplyAsync(() -> doFetch(username), threading.skinIo());
    }

    private Optional<Skin> doFetch(String username) {
        try {
            String uuid = resolveUuid(username);
            if (uuid == null) return Optional.empty();
            String profileJson = httpGet(cfg.mojangProfileBase() + "/" + uuid + "?unsigned=false");
            if (profileJson == null) return Optional.empty();
            Matcher m = TEXTURE_VALUE.matcher(profileJson);
            if (!m.find()) return Optional.empty();
            return Optional.of(new Skin(username, m.group(1), m.group(2),
                    SkinSource.MOJANG, System.currentTimeMillis()));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    private String resolveUuid(String username) {
        String body = httpGet(cfg.mojangUsernameBase() + "/" + username);
        if (body == null) return null;
        Matcher m = UUID_FIELD.matcher(body);
        return m.find() ? m.group(1) : null;
    }

    private String httpGet(String url) {
        int retries = Math.max(0, cfg.maxRetries());
        Throwable last = null;
        for (int i = 0; i <= retries; i++) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(url))
                        .timeout(Duration.ofMillis(cfg.requestTimeoutMs()))
                        .header("User-Agent", "NpcPlugin/1.0 (+https://github.com/davidml16)")
                        .build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) return resp.body();
                if (resp.statusCode() == 204 || resp.statusCode() == 404) return null;
                if (resp.statusCode() == 429) {
                    Thread.sleep(500L * (i + 1));
                    continue;
                }
                return null;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Throwable t) {
                last = t;
            }
        }
        return null;
    }
}
