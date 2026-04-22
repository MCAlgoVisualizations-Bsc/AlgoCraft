package io.github.mcalgovisualizations.visualization.instance;

import java.util.HashSet;
import java.util.UUID;

public class PartyService {
    public UUID host;
    public HashSet<UUID> spectators = new HashSet<>();

}
