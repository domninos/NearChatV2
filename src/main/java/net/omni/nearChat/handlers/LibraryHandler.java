package net.omni.nearChat.handlers;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.omni.nearChat.NearChatPlugin;
import net.omni.nearChat.database.NearChatDatabase;
import net.omni.nearChat.util.Libraries;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LibraryHandler {
    private final NearChatPlugin plugin;

    private final BukkitLibraryManager libraryManager;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public LibraryHandler(NearChatPlugin plugin) {
        this.plugin = plugin;
        this.libraryManager = new BukkitLibraryManager(plugin);
    }

    public void loadLibraries(NearChatDatabase.Type type) throws ExecutionException, InterruptedException {
        switch (type) {
            case SQLITE:
                loadSQLiteLibraries().get();
                break;
            case POSTGRESQL:
                loadPostgresLib().get();
                break;
            case REDIS:
                loadRedisLib().get();
                break;
            case FLAT_FILE:
            default:
                break;
        }

        plugin.sendConsole(plugin.getMessageHandler().getLibraryLoaded(type.getLabel()));
    }

    public boolean isLibLoaded(NearChatDatabase.Type type) {
        return type.isLoaded(plugin);
    }

    public boolean isLibLoaded() {
        return plugin.getDatabaseHandler().getAdapter() != null && plugin.getDatabaseHandler().getAdapter().getType().isLoaded(plugin);
    }

    public Future<Boolean> loadSQLiteLibraries() {
        return submitExec(() -> {
            Library sqlite = Library.builder()
                    .groupId("org{}xerial")
                    .artifactId("sqlite-jdbc")
                    .version("3.49.1.0")
                    .relocate("org{}sqlite", "net{}omni{}nearChat{}libs{}org{}xerial")
                    .build();

            libraryManager.loadLibrary(sqlite);

            Libraries.SQLITE.load(plugin.getDataFolder());
            return true;
        });
    }

    public Future<Boolean> loadPostgresLib() {
        return submitExec(() -> {
            ensureHikari();

            Library postgres = Library.builder()
                    .groupId("org{}postgresql")
                    .artifactId("postgresql")
                    .version("42.7.7")
                    .relocate("org{}postgresql", "net{}omni{}nearChat{}libs{}org{}postgresql")
                    .build();

            libraryManager.loadLibrary(postgres);

            Libraries.POSTGRESQL.load(plugin.getDataFolder());
            return true;
        });
    }

    public Future<Boolean> loadRedisLib() {
        return submitExec(() -> {
            Library reactivestreams = Library.builder()
                    .groupId("org{}reactivestreams")
                    .artifactId("reactive-streams")
                    .version("1.0.4")
                    .build();

            Library projectreactor = Library.builder()
                    .groupId("io{}projectreactor")
                    .artifactId("reactor-core")
                    .version("3.6.6")
                    .build();

            Library lettuce = Library.builder()
                    .groupId("io{}lettuce")
                    .artifactId("lettuce-core")
                    .version("6.6.0.RELEASE")
                    .id("AlessioDP")
                    .repository("https://repo.alessiodp.com/releases/")
                    .relocate("io{}lettuce{}core", "net{}omni{}nearChat{}libs{}io{}lettuce{}core")
                    .build();

            libraryManager.loadLibrary(reactivestreams);
            Libraries.REACTIVE_STREAMS.load(plugin.getDataFolder());

            libraryManager.loadLibrary(projectreactor);
            Libraries.PROJECT_REACTOR.load(plugin.getDataFolder());

            libraryManager.loadLibrary(lettuce);
            Libraries.REDIS.load(plugin.getDataFolder());
            return true;
        });
    }

    public void ensureMainLibraries() {
        libraryManager.addMavenCentral();
        libraryManager.addSonatype();
    }

    // use hikari connection pool when dealing with SQL databases except SQLite (?)
    public void ensureHikari() {
        Library hikaricp = Library.builder()
                .groupId("com{}zaxxer")
                .artifactId("HikariCP")
                .version("6.3.0")
                .relocate("com{}zaxxer{}hikari", "net{}omni{}nearChat{}libs{}com{}zaxxer{}hikari")
                .build();

        libraryManager.loadLibrary(hikaricp);

        Libraries.HIKARICP.load(plugin.getDataFolder());

        plugin.sendConsole(plugin.getMessageHandler().getLibraryLoaded("HikariCP"));
    }

    public Future<Boolean> ensureJSON() {
        return submitExec(() -> {
            Library json = Library.builder()
                    .groupId("org{}json")
                    .artifactId("json")
                    .version("20250517")
                    .relocate("org{}json", "net{}omni{}nearChat{}libs{}org{}json")
                    .build();

            libraryManager.loadLibrary(json);

            Libraries.JSON.load(plugin.getDataFolder());

            plugin.sendConsole(plugin.getMessageHandler().getLibraryLoaded("JSON"));
            return true;
        });
    }

    public Future<Boolean> submitExec(NCFunction func) {
        return executor.submit(func::exec);
    }

    public void stopExecutor() {
        this.executor.shutdown();
    }

    public interface NCFunction {
        boolean exec();
    }

}