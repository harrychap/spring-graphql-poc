# README Maintenance Standards

Any code change that affects how this project is described, run, or understood must include a corresponding update to the root-level `README.md`.

## When to update

Update the README if your change affects any of the following:

- How to run a service (commands, flags, environment setup)
- Which port a service runs on
- A new service being added or an existing one removed
- The GraphQL schema in a way that changes available queries, types, or fields
- The data model (fields added, removed, or renamed)
- Prerequisites or requirements (Java version, tooling)
- The high-level purpose or architecture of the project

## When NOT to update

Do not update the README for:

- Internal refactors that don't change behaviour or the developer experience
- Renaming internal classes, variables, or packages
- Adding or changing hardcoded dummy data (unless it changes the shape of the data)
- Test changes
- Dependency version bumps that don't affect how the project is run

## What to update

Only change the section(s) of the README that are directly affected. Do not rewrite unrelated sections.
