/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.kinesis.multilang.config;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum RetrievalMode {
    FANOUT(MultiLangDaemonConfiguration::getFanoutConfig), POLLING(
            MultiLangDaemonConfiguration::getPollingConfig), DEFAULT(RetrievalMode::decideForDefault);

    private final Function<MultiLangDaemonConfiguration, RetrievalConfigBuilder> builderFor;

    public RetrievalConfigBuilder builder(MultiLangDaemonConfiguration configuration) {
        return builderFor.apply(configuration);
    }

    RetrievalMode(Function<MultiLangDaemonConfiguration, RetrievalConfigBuilder> builderFor) {
        this.builderFor = builderFor;
    }

    public static RetrievalMode from(String source) {
        Validate.notEmpty(source);
        try {
            return RetrievalMode.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException(
                    "Unknown retrieval type '" + source + "'. Available retrieval types: " + availableRetrievalModes());
        }
    }

    private static String availableRetrievalModes() {
        return "(" + Arrays.stream(RetrievalMode.values()).map(Enum::name).collect(Collectors.joining(", ")) + ")";
    }

    private static RetrievalConfigBuilder decideForDefault(MultiLangDaemonConfiguration configuration) {
        if (configuration.getPollingConfig().anyPropertiesSet()) {
            log.warn("Some polling properties have been set, defaulting to polling. "
                    + "To switch to Fanout either add `RetrievalMode=FANOUT` to your "
                    + "properties or remove the any configuration for polling.");
            return configuration.getPollingConfig();
        }
        return configuration.getFanoutConfig();
    }
}
