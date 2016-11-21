package com.example

import akka.actor._

object PollingConsumerDriver extends CompletableApp(1) {
}

case class WorkNeeded()
case class WorkOnItem(workItem: WorkItem)

class WorkConsumer(workItemsProvider: ActorRef) extends Actor {
  var totalItemsWorkedOn = 0

  def performWorkOn(workItem: WorkItem) = {
    totalItemsWorkedOn = totalItemsWorkedOn + 1
    if (totalItemsWorkedOn >= 15) {
      context.stop(self)
      PollingConsumerDriver.completeAll
    }
  }

  override def postStop() = {
    context.stop(workItemsProvider)
  }

  def receive = {
    case allocated: WorkItemsAllocated =>
      println("WorkItemsAllocated...")
      allocated.workItems map { workItem =>
        self ! WorkOnItem(workItem)
      }
      self ! WorkNeeded()
    case workNeeded: WorkNeeded =>
      println("WorkNeeded...")
      workItemsProvider ! AllocateWorkItems(5)
    case workOnItem: WorkOnItem =>
      println(s"Performed work on: ${workOnItem.workItem.name}")
      performWorkOn(workOnItem.workItem)
  }
}

case class AllocateWorkItems(numberOfItems: Int)
case class WorkItemsAllocated(workItems: List[WorkItem])
case class WorkItem(name: String)
