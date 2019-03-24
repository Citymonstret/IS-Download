//
// MIT License
//
// Copyright (c) 2019 Alexander Söderberg
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package com.intellectualsites.download;

import com.intellectualsites.configurable.ConfigurationFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.implementation.QuickStart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

class DownloadService extends AutoCloseable {

    public static void main(final String[] args) {
        new DownloadService();
    }

    private Timer timer;

    private DownloadService() {final File projectFolder = new File("./projects");
        if (!projectFolder.exists() || !projectFolder.isDirectory()) {
            System.out.println("No project folder exists. Attempting to create one...");
            if (!projectFolder.mkdir()) {
                System.err.println("Failed to create projects folder...");
                return;
            } else {
                System.out.println("Projects folder created");
            }
        }
        ConfigurationFactory.load(DownloadServiceConfig.class, new File("./"));
        System.out.println("Loading projects...");
        final Map<String, Project> projects = new HashMap<>();
        final JSONParser jsonParser = new JSONParser();
        try {
            Files.list(projectFolder.toPath()).forEach(path -> {
                if (!path.getFileName().toString().endsWith("json")) {
                    System.out.printf("Skipping non-project file: %s", path.getFileName());
                    return;
                }
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

        //
        // Setups a timer that re-fetches build info
        //
        if (DownloadServiceConfig.Download.refetchTime != -1) {
            System.out.printf("Setting up project re-fetching scheduler. Will run every %d seconds\n",
                DownloadServiceConfig.Download.refetchTime);
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(new TimerTask() {
                @Override public void run() {
                    for (final Project project : projects.values()) {
                        project.loadBuilds();
                    }
                }
            }, 1000 * DownloadServiceConfig.Download.refetchTime, 1000 * DownloadServiceConfig.Download.refetchTime);
        }

        //
        // Start the server using the quickstart utility
        //
        QuickStart.newStandaloneServer(new ProjectListing(projects)).start();
    }

    @Override protected void handleClose() {
        Logger.info("Closing the timer...");
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

}
