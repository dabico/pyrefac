# PyRefac

## Requirements

- Java 17
- Gradle 8.7

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

| Parameter                   | Description                                                                          |
|-----------------------------|--------------------------------------------------------------------------------------|
| `$GIT_REPOSITORY_URL`       | URL of the Git repository to clone. Can be either an HTTPS or SSH                    |
| `$GIT_REPOSITORY_FILE_PATH` | Repository root-relative path of the file to refactor                                |
| `$REFACTORING_NAME`         | Either `add_comment`, `rename_literal`, or `rename_function_parameters`              |
| `$REFACTORING_CONFIG_PATH`  | _Relative_ path to the JSON configuration file containing the refactoring parameters |

IntelliJ run configurations are also provided for easier debugging.

### Command Line Interface (CLI)

For convenience, a helper script is provided to run the plugin:

```shell
./pyrefac.sh $GIT_REPOSITORY_URL $GIT_REPOSITORY_FILE_PATH $REFACTORING_NAME $REFACTROING_CONFIG_PATH
```

The parameters are the same as the Gradle task.

## Supported Refactorings

### `add_comment`

Adds a [DocString](https://peps.python.org/pep-0257/) comment to a targeted function. Functions are identified by their
name, as well as the class they reside in. To target functions within the root scope, the class name should be omitted.
If the function already has a documentation comment, its contents will be replaced. If the input comment does not
contain any line breaks, the injected DocString will also be single-line. On the other hand, if the comment contains
line breaks, the injected DocString will be multi-line. The refactoring will also ensure that the comment body is\
properly indented.

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

Renames a local variable or class attribute reference within a targeted function. Functions are identified by their
name, as well as the class they reside in. To target functions within the root scope, the class name should be omitted.
If the refactoring target is a local variable, the refactoring will ensure that the variable is renamed in its
definition, and all subsequent references within the body. If the refactoring target is a class attribute, the
refactoring will ensure that the attribute is renamed in its definition, and all subsequent references within the class.
Refactoring will fail if the provided name is not found, or if the new name is already in use.

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

Self-explanatory. Renames the parameter of a targeted function. Functions are identified by their name, as well as the
class they reside in. To target functions within the root scope, the class name should be omitted. The refactoring will
ensure that the parameter is renamed both in the function signature, and within the function body. Refactoring will fail
if the parameter name is not found in the function signature, or if the new parameter name is already in use.

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
  "old_name": "cageAlpha",
  "new_name": "cage_alpha"
}
```

Should produce the following diff:

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
-        ax.plot_wireframe(x, y, z, rstride=4, cstride=4, color=cageColor, alpha=cageAlpha)
+        ax.plot_wireframe(x, y, z, rstride=4, cstride=4, color=cageColor, alpha=cage_alpha)

         if make_ax:
             plt.show()
```
