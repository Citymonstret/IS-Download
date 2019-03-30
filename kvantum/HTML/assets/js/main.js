var BASE_PATH = 'https://incendo.org/download/api/';
 
var $projects;
var $projectEntries;

$(document).ready(function() {
    $('[data-toggle="tooltip"]').tooltip();
    loadProjects();
});

function loadProjects() {
    resetState();
    $projects = $('#projects');
    $projectEntries = $('#project-picker');
    readProjects(function(projectsRaw) {
        let projects = JSON.parse(projectsRaw);
        projects.forEach(project => {
            readTargets(project, function(targetsRaw) {
                let projectDisplayName = targetsRaw.display_name;
                let targets = JSON.parse(targetsRaw.targets);
                targets.forEach(target => {
                    readTypes(project, target, function(typesRaw) {
                        let targetDisplayName = typesRaw.display_name;
                        let types = JSON.parse(typesRaw.types);
                        types.forEach(type => {
                            let projectEntry = getProjectEntry(project, target, type);
                            projectEntry.find('.entry-project').text(projectDisplayName);
                            projectEntry.find('.entry-target').text(targetDisplayName);
                            projectEntry.find('.entry-type').text(type);
                            projectEntry.attr('data-project', project);
                            projectEntry.attr('data-target', target);
                            projectEntry.attr('data-type');
                            $('[data-toggle="tooltip"]').tooltip()

                            projectEntry.on('click', function(e) {
                                $('[data-toggle="tooltip"]').tooltip('hide');
                                // First fade out project entries
                                $projectEntries.fadeOut(400, function() {
                                    resetState();
                                    readBuilds(project, target, type, function(buildsRaw) {
                                        // Then fade in project info
                                        let $display = getProjectDisplay(project);
                                        $display.find('.project-title').text(project);
                                        $display.find('.project-desc').text(buildsRaw.description);
                                        $display.show();
                                        $projects.append($display);
                                        $projects.fadeIn();
                                        
                                        $('.back-button').on('click', function() {
                                            $
                                            resetState();
                                            loadProjects();
                                            $projectEntries.fadeIn();
                                        });

                                        let builds = JSON.parse(buildsRaw.builds).reverse();
                                        builds.forEach(build => {
                                            readVersions(project, target, type, build, function(versionsRaw) {
                                                let buildDisplayName = versionsRaw.display_name;
                                                let versions = JSON.parse(versionsRaw.versions);
                                                versions.forEach(version => {
                                                    readFile(project, target, type, build, version, function(fileData) {
                                                        let fileDisplay = getFileDisplay(fileData.fileName);
                                                        fileDisplay.find('.file-build').text(buildDisplayName);
                                                        fileDisplay.find('.file-target').text(targetDisplayName);
                                                        fileDisplay.find('.file-type').text(type);
                                                        fileDisplay.find('.file-version').text(fileData.display_name);
                                                        fileDisplay.find('.file-file').html(`<a href="${fileData.download}" data-toggle="tooltip" title="Download via Jenkins">${fileData.fileName}`);
                                                        $display.append(fileDisplay);
                                                        $('[data-toggle="tooltip"]').tooltip()
                                                        fileDisplay.fadeIn();
                                                    });
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                            $projectEntries.append(projectEntry);
                            projectEntry.fadeIn();
                        });
                    });
                });
            });
        });
    });
}

function resetState() {
    // Destroy all copies
    $('[data-toggle="tooltip"]').tooltip('hide');
    $('.copy').remove();
}

function getProjectEntry(projectName, target, type) {
    let object = $('#project-entry').clone();
    object.addClass('copy');
    object.attr('id', `entry-${projectName}-${target}-${type}`);
    return object;
}

function getFileDisplay(file) {
    let object = $('#file-display').clone();
    object.addClass('copy');
    object.attr('id', `file-${file}`);
    return object;
}

function getProjectDisplay(projectName) {
    let object = $('#project-display').clone();
    object.addClass('copy');
    object.attr('id', `project-${projectName}`);
    return object;
}

function readFile(projectName, targetName, typeName, buildName, versionName, successf) {
    read(`${projectName}/${targetName}/${typeName}/${buildName}/${versionName}`, function(data) {
        successf(data);
    })
}

function readVersions(projectName, targetName, typeName, buildName, successf) {
    read(`${projectName}/${targetName}/${typeName}/${buildName}`, function(data) {
        successf(data);
    })
}

function readBuilds(projectName, targetName, typeName, successf) {
    read(`${projectName}/${targetName}/${typeName}`, function(data) {
        successf(data);
    })
}

function readTypes(projectName, targetName, successf) {
    read(`${projectName}/${targetName}`, function(data) {
        successf(data);
    })
}

function readTargets(projectName, successf) {
    read(projectName, function(data) {
        successf(data);
    });
}

function readProjects(successf) {
    read('', function(data) {
        successf(data.projects);
    });
}

function read(path, successf) {
    var completePath = BASE_PATH + path;
    console.log(`Reading ${completePath}`);
    $.ajax({
        method: 'GET',
        url: completePath,
        async: false,
        dataType: 'json'
    })
    .done(successf)
    .fail(function(jqXHR, textStatus) {
        alert( "Request failed: " + textStatus );
    });
}
