/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.streaming

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import org.apache.spark.scheduler.SparkListener
import org.apache.spark.sql.SparkSession
import org.apache.spark.status.api.v1.sql.BatchInfo

private[spark] class StructuredStreamingJobListener(sparkSession: SparkSession)
  extends SparkListener {
  val queryListener = new StructuredStreamingQueryListener()
  sparkSession.streams.addListener(queryListener)

  def getBatchList(): Seq[BatchInfo] = {
    return Seq.empty
  }

  def numQueries(): Int = queryListener.numQueries.get()
  def numInProgress(): Int = queryListener.numInProgress.get()
  def numFinished(): Int = queryListener.numFinished.get()
  def realActiveQueriesNum(): Int = queryListener.activeQueries.length
}

class StructuredStreamingQueryListener extends StreamingQueryListener {
  val numQueries: AtomicInteger = new AtomicInteger(0)
  val numInProgress: AtomicInteger = new AtomicInteger(0)
  val numFinished: AtomicInteger = new AtomicInteger(0)
  var activeQueries: Seq[UUID] = Seq.empty

  /**
   * Called when a query is started.
   *
   * @note This is called synchronously with
   *       [[org.apache.spark.sql.streaming.DataStreamWriter `DataStreamWriter.start()`]],
   *       that is, `onQueryStart` will be called on all listeners before
   *       `DataStreamWriter.start()` returns the corresponding [[StreamingQuery]]. Please
   *       don't block this method as it will block your query.
   * @since 2.0.0
   */
  override def onQueryStarted(event: StreamingQueryListener.QueryStartedEvent): Unit = {
    numQueries.incrementAndGet()
    activeQueries = activeQueries :+ event.id
  }

  /**
   * Called when there is some status update (ingestion rate updated, etc.)
   *
   * @note This method is asynchronous. The status in [[StreamingQuery]] will always be
   *       latest no matter when this method is called. Therefore, the status of [[StreamingQuery]]
   *       may be changed before/when you process the event. E.g., you may find [[StreamingQuery]]
   *       is terminated when you are processing `QueryProgressEvent`.
   * @since 2.0.0
   */
  override def onQueryProgress(event: StreamingQueryListener.QueryProgressEvent): Unit = {
    event.progress

    numInProgress.incrementAndGet()
  }

  /**
   * Called when a query is stopped, with or without error.
   *
   * @since 2.0.0
   */
  override def onQueryTerminated(event: StreamingQueryListener.QueryTerminatedEvent): Unit = {
    numFinished.incrementAndGet()
    numInProgress.decrementAndGet()
  }
}

