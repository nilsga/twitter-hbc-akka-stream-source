package com.github.nilsga.stream.twitter

import java.util
import java.util.UUID
import java.util.concurrent.{ArrayBlockingQueue, Executors}

import akka.stream.stage._
import akka.stream.{Attributes, Outlet, SourceShape}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{Constants, HttpHosts}
import com.twitter.hbc.twitter4j.Twitter4jStatusClient
import twitter4j.{StallWarning, Status, StatusDeletionNotice, StatusListener}

import scala.collection.{JavaConverters, mutable}

object TwitterHbcFilterStreamSourceStage {
  def apply(settings: TwitterHbcFilterStreamSettings, bufferSize: Int) = new TwitterHbcFilterStreamSourceStage(settings, bufferSize)
}

class TwitterHbcFilterStreamSourceStage(val settings: TwitterHbcFilterStreamSettings, val bufferSize: Int) extends GraphStage[SourceShape[Status]] {

  private val out = Outlet[Status]("TwitterHbcFilterStreamSource.out")

  override def shape = SourceShape.of(out)

  override def createLogic(inheritedAttributes: Attributes) = new GraphStageLogic(shape) with StageLogging {

    private val msgQueue = new ArrayBlockingQueue[String](1000)
    private val statusBuffer = mutable.Queue[Status]()
    private val endpoint = new StatusesFilterEndpoint(settings.gnip)
    endpoint.trackTerms(JavaConverters.seqAsJavaList(settings.trackTerms))
    settings.language.foreach(lang => endpoint.addQueryParameter("language", lang))
    private val host = new HttpHosts(if (!settings.gnip) Constants.STREAM_HOST else Constants.ENTERPRISE_STREAM_HOST)
    private val internal = new ClientBuilder().authentication(settings.credentials)
      .name(s"akka-stream-twitter-hbc-source-${UUID.randomUUID}")
      .endpoint(endpoint)
      .hosts(host)
      .gzipEnabled(true)
      .processor(new StringDelimitedProcessor(msgQueue)).build()

    private val onStatusCallback = getAsyncCallback[Status](status => {
      if (isAvailable(out)) {
        push(out, status)
      }
      else {
        if (statusBuffer.size + 1 > bufferSize) {
          failStage(new RuntimeException("Max buffer size reached"))
        }
      }
    })
    private val failCallback = getAsyncCallback[Exception](ex => failStage(ex))
    private val client = new Twitter4jStatusClient(internal, msgQueue, util.Arrays.asList(new TwitterHbcFilterStreamSourceListener(onStatusCallback, failCallback)), Executors.newFixedThreadPool(1))

    setHandler(out, new OutHandler {
      override def onPull(): Unit = {
        if (statusBuffer.nonEmpty) {
          push(out, statusBuffer.dequeue())
        }
      }
    })

    override def preStart(): Unit = {
      log.debug("Starting twitter hbc stream client")
      super.preStart()
      client.connect()
      client.process()
    }

    override def postStop(): Unit = {
      log.debug("Terminating twitter hbc stream client")
      super.postStop()
      client.stop()
    }
  }

}

private[twitter] class TwitterHbcFilterStreamSourceListener(val statusCallback: AsyncCallback[Status], val failCallback: AsyncCallback[Exception]) extends StatusListener {
  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) = {}

  override def onScrubGeo(userId: Long, upToStatusId: Long) = {}

  override def onStatus(status: Status) = statusCallback.invoke(status)

  override def onTrackLimitationNotice(numberOfLimitedStatuses: Int) = {}

  override def onStallWarning(warning: StallWarning) = {}

  override def onException(ex: Exception) = failCallback.invoke(ex)
}
