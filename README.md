# PyRefac

## Requirements

- Java 17
- Gradle 8.7

## Running

First define the refactoring configuration in a JSON file. For example:

```json
{
  "class": "EllipsoidTool",
  "function": "plotEllipsoid",
  "comment": "Plots an ellipsoid based on the provided center, radii, and rotation matrix. Allows plotting with custom color and transparency settings."
}
```

Then run the helper script from the terminal:

```shell
./pyrefac.sh $GIT_REPOSITORY_URL $GIT_REPOSITORY_FILE_PATH $REFACTORING_NAME $REFACTROING_CONFIG_PATH
```
