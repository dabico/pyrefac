# PyRefac

_PyRefac_ is a plugin for IntelliJ IDEA that facilitates refactoring Python source code using a set of predefined
operations. Written in Java, it uses the IntelliJ Platform to interact with the IDE, performing tasks such as cloning
repositories, transforming code, and generating patches. Although primarily designed to be run from the command line,
its core functionality can be extended by other plugins or applications that run on the IDEA platform.

## Requirements

| Software | Version |
|:---------|--------:|
| Java     |      17 |
| Gradle   |     8.7 |
| Git      |      \* |

## Running

### Gradle Task

You can run the plugin using the Gradle task:

```shell
./gradlew runPyRefac -Prepository="$GIT_REPOSITORY_URL" \
                    -PfilePath="$GIT_REPOSITORY_FILE_PATH" \
                    -Prefactoring="$REFACTORING_NAME" \
                    -Pparameters="$REFACTORING_CONFIG_PATH"
```

Where:

| Parameter                   | Description                                                                           |
|-----------------------------|---------------------------------------------------------------------------------------|
| `$GIT_REPOSITORY_URL`       | URL of the Git repository to clone. Can be either an HTTPS or SSH[^1].                |
| `$GIT_REPOSITORY_FILE_PATH` | Repository root-relative path of the file to refactor.                                |
| `$REFACTORING_NAME`         | Either `add_comment`, `rename_literal`, or `rename_function_parameters`.              |
| `$REFACTORING_CONFIG_PATH`  | _Relative_ path to the JSON configuration file containing the refactoring parameters. |

IntelliJ run configurations are also provided for easier debugging.

[^1]: If you are using SSH, you may need to add your SSH key to the agent before running the task.

### Command Line Interface (CLI)

For convenience, a helper script is provided to run the plugin:

```shell
./pyrefac.sh $GIT_REPOSITORY_URL $GIT_REPOSITORY_FILE_PATH $REFACTORING_NAME $REFACTROING_CONFIG_PATH
```

The parameters are the same as the Gradle task.

## Supported Refactorings

### `add_comment`

