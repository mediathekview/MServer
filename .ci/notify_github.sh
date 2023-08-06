#!/bin/bash

# Installiere curl
apt-get install curl

GITHUB_ORG="mediathekview"
GITHUB_REPO="MServer"
GITHUB_SHA="${CI_COMMIT_SHA}"
cat << EOF > headers.curl
Accept: application/vnd.github+json
Authorization: token ${GITHUB_API_TOKEN}
EOF
cat << EOF > success.json
{
  "state" : "success",
  "target_url" : "${CI_PIPELINE_URL}",
  "description" : "CI runs at ElaonDE systems successful"
}
EOF
cat << EOF > failure.json
{
  "state" : "failure",
  "target_url" : "${CI_PIPELINE_URL}",
  "description" : "CI runs at ElaonDE systems failed"
}
EOF
cat << EOF > pending.json
{
  "state" : "pending",
  "target_url" : "${CI_PIPELINE_URL}",
  "description" : "CI runs at ElaonDE systems pending"
}
EOF
GITHUB_API_URL="https://api.github.com/repos/${GITHUB_ORG}/${GITHUB_REPO}/statuses/${GITHUB_SHA}"
if [ "$1" == "success" ]; then
curl -s -X POST -H @headers.curl "${GITHUB_API_URL}" -d @success.json
elif [ "$1" == "failure" ]; then
curl -s -X POST -H @headers.curl "${GITHUB_API_URL}" -d @failure.json
elif [ "$1" == "pending" ]; then
  curl -s -X POST -H @headers.curl "${GITHUB_API_URL}" -d @pending.json
fi 