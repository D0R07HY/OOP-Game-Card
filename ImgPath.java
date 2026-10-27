import java.io.File;

public class ImgPath {

    public static final String DIR = System.getProperty("user.dir") + File.separator + "pic" + File.separator;

    public static final String The_Sun = DIR + "the_sun.JPG";
    public static final String Mercury = DIR + "mecury.JPG";
    public static final String Venus = DIR + "venus.JPG";
    public static final String Earth = DIR + "earth.JPG";
    public static final String Mars = DIR + "mars.JPG";
    public static final String Jupiter = DIR + "jupiter.JPG";
    public static final String Saturn = DIR + "saturn.JPG";
    public static final String Uranus = DIR + "uranus.JPG";
    public static final String Neptune = DIR + "neptune.JPG";
    public static final String Black_hole = DIR + "black_hole.JPG";
    public static final String Meteor_Strike = DIR + "meteor_strike.JPG";
    public static final String Bigbang = DIR + "bigbang.JPG";
    public static final String Super_nova = DIR + "super_nova.JPG";
    public static final String worm_hole = DIR + "worm_hole.JPG";
    public static final String pulsar = DIR + "pulsar.JPG";
    public static final String forc_field = DIR + "forc_field.JPG";
    public static final String nebular_cloak = DIR + "nebular_cloak.JPG";
    public static final String stallar_regenneration = DIR + "stallar_regenneration.JPG";
    public static final String space_prode = DIR + "space_prode.JPG";
    public static final String Field = DIR + "field.JPG";
    public static final String GameTable = DIR + "Game-Table.jpg";

    public static String get(String cardName) {
        if (cardName == null) {
            return null;
        }

        String key = cardName.trim().toLowerCase();

        if (key.equals("the sun")) {
            return The_Sun;
        }
        if (key.equals("mercury")) {
            return Mercury;
        }
        if (key.equals("venus")) {
            return Venus;
        }
        if (key.equals("earth")) {
            return Earth;
        }
        if (key.equals("mars")) {
            return Mars;
        }
        if (key.equals("jupiter")) {
            return Jupiter;
        }
        if (key.equals("saturn")) {
            return Saturn;
        }
        if (key.equals("uranus")) {
            return Uranus;
        }
        if (key.equals("neptune")) {
            return Neptune;
        }
        if (key.equals("black hole")) {
            return Black_hole;
        }
        if (key.equals("meteor strike")) {
            return Meteor_Strike;
        }
        if (key.equals("big bang")) {
            return Bigbang;
        }
        if (key.equals("super nova") || key.equals("supernova")) {
            return Super_nova;
        }
        if (key.equals("worm hole")) {
            return worm_hole;
        }
        if (key.equals("pulsar")) {
            return pulsar;
        }
        if (key.equals("force field")) {
            return forc_field;
        }
        if (key.equals("nebula cloak")) {
            return nebular_cloak;
        }
        if (key.equals("stellar regeneration")) {
            return stallar_regenneration;
        }
        if (key.equals("space probe")) {
            return space_prode;
        }

        return null;
    }
}
