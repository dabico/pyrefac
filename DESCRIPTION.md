# Task

## Description

We invite you to dive into the development with IntelliJ Platform. Your test assignment is as follows:

- [X] Write a CLI application that starts IntelliJ IDEA in headless mode, i.e. without UI;
- [X] Upon startup, the program should clone a Git repository;
- [ ] After that, it should apply one of the refactorings to a specified file:
  - [X] `add_comment`
  - [ ] `rename_literal`
  - [X] `rename_function_parameters`
- [ ] The app should return the Unix `patch` that actually performs the changes;

> [!NOTE]
> If you find the task too easy, you can turn your application into a server,
> so you don't have to restart IDEA at every call :D

The following requirements should be met:

- The CLI command inputs include the repository URL, file path, refactoring name, and refactoring parameters;
- Refactorings need only support Python code;
- Refactorings always target files found within the latest commit of the default branch;
- If the `class` argument is not specified, `rename_literal` will target the root scope;
- The `add_comment` refactoring will add a Python DocString to the function;

> [!IMPORTANT]
> We would also like you to use existing high-level APIs when available for the performed refactorings.
> However, if you feel stuck for this reason, you can move on with other technologies.

## Testing

For testing, we attached a `test.yaml` with some data.
Here's an excerpt from the file featuring one of each refactoring type:

```yaml
- refactorings:
  - parameters:
      class: EllipsoidTool
      comment: Plots an ellipsoid based on the provided center, radii, and rotation
        matrix. Allows plotting with custom color and transparency settings.
      function: plotEllipsoid
    path: src/ellipsoid_tool.py
    refactoring_function: add_comment
  - parameters:
      class: ''
      function: radius_axial
      new_name: radius_after_center
      old_name: r2
    path: src/measures.py
    refactoring_function: rename_literal
  - parameters:
      class: organ_measure
      function: volume
      old_name: voxel_data
    path: src/measures.py
    refactoring_function: rename_function_parameters
  repository: git@github.com:DL4XRayTomoImaging-KIT/measuring-repo.git
```

## Timeline

- Development time: 1 week.
- Start date: 2024-05-04 09:00:00 CEST
- End date: 2024-05-11 09:00:00 CEST
