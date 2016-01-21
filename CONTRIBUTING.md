# Contributing to the control software

Eastern Edge welcomes contributions from everyone. By contributing to this project, you agree to the license terms of the project.

In brief, contributing involves the following process:

1. [Clone the repository](https://help.github.com/articles/cloning-a-repository/)
1. [Create a branch](https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/)
1. ??? (Make changes?)
1. Push the changes to GitHub
1. [Open a Pull Request](https://help.github.com/articles/creating-a-pull-request/)
1. ??? (Code review?)
1. Merge changes into the `master` branch

## The development process

All development happens on GitHub—there exists the possibility of brainstorming happening outside of GitHub, but an attempt should be made to convey all design decisions in a GitHub issue or Pull Request.

### `master` should be stable

The `master` branch should always contain a stable version of the control software, with (passing) tests, and decent test coverage.

### Issues

Issue should be used liberally. Example usage:

- Brainstorming and ideas
- General discussion
- Actual issues (e.g. bugs)

### Pull Requests

(See also: "`master` should be stable" above for information about branches.)

Pull Requests (PR) are the means by which branches are merged into `master`. Open a Pull Request and describe what changes are being introduced. When a PR is submitted or updated with more commits, [CircleCI](https://circleci.com/gh/EasternEdgeRobotics/2016) will run "tests" against just the most recent of them and update the PR to reflect whether it passed or failed. You can run the same "tests" locally via `gradle check` and `gradle test`.

### Style Guide

The project uses [Checkstyle](http://checkstyle.sourceforge.net) to enforce some Java practices and certain formatting styles. These checks will be run automatically before Pull Requests are merged into `master` and can be run locally via `gradle check`.
