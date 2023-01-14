package de.teddy.minesweeper.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class JarWalker {

    /**
     * @author SkytAsul
     */
    public static void walkResources(Class<?> clazz, String path, int depth, Consumer<Path> consumer) throws URISyntaxException, IOException {
        URL resource = clazz.getResource(path);
        if (resource == null)
            return;

        URI uri = resource.toURI();
        FileSystem fileSystem = null;
        Path myPath;
        try{
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath(path);
            } else {
                myPath = Paths.get(uri);
            }

            try(Stream<Path> walker = Files.walk(myPath, depth)){
                walker.forEach(consumer);
            }
        }finally{
            if (fileSystem != null) fileSystem.close();
        }
    }

}
