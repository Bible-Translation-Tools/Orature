#!/bin/bash
# Get the currently checked out branch
jvm_branch=$BRANCH
echo "Detected otter-jvm on $jvm_branch"
# Check if branch exists in common
pushd ../otter-common
echo "Looking for origin/$jvm_branch in otter-common..."
git rev-parse --verify origin/$jvm_branch
if [ $? -eq 0 ]; then
    # Branch exists
    echo "Found origin/$jvm_branch"
    git checkout $jvm_branch
else
    echo "origin/$jvm_branch does not exist. Falling back to dev..."
    git checkout dev;
fi
popd