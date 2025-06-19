package net.omni.nearChat.handlers;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.omni.nearChat.NearChatPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LibraryHandler {
    private final NearChatPlugin plugin;

    private final BukkitLibraryManager libraryManager;


    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    public LibraryHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.libraryManager = new BukkitLibraryManager(plugin);
    }

    public void loadSQLiteLibraries() {
        executor.submit(() -> {
            Library sqlite = Library.builder()
                    .groupId("org{}xerial")
                    .artifactId("sqlite-jdbc")
                    .version("3.49.1.0")
                    .relocate("org{}sqlite", "net{}omni{}nearChat{}libs{}org{}xerial")
                    .build();

            libraryManager.loadLibrary(sqlite);

            plugin.sendConsole("Loaded SQLite library..."); // TODO messages.yml
        });
    }

    public void loadPostgresLib() {
        executor.submit(() -> {
            ensureHikari();

            Library postgres = Library.builder()
                    .groupId("org{}postgresql")
                    .artifactId("postgresql")
                    .version("42.7.5")
                    .relocate("org{}postgresql", "net{}omni{}nearChat{}libs{}org{}postgresql")
                    .build();

            libraryManager.loadLibrary(postgres);

            plugin.sendConsole("Loaded PostgreSQL libraries..."); // TODO messages.yml
        });
    }

    public void loadRedisLib() {
        // TODO fix. load before instantiating RedisAdapter on DatabaseHandler

        executor.submit(() -> {
            Library lettuce = Library.builder()
                    .groupId("io{}lettuce")
                    .artifactId("lettuce-core")
                    .version("6.6.0.RELEASE")
                    .id("AlessioDP")
                    .repository("https://repo.alessiodp.com/releases/")
                    // Sets an id for the library
                    // Relocation is applied to the downloaded jar before loading it
                    .relocate("io{}lettuce{}core", "net{}omni{}nearChat{}libs{}io{}lettuce{}core")
                    .build();

            Library reactive_streams = Library.builder()
                    .groupId("org{}reactivestreams")
                    .artifactId("reactive-streams")
                    .version("1.0.4")
                    .build();

            Library reactor_core = Library.builder()
                    .groupId("io{}projectreactor")
                    .artifactId("reactor-core")
                    .version("3.6.6")
                    .build();

            libraryManager.loadLibrary(reactor_core);
            libraryManager.loadLibrary(reactive_streams);
            libraryManager.loadLibrary(lettuce);

            plugin.sendConsole("Loaded Redis libraries..."); // TODO messages.yml
        });
    }

    public void ensureMainLibraries() {
        libraryManager.addMavenCentral();

        plugin.sendConsole("&aLoaded main libraries"); // TODO messages.yml
    }

    // use hikari connection pool when dealing with SQL databases
    public void ensureHikari() {
        executor.submit(() -> {
            Library hikaricp = Library.builder()
                    .groupId("com{}zaxxer")
                    .artifactId("HikariCP")
                    .version("6.3.0")
                    .relocate("com{}zaxxer{}hikari", "net{}omni{}nearChat{}libs{}com{}zaxxer{}hikari")
                    .build();

            libraryManager.loadLibrary(hikaricp);

            plugin.sendConsole("&aLoaded HikariCP"); // TODO messages.yml
        });
    }

    public void stopExecutor() {
        executor.shutdownNow();
    }
}
