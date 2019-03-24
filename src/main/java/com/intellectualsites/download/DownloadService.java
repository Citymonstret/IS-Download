package com.intellectualsites.download;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.kvantum.server.implementation.QuickStart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class DownloadService {

    public static void main(final String[] args) {
        final File projectFolder = new File("./projects");
        if (!projectFolder.exists() || !projectFolder.isDirectory()) {
            System.out.println("No project folder exists. Attempting to create one...");
            if (!projectFolder.mkdir()) {
                System.err.println("Failed to create projects folder...");
                return;
            } else {
                System.out.println("Projects folder created");
            }
        }
        System.out.println("Loading projects...");
        final Map<String, Project> projects = new HashMap<>();
        final JSONParser jsonParser = new JSONParser();
        try {
            Files.list(projectFolder.toPath()).forEach(path -> {
                System.out.printf("Found project schema file: %s\n", path.getFileName());
                try {
                    final JSONObject object = (JSONObject) jsonParser.parse(String.join("\n",
                        Files.readAllLines(path)));
                    final Project project = new Project(path.getFileName().getFileName().toString().split("\\.")[0], object);
                    System.out.printf("Read project %s\n", project.getIdentifier());
                    projects.put(project.getIdentifier(), project);
                } catch (final IOException | ParseException e) {
                    System.err.printf("Failed to read project schema (file: %s)", path.getFileName());
                    e.printStackTrace();
                }
            });
        } catch (final IOException e) {
            System.err.println("Failed to load project schemas");
            e.printStackTrace();
        }
        System.out.println("Projects read...");
        QuickStart.newStandaloneServer(new ProjectListing(projects)).start();
    }

}
