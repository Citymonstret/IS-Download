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

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import xyz.kvantum.server.api.request.AbstractRequest;
import xyz.kvantum.server.api.request.HttpMethod;
import xyz.kvantum.server.api.response.Header;
import xyz.kvantum.server.api.response.Response;
import xyz.kvantum.server.api.util.KvantumJsonFactory;
import xyz.kvantum.server.api.util.MapBuilder;
import xyz.kvantum.server.api.views.annotatedviews.ViewMatcher;
import xyz.kvantum.server.api.views.annotatedviews.converters.StandardConverters;

import java.util.Map;
import java.util.Objects;

/**
 * Projects use the following format
 * /project/version/type/build/target
 * <p>
 * An example being the latest release build of PlotSquared 4.0 for Bukkit:
 * /PlotSquared/4/release/latest/bukkit
 */
@SuppressWarnings("unused") @RequiredArgsConstructor public class ProjectListing extends Node<Project> {

    private final Map<String, Project> projects;

    private JSONObject generateUnknown(final String key, final String value) {
        return KvantumJsonFactory.toJSONObject(
            MapBuilder.<String, Object>newHashMap().put("status", "unknown_value").put("unknown",
                new JSONObject(MapBuilder.<String, Object>newHashMap().put("type", key)
                    .put("value", value).get())).get());
    }

    private Response generateUnknown404(final String key, final String value) {
        final Response response = new Response();
        response.getHeader().setStatus(Header.STATUS_NOT_FOUND);
        response.setResponse(generateUnknown(key, value).toJSONString());
        return response;
    }

    private JSONObject generateSuccess(final JSONObject object) {
        object.put("status", "success");
        return object;
    }

    @ViewMatcher(filter = "download/api", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onRoot(final AbstractRequest request) {
        return generateSuccess(this.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onProject(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown("project", projectName);
        }
        return generateSuccess(project.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>/<target>", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onTarget(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown("project", projectName);
        }
        final String targetName = Objects.requireNonNull(request.get("target")).toString();
        final Project.Target target = project.getChild(targetName);
        if (target == null) {
            return generateUnknown("target", targetName);
        }
        return generateSuccess(target.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>/<target>/<type>", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onType(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown("project", projectName);
        }
        final String targetName = Objects.requireNonNull(request.get("target")).toString();
        final Project.Target target = project.getChild(targetName);
        if (target == null) {
            return generateUnknown("target", targetName);
        }
        final String typeName = Objects.requireNonNull(request.get("type")).toString();
        final Project.Type type = target.getChild(typeName);
        if (type == null) {
            return generateUnknown("type", targetName);
        }
        return generateSuccess(type.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>/<target>/<type>/<build>", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onBuild(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown("project", projectName);
        }
        final String targetName = Objects.requireNonNull(request.get("target")).toString();
        final Project.Target target = project.getChild(targetName);
        if (target == null) {
            return generateUnknown("target", targetName);
        }
        final String typeName = Objects.requireNonNull(request.get("type")).toString();
        final Project.Type type = target.getChild(typeName);
        if (type == null) {
            return generateUnknown("type", targetName);
        }
        final String buildName = Objects.requireNonNull(request.get("build")).toString();
        final Project.Type.Build build = type.getChild(buildName);
        if (build == null) {
            return generateUnknown("build", buildName);
        }
        return generateSuccess(build.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>/<target>/<type>/<build>/<version>", httpMethod = HttpMethod.ALL, outputType = StandardConverters.JSON)
    public JSONObject onVersion(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown("project", projectName);
        }
        final String targetName = Objects.requireNonNull(request.get("target")).toString();
        final Project.Target target = project.getChild(targetName);
        if (target == null) {
            return generateUnknown("target", targetName);
        }
        final String typeName = Objects.requireNonNull(request.get("type")).toString();
        final Project.Type type = target.getChild(typeName);
        if (type == null) {
            return generateUnknown("type", typeName);
        }
        final String buildName = Objects.requireNonNull(request.get("build")).toString();
        final Project.Type.Build build = type.getChild(buildName);
        if (build == null) {
            return generateUnknown("build", buildName);
        }
        final String versionName = Objects.requireNonNull(request.get("version")).toString();
        final Project.Type.Version version = build.getChild(versionName);
        if (version == null) {
            return generateUnknown("version", versionName);
        }
        return generateSuccess(version.toJSON());
    }

    @ViewMatcher(filter = "download/api/<project>/<target>/<type>/<build>/<version>/download", httpMethod = HttpMethod.ALL)
    public Response onDownload(final AbstractRequest request) {
        final String projectName = Objects.requireNonNull(request.get("project")).toString();
        final Project project = getChild(projectName);
        if (project == null) {
            return generateUnknown404("project", projectName);
        }
        final String targetName = Objects.requireNonNull(request.get("target")).toString();
        final Project.Target target = project.getChild(targetName);
        if (target == null) {
            return generateUnknown404("target", targetName);
        }
        final String typeName = Objects.requireNonNull(request.get("type")).toString();
        final Project.Type type = target.getChild(typeName);
        if (type == null) {
            return generateUnknown404("type", typeName);
        }
        final String buildName = Objects.requireNonNull(request.get("build")).toString();
        final Project.Type.Build build = type.getChild(buildName);
        if (build == null) {
            return generateUnknown404("build", buildName);
        }
        final String versionName = Objects.requireNonNull(request.get("version")).toString();
        final Project.Type.Version version = build.getChild(versionName);
        if (version == null) {
            return generateUnknown404("version", versionName);
        }
        final Response response = new Response();
        response.getHeader().setStatus(Header.STATUS_TEMPORARY_REDIRECT)
            .set(Header.HEADER_LOCATION, version.getDownloadUrl());
        return response;
    }

    @Override protected String getIdentifier() {
        return "projects";
    }

    @Override protected JSONObject generateJSON() {
        return KvantumJsonFactory.toJSONObject(
            MapBuilder.<String, Object>newHashMap().put("projects",
                KvantumJsonFactory.toJsonArray(this.projects.keySet())).get());
    }

    @Override protected Project getChild(final String key) {
        return this.projects.get(key);
    }
}
