package com.example

import akka.actor._

object PollingConsumerDriver extends CompletableApp(1) {
}

case class WorkNeeded()
case class WorkOnItem(workItem: WorkItem)
