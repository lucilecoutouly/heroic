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

package com.spotify.heroic.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.heroic.HeroicConfig;
import com.spotify.heroic.HeroicParameters;
import com.spotify.heroic.HeroicProfile;
import com.spotify.heroic.metric.MetricManagerModule;
import com.spotify.heroic.metric.MetricModule;
import com.spotify.heroic.metric.datastax.DatastaxMetricModule;
import com.spotify.heroic.metric.datastax.schema.SchemaModule;
import com.spotify.heroic.metric.datastax.schema.legacy.LegacySchemaModule;
import com.spotify.heroic.metric.datastax.schema.ng.NextGenSchemaModule;
import com.spotify.heroic.shell.ShellServerModule;

public class CassandraProfile extends HeroicProfileBase {
    private static final Splitter splitter = Splitter.on(',').trimResults();

    private static final Map<String, Callable<SchemaModule>> types = new HashMap<>();

    {
        types.put("ng", () -> NextGenSchemaModule.builder().build());
        types.put("legacy", () -> LegacySchemaModule.builder().build());
    }

    @Override
    public HeroicConfig.Builder build(final HeroicParameters params) throws Exception {
        final boolean configure = params.contains("cassandra.configure");
        final Optional<String> type = params.get("cassandra.type");

        final SchemaModule schema;

        if (type.isPresent()) {
            final Callable<SchemaModule> builder;

            if ((builder = types.get(type.get())) == null) {
                throw new IllegalArgumentException("Unknown cassandra.type: " + type.get());
            }

            schema = builder.call();
        } else {
            schema = LegacySchemaModule.builder().build();
        }

        final Set<String> seeds = params.get("cassandra.seeds").map(s -> ImmutableSet.copyOf(splitter.split(s)))
                .orElseGet(() -> ImmutableSet.of("localhost"));

        // @formatter:off
        return HeroicConfig.builder()
            .metric(
                MetricManagerModule.builder()
                    .backends(ImmutableList.<MetricModule>of(
                        DatastaxMetricModule.builder()
                        .seeds(seeds)
                        .configure(configure)
                        .schema(schema)
                        .build()
                    ))
            )
            .shellServer(ShellServerModule.builder());
        // @formatter:on
    }

    @Override
    public String description() {
        return "Configures a metric backend for Cassandra";
    }

    private static final Joiner parameters = Joiner.on(", ");

    @Override
    public List<Option> options() {
        // @formatter:off
        return ImmutableList.of(
            HeroicProfile.option("cassandra.configure", "If set, will cause the cluster to be automatically configured"),
            HeroicProfile.option("cassandra.type", "Type of backend to use, valid values are: " + parameters.join(types.keySet()), "<type>"),
            HeroicProfile.option("cassandra.seeds", "Seeds to use when configuring backend", "<host>[:<port>][,..]")
        );
        // @formatter:on
    }
}
