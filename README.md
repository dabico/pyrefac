# PyRefac

## Requirements

- Java 17
- Gradle 8.7

## Running

### Command Line Interface (CLI)

You can run the helper script from the terminal:

```shell
./pyrefac.sh $GIT_REPOSITORY_URL $GIT_REPOSITORY_FILE_PATH $REFACTORING_NAME $REFACTROING_CONFIG_PATH
```

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
