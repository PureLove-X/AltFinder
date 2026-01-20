package tech.purelove.altfinder.database.model;

import java.util.List;

public record ResolvedPairView(
        String uuidA,
        String uuidB,
        String reason,
        List<String> sharedIps
) {}

