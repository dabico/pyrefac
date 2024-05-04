package ch.usi.si.seart.pyrefac;

final class StringUtils {

    private StringUtils() {
    }

    public static String snakeToTitleCase(String snake) {
        StringBuilder title = new StringBuilder();
        boolean capitalize = true;
        for (char character : snake.toCharArray()) {
            if (character == '_') {
                capitalize = true;
            } else {
                title.append(capitalize ? Character.toUpperCase(character) : character);
                capitalize = false;
            }
        }
        return title.toString();
    }
}
