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

package com.spotify.heroic.cache.memory;

import com.google.inject.Exposed;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.spotify.heroic.cache.CacheModule;
import com.spotify.heroic.cache.QueryCache;

public class MemoryCacheModule implements CacheModule {
    @Override
    public Module module() {
        return new PrivateModule() {
            @Provides
            @Singleton
            @Exposed
            public QueryCache queryCache() {
                return new MemoryQueryCache();
            }

            @Override
            protected void configure() {
            };
        };
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements CacheModule.Builder {
        @Override
        public CacheModule build() {
            return new MemoryCacheModule();
        }
    }
}