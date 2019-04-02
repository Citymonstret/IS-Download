var BASE_PATH = 'https:/incendo.org/download/api/';
 
var $projects;
var $projectEntries;
var converter = new showdown.Converter();
var $isInProject = false;

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
                            projectEntry.attr('data-type', type);
                            $('[data-toggle="tooltip"]').tooltip()

                            projectEntry.on('click', function(e) {
                                $('[data-toggle="tooltip"]').tooltip('hide');

                                if ($isInProject) {
                                    return;
                                } else { 
                                    $isInProject = true;
                                }
                                
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

                                        if ('wiki' in buildsRaw && buildsRaw.wiki.length > 0) {
                                            let $wiki = createWiki(project, target, type, buildsRaw.wiki);
                                            $wiki.show();
                                            $projects.append($wiki);
                                        }

                                        $projects.fadeIn();
                                        
                                        $('.back-button').on('click', function() {
                                            $isInProject = false;
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

                            // console.log('SORTING');
                            $projectEntries.append($projectEntries.children().sort(function(aa, bb) {
                                var a = $(aa);
                                var b = $(bb);
                                // console.log(`proj > a: ${a.attr('data-project')}, b:  ${b.attr('data-project')}`);
                                var projA = a.attr('data-project');
                                var projB = b.attr('data-project');
                                if (projA == projB) {
                                    var typeA = a.attr('data-type');
                                    var typeB = b.attr('data-type');
                                    // console.log(`type > a ${typeA}, b: ${typeB}`);
                                    return (typeA < typeB) ? -1 : (typeA > typeB)? 1 : 0;
                                }
                                if (projA < projB) {
                                    return -1;
                                }
                                return 1;
                            }));

                            projectEntry.fadeIn();
                        });
                    });
                });
            });
        });
    });
}

function createWiki(project, target, type, input) {
    let object = $('#wiki').clone();
    object.attr('id', `wiki-${project}-${target}-${type}`);
    object.find('.project-wiki').html(converter.makeHtml(input));
    object.addClass("copy");
    return object;
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
    }, true);
}

function readVersions(projectName, targetName, typeName, buildName, successf) {
    read(`${projectName}/${targetName}/${typeName}/${buildName}`, function(data) {
        successf(data);
    }, false);
}

function readBuilds(projectName, targetName, typeName, successf) {
    read(`${projectName}/${targetName}/${typeName}`, function(data) {
        successf(data);
    }, true);
}

function readTypes(projectName, targetName, successf) {
    read(`${projectName}/${targetName}`, function(data) {
        successf(data);
    }, true);
}

function readTargets(projectName, successf) {
    read(projectName, function(data) {
        successf(data);
    }, true);
}

function readProjects(successf) {
    read('', function(data) {
        successf(data.projects);
    }, true);
}

function read(path, successf, async) {
    var completePath = BASE_PATH + path;
    console.log(`Reading ${completePath}`);
    $.ajax({
        method: 'GET',
        url: completePath,
        async: async,
        dataType: 'json'
    })
    .done(successf)
    .fail(function(jqXHR, textStatus) {
        alert( "Request failed: " + textStatus );
    });
}
