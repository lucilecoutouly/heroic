/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.heroic.aggregation;

import java.util.List;

import com.spotify.heroic.common.DateRange;

public interface AggregationInstance {
    public static final String SAMPLE_SIZE = "Aggregation.sampleSize";

    /**
     * Estimate number of points this aggregation will produce.
     *
     * @param range Range to perform aggregation over.
     * @return Number of datapoints required for aggregation, or {@code -1} if not known.
     */
    public long estimate(DateRange range);

    /**
     * Get the cadence of the current aggregation.
     *
     * The cadence indicates the interval in milliseconds that samples can be expected.
     *
     * @return The cadence of the resulting aggregation, or {@code 0} if this is unknown.
     */
    public long cadence();

    /**
     * Traverse the possible aggregations and build the necessary graph out of them.
     */
    public AggregationTraversal session(List<AggregationState> states, DateRange range);

    /**
     * Transform the given aggregation, into a distributed aggregation.
     */
    default AggregationInstance distributed() {
        return this;
    }

    /**
     * Get the reducer for the given aggregation.
     */
    public AggregationSession reducer(final DateRange range);

    /**
     * Get an aggregation combiner for the given aggregation.
     * @return An aggregation combiner.
     */
    default AggregationCombiner combiner(final DateRange range) {
        return AggregationCombiner.DEFAULT;
    }
}