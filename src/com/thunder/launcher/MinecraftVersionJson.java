package com.thunder.launcher;

import java.util.List;
import java.util.Map;

public class MinecraftVersionJson {
    public Downloads downloads;
    public List<Library> libraries;
    public AssetIndexRef assetIndex;

    // para las versiones
    public static class Downloads {
        public DownloadItem client;
    }

    public static class DownloadItem {
        public String url;
        public long size;
        public String sha1;
    }

    // para las librerias
    public static class Library {
        public String name;
        public LibraryDownloads downloads;
        public Map<String, String> natives; // mapea linux y windows

        public static class LibraryDownloads {
            public DownloadItem artifact;
            public Map<String, DownloadItem> classifiers; // contiene los archivos
        }
    }

    // para que sepa el launcher donde descargar
    public static class AssetIndexRef {
        public String id;
        public String url;
    }
}