Adds a [DocString](https://peps.python.org/pep-0257/) comment to a specific function, identified by its name and class.
To target functions in the root scope, omit the class name. If a function already has a documentation comment, it will
be replaced. If the input comment has no line breaks, the added DocString will be single-line; otherwise, it will be
multi-line. The refactoring ensures proper indentation of the comment body.

#### Example 1

```shell
./pyrefac.sh git@github.com:DL4XRayTomoImaging-KIT/BinScale3D.git \
            src/binscale/scaler.py \
            add_comment \
            config.json
```

Where `config.json` contains:

```json
{
  "class": "Scaler",
  "function": "__init__",
  "comment": "Initialize the Scaler with a specific scaling factor."
}
```

Should produce the following diff:

```diff
diff --git a/src/binscale/scaler.py b/src/binscale/scaler.py
index cfe3c42..4745d35 100644
--- a/src/binscale/scaler.py
+++ b/src/binscale/scaler.py
@@ -7,6 +7,7 @@ import dask.array as da

 class Scaler:
     def __init__(self, scale):
+        """Initialize the Scaler with a specific scaling factor."""
         self.scale = scale
         self.prefix = f'scaled_{self.scale}'
```

#### Example 2

```shell
./pyrefac.sh git@github.com:DL4XRayTomoImaging-KIT/measuring-repo.git \
            src/ellipsoid_tool.py \
            add_comment \
            config.json
```

Where `config.json` contains:

```json
{
  "class": "EllipsoidTool",
  "function": "plotEllipsoid",
  "comment": "Plots an ellipsoid based on the provided center, radii, and rotation matrix.\nAllows plotting with custom color and transparency settings."
}
```

Should produce the following diff:

```diff
diff --git a/src/ellipsoid_tool.py b/src/ellipsoid_tool.py
index 5f70dcd..1afb9be 100644
--- a/src/ellipsoid_tool.py
+++ b/src/ellipsoid_tool.py
@@ -71,7 +71,10 @@ class EllipsoidTool:
         return 4./3.*np.pi*radii[0]*radii[1]*radii[2]

     def plotEllipsoid(self, center, radii, rotation, ax=None, plotAxes=False, cageColor='b', cageAlpha=0.2):
-        """Plot an ellipsoid"""
+        """
+        Plots an ellipsoid based on the provided center, radii, and rotation matrix.
+        Allows plotting with custom color and transparency settings.
+        """
         make_ax = ax == None
         if make_ax:
             fig = plt.figure()
```

### `rename_literal`

This function renames a local variable or class attribute reference within a specific function, identified by its name
and class. Omit the class name to target functions in the root scope. The refactoring fails if the provided name isn't
found or if the new name is already in use.

#### Example 1

```shell
./pyrefac.sh git@github.com:DL4XRayTomoImaging-KIT/measuring-repo.git \
            src/measures.py \
            rename_literal \
            config.json
```

Where `config.json` contains:

```json
{
  "class": "",
  "function": "radius_axial",
  "new_name": "radius_before_center",
  "old_name": "r1"
}
```

Should produce the following diff:

```diff
diff --git a/src/measures.py b/src/measures.py
index e566219..b1ac510 100644
--- a/src/measures.py
+++ b/src/measures.py
@@ -202,9 +202,9 @@ def thickness_axial(markup, line, center):
 def radius_axial(markup, line, center):
     """For each direction along of one of three axis calculate distance between center and last segmented pixel"""
     touched = np.where(markup[tuple(line)])[0]
-    r1 = center - touched[0]
+    radius_before_center = center - touched[0]
     r2 = touched[-1] - center
-    return (r1, r2)
+    return (radius_before_center, r2)

 def get_radii(markup):
     mp = np.dstack(np.where(markup))[0] # marked points
```

#### Example 2

```shell
./pyrefac.sh git@github.com:DL4XRayTomoImaging-KIT/BinScale3D.git \
            src/binscale/converter.py \
            rename_literal \
            config.json
```

Where `config.json` contains:

```json
{
  "class": "Converter",
  "function": "__init__",
  "new_name": "to_threshold",
  "old_name": "_t"
}
```

Should produce the following diff:

```diff
diff --git a/src/binscale/converter.py b/src/binscale/converter.py
index c45c22a..5107d73 100644
--- a/src/binscale/converter.py
+++ b/src/binscale/converter.py
@@ -29,7 +29,7 @@ def get_disjoint_thresholds(rs, ors, f=1, t=99.95):
 class Converter:
     def __init__(self, from_percentile=1, to_percentile=99.95, apply_sigmoid=False, disjoint_distributions=False, to_format='uint8', autoscale=True):
         self._f = from_percentile
-        self._t = to_percentile
+        self.to_threshold = to_percentile
         self._s = apply_sigmoid
         self._dis = disjoint_distributions
         self.to_format = to_format
@@ -50,10 +50,10 @@ class Converter:
     @dasked
     def _scale(self, img, rs, ors):
         if self._dis:
-            f, t = get_disjoint_thresholds(rs, ors, self._f, self._t)
+            f, t = get_disjoint_thresholds(rs, ors, self._f, self.to_threshold)
         else:
             f = np.percentile(rs, self._f)
-            t = np.percentile(rs, self._t)
+            t = np.percentile(rs, self.to_threshold)

         if self.autoscale:
             if self._s:
```

### `rename_function_parameters`

Renames a parameter of a specific function, identified by its name and class. Omit the class name to target functions in
the root scope. The refactoring updates both the function signature and references within the function body. It fails if
the parameter name isn't found in the function signature or if the new parameter name is already in use.

#### Example

```shell
./pyrefac.sh git@github.com:DL4XRayTomoImaging-KIT/measuring-repo.git \
            src/ellipsoid_tool.py \
            rename_function_parameters \
            config.json
```

Where `config.json` contains:

```json
{
  "class": "EllipsoidTool",
  "function": "plotEllipsoid",
  "old_name": "plotAxes",
  "new_name": "plot_axes"
}
```

Should produce the following diff:

```diff
diff --git a/src/ellipsoid_tool.py b/src/ellipsoid_tool.py
index 5f70dcd..0fe14a1 100644
--- a/src/ellipsoid_tool.py
+++ b/src/ellipsoid_tool.py
@@ -70,7 +70,7 @@ class EllipsoidTool:
         """Calculate the volume of the blob"""
         return 4./3.*np.pi*radii[0]*radii[1]*radii[2]

-    def plotEllipsoid(self, center, radii, rotation, ax=None, plotAxes=False, cageColor='b', cageAlpha=0.2):
+    def plotEllipsoid(self, center, radii, rotation, ax=None, plot_axes=False, cageColor='b', cageAlpha=0.2):
         """Plot an ellipsoid"""
         make_ax = ax == None
         if make_ax:
@@ -89,7 +89,7 @@ class EllipsoidTool:
             for j in range(len(x)):
                 [x[i,j],y[i,j],z[i,j]] = np.dot([x[i,j],y[i,j],z[i,j]], rotation) + center

-        if plotAxes:
+        if plot_axes:
             # make some purdy axes
             axes = np.array([[radii[0],0.0,0.0],
                              [0.0,radii[1],0.0],
```

## Testing

To test the plugin functionalities, you can run the aptly named Python helper script:

```shell
pip install virtualenv
virtualenv venv
./venv/bin/pip install -r requirements.txt
./venv/bin/python test.py
```

Note that this only checks if the CLI commands execute without errors. To inspect the refactoring results, pass the
`--verbose` flag to the script. You can also run it directly from the IDE with the provided run configuration, but you
may need to install the packages from `requirements.txt` in your IDE's Python interpreter before doing so.

## Known Limitations

### Git Operations

The plugin clones the entire repository to a temporary directory to perform the refactorings. In order to make the
process as efficient as possible, the plugin performs a shallow clone, which is then automatically deleted once the
patch has been obtained. Although this approach is efficient for a single run, it may not be suitable for repeated runs
on the same repository, as each run will perform a clone. One could consider caching the repository locally to avoid
this, but then we have to make sure the local clone is kept up-to-date between runs. Furthermore, each run will require
the repository contents to be reset to the original state. This is all assuming only a single instance of the plugin is
running at any given time. If multiple instances are running concurrently, they may interfere with each other. With the
current approach, each instance is completely isolated from the others.

One thing to note is that this plugin uses `Git4Idea` to perform Git operations. Since the transitively included plugin
uses the `git` executable under the hood, it is necessary to have said software installed on your system before running.
Although most (if not all) modern systems will meet this requirement, it is worth pointing out if you intend to run the
plugin in a containerized environment (e.g. a CI/CD pipeline or Alpine-based Docker container). Switching to a natively
written Git implementation (e.g. JGit) would alleviate this issue, but I wanted to use as much of the existing JetBrains
tooling as possible.

### Whitespace Preservation

Some changes to the source code may result in whitespace changes. For example, changing the name of a parameter that is
used in a statement with non-standard spacing may result in the spacing being normalized:

```diff
diff --git a/src/ellipsoid_tool.py b/src/ellipsoid_tool.py
index 5f70dcd..e4110d2 100644
--- a/src/ellipsoid_tool.py
+++ b/src/ellipsoid_tool.py
@@ -70,7 +70,7 @@ class EllipsoidTool:
         """Calculate the volume of the blob"""
         return 4./3.*np.pi*radii[0]*radii[1]*radii[2]

-    def plotEllipsoid(self, center, radii, rotation, ax=None, plotAxes=False, cageColor='b', cageAlpha=0.2):
+    def plotEllipsoid(self, center, radii, rotation, ax=None, plotAxes=False, cageColor='b', cage_alpha=0.2):
         """Plot an ellipsoid"""
         make_ax = ax == None
         if make_ax:
@@ -107,7 +107,7 @@ class EllipsoidTool:
                 ax.plot(X3, Y3, Z3, color=cageColor)

         # plot ellipsoid
-        ax.plot_wireframe(x, y, z,  rstride=4, cstride=4, color=cageColor, alpha=cageAlpha)
+        ax.plot_wireframe(x, y, z, rstride=4, cstride=4, color=cageColor, alpha=cage_alpha)

         if make_ax:
             plt.show()
```

Another example, adding a DocString to a Python decorator definition where the nested function is not preceded by a
newline leads to an extra newline being inserted:

```diff
diff --git a/src/measures.py b/src/measures.py
index e566219..d29757d 100644
--- a/src/measures.py
+++ b/src/measures.py
@@ -35,6 +35,8 @@ def recurrent_cleaner(s):
     raise Exception(f'Unexpected type for serialisation: {type(s)} for value {s}')

 def organ_measure(mf):
+    """Decorator to apply an organ measurement function to markup and volume."""
+
     def wrapped_mf(markup, volume, separator=None):
         if separator is None:
             markup_ids = [1]
```
