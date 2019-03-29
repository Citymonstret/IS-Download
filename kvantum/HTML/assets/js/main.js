var BASE_PATH = 'https://incendo.org/downloads/api/';
 
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
                let targets = JSON.parse(targetsRaw);
                targets.forEach(target => {
                    readTypes(project, target, function(typesRaw) {
                        let types = JSON.parse(typesRaw);
                        types.forEach(type => {
                            let projectEntry = getProjectEntry(project, target, type);
                            projectEntry.find('.entry-project').text(project);
                            projectEntry.find('.entry-target').text(target);
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
                                                let versions = JSON.parse(versionsRaw);
                                                versions.forEach(version => {
                                                    readFile(project, target, type, build, version, function(fileData) {
                                                        let fileDisplay = getFileDisplay(fileData.fileName);
                                                        fileDisplay.find('.file-build').text(build);
                                                        fileDisplay.find('.file-target').text(target);
                                                        fileDisplay.find('.file-type').text(type);
                                                        fileDisplay.find('.file-version').text(version);
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
        successf(data.versions);
    })
}

function readBuilds(projectName, targetName, typeName, successf) {
    read(`${projectName}/${targetName}/${typeName}`, function(data) {
        successf(data);
    })
}

function readTypes(projectName, targetName, successf) {
    read(`${projectName}/${targetName}`, function(data) {
        successf(data.types);
    })
}

function readTargets(projectName, successf) {
    read(projectName, function(data) {
        successf(data.targets);
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
