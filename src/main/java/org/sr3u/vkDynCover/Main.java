package org.sr3u.vkDynCover;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static final String CONFIGURATION_TXT = System.getProperty("user.dir") + "/configuration.txt";
    public static final Random RANDOM = new Random();
    static Configuration configuration = null;
    private static VKAPI vkapi;

    public static void main(String[] args) throws IOException {
        makeFilesAndDirs();
        try {
            configuration = new Configuration(CONFIGURATION_TXT);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (configuration.getGroups().isEmpty()) {
            System.out.println("Failed to get configuration, please check configuration.txt\n" + CONFIGURATION_TXT);
            System.exit(-1);
        }

        List<File> imagesDirs = configuration.getGroups().stream()
                .map(Configuration.Group::getImagesDirectory)
                .map(File::new)
                .filter(file -> !file.exists())
                .collect(Collectors.toList());
        for (File f : imagesDirs) {
            f.mkdirs();
            f.mkdir();
        }
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        configuration.getGroups().forEach(group -> {
            executor.scheduleAtFixedRate(() -> {
                try {
                    sendRandomPhoto(group);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, 0L, 1L, TimeUnit.MINUTES);
        });

    }

    private static void sendRandomPhoto(Configuration.Group group) throws IOException {
        boolean sent = false;
        for (int i = 0; (i < 100) && (!sent); i++) {
            File file = randomFileIn(group.getImagesDirectory());
            if (file == null) {
                System.out.println("Failed to get random file in " + group.getImagesDirectory());
                return;
            }
            if (!isImage(file)) {
                continue;
            }
            vkapi = new VKAPI(group);
            String uploadServerJsonString = vkapi.getUploadServer();
            JSONObject json = new JSONObject(uploadServerJsonString);
            if (json.has("error")) {
                System.out.println(uploadServerJsonString);
                System.out.println(json.get("error"));
                System.exit(-1);
            }
            if (!json.has("response")) {
                System.out.println(uploadServerJsonString);
                System.exit(-1);
            }
            JSONObject response = json.getJSONObject("response");
            String uploadUrl = response.getString("upload_url");
            if (uploadUrl == null) {
                System.out.println(uploadServerJsonString);
                System.exit(-1);
            }
            String s = vkapi.setCover(file, uploadUrl);
            System.out.println(s);
            sent = true;
        }
    }

    private static boolean isImage(File f) throws IOException {
        if (f == null) {
            return false;
        }
        return ImageIO.read(new FileInputStream(f)) != null;
    }

    private static File randomFileIn(String dir) {
        File[] files = new File(dir).listFiles();
        assert files != null;
        return files[RANDOM.nextInt(files.length)];
    }

    private static void makeFilesAndDirs() throws IOException {
        File configFile = new File(CONFIGURATION_TXT);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(configFile));
            printWriter.println(Configuration.DUMMY);
            printWriter.flush();
        }
        File imagesDir = new File(Configuration.Group.IMAGES_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
    }
}
