package tech.purelove.altfinder.database.model;

import java.util.List;

public record UnresolvedIpGroup(
        String ip,
        List<UnresolvedPair> pairs
) {}
