package net.omni.nearChat.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Map;

public class JSONUtil {
    // TODO to use?
    private static final Gson GSON = new Gson();
    private static final Type TEST_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static void toGson(String file, Map<String, Boolean> map) throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(file));

    }
}
