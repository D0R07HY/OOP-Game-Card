public final class CardFactory {

    private CardFactory() {
    }

    public static Card create(String id, String name, String type, int hp, int orbit) {
        if (name == null) {
            return null;
        }

        if ("STAR".equals(type)) {
            return new StarCard(id, name, Math.max(1, hp), ImgPath.get(name), orbit);
        }

        if (!"SPECIAL".equals(type)) {
            return null;
        }

        String imagePath = ImgPath.get(name);

        if (name.equals("Meteor Strike")) {
            return new MeteorStrike(id, imagePath);
        }
        if (name.equals("Supernova") || name.equals("Super Nova")) {
            return new Supernova(id, imagePath);
        }
        if (name.equals("Pulsar")) {
            return new Pulsar(id, imagePath);
        }
        if (name.equals("Force Field")) {
            return new ForceField(id, imagePath);
        }
        if (name.equals("Nebula Cloak")) {
            return new NebulaCloak(id, imagePath);
        }
        if (name.equals("Stellar Regeneration")) {
            return new StellarRegeneration(id, imagePath);
        }
        if (name.equals("Space Probe")) {
            return new SpaceProbe(id, imagePath);
        }
        if (name.equals("Big Bang")) {
            return new BigBang(id, imagePath);
        }
        if (name.equals("Black Hole")) {
            return new BlackHole(id, imagePath);
        }
        if (name.equals("Worm Hole")) {
            return new WormHole(id, imagePath);
        }

        return null;
    }
}
