/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.ExpressionClause;
import org.apache.camel.model.config.BatchResequencerConfig;
import org.apache.camel.model.config.StreamResequencerConfig;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.processor.Resequencer;
import org.apache.camel.processor.StreamResequencer;
import org.apache.camel.processor.resequencer.ExpressionResultComparator;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents an XML &lt;resequence/&gt; element
 *
 * @version 
 */
@XmlRootElement(name = "resequence")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResequenceDefinition extends ProcessorDefinition<ResequenceDefinition> {
    @XmlElement(name = "batch-config")
    private BatchResequencerConfig batchConfig;
    @XmlElement(name = "stream-config")
    private StreamResequencerConfig streamConfig;
    @XmlElementRef
    private ExpressionDefinition expression;
    @XmlElementRef
    private List<ProcessorDefinition> outputs = new ArrayList<ProcessorDefinition>();

    public ResequenceDefinition() {
    }

    @Override
    public String getShortName() {
        return "resequence";
    }

    public List<ProcessorDefinition> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ProcessorDefinition> outputs) {
        this.outputs = outputs;
    }

    @Override
    public boolean isOutputSupported() {
        return true;
    }

    // Fluent API
    // -------------------------------------------------------------------------
    /**
     * Configures the stream-based resequencing algorithm using the default
     * configuration.
     *
     * @return the builder
     */
    public ResequenceDefinition stream() {
        return stream(StreamResequencerConfig.getDefault());
    }

    /**
     * Configures the batch-based resequencing algorithm using the default
     * configuration.
     *
     * @return the builder
     */
    public ResequenceDefinition batch() {
        return batch(BatchResequencerConfig.getDefault());
    }

    /**
     * Configures the stream-based resequencing algorithm using the given
     * {@link StreamResequencerConfig}.
     *
     * @param config  the config
     * @return the builder
     */
    public ResequenceDefinition stream(StreamResequencerConfig config) {
        this.streamConfig = config;
        this.batchConfig = null;
        return this;
    }

    /**
     * Configures the batch-based resequencing algorithm using the given
     * {@link BatchResequencerConfig}.
     *
     * @param config  the config
     * @return the builder
     */
    public ResequenceDefinition batch(BatchResequencerConfig config) {
        this.batchConfig = config;
        this.streamConfig = null;
        return this;
    }

    /**
     * Sets the timeout
     * @param timeout  timeout in millis
     * @return the builder
     */
    public ResequenceDefinition timeout(long timeout) {
        if (batchConfig != null) {
            batchConfig.setBatchTimeout(timeout);
        } else {
            streamConfig.setTimeout(timeout);
        }
        return this;
    }

    /**
     * Sets the in batch size for number of exchanges received
     * @param batchSize  the batch size
     * @return the builder
     */
    public ResequenceDefinition size(int batchSize) {
        if (streamConfig != null) {
            throw new IllegalStateException("size() only supported for batch resequencer");
        }
        // initialize batch mode as its default mode
        if (batchConfig == null) {
            batch();
        }
        batchConfig.setBatchSize(batchSize);
        return this;
    }

    /**
     * Sets the capacity for the stream resequencer
     *
     * @param capacity  the capacity
     * @return the builder
     */
    public ResequenceDefinition capacity(int capacity) {
        if (streamConfig == null) {
            throw new IllegalStateException("capacity() only supported for stream resequencer");
        }
        streamConfig.setCapacity(capacity);
        return this;

    }

    /**
     * Enables duplicates for the batch resequencer mode
     * @return the builder
     */
    public ResequenceDefinition allowDuplicates() {
        if (streamConfig != null) {
            throw new IllegalStateException("allowDuplicates() only supported for batch resequencer");
        }
        // initialize batch mode as its default mode
        if (batchConfig == null) {
            batch();
        }
        batchConfig.setAllowDuplicates(true);
        return this;
    }

    /**
     * Enables reverse mode for the batch resequencer mode.
     * <p/>
     * This means the expression for determine the sequence order will be reversed.
     * Can be used for Z..A or 9..0 ordering.
     *
     * @return the builder
     */
    public ResequenceDefinition reverse() {
        if (streamConfig != null) {
            throw new IllegalStateException("reverse() only supported for batch resequencer");
        }
        // initialize batch mode as its default mode
        if (batchConfig == null) {
            batch();
        }
        batchConfig.setReverse(true);
        return this;
    }

    /**
     * Sets the comparator to use for stream resequencer
     *
     * @param comparator  the comparator
     * @return the builder
     */
    public ResequenceDefinition comparator(ExpressionResultComparator comparator) {
        if (streamConfig == null) {
            throw new IllegalStateException("comparator() only supported for stream resequencer");
        }
        streamConfig.setComparator(comparator);
        return this;
    }

    public ExpressionClause<ResequenceDefinition> createAndSetExpression() {
        ExpressionClause<ResequenceDefinition> clause = new ExpressionClause<ResequenceDefinition>(this);
        this.setExpression(clause);
        return clause;
    }

    @Override
    public String toString() {
        return "Resequencer[" + getExpression() + " -> " + getOutputs() + "]";
    }

    @Override
    public String getLabel() {
        String s = "";
        if (getExpression() != null) {
            s = getExpression().getLabel();
        }
        return "Resequencer[" + s + "]";
    }

    public BatchResequencerConfig getBatchConfig() {
        return batchConfig;
    }

    public StreamResequencerConfig getStreamConfig() {
        return streamConfig;
    }

    public void setBatchConfig(BatchResequencerConfig batchConfig) {
        this.batchConfig = batchConfig;
    }

    public void setStreamConfig(StreamResequencerConfig streamConfig) {
        this.streamConfig = streamConfig;
    }

    public ExpressionDefinition getExpression() {
        return expression;
    }

    public void setExpression(ExpressionDefinition expression) {
        this.expression = expression;
    }

    @Override
    public Processor createProcessor(RouteContext routeContext) throws Exception {
        if (streamConfig != null) {
            return createStreamResequencer(routeContext, streamConfig);
        } else {
            if (batchConfig == null) {
                // default as batch mode
                batch();
            }
            return createBatchResequencer(routeContext, batchConfig);
        }
    }

    /**
     * Creates a batch {@link Resequencer} instance applying the given <code>config</code>.
     * 
     * @param routeContext route context.
     * @param config batch resequencer configuration.
     * @return the configured batch resequencer.
     * @throws Exception can be thrown
     */
    protected Resequencer createBatchResequencer(RouteContext routeContext,
                                                 BatchResequencerConfig config) throws Exception {
        Processor processor = this.createChildProcessor(routeContext, true);
        Expression expression = getExpression().createExpression(routeContext);

        ObjectHelper.notNull(config, "config", this);
        ObjectHelper.notNull(expression, "expression", this);

        Resequencer resequencer = new Resequencer(routeContext.getCamelContext(), processor, expression,
                config.isAllowDuplicates(), config.isReverse());
        resequencer.setBatchSize(config.getBatchSize());
        resequencer.setBatchTimeout(config.getBatchTimeout());
        return resequencer;
    }

    /**
     * Creates a {@link StreamResequencer} instance applying the given <code>config</code>.
     * 
     * @param routeContext route context.
     * @param config stream resequencer configuration.
     * @return the configured stream resequencer.
     * @throws Exception can be thrwon
     */
    protected StreamResequencer createStreamResequencer(RouteContext routeContext,
                                                        StreamResequencerConfig config) throws Exception {
        Processor processor = this.createChildProcessor(routeContext, true);
        Expression expression = getExpression().createExpression(routeContext);

        ObjectHelper.notNull(config, "config", this);
        ObjectHelper.notNull(expression, "expression", this);

        ExpressionResultComparator comparator = config.getComparator();
        comparator.setExpression(expression);

        StreamResequencer resequencer = new StreamResequencer(routeContext.getCamelContext(), processor, comparator);
        resequencer.setTimeout(config.getTimeout());
        resequencer.setCapacity(config.getCapacity());
        return resequencer;
    }

}
