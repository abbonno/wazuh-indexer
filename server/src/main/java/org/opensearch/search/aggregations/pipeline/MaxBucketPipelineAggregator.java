/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.search.aggregations.pipeline;

import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.search.DocValueFormat;
import org.opensearch.search.aggregations.InternalAggregation;
import org.opensearch.search.aggregations.pipeline.BucketHelpers.GapPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aggregate all docs into a max bucket
 *
 * @opensearch.internal
 */
public class MaxBucketPipelineAggregator extends BucketMetricsPipelineAggregator {
    private List<String> maxBucketKeys;
    private double maxValue;

    MaxBucketPipelineAggregator(
        String name,
        String[] bucketsPaths,
        GapPolicy gapPolicy,
        DocValueFormat formatter,
        Map<String, Object> metadata
    ) {
        super(name, bucketsPaths, gapPolicy, formatter, metadata);
    }

    /**
     * Read from a stream.
     */
    public MaxBucketPipelineAggregator(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public String getWriteableName() {
        return MaxBucketPipelineAggregationBuilder.NAME;
    }

    @Override
    protected void preCollection() {
        maxBucketKeys = new ArrayList<>();
        maxValue = Double.NEGATIVE_INFINITY;
    }

    @Override
    protected void collectBucketValue(String bucketKey, Double bucketValue) {
        if (bucketValue > maxValue) {
            maxBucketKeys.clear();
            maxBucketKeys.add(bucketKey);
            maxValue = bucketValue;
        } else if (bucketValue.equals(maxValue)) {
            maxBucketKeys.add(bucketKey);
        }
    }

    @Override
    protected InternalAggregation buildAggregation(Map<String, Object> metadata) {
        String[] keys = maxBucketKeys.toArray(new String[0]);
        return new InternalBucketMetricValue(name(), keys, maxValue, format, metadata());
    }

}
