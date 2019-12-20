package org.rascat.gcl.functions;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.rascat.gcl.model.Force;

public class SubtractForces implements ReduceFunction<Force> {
    @Override
    public Force reduce(Force force, Force t1) {
        return new Force(force.getId(), force.getVector().subtract(t1.getVector()));
    }
}
