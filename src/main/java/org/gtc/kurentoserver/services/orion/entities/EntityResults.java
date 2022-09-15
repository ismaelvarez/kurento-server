package org.gtc.kurentoserver.services.orion.entities;

import java.util.List;

public class EntityResults<T> {
    private final List<T> cameras;
    private final int numOfResults;

    public EntityResults(List<T> cameras, int numOfResults) {
        this.cameras = cameras;
        this.numOfResults = numOfResults;
    }

    public List<T> getResults() {
        return cameras;
    }

    public int getNumOfResults() {
        return numOfResults;
    }
}
