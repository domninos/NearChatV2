package net.omni.nearChat.database.flatfile;

import net.omc.database.OMCDatabase;
import net.omc.database.flatfile.OMCFlatFileDatabase;
import net.omni.nearChat.NearChatPlugin;

public class FlatFileDatabase extends OMCFlatFileDatabase implements OMCDatabase {

    public FlatFileDatabase(NearChatPlugin plugin) {
        super(plugin, "nearchat.txt");
    }
}
