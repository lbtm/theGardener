#!/bin/bash

executeCommandOnH2() {
    java -cp /app/lib/com.h2database.h2-1.4.199.jar org.h2.tools.Shell -url "jdbc:h2:/data/thegardener;AUTO_SERVER=TRUE" -user "sa" -password "" -sql "$1"
}

echo "Starting to insert sample data..."

executeCommandOnH2 "INSERT INTO project (id, name, repositoryUrl, stableBranch, featuresRootPath) VALUES ('theGardener', 'theGardener', 'https://github.com/KelkooGroup/theGardener.git', 'master', 'test/features');"
executeCommandOnH2 "INSERT INTO hierarchyNode (id, slugName, name, childrenLabel, childLabel) VALUES ('.', 'root', 'root', 'Views', 'View');"
executeCommandOnH2 "INSERT INTO hierarchyNode (id, slugName, name, childrenLabel, childLabel) VALUES ('.01.', 'tools', 'Tools', 'Projects', 'Project');"
executeCommandOnH2 "INSERT INTO project_hierarchyNode (projectId, hierarchyId) VALUES ('theGardener', '.01.');"

echo "Sample data inserted"